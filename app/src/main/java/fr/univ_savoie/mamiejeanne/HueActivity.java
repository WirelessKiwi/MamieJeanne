package fr.univ_savoie.mamiejeanne;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import fr.univ_savoie.mamiejeanne.beans.Hue;
import fr.univ_savoie.mamiejeanne.beans.Light;
import fr.univ_savoie.mamiejeanne.utils.HttpRequest;

public class HueActivity extends AppCompatActivity {

    public Hue hue;
    public List<Light> lights;
    public int averageBrightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hue);

        // Initialization Hue
        this.initializeHue();
        JSONObject jsonObject = HttpRequest.get("http://192.168.1.44:3000/api/" + hue.getUsername() + "/lights");
        this.initializeLights(jsonObject);

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
        try {
            this.hue = new Hue();
            JSONObject params = new JSONObject();
            params.accumulate("devicetype", "my_hue_app#mamieJeanne");
            JSONObject jsonObject = HttpRequest.post("http://192.168.1.44:3000/api", params);
            JSONArray jsonArray = HttpRequest.convertJsonObjectToJsonArray(jsonObject);
            hue.setUsername(jsonArray.getJSONObject(0).getString("success"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to initialize lights
     * with state characteristics
     *
     * @param jsonObject
     */
    public void initializeLights(JSONObject jsonObject) {
        int sumBrightness = 0;
        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()){
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

                // Sum brightness for average
                sumBrightness += light.getBrightness();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Calculate average brightness with all lights
        this.averageBrightness = sumBrightness / this.lights.size();
    }

    /**
     * Method to send request PUT for each
     * light by varying the brightness
     *
     * @param increase
     */
    public void huePutLights(boolean increase) {
        JSONObject jsonObject = new JSONObject();
        String uri = "/api/" + this.hue.getUsername() + "/lights";

        for(int i = 0; i < this.lights.size(); i++) {
            try {
                Light light = this.lights.get(i);
                int brightness = light.getBrightness();

                if(increase) {
                    if(!light.isOn()) {
                        jsonObject.accumulate("on", true);
                    } else if(light.getSaturation() > brightness + 1) {
                        light.setBrightness(brightness + 1);
                        jsonObject.accumulate("bri", brightness + 1);
                    }
                } else {
                    if(light.isOn()) {
                        if(light.getBrightness() - 1 > 0) {
                            light.setBrightness(brightness - 1);
                            jsonObject.accumulate("bri", brightness - 1);
                        } else {
                            jsonObject.accumulate("on", false);
                        }
                    }
                }
                uri += light.getId() + "/state";
                HttpRequest.put("http://192.168.1.44:3000" + uri, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**********************
     *     Listeners
     **********************/

    public class ClickButtonHuePlus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ++averageBrightness;
            TextView brightnessValue = (TextView) findViewById(R.id.brightnessValue);
            brightnessValue.setText(Integer.toString(averageBrightness));
            huePutLights(true);
        }
    }

    public class ClickButtonHueMinus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            --averageBrightness;
            TextView brightnessValue = (TextView) findViewById(R.id.brightnessValue);
            brightnessValue.setText(Integer.toString(averageBrightness));
            huePutLights(false);
        }
    }

    /**********************/
}
