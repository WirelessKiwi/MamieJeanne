package fr.univ_savoie.mamiejeanne.controllers;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import fr.univ_savoie.mamiejeanne.services.LampService;

/**
 * Created by celinederoland on 2/17/16.
 */
public class LightController {


    private final Context applicationContext;
    private final TextView brightnessHueValue;
    public View.OnClickListener clickMinusListener = new ClickButtonHueMinus();
    public View.OnClickListener clickPlusListener = new ClickButtonHuePlus();
    private LampService lampService;

    public LightController(Context context, TextView brightnessHueValue) {

        this.applicationContext = context;
        this.brightnessHueValue = brightnessHueValue;
        this.lampService = new LampService(this.applicationContext);
        lampService.initializeHue();
        brightnessHueValue.setText(Integer.toString(lampService.getPercentageBrightnessSaturation()));
    }

    private class ClickButtonHuePlus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            brightnessHueValue.setText(Integer.toString(lampService.huePutLights(true)));

        }
    }

    private class ClickButtonHueMinus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            brightnessHueValue.setText(Integer.toString(lampService.huePutLights(false)));
        }
    }

}
