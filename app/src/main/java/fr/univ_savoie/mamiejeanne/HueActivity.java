package fr.univ_savoie.mamiejeanne;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import fr.univ_savoie.mamiejeanne.beans.Hue;
import fr.univ_savoie.mamiejeanne.beans.Light;
import fr.univ_savoie.mamiejeanne.utils.HttpRequestHue;

public class HueActivity extends AppCompatActivity {

    public int brightness;

    public Hue hue;

    public List<Light> lights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hue);

        // Initialization Hue
        this.hue = HttpRequestHue.hueConnection();
        JSONObject jsonObject = HttpRequestHue.hueGet("/api/" + hue.getUsername() + "/lights");
        this.initializeLights(jsonObject);

        // Listener buttons
        final Button buttonHueMinus = (Button) findViewById(R.id.hueMinus_id);
        buttonHueMinus.setOnClickListener(new ClickButtonHueMinus());

        final Button buttonHuePlus = (Button) findViewById(R.id.huePlus_id);
        buttonHuePlus.setOnClickListener(new ClickButtonHuePlus());
    }

    private void initializeLights(JSONObject jsonObject) {

    }

    /**
     * Listeners
     */
    public class ClickButtonHuePlus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ++brightness;
            TextView brightnessValue = (TextView) findViewById(R.id.brightnessValue);
            brightnessValue.setText(Integer.toString(brightness));
        }
    }

    public class ClickButtonHueMinus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            --brightness;
            TextView brightnessValue = (TextView) findViewById(R.id.brightnessValue);
            brightnessValue.setText(Integer.toString(brightness));
        }
    }
}
