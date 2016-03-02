package fr.univ_savoie.mamiejeanne.services;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import fr.univ_savoie.mamiejeanne.utils.Constants;

public class PriseService {

    private String idPrise;
    private HandlerPrises handlerPrisesOn = new HandlerPrises(Constants.PRISES_STATE_ON);
    private HandlerPrises handlerPrisesOff = new HandlerPrises(Constants.PRISES_STATE_OFF);
    private Context context;
    private String priseState = Constants.PRISES_STATE_ON;

    public PriseService(String idPrise, Context context) {
        this.idPrise = idPrise;
        this.context = context;
    }

    public void turnPriseOn() {
        this.turnPrise(Constants.PRISES_STATE_ON, this.handlerPrisesOn);
    }

    public void turnPriseOff() {
        this.turnPrise(Constants.PRISES_STATE_OFF, this.handlerPrisesOff);
    }

    private void turnPrise(String newState, HandlerPrises handlerPrises) {
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setBasicAuth("stretch", "mwnghcck");
        try {
            StringEntity entity = new StringEntity("<relay><state>" + newState + "</state></relay>");
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
            myClient.put(
                    this.context,
                    "http://192.168.140.191/core/appliances/" + this.idPrise + "/relay",
                    entity,
                    "application/x-www-form-urlencoded",
                    handlerPrises
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // MOCK //
        /*
        System.out.println("PriseService.turnPrise to " + newState);
        priseState = newState;*/
        /////////
    }

    public boolean isOff() {
        return this.priseState.equals(Constants.PRISES_STATE_OFF);
    }

    public boolean isOn() {
        return this.priseState.equals(Constants.PRISES_STATE_ON);
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
            priseState = switchTo;
        }

    }


}
