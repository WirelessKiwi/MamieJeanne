package fr.univ_savoie.mamiejeanne.utils;

/**
 * Created by juliana on 15/02/2016.
 */
public class Constants {
    // Pour la luminosité
    public static String LIGHT_IP = "192.168.140.167";
    public static int BRIGHTNESS_INCREASE = 25;
    public static int SATURATION_INCREASE = 25;
    public static int BRIGHTNESS_DECREASE = 25;
    public static int SATURATION_DECREASE = 25;
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
    public static final String PRISES_ID_TEMPERATURE = "00636707cfa2422cb0ef28a7ec98ef03";
    public static final String PRISES_ID_IGROMETRIE = "11da93b75017489cb78b8856dec44d2a";


}


