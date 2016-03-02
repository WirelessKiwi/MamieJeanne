package fr.univ_savoie.mamiejeanne;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import fr.univ_savoie.mamiejeanne.flowerpower.FlowerPowerConstants;
import fr.univ_savoie.mamiejeanne.flowerpower.FlowerPowerConverter;

public class BluetoothActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBlueToothGatt;
    private static final String LOG_TAG = "TOTO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.w("bluetooth disabled", "Bluetooth is disabled");
        }
        else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private void onDeviceFound(BluetoothDevice device){
        if(device.getName() != null){
            Log.d(LOG_TAG, device.getName());
            if(device.getName().contains("Flower")){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mBlueToothGatt = device.connectGatt(this, false, mGattCallback);
            }
        } else {
            Log.d(LOG_TAG, "Device name is null");
            Log.d(LOG_TAG, device.getAddress());
        }
    }

    private BluetoothGattCallback mGattCallback =
    new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(LOG_TAG, "Connected to GATT server.");
                mBlueToothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(LOG_TAG, "Disconnected from device");
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService fpService = gatt.getService(UUID.fromString(FlowerPowerConstants.SERVICE_UUID_FLOWER_POWER));
                gatt.readCharacteristic(fpService.getCharacteristic(UUID.fromString(FlowerPowerConstants.CHARACTERISTIC_UUID_TEMPERATURE)));
            } else {
                Log.w(LOG_TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            Log.d(LOG_TAG, "On characteristic read");
            FlowerPowerConverter.sendSensorValueRequested(characteristic, BluetoothActivity.this);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(LOG_TAG, "Characteristic : "+ FlowerPowerConstants.getCharacteristicName(characteristic));
        }
    };



    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
    new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            onDeviceFound(device);
        }
    };
}
