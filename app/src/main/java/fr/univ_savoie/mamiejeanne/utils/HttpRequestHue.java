package fr.univ_savoie.mamiejeanne.utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import fr.univ_savoie.mamiejeanne.beans.Hue;

/**
 * Created by juliana on 03/02/2016.
 */
public class HttpRequestHue {

    public static JSONObject hueGet(String url){
        InputStream inputStream = null;
        String result = "";
        JSONObject json = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
                json = new JSONObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    private static String convertInputStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static Hue hueConnection() {
        Hue hue = new Hue();
        InputStream inputStream = null;
        String result = "";
        try {

            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // Make POST request to the given URL
            HttpPost httpPost = new HttpPost("http://192.168.1.44:3000/api");

            String json = "";

            // Build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("devicetype", "my_hue_app#mamieJeanne");

            // Convert JSONObject to JSON to String
            json = jsonObject.toString();

            // Set json to StringEntity
            StringEntity se = new StringEntity(json);

            // Set httpPost Entity
            httpPost.setEntity(se);

            // Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
                JSONObject jsonResult = new JSONObject(result);
                hue.setUsername(jsonResult.getString("success"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hue;
    }
}
