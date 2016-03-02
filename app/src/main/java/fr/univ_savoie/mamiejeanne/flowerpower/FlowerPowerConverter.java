package fr.univ_savoie.mamiejeanne.flowerpower;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.util.Log;


public class FlowerPowerConverter {

    public static String getValueFromSensor(String parameter) {
        String uuid = null;
        if (parameter.equals(FlowerPowerConstants.NAME_AIR_TEMP)) {
            return FlowerPowerConstants.CHARACTERISTIC_UUID_TEMPERATURE;
        } else if (parameter.equals(FlowerPowerConstants.NAME_SUNLIGHT_UUID)) {
            return FlowerPowerConstants.CHARACTERISTIC_UUID_SUNLIGHT;
        } else if (parameter.equals(FlowerPowerConstants.NAME_SOIL_EC)) {
            return FlowerPowerConstants.CHARACTERISTIC_UUID_SOIL_EC;
        } else if (parameter.equals(FlowerPowerConstants.NAME_SOIL_TEMP)) {
            return FlowerPowerConstants.CHARACTERISTIC_UUID_SOIL_TEMP;
        } else if (parameter.equals(FlowerPowerConstants.NAME_SOIL_WC)) {
            return FlowerPowerConstants.CHARACTERISTIC_UUID_SOIL_MOISTURE;
        } else if (parameter.equals(FlowerPowerConstants.NAME_BATTERY_CHARAC)) {
            return FlowerPowerConstants.CHARACTERISTIC_UUID_BATTERY_LEVEL;
        }
        return uuid;
    }


    public static double sendSensorValueRequested(BluetoothGattCharacteristic characteristic, Activity activity)
    {

        final byte[] data = characteristic.getValue();
        System.out.println("characteristic UUID : " + characteristic.getUuid().toString());
        if (characteristic.getUuid().toString().equals(FlowerPowerConstants.CHARACTERISTIC_UUID_SUNLIGHT))
        {
            int i = data[0] + ((data[1] & 0xFF) * 256);
            double sunlight = ValueMapper.getInstance(activity).mapSunlight(i);
            System.out.println("sunlight : " + sunlight);
            return sunlight;
        }
        else if (characteristic.getUuid().toString().equals(FlowerPowerConstants.CHARACTERISTIC_UUID_TEMPERATURE))
        {
            int i = data[1] * 256 + (data[0] & 0xFF);
            int temperature = ValueMapper.getInstance(activity).mapTemperature(i);
            System.out.println("Température de l'air : " + temperature);
            return temperature;
        }
        else if (characteristic.getUuid().toString().equals(FlowerPowerConstants.CHARACTERISTIC_UUID_SOIL_MOISTURE))
        {
            int i = data[1] * 256 + (data[0] & 0xFF);
            double soilMoisture = ValueMapper.getInstance(activity).mapSoilMoisture(i);
            System.out.println("soilMoisture : " + soilMoisture);
            return soilMoisture;
        }
        else if (characteristic.getUuid().toString().equals(FlowerPowerConstants.CHARACTERISTIC_UUID_SOIL_TEMP))
        {
            int i = data[1] * 256 + (data[0] & 0xFF);
            int temperature = ValueMapper.getInstance(activity).mapTemperature(i);
            System.out.println("temperature : " + temperature);
            return temperature;
        }
        else if (characteristic.getUuid().toString().equals(FlowerPowerConstants.CHARACTERISTIC_UUID_SOIL_EC))
        {
            int i = new Byte(data[0]).intValue();
            //double soilMoisture = ValueMapper.getInstance(activity).mapSoilMoisture(i);
            System.out.println("Humidité : " + i);
            return i;
        }
        else if (characteristic.getUuid().toString().equals(FlowerPowerConstants.CHARACTERISTIC_UUID_BATTERY_LEVEL))
        {
            int batteryLevel = new Byte(data[0]).intValue();
            System.out.println("batteryLevel : " + batteryLevel);
            return batteryLevel;
        }

        return 0;
    }

}
