package fr.univ_savoie.mamiejeanne.controllers;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import fr.univ_savoie.mamiejeanne.callbacks.ICallbackAfterReadingValue;
import fr.univ_savoie.mamiejeanne.services.BluetoothService;
import fr.univ_savoie.mamiejeanne.services.PriseService;
import fr.univ_savoie.mamiejeanne.utils.Constants;

public class IgrometrieController {

    private final int igrometrie = 50;
    private String prisesIgrometrieState = Constants.PRISES_STATE_ON;
    private Context applicationContext;
    private TextView txtIgrometrie;
    private PriseService priseService;
    private BluetoothService bluetoothService;

    public IgrometrieController(Context context, TextView txtIgrometrie, BluetoothService bluetoothService) {

        this.applicationContext = context;
        this.bluetoothService = bluetoothService;
        this.bluetoothService.setAfterReadingValue(new MyReactIgrometrie());
        this.txtIgrometrie = txtIgrometrie;
        this.priseService = new PriseService(Constants.PRISES_ID_IGROMETRIE, context);


        //Tâches répétitives pour l'igrométrie
        igrometrieVerifHandler = new Handler();
        igrometrieVerifHandler.postDelayed(igrometrieVerifRunnable, Constants.IGROMETRIE_VERIF_DELAY);
    }

    public class MyReactIgrometrie implements ICallbackAfterReadingValue {

        @Override
        public void react(int igrometrieReelle) {
            System.out.println("MyReactIgrometrie.react");
            System.out.println("igrometrieReelle = " + igrometrieReelle);
            txtIgrometrie.setText(Integer.toString(igrometrieReelle));
            if (igrometrieReelle <= 50 && prisesIgrometrieState.equals(Constants.PRISES_STATE_OFF)) {
                priseService.turnPriseOn();
            } else if (igrometrieReelle > 50 && prisesIgrometrieState.equals(Constants.PRISES_STATE_ON)) {
                priseService.turnPriseOff();
            }

            igrometrieVerifHandler.postDelayed(IgrometrieController.this.igrometrieVerifRunnable, Constants.IGROMETRIE_VERIF_DELAY);
        }
    }

    //Toutes les 5 minutes, on éteind la prise si l'igrométrie réelle est > à 50
    //Et sinon on l'allume.
    private Handler igrometrieVerifHandler;
    private Runnable igrometrieVerifRunnable = new Runnable() {

        @Override
        public void run() {

            bluetoothService.getValue();
        }
    };
}
