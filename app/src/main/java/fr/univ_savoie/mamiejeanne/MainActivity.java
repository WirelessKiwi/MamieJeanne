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
import fr.univ_savoie.mamiejeanne.utils.Constants;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.univ_savoie.mamiejeanne.beans.Hue;
import fr.univ_savoie.mamiejeanne.beans.Light;
import fr.univ_savoie.mamiejeanne.utils.HttpClient;

public class MainActivity extends AppCompatActivity {

    public Hue hue;
    public List<Light> lights;
    public int percentageBrightnessSaturation;
    public int averageBrightness;
    public int averageSaturation;

    private String databaseName = "TemperaturesDatabase";
    private SQLiteDatabase db;
    private DBManager manager;
    private int temperature;
    private String prisesTemperatureState = Constants.PRISES_STATE_ON;
    private String prisesIgrometrieState = Constants.PRISES_STATE_ON;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Création de la base de données
        db = openOrCreateDatabase(databaseName, MODE_APPEND, null);
        manager = new DBManager(db);
        manager.cleanTable();

        //Boutons pour la température
        Button btnPlus = (Button) findViewById(R.id.btnTemperaturePlus);
        btnPlus.setOnClickListener(new ClicTemperaturePlus());

        Button btnMoins = (Button) findViewById(R.id.btnMoins);
        btnMoins.setOnClickListener(new ClicTemperatureMoins());

        //Tâches répétitives pour la température
        temperatureRecordHandler = new Handler();
        temperatureRecordHandler.postDelayed(temperatureRecordRunnable, Constants.TEMPERATURE_RECORD_DELAY);

        temperatureRetrieveHandler = new Handler();
        temperatureRetrieveHandler.postDelayed(temperatureRetrieveRunnable, Constants.TEMPERATURE_RETRIEVE_DELAY);

        temperatureVerifHandler = new Handler();
        temperatureVerifHandler.postDelayed(temperatureVerifRunnable, Constants.TEMPERATURE_VERIF_DELAY);

        //Tâches répétitives pour l'igrométrie
        igrometrieVerifHandler = new Handler();
        igrometrieVerifHandler.postDelayed(igrometrieVerifRunnable, Constants.IGROMETRIE_VERIF_DELAY);


        // Initializations
        this.initializeHue();

        // Listener buttons
        final Button buttonHueMinus = (Button) findViewById(R.id.hueMinus_id);
        buttonHueMinus.setOnClickListener(new ClickButtonHueMinus());

