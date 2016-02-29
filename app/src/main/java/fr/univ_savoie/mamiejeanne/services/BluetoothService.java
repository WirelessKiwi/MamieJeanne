package fr.univ_savoie.mamiejeanne.services;

import fr.univ_savoie.mamiejeanne.utils.IReactIgrometrie;
import fr.univ_savoie.mamiejeanne.utils.IReactTemperature;

/**
 * Created by celinederoland on 2/17/16.
 */
public class BluetoothService {

    public int getTemperature(IReactTemperature reactTemperature) {

        reactTemperature.react((int) Math.floor(Math.random()*15 + 15));
        // MOCK //
        //return (int) Math.floor(Math.random()*15 + 15);
        //////////
        return 0;
    }

    public int getIgrometrie(IReactIgrometrie reactIgrometrie) {

        reactIgrometrie.react((int) Math.floor(Math.random()*15 + 15));
        // MOCK //
        //return (int) Math.floor(Math.random()*30 + 20);
        //////////
        return 0;
    }
}
