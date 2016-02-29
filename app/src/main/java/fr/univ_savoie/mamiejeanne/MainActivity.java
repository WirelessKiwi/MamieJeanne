package fr.univ_savoie.mamiejeanne;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import fr.univ_savoie.mamiejeanne.beans.Hue;
import fr.univ_savoie.mamiejeanne.callbacks.ILight;
import fr.univ_savoie.mamiejeanne.controllers.IgrometrieController;
import fr.univ_savoie.mamiejeanne.controllers.LightController;
import fr.univ_savoie.mamiejeanne.controllers.TemperatureController;


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

        this.initializeLight();
        this.initializeTemperature();
        this.initializeIgrometrie();
    }

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

    public void initializeLight() {

        this.lightController = new LightController(
                this.getApplicationContext(),
                (TextView) findViewById(R.id.brightnessSaturationValue),
                new ButtonsLight()
        );
    }

    public class ButtonsLight implements ILight {
        public void setButtons() {
            final Button buttonHueMinus = (Button) findViewById(R.id.hueMinus_id);
            buttonHueMinus.setOnClickListener(lightController.clickMinusListener);

            final Button buttonHuePlus = (Button) findViewById(R.id.huePlus_id);
            buttonHuePlus.setOnClickListener(lightController.clickPlusListener);
        }

        public void setAverage() {
            lightController.brightnessHueValue.setText(Integer.toString(lightController.lampService.getPercentageBrightnessSaturation()) + "%");
        }
    }

}