        final Button buttonHuePlus = (Button) findViewById(R.id.huePlus_id);
        buttonHuePlus.setOnClickListener(new ClickButtonHuePlus());
    }

    /**
     * Method to initialize hue
     */
    public void initializeHue() {
        this.hue = new Hue();
        this.lights = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("devicetype", "my_hue_app#mamieJeanne");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpClient.post(getApplicationContext(), "/api", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    JSONObject username = new JSONObject(response.getJSONObject(0).getString("success"));
                    hue.setUsername(username.getString("username"));
                    initializeLights();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Method to initialize lights
     * with state characteristics
     */
    public void initializeLights() {
        HttpClient.get("/api/" + hue.getUsername() + "/lights", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                bindLights(response);
            }
        });
    }

    /**
     * Method to feed the array
     * list lights
     *
     * @param jsonObject
     */
    public void bindLights(JSONObject jsonObject) {
        int sumBrightness = 0;
        int sumSaturation = 0;
        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            JSONObject jsonState = null;
            String key = (String) iterator.next();
            try {
                jsonState = jsonObject.getJSONObject(key).getJSONObject("state");

                // Initialize light
                Light light = new Light();
                light.setId(key);
                light.setIsOn(jsonState.getBoolean("on"));
                light.setBrightness(jsonState.getInt("bri"));
                light.setSaturation(jsonState.getInt("sat"));
                light.setColorMode(jsonState.getString("colormode"));
                this.lights.add(light);

                // Sum brightness and saturation for average
                sumBrightness += light.getBrightness();
                sumSaturation += light.getSaturation();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Calculate average brightness and saturation with all lights
        this.averageBrightness = sumBrightness / this.lights.size();
        this.averageSaturation = sumSaturation / this.lights.size();

        this.percentageBrightnessSaturation = (int) (((double) this.averageSaturation + (double) this.averageBrightness) / (double) Constants.BRIGHTNESS_SATURATION_MAX * 100);

        // Set average on the view
        TextView brightnessSaturationValue = (TextView) findViewById(R.id.brightnessSaturationValue);
        brightnessSaturationValue.setText(Integer.toString(percentageBrightnessSaturation));
    }

    /**
     * Method to send request PUT for each
     * light by varying the brightness
     *
     * @param increase
     */
    public void huePutLights(final boolean increase) {
        final String base_uri = "/api/" + this.hue.getUsername() + "/lights/";

        StringEntity entity = null;
        JSONObject jsonObject = new JSONObject();

        if (! this.lights.isEmpty()) {
            for (int i = 0; i < this.lights.size(); i++) {
                HttpClient.uri = base_uri;

                Light light = this.lights.get(i);
                int brightness = light.getBrightness();
                int saturation = light.getSaturation();

                if (increase) {
                    if (!light.isOn()) {
                        try {
                            light.setBrightness(Constants.BRIGHTNESS_INCREASE);
                            light.setSaturation(Constants.BRIGHTNESS_INCREASE);
                            jsonObject.put("bri", Constants.SATURATION_INCREASE);
                            jsonObject.put("sat", Constants.SATURATION_INCREASE);
                            jsonObject.put("on", true);
                            light.setIsOn(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (brightness + Constants.BRIGHTNESS_INCREASE < Constants.BRIGHTNESS_MAX
                            && saturation + Constants.SATURATION_INCREASE < Constants.SATURATION_MAX) {
                        // Brightness
                        brightness = brightness + Constants.BRIGHTNESS_INCREASE;
                        light.setBrightness(brightness);

                        // Saturation
                        saturation = saturation + Constants.BRIGHTNESS_INCREASE;
                        light.setSaturation(saturation);

                        try {
                            jsonObject.put("bri", brightness);
                            jsonObject.put("sat", saturation);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (light.isOn()) {
                        if (brightness - Constants.BRIGHTNESS_DECREASE > 1
                                && saturation - Constants.SATURATION_DECREASE > 1) {
                            // Brightness
                            brightness = brightness - Constants.BRIGHTNESS_DECREASE;
                            light.setBrightness(brightness);

                            // Saturation
                            saturation = saturation - Constants.SATURATION_DECREASE;
                            light.setSaturation(saturation);

                            try {
                                jsonObject.put("bri", brightness);
                                jsonObject.put("sat", saturation);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                light.setBrightness(0);
                                light.setSaturation(0);
                                jsonObject.put("bri", 0);
                                jsonObject.put("sat", 0);
                                jsonObject.put("on", false);
                                light.setIsOn(false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                try {
                    entity = new StringEntity(jsonObject.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                HttpClient.uri += light.getId() + "/state";

                HttpClient.put(getApplicationContext(), HttpClient.uri, entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        HttpClient.uri = "";
                        calculateAverage();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });
            }
        }
    }

    /**
     * Method to recalculate average
     * luminosity and update view
     */
    private void calculateAverage() {
        int sumBrightness = 0;
        int sumSaturation = 0;
        for (int i = 0; i < this.lights.size(); i++) {
            Light light = this.lights.get(i);
            System.err.println(light.getBrightness());
            System.err.println(light.getSaturation());
            sumBrightness += light.getBrightness();
            sumSaturation += light.getSaturation();
        }

        averageBrightness = sumBrightness / this.lights.size();
        averageSaturation = sumSaturation / this.lights.size();

        this.percentageBrightnessSaturation = (int) (((double) this.averageSaturation + (double) this.averageBrightness) / (double) Constants.BRIGHTNESS_SATURATION_MAX * 100);

        // Set average on the view
        TextView brightnessSaturationValue = (TextView) findViewById(R.id.brightnessSaturationValue);
        brightnessSaturationValue.setText(Double.toString(percentageBrightnessSaturation));
    }

    /**********************
     * Listeners
     **********************/

    public class ClickButtonHuePlus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            huePutLights(true);
        }
    }

    public class ClickButtonHueMinus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            huePutLights(false);
        }
    }

    /**********************/

    private abstract class ClicTemperature implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            updateTemperature();
            TextView txtTemperature = (TextView) findViewById(R.id.txtTemperature);
            txtTemperature.setText(Integer.toString(temperature));
        }

        protected abstract void updateTemperature();
    }

    //Toutes les heures, on enregistre la température utilisée
    private Handler temperatureRecordHandler;
    private Runnable temperatureRecordRunnable = new Runnable() {

        @Override
        public void run() {

            Date date = new Date();
            manager.add(new Temperature(date.getSeconds() % 10,temperature));

            temperatureRecordHandler.postDelayed(this, Constants.TEMPERATURE_RECORD_DELAY);
        }
    };

    //Toutes les heures, on récupère la température moyenne pour cette heure et on la règle
    private Handler temperatureRetrieveHandler;
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
    private Handler temperatureVerifHandler;
    private Runnable temperatureVerifRunnable = new Runnable() {

        @Override
        public void run() {

            int temperatureReelle = (int) Math.floor(Math.random()*15 + 15); //TODO : remplacer par la température scannée par bluetooth

            if (temperatureReelle <= temperature && prisesTemperatureState.equals(Constants.PRISES_STATE_OFF)) {
                turnPrise(Constants.PRISES_STATE_ON, handlerPrisesOn, Constants.PRISES_ID_TEMPERATURE);
            } else if (temperatureReelle > temperature && prisesTemperatureState.equals(Constants.PRISES_STATE_ON)) {
                turnPrise(Constants.PRISES_STATE_OFF, handlerPrisesOff, Constants.PRISES_ID_TEMPERATURE);
            }

            temperatureVerifHandler.postDelayed(this, Constants.TEMPERATURE_VERIF_DELAY);
        }
    };

    //Toutes les 5 minutes, on éteind la prise si la température réelle est > à 50
    //Et sinon on l'allume.
    private Handler igrometrieVerifHandler;
    private Runnable igrometrieVerifRunnable = new Runnable() {

        @Override
        public void run() {

            int igrometrieReelle = (int) Math.floor(Math.random()*30 + 20); //TODO : remplacer par l'igrométrie scannée par bluetooth

            if (igrometrieReelle <= 50 && prisesIgrometrieState.equals(Constants.PRISES_STATE_OFF)) {
                turnPrise(Constants.PRISES_STATE_ON, handlerPrisesOn, Constants.PRISES_ID_IGROMETRIE);
            } else if (igrometrieReelle > 50 && prisesIgrometrieState.equals(Constants.PRISES_STATE_ON)) {
                turnPrise(Constants.PRISES_STATE_OFF, handlerPrisesOff, Constants.PRISES_ID_IGROMETRIE);
            }

            igrometrieVerifHandler.postDelayed(this, Constants.IGROMETRIE_VERIF_DELAY);
        }
    };

    private void turnPrise(String newState, HandlerPrises handlerPrises, String idPrise) {
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setBasicAuth("stretch", "mwnghcck");
        try {
            StringEntity entity = new StringEntity("<relay><state>" + newState + "</state></relay>");
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
            myClient.put(getApplicationContext(), "http://192.168.140.191/core/appliances/" + idPrise + "/relay", entity, "application/x-www-form-urlencoded", handlerPrises);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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

    private class HandlerPrises extends TextHttpResponseHandler {

        private String switchTo = Constants.PRISES_STATE_OFF;

        public HandlerPrises(String switchTo) {
            this.switchTo = switchTo;
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            prisesTemperatureState = switchTo;
        }

    }

    private HandlerPrises handlerPrisesOn = new HandlerPrises(Constants.PRISES_STATE_ON);
    private HandlerPrises handlerPrisesOff = new HandlerPrises(Constants.PRISES_STATE_OFF);
}
