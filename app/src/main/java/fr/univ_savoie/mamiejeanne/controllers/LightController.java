package fr.univ_savoie.mamiejeanne.controllers;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import fr.univ_savoie.mamiejeanne.ILight;
import fr.univ_savoie.mamiejeanne.services.LampService;

/**
 * Created by celinederoland on 2/17/16.
 */
public class LightController {


    private final Context applicationContext;
    public final TextView brightnessHueValue;
    public View.OnClickListener clickMinusListener = new ClickButtonHueMinus();
    public View.OnClickListener clickPlusListener = new ClickButtonHuePlus();
    public LampService lampService;
    public ILight buttonsLight;

    public LightController(Context context, TextView brightnessHueValue, ILight buttonsLight) {

        this.applicationContext = context;
        this.brightnessHueValue = brightnessHueValue;
        this.lampService = new LampService(this.applicationContext);
        this.buttonsLight = buttonsLight;
        lampService.initializeHue(buttonsLight);
        brightnessHueValue.setText(Integer.toString(lampService.getPercentageBrightnessSaturation()));
    }

    private class ClickButtonHuePlus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            lampService.huePutLights(true, buttonsLight);
        }
    }

    private class ClickButtonHueMinus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            lampService.huePutLights(false, buttonsLight);
        }
    }
}
