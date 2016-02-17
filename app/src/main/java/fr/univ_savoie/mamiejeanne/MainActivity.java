package fr.univ_savoie.mamiejeanne;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import fr.univ_savoie.mamiejeanne.controllers.IgrometrieController;
import fr.univ_savoie.mamiejeanne.controllers.LightController;
import fr.univ_savoie.mamiejeanne.controllers.TemperatureController;


public class MainActivity extends AppCompatActivity {

    private TemperatureController temperatureController;
    private IgrometrieController igrometrieController;
    private LightController lightController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.initializeTemperature();
        this.initializeIgrometrie();
        this.initializeLight();

    }

    private void initializeTemperature() {

        this.temperatureController = new TemperatureController(
                this.getApplicationContext(),
                (TextView) findViewById(R.id.txtTemperature)
        );

        final Button btnPlus = (Button) findViewById(R.id.btnTemperaturePlus);
        btnPlus.setOnClickListener(this.temperatureController.clicPlusListener);

        final Button btnMoins = (Button) findViewById(R.id.btnMoins);
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
                (TextView) findViewById(R.id.brightnessSaturationValue)
        );

        final Button buttonHueMinus = (Button) findViewById(R.id.hueMinus_id);
        buttonHueMinus.setOnClickListener(this.lightController.clickMinusListener);

        final Button buttonHuePlus = (Button) findViewById(R.id.huePlus_id);
        buttonHuePlus.setOnClickListener(this.lightController.clickPlusListener);
    }

}
