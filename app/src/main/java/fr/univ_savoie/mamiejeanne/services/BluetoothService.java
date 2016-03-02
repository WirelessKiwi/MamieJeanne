package fr.univ_savoie.mamiejeanne.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.UUID;

import fr.univ_savoie.mamiejeanne.MainActivity;
import fr.univ_savoie.mamiejeanne.callbacks.ICallbackAfterReadingValue;
import fr.univ_savoie.mamiejeanne.utils.flowerpower.FlowerPowerConstants;
import fr.univ_savoie.mamiejeanne.utils.flowerpower.FlowerPowerConverter;

public class BluetoothService {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBlueToothGatt;
    private static final String LOG_TAG = "TOTO";
    private ICallbackAfterReadingValue callbackTemperature;
    private MainActivity mainActivity;
    private String serviceUUID;
    private ICallbackAfterReadingValue afterReadingValue;

    public BluetoothService(BluetoothManager bluetoothManager, MainActivity mainActivity, String serviceUUID) {

        mBluetoothAdapter = bluetoothManager.getAdapter();
        this.serviceUUID = serviceUUID;
        this.mainActivity = mainActivity;
    }


    public int getValue() {

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.w("bluetooth disabled", "Bluetooth is disabled");
        }
        else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        // MOCK //
        //reactTemperature.react((int) Math.floor(Math.random()*15 + 15));
        //////////
        return 0;
    }

    private void onDeviceFound(BluetoothDevice device){
        if(device.getName() != null){
            Log.d(LOG_TAG, device.getName());
            if(device.getName().contains("Flower")){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mBlueToothGatt = device.connectGatt(mainActivity, false, mGattCallback);
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
                        gatt.readCharacteristic(fpService.getCharacteristic(UUID.fromString(serviceUUID)));
                    } else {
                        Log.w(LOG_TAG, "onServicesDiscovered received: " + status);
                    }
                }
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
                    Log.d(LOG_TAG, "On characteristic read");
                    afterReadingValue.react((int)FlowerPowerConverter.sendSensorValueRequested(characteristic, mainActivity));
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

    public void setAfterReadingValue(ICallbackAfterReadingValue afterReadingValue) {
        this.afterReadingValue = afterReadingValue;
    }
}
