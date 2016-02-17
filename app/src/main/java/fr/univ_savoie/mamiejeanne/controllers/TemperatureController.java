package fr.univ_savoie.mamiejeanne.controllers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import fr.univ_savoie.mamiejeanne.beans.Temperature;
import fr.univ_savoie.mamiejeanne.database.DBManager;
import fr.univ_savoie.mamiejeanne.services.BluetoothService;
import fr.univ_savoie.mamiejeanne.services.PriseService;
import fr.univ_savoie.mamiejeanne.utils.Constants;

/**
 * Created by celinederoland on 2/17/16.
 */
public class TemperatureController {

    private String databaseName = "TemperaturesDatabase";
    private SQLiteDatabase db;
    private DBManager manager;
    private int temperature = 20;
    private Context applicationContext;
    private TextView txtTemperature;
    private PriseService priseService;
    public View.OnClickListener clicMoinsListener = new ClicTemperatureMoins();
    public View.OnClickListener clicPlusListener = new ClicTemperaturePlus();
    private BluetoothService bluetoothService = new BluetoothService();
    private Handler temperatureRecordHandler;
    private Handler temperatureRetrieveHandler;
    private Handler temperatureVerifHandler;


    public TemperatureController(Context context, TextView txtTemperature) {

        this.applicationContext = context;
        this.txtTemperature = txtTemperature;
        this.priseService = new PriseService(Constants.PRISES_ID_TEMPERATURE, context);

        //Création de la base de données
        db = this.applicationContext.openOrCreateDatabase(databaseName, Context.MODE_APPEND, null);
        manager = new DBManager(db);
        manager.cleanTable();

        //Tâches répétitives pour la température
        temperatureRecordHandler = new Handler();
        temperatureRecordHandler.postDelayed(temperatureRecordRunnable, Constants.TEMPERATURE_RECORD_DELAY);

        temperatureRetrieveHandler = new Handler();
        temperatureRetrieveHandler.postDelayed(temperatureRetrieveRunnable, Constants.TEMPERATURE_RETRIEVE_DELAY);

        temperatureVerifHandler = new Handler();
        temperatureVerifHandler.postDelayed(temperatureVerifRunnable, Constants.TEMPERATURE_VERIF_DELAY);
    }

    //Toutes les heures, on enregistre la température utilisée
    private Runnable temperatureRecordRunnable = new Runnable() {

        @Override
        public void run() {

            Date date = new Date();
            manager.add(new Temperature(date.getSeconds() % 10,temperature));

            temperatureRecordHandler.postDelayed(this, Constants.TEMPERATURE_RECORD_DELAY);
        }
    };

    //Toutes les heures, on récupère la température moyenne pour cette heure et on la règle
    private Runnable temperatureRetrieveRunnable = new Runnable() {

        @Override
        public void run() {

            Date date = new Date();
            temperature = manager.getTemperatureMoyenneByTime(date.getSeconds() % 10);

            temperatureRetrieveHandler.postDelayed(this, Constants.TEMPERATURE_RETRIEVE_DELAY);
        }
    };

    //Toutes les 5 minutes, on éteind la prise si la température réelle est > à la température souhaitée
    //Et sinon on l'allume.
    private Runnable temperatureVerifRunnable = new Runnable() {

        @Override
        public void run() {

            int temperatureReelle = bluetoothService.getTemperature();

            if (temperatureReelle <= temperature && priseService.isOff()) {
                priseService.turnPriseOn();
            } else if (temperatureReelle > temperature && priseService.isOn()) {
                priseService.turnPriseOff();
            }

            temperatureVerifHandler.postDelayed(this, Constants.TEMPERATURE_VERIF_DELAY);
        }
    };

    private abstract class ClicTemperature implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            updateTemperature();
            txtTemperature.setText(Integer.toString(temperature));
        }

        protected abstract void updateTemperature();
    }

    private class ClicTemperaturePlus extends ClicTemperature {
        @Override
        protected void updateTemperature() {
            temperature++;
        }
    }

    private class ClicTemperatureMoins extends ClicTemperature {
        @Override
        protected void updateTemperature() {
            temperature--;
        }
    }

}
