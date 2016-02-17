package fr.univ_savoie.mamiejeanne.utils;

/**
 * Created by juliana on 15/02/2016.
 */
public class Constants {
    public static int BRIGHTNESS_INCREASE = Constants.BRIGHTNESS_MAX / 10;
    public static int SATURATION_INCREASE = Constants.SATURATION_MAX / 10;
    public static int BRIGHTNESS_DECREASE = Constants.BRIGHTNESS_MAX / 10;
    public static int SATURATION_DECREASE = Constants.SATURATION_MAX / 10;
    public static int BRIGHTNESS_MAX = 254;
    public static int SATURATION_MAX = 254;
    public static int BRIGHTNESS_SATURATION_MAX = BRIGHTNESS_MAX + SATURATION_MAX;

    //Pour la température
    public static final int TEMPERATURE_RECORD_DELAY = 1000; //en millisecondes
    public static final int TEMPERATURE_RETRIEVE_DELAY = 1000; //en millisecondes
    public static final int TEMPERATURE_VERIF_DELAY = 5000;

    //Pour l'igrométrie
    public static final int IGROMETRIE_VERIF_DELAY = 1000; //en millisecondes

    //Pour les prises
    public static final String PRISES_STATE_ON = "on";
    public static final String PRISES_STATE_OFF = "off";
    public static final String PRISES_ID_TEMPERATURE = "7f027c2d84f141299be1220c73f99481";
    public static final String PRISES_ID_IGROMETRIE = "uneautreid";


}


