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
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.util.*

class GoalActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityGoalBinding

    private val bluetoothPermissionRequestCode = 1
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var isConnected = false

    private val pairedDevicesList = mutableListOf<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkAndRequestPermissions()

        // Set daily goal button
        binding.btnSetDailyGoal.setOnClickListener {
            startActivity(Intent(this, GenderInputFragment::class.java))
        }

        // Add water button
        binding.addWaterButton.setOnClickListener {
            val waterIntake = binding.waterInput.text.toString().toIntOrNull()
            if (waterIntake != null && waterIntake > 0) {
                updateWaterConsumed(waterIntake)
                binding.waterInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout button
        binding.LogoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBluetoothDropdown()
    }

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

    private fun setupBluetoothDropdown() {
        binding.connectButton.setOnClickListener {
            val selectedDeviceIndex = binding.deviceDropdown.selectedItemPosition
            if (selectedDeviceIndex >= 0 && selectedDeviceIndex < pairedDevicesList.size) {
                connectToDevice(pairedDevicesList[selectedDeviceIndex])
            } else {
                Toast.makeText(this, "Please select a device", Toast.LENGTH_SHORT).show()
            }
        }
        loadPairedDevices()
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
            val uuid: UUID = device.uuids?.firstOrNull()?.uuid
                ?: UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Default SPP UUID
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()

            inputStream = bluetoothSocket?.inputStream
            isConnected = true
            binding.deviceNameTextView.text = "Connected to: ${device.name}"

            Toast.makeText(this, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
            startReadingData()
        } catch (e: Exception) {
            e.printStackTrace()
            closeBluetoothConnection()
            Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startReadingData() {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            try {
                Log.d("Bluetooth", "Starting data read thread")
                while (isConnected) {
                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream?.read(buffer)
                    if (bytesRead != null && bytesRead > 0) {
                        val receivedData = String(buffer, 0, bytesRead)
                        Log.d("Bluetooth", "Data received: $receivedData")
                        handler.post {
                            binding.receivedDataTextView.text = "Received Data: $receivedData"
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
        if (currentUser != null) {
            val userId = currentUser.uid

            val userDocRef = db.collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentWaterIntake = document.getLong("waterConsumed")?.toInt() ?: 0
                        val updatedWaterIntake = currentWaterIntake + waterIntake

                        userDocRef.update("waterConsumed", updatedWaterIntake)
                            .addOnSuccessListener {
                                binding.receivedDataTextView.text =
                                    "Water Consumed: $updatedWaterIntake ml"
                                Toast.makeText(this, "Water intake updated!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update water intake: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        userDocRef.set(mapOf("waterConsumed" to waterIntake))
                            .addOnSuccessListener {
                                binding.receivedDataTextView.text =
                                    "Water Consumed: $waterIntake ml"
                                Toast.makeText(this, "Water intake updated!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to set water intake: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error retrieving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeBluetoothConnection() {
        try {
            inputStream?.close()
            bluetoothSocket?.close()
            isConnected = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeBluetoothConnection()
    }
}
