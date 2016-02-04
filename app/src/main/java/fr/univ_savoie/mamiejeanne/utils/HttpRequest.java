package fr.univ_savoie.mamiejeanne.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.univ_savoie.mamiejeanne.beans.Hue;

/**
 * Created by juliana on 04/02/2016.
 */
public class HttpRequest {
    /**
     * Method to do a GET request
     *
     * @param url
     * @return
     */
    public static JSONObject get(String url){
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

    /**
     * Method to do a POST request
     *
     * @return
     */
    public static JSONObject post(String url, JSONObject jsonObject) {
        InputStream inputStream = null;
        String result = "";
        JSONObject jsonResult = null;
        try {

            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // Make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

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
                result = HttpRequest.convertInputStreamToString(inputStream);
                jsonResult = new JSONObject(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonResult;
    }

    /**
     * Method to do a PUT request
     *
     * @param jsonObject
     * @param url
     * @return
     */
    public static JSONObject put(String url, JSONObject jsonObject) {
        InputStream inputStream = null;
        String result = "";
        JSONObject jsonResult = null;
        try {

            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // Make POST request to the given URL
            HttpPost httpPut = new HttpPost(url);

            String json = "";

            // Convert JSONObject to JSON to String
            json = jsonObject.toString();

            // Set json to StringEntity
            StringEntity se = new StringEntity(json);

            // Set httpPost Entity
            httpPut.setEntity(se);

            // Set some headers to inform server about the type of the content
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");

            // Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPut);
            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
                jsonResult = new JSONObject(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonResult;
    }

    /**
     * Method to convert request
     * result in String
     *
     * @param is
     * @return
     */
    public static String convertInputStreamToString(InputStream is) {
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

    public static JSONArray convertJsonObjectToJsonArray(JSONObject jsonObject) {
        JSONArray jsonResult = null;
        try {
            jsonResult = new JSONArray(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonResult;
    }
}
