package fr.univ_savoie.mamiejeanne;

import android.bluetooth.BluetoothManager;
import android.content.Context;
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
import fr.univ_savoie.mamiejeanne.services.BluetoothService;
import fr.univ_savoie.mamiejeanne.utils.flowerpower.FlowerPowerConstants;


public class MainActivity extends AppCompatActivity {

    private TemperatureController temperatureController;
    private IgrometrieController igrometrieController;
    private LightController lightController;
    private BluetoothService temperatureBluetoothService;
    private BluetoothService igrometrieBluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.temperatureBluetoothService = new BluetoothService(
                bluetoothManager,
                this,
                FlowerPowerConstants.CHARACTERISTIC_UUID_TEMPERATURE
        );
        this.igrometrieBluetoothService = new BluetoothService(
                bluetoothManager,
                this,
                FlowerPowerConstants.CHARACTERISTIC_UUID_SOIL_MOISTURE
        );

        this.initializeLight();
        this.initializeTemperature();
        this.initializeIgrometrie();
    }

    private void initializeTemperature() {

        this.temperatureController = new TemperatureController(
                this.getApplicationContext(),
                (TextView) findViewById(R.id.txtTemperature),
                this.temperatureBluetoothService
        );

        final Button btnPlus = (Button) findViewById(R.id.btnTemperaturePlus);
        btnPlus.setOnClickListener(this.temperatureController.clicPlusListener);

        final Button btnMoins = (Button) findViewById(R.id.btnTemperatureMoins);
        btnMoins.setOnClickListener(this.temperatureController.clicMoinsListener);
    }

    public void initializeIgrometrie() {

        this.igrometrieController = new IgrometrieController(
                this.getApplicationContext(),
                (TextView) findViewById(R.id.txtIgrometrie),
                this.igrometrieBluetoothService
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
