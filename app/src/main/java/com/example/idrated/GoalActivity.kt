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
import com.example.idrated.WaterInputActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.util.*

class GoalActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Bluetooth-related variables
    private val bluetoothPermissionRequestCode = 1
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var isConnected = false
    private val pairedDevicesList = mutableListOf<BluetoothDevice>()

    // Binding
    private lateinit var binding: ActivityGoalBinding

    // GoalActivity.kt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the water amount from the Intent that was passed from WaterInputActivity
        val waterAmount = intent.getDoubleExtra("waterAmount", 0.0)

        // Display the water amount in the goalDisplay TextView
        binding.goalDisplay.text = "${waterAmount.toInt()} ml"

        // Initialize Firebase and other setups
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkBluetoothState()
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

    // Function to handle permissions
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

    // Handle the result of enabling Bluetooth
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
            startActivity(Intent(this, WaterInputActivity::class.java))
        }

        binding.addWaterButton.setOnClickListener {
            val waterIntake = binding.waterInput.text.toString().toIntOrNull()
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
                        val receivedData = String(buffer, 0, bytesRead)
                        handler.post {
                            binding.receivedDataTextView.text = "Received: $receivedData"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Bluetooth", "Error reading data: ${e.message}")
                isConnected = false
            }
        }.start()
    }

    private fun updateWaterConsumed(waterIntake: Int) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userRef = db.collection("users").document(user.uid)
            userRef.get()
                .addOnSuccessListener { doc ->
                    val current = doc.getLong("waterConsumed")?.toInt() ?: 0
                    val updated = current + waterIntake

                    userRef.update("waterConsumed", updated)
                        .addOnSuccessListener {
                            binding.updatedWaterIntake.text = "$updated ml"
                            Toast.makeText(this, "Water intake updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update water", Toast.LENGTH_SHORT).show()
                        }
                }
        } ?: Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
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
