package com.example.idrated

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.idrated.databinding.ActivityGoalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityGoalBinding

    private val bluetoothPermissionRequestCode = 1
    private val esp32MacAddress = "EC:64:C9:5E:05:B2"

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Bluetooth connection status flag
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkAndRequestPermissions()

        loadWaterGoalAndConsumed()

        binding.btnSetDailyGoal.setOnClickListener {
            val intent = Intent(this, GenderSelectionActivity::class.java)
            startActivity(intent)
        }

        binding.addWaterButton.setOnClickListener {
            val waterIntake = binding.waterInput.text.toString().toIntOrNull()
            if (waterIntake != null && waterIntake > 0) {
                updateWaterConsumed(waterIntake)
                binding.waterInput.text.clear() // Clear the input after adding
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        binding.LogoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Register the Bluetooth state change receiver
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)
    }

    private fun checkAndRequestPermissions() {
        val bluetoothPermission = android.Manifest.permission.BLUETOOTH
        val bluetoothAdminPermission = android.Manifest.permission.BLUETOOTH_ADMIN
        val bluetoothConnectPermission = android.Manifest.permission.BLUETOOTH_CONNECT
        val bluetoothScanPermission = android.Manifest.permission.BLUETOOTH_SCAN
        val locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(this, bluetoothPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, bluetoothAdminPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, bluetoothConnectPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, bluetoothScanPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, locationPermission) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(bluetoothPermission, bluetoothAdminPermission, bluetoothConnectPermission,
                    bluetoothScanPermission, locationPermission),
                bluetoothPermissionRequestCode)
        } else {
            enableBluetoothIfNeeded()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == bluetoothPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                enableBluetoothIfNeeded()
            } else {
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableBluetoothIfNeeded() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
        } else {
            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, bluetoothPermissionRequestCode)
            } else {
                startBluetoothScan() // Start scanning for devices if Bluetooth is enabled
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothScan() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(bluetoothReceiver, filter)
            bluetoothAdapter.startDiscovery()
        } else {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                val deviceAddress = device.address
                if (deviceAddress == esp32MacAddress) {
                    binding.deviceNameTextView.text = "Connected to: ${device.name}"
                    isConnected = true
                }
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    Toast.makeText(this@GoalActivity, "Bluetooth turned off", Toast.LENGTH_SHORT).show()
                    binding.deviceNameTextView.text = "No device connected"
                    isConnected = false
                }
                BluetoothAdapter.STATE_ON -> {
                    Toast.makeText(this@GoalActivity, "Bluetooth turned on", Toast.LENGTH_SHORT).show()
                    startBluetoothScan() // Start scanning again
                }
            }
        }
    }

    private fun loadWaterGoalAndConsumed() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val savedGoal = document.getLong("waterGoal")?.toInt() ?: 0
                        val consumed = document.getLong("waterConsumed")?.toInt() ?: 0
                        updateGoalDisplay(savedGoal, consumed)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load goal", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateGoalDisplay(goal: Int, consumed: Int) {
        binding.goalDisplay.text = goal.toString()
        binding.goalConsumed.text = consumed.toString()
        updatePercentage(goal, consumed)
        updateProgressBar(goal, consumed)
    }

    private fun updatePercentage(goal: Int, consumed: Int) {
        val percentage = if (goal > 0) {
            ((consumed.toDouble() / goal) * 100).toInt()
        } else {
            0
        }
        binding.percent.text = "$percentage%"
    }

    private fun updateProgressBar(goal: Int, consumed: Int) {
        binding.progressBar.max = goal
        binding.progressBar.progress = consumed
    }

    private fun updateWaterConsumed(waterIntake: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val currentConsumed = document.getLong("waterConsumed")?.toInt() ?: 0
                        val newConsumed = (currentConsumed + waterIntake).coerceAtMost(binding.goalDisplay.text.toString().toInt())

                        db.collection("users").document(userId)
                            .update("waterConsumed", newConsumed)
                            .addOnSuccessListener {
                                loadWaterGoalAndConsumed() // Refresh the display with updated data
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update water consumed", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        unregisterReceiver(bluetoothStateReceiver) // Unregister Bluetooth state change receiver
    }
}
