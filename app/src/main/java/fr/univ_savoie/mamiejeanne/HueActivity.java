package fr.univ_savoie.mamiejeanne;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import fr.univ_savoie.mamiejeanne.beans.Hue;
import fr.univ_savoie.mamiejeanne.beans.Light;
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
        this.initializeLights();

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
        RequestParams params = new RequestParams();
        params.put("devicetype", "my_hue_app#mamieJeanne");
        HttpClient.post("/api", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    JSONObject username = new JSONObject(response.getJSONObject(0).getString("success"));
                    hue.setUsername(username.getString("username"));
                    System.out.println(hue.getUsername());
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
        this.lights = new ArrayList<>();

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
        RequestParams params = new RequestParams();
        String uri = "/api/" + this.hue.getUsername() + "/lights/";

        for (int i = 0; i < this.lights.size(); i++) {
            Light light = this.lights.get(i);
            int brightness = light.getBrightness();
            int saturation = light.getSaturation();

            if (increase) {
                System.err.println("INCREASE");
                if (!light.isOn()) {
                    System.err.println("ALLUMER");
                    params.put("on", true);
                } else if (brightness < 254) {
                    System.err.println("AUGMENTER brightness");
                    brightness++;
                    light.setBrightness(brightness);
                    params.put("bri", brightness);
                } else if (saturation < 254) {
                    System.err.println("AUGMENTER saturation");
                    saturation++;
                    light.setSaturation(saturation);
                    params.put("sat", saturation);
                }
            } else {
                if (light.isOn()) {
                    if (brightness > 1) {
                        brightness--;
                        light.setBrightness(brightness);
                        params.put("bri", brightness);
                    } else if (saturation > 1) {
                        saturation--;
                        light.setSaturation(saturation);
                        params.put("sat", saturation);
                    } else {
                        params.put("on", false);
                    }
                }
            }
            uri += light.getId() + "/state";

            HttpClient.put(uri, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    calculateAverage();
                }
            });
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
