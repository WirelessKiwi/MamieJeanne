package fr.univ_savoie.mamiejeanne;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import fr.univ_savoie.mamiejeanne.beans.Temperature;
import fr.univ_savoie.mamiejeanne.database.DBManager;

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
        System.out.println("on create Celine Activity");
        setContentView(R.layout.activity_celine_essai);

        //Associate buttons with actions
        Button btnPlus = (Button) findViewById(R.id.btnPlus);
        btnPlus.setOnClickListener(new ClicPlus());

        Button btnMoins = (Button) findViewById(R.id.btnMoins);
        btnMoins.setOnClickListener(new ClicMoins());

        Button btnPriseOn = (Button) findViewById(R.id.btnPrise);
        btnPriseOn.setOnClickListener(new PriseListener("on"));

        Button btnPriseOff = (Button) findViewById(R.id.btnPriseOff);
        btnPriseOff.setOnClickListener(new PriseListener("off"));

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

    private class PriseListener implements View.OnClickListener {

        private String state = "off";

        public PriseListener(String state) {
            super();
            this.state = state;
        }

        @Override
        public void onClick(View v) {
            AsyncHttpClient myClient = new AsyncHttpClient();
            myClient.setBasicAuth("stretch", "mwnghcck");
            try {
                StringEntity entity = new StringEntity("<relay><state>" + this.state + "</state></relay>");
                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
                myClient.put(getApplicationContext(), "http://192.168.140.191/core/appliances/7f027c2d84f141299be1220c73f99481/relay", entity, "application/x-www-form-urlencoded", new HandlerPrises());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private class HandlerPrises extends TextHttpResponseHandler {

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            System.out.println("fail with request");
            System.out.println("statusCode = " + statusCode);
            System.out.println("responseString = " + responseString);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            System.out.println("success with request");
            System.out.println("responseString = " + responseString);
        }

    }
}
