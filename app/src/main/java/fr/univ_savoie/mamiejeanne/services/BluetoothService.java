package fr.univ_savoie.mamiejeanne.services;

/**
 * Created by celinederoland on 2/17/16.
 */
public class BluetoothService {

    public int getTemperature() {
        return (int) Math.floor(Math.random()*15 + 15);
    }

    public int getIgrometrie() {
        return (int) Math.floor(Math.random()*30 + 20);
    }
}
