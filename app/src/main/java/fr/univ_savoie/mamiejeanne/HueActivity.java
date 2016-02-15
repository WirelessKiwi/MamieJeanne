package fr.univ_savoie.mamiejeanne;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
import fr.univ_savoie.mamiejeanne.beans.Light;
import fr.univ_savoie.mamiejeanne.utils.Constants;
import fr.univ_savoie.mamiejeanne.utils.HttpClient;

public class HueActivity extends AppCompatActivity {

    public Hue hue;
    public List<Light> lights;
    public int averageBrightness;
    public int averageSaturation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hue);

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

        // Set average on the view
        TextView brightnessValue = (TextView) findViewById(R.id.brightnessValue);
        brightnessValue.setText(Integer.toString(averageBrightness));

        TextView saturationValue = (TextView) findViewById(R.id.saturationValue);
        saturationValue.setText(Integer.toString(averageSaturation));
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
                    } else if (brightness + Constants.BRIGHTNESS_INCREASE < Constants.BRIGHTNESS_MAX) {
                        brightness = brightness + Constants.BRIGHTNESS_INCREASE;
                        light.setBrightness(brightness);
                        try {
                            jsonObject.put("bri", brightness);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (saturation + Constants.SATURATION_INCREASE < Constants.SATURATION_MAX) {
                        saturation = saturation + Constants.BRIGHTNESS_INCREASE;
                        light.setSaturation(saturation);
                        try {
                            jsonObject.put("sat", saturation);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (light.isOn()) {
                        if (brightness - Constants.BRIGHTNESS_DECREASE > 1) {
                            brightness = brightness - Constants.BRIGHTNESS_DECREASE;
                            light.setBrightness(brightness);
                            try {
                                jsonObject.put("bri", brightness);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (saturation - Constants.SATURATION_DECREASE > 1) {
                            saturation = saturation - Constants.SATURATION_DECREASE;
                            light.setSaturation(saturation);
                            try {
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

        // Brightness
        TextView brightnessValue = (TextView) findViewById(R.id.brightnessValue);
        brightnessValue.setText(Integer.toString(averageBrightness));

        // Saturation
        TextView saturationValue = (TextView) findViewById(R.id.saturationValue);
        saturationValue.setText(Integer.toString(averageSaturation));
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
}
