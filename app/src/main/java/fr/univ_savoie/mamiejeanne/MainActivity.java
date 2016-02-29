package fr.univ_savoie.mamiejeanne;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import fr.univ_savoie.mamiejeanne.beans.Hue;
import fr.univ_savoie.mamiejeanne.controllers.IgrometrieController;
import fr.univ_savoie.mamiejeanne.controllers.LightController;
import fr.univ_savoie.mamiejeanne.controllers.TemperatureController;
import fr.univ_savoie.mamiejeanne.utils.Constants;
import fr.univ_savoie.mamiejeanne.utils.HttpClient;


public class MainActivity extends AppCompatActivity {

    private TemperatureController temperatureController;
    private IgrometrieController igrometrieController;
    private LightController lightController;

    public Hue hue;
    public List<fr.univ_savoie.mamiejeanne.beans.Light> lights;
    public int percentageBrightnessSaturation;
    public int averageBrightness;
    public int averageSaturation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initializeTemperature();
        this.initializeIgrometrie();

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
                fr.univ_savoie.mamiejeanne.beans.Light light = new fr.univ_savoie.mamiejeanne.beans.Light();
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

                fr.univ_savoie.mamiejeanne.beans.Light light = this.lights.get(i);
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
                        saturation = saturation + Constants.SATURATION_INCREASE;
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
            fr.univ_savoie.mamiejeanne.beans.Light light = this.lights.get(i);
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

    private void initializeTemperature() {

        this.temperatureController = new TemperatureController(
                this.getApplicationContext(),
                (TextView) findViewById(R.id.txtTemperature)
        );

        final Button btnPlus = (Button) findViewById(R.id.btnTemperaturePlus);
        btnPlus.setOnClickListener(this.temperatureController.clicPlusListener);

        final Button btnMoins = (Button) findViewById(R.id.btnTemperatureMoins);
        btnMoins.setOnClickListener(this.temperatureController.clicMoinsListener);
    }

    public void initializeIgrometrie() {

        this.igrometrieController = new IgrometrieController(
                this.getApplicationContext(),
                (TextView) findViewById(R.id.txtIgrometrie)
        );
    }

    /*public void initializeLight() {

        this.lightController = new LightController(
                this.getApplicationContext(),
                (TextView) findViewById(R.id.brightnessSaturationValue),
                new Light()
        );
    }

    public class Light implements ILight {
        public void setButtons() {
            lightController.brightnessHueValue.setText(Integer.toString(lightController.lampService.getPercentageBrightnessSaturation()));

            final Button buttonHueMinus = (Button) findViewById(R.id.hueMinus_id);
            buttonHueMinus.setOnClickListener(lightController.clickMinusListener);

            final Button buttonHuePlus = (Button) findViewById(R.id.huePlus_id);
            buttonHuePlus.setOnClickListener(lightController.clickPlusListener);
        }
    }*/

}
