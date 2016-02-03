package fr.univ_savoie.mamiejeanne;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class CelineEssaiActivity extends AppCompatActivity {

    //for temperature management
    public static final int TEMPERATURE_RECORD_DELAY = 1000; //en millisecondes
    public static final int TEMPERATURE_RETRIEVE_DELAY = 1000; //en millisecondes
    private static final int VERIF_DELAY = 5000;
    public int temperature = 20;

    //for database management
    public String databaseName = "TemperaturesDatabase";
    private SQLiteDatabase db;
    public DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_celine_essai);

        //Associate buttons with actions
        Button btnPlus = (Button) findViewById(R.id.btnPlus);
        btnPlus.setOnClickListener(new ClicPlus());

        Button btnMoins = (Button) findViewById(R.id.btnMoins);
        btnMoins.setOnClickListener(new ClicMoins());

        //Record temperature every DELAY ms
        temperatureRecordHandler = new Handler();
        temperatureRecordHandler.postDelayed(temperatureRecordRunnable, TEMPERATURE_RECORD_DELAY);

        temperatureRetrieveHandler = new Handler();
        temperatureRetrieveHandler.postDelayed(temperatureRetrieveRunnable, TEMPERATURE_RETRIEVE_DELAY);

        verifHandler = new Handler();
        verifHandler.postDelayed(verifRunnable, VERIF_DELAY);

        //create database manager
        db = openOrCreateDatabase(databaseName, MODE_APPEND, null);
        manager = new DBManager(db);
        manager.cleanTable();


    }

    private Handler temperatureRecordHandler;
    private Runnable temperatureRecordRunnable = new Runnable() {

        @Override
        public void run() {
            // Code à éxécuter de façon périodique

            TextView txtTime = (TextView) findViewById(R.id.txtTime);
            Date date = new Date();
            txtTime.setText(Integer.toString(date.getSeconds() % 10));

            manager.add(new Temperature(date.getSeconds() % 10,temperature));
            //txtTime.setText(Integer.toString(date.getHours()*60*60 + date.getMinutes()*60 + date.getSeconds()));

            temperatureRecordHandler.postDelayed(this, TEMPERATURE_RECORD_DELAY);
        }
    };

    private Handler temperatureRetrieveHandler;
    private Runnable temperatureRetrieveRunnable = new Runnable() {

        @Override
        public void run() {

            // Code à éxécuter de façon périodique
            Date date = new Date();
            int m = manager.getTemperatureMoyenneByTime(date.getSeconds() % 10);
            TextView txtTime = (TextView) findViewById(R.id.txtMoyenne);
            txtTime.setText(Integer.toString(m));

            temperatureRetrieveHandler.postDelayed(this, TEMPERATURE_RETRIEVE_DELAY);
        }
    };

    private Handler verifHandler;
    private Runnable verifRunnable = new Runnable() {

        @Override
        public void run() {

            // Code à éxécuter de façon périodique
            manager.getTemperatures();

            verifHandler.postDelayed(this, VERIF_DELAY);
        }
    };

    public void onPause() {
        super.onPause();
        if(temperatureRecordHandler != null)
            temperatureRecordHandler.removeCallbacks(temperatureRecordRunnable); // On arrete le callback
    }

    private class ClicPlus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            temperature++;
            TextView txtTemperature = (TextView) findViewById(R.id.txtTemperature);
            txtTemperature.setText(Integer.toString(temperature));
        }
    }

    private class ClicMoins implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            temperature--;
            TextView txtTemperature = (TextView) findViewById(R.id.txtTemperature);
            txtTemperature.setText(Integer.toString(temperature));
        }
    }
}
