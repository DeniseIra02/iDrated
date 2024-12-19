package com.example.idrated

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.idrated.databinding.ActivityGoalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.util.*

class GoalActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var realtimeDatabase: DatabaseReference

    // Bluetooth-related variables
    private val bluetoothPermissionRequestCode = 1
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var isConnected = false
    private val pairedDevicesList = mutableListOf<BluetoothDevice>()

    // Binding
    private lateinit var binding: ActivityGoalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance().reference

        // Permissions & Bluetooth Setup
        checkBluetoothState()

        // Observe changes to waterConsumed
        observeWaterConsumedUpdates()

        setupUIListeners()
    }


    // Check if Bluetooth is enabled; prompt if not
    private fun checkBluetoothState() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth is disabled. Please enable it.", Toast.LENGTH_SHORT).show()
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, bluetoothPermissionRequestCode)
        } else {
            checkAndRequestPermissions()
        }
    }

    // Handle permissions
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), bluetoothPermissionRequestCode)
        } else {
            loadPairedDevices()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == bluetoothPermissionRequestCode) {
            if (bluetoothAdapter?.isEnabled == true) {
                Toast.makeText(this, "Bluetooth enabled successfully", Toast.LENGTH_SHORT).show()
                checkAndRequestPermissions()
            } else {
                Toast.makeText(this, "Bluetooth must be enabled to proceed.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun setupUIListeners() {
        binding.btnSetDailyGoal.setOnClickListener {
            val intent = Intent(this, WaterInputActivity::class.java)
            startActivity(intent)
        }

        binding.addWaterButton.setOnClickListener {
            val waterIntake = binding.waterInput.text.toString().toDoubleOrNull()
            if (waterIntake != null && waterIntake > 0) {
                updateWaterConsumed(waterIntake)
                binding.waterInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        binding.LogoutBtn.setOnClickListener {
            auth.signOut()
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(this)
            }
            finish()
        }

        binding.connectButton.setOnClickListener {
            val selectedIndex = binding.deviceDropdown.selectedItemPosition
            if (selectedIndex in pairedDevicesList.indices) {
                connectToDevice(pairedDevicesList[selectedIndex])
            } else {
                Toast.makeText(this, "Please select a valid device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadPairedDevices() {
        pairedDevicesList.clear()
        bluetoothAdapter?.bondedDevices?.let { devices ->
            pairedDevicesList.addAll(devices)
            val deviceNames = devices.map { it.name ?: "Unknown Device" }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, deviceNames)
            binding.deviceDropdown.adapter = adapter
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (isConnected) {
            Toast.makeText(this, "Already connected to a device", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uuid = device.uuids?.firstOrNull()?.uuid
                ?: UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()

            inputStream = bluetoothSocket?.inputStream
            isConnected = true

            binding.deviceNameTextView.text = "Connected to: ${device.name}"
            Toast.makeText(this, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
            startReadingData()
        } catch (e: Exception) {
            Log.e("Bluetooth", "Connection failed: ${e.message}")
            closeBluetoothConnection()
            Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startReadingData() {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            try {
                val buffer = ByteArray(1024)
                while (isConnected) {
                    val bytesRead = inputStream?.read(buffer)
                    if (bytesRead != null && bytesRead > 0) {
                        val receivedData = String(buffer, 0, bytesRead).trim()
                        handler.post {
                            binding.receivedDataTextView.text = "Received: $receivedData"

                            // Convert to Double and add to waterConsumed
                            val waterIntake = receivedData.toDoubleOrNull()
                            if (waterIntake != null && waterIntake > 0) {
                                addToWaterConsumed(waterIntake)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Bluetooth", "Error reading data: ${e.message}")
                isConnected = false
            }
        }.start()
    }

    private fun observeWaterConsumedUpdates() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = realtimeDatabase.child("users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentConsumed = snapshot.child("waterConsumed").getValue(Double::class.java) ?: 0.0
                    val currentGoal = snapshot.child("waterGoal").getValue(Double::class.java) ?: 0.0

                    updateGoalDisplay(currentGoal, currentConsumed)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GoalActivity, "Failed to retrieve updates", Toast.LENGTH_SHORT).show()
                    Log.e("Firebase", "Failed to listen for updates: ${error.message}")
                }
            })
        }
    }



    private fun addToWaterConsumed(waterIntake: Double) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = realtimeDatabase.child("users").child(userId)
            userRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentConsumed = currentData.child("waterConsumed").getValue(Double::class.java) ?: 0.0
                    val newConsumed = currentConsumed + waterIntake
                    currentData.child("waterConsumed").value = newConsumed
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    snapshot: DataSnapshot?
                ) {
                    if (error != null) {
                        Log.e("Firebase", "Failed to update waterConsumed: ${error.message}")
                        Toast.makeText(this@GoalActivity, "Failed to update water intake", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("Firebase", "Successfully updated waterConsumed")
                        Toast.makeText(this@GoalActivity, "Water intake updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun loadWaterGoalAndConsumed() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = realtimeDatabase.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val savedGoal = snapshot.child("waterGoal").getValue(Double::class.java) ?: 0.0
                    val consumed = snapshot.child("waterConsumed").getValue(Double::class.java) ?: 0.0
                    updateGoalDisplay(savedGoal, consumed)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GoalActivity, "Failed to load goal", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateGoalDisplay(goal: Double, consumed: Double) {
        binding.goalDisplay.text = String.format("%.2f", goal)
        binding.goalConsumed.text = String.format("%.2f", consumed)
        updatePercentage(goal, consumed)
        updateProgressBar(goal, consumed)
    }


    private fun updatePercentage(goal: Double, consumed: Double) {
        val percentage = if (goal > 0) {
            (consumed / goal) * 100
        } else {
            0.0
        }
        binding.percent.text = String.format("%.2f%%", percentage)
    }

    private fun updateProgressBar(goal: Double, consumed: Double) {
        binding.progressBar.max = goal.toInt() // Cast to Int for progress bar max
        binding.progressBar.progress = consumed.toInt() // Cast to Int for progress bar progress
    }

    private fun updateWaterConsumed(waterIntake: Double) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = realtimeDatabase.child("users").child(userId)
        userRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentConsumed = currentData.child("waterConsumed").getValue(Double::class.java) ?: 0.0
                val goal = currentData.child("waterGoal").getValue(Double::class.java) ?: 0.0

                // Calculate the new consumed value while ensuring it doesn't exceed the goal
                val newConsumed = (currentConsumed + waterIntake).coerceAtMost(goal)
                currentData.child("waterConsumed").value = newConsumed

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    Toast.makeText(this@GoalActivity, "Failed to update water intake: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firebase", "Transaction failed: ${error.message}")
                } else if (committed) {
                    val goal = snapshot?.child("waterGoal")?.getValue(Double::class.java) ?: 0.0
                    val newConsumed = snapshot?.child("waterConsumed")?.getValue(Double::class.java) ?: 0.0
                    updateGoalDisplay(goal, newConsumed)
                    Toast.makeText(this@GoalActivity, "Water intake updated!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun closeBluetoothConnection() {
        inputStream?.close()
        bluetoothSocket?.close()
        isConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        closeBluetoothConnection()
    }
}
