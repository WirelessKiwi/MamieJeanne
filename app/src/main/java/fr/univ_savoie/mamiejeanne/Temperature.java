package fr.univ_savoie.mamiejeanne;

/**
 * Created by celinederoland on 2/3/16.
 */
public class Temperature {

    private int id = 0;
    private int heure;
    private int value;

    public Temperature(int id, int heure, int value) {
        this.id = id;
        this.heure = heure;
        this.value = value;
    }

    public Temperature(int heure, int value) {
        this.heure = heure;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeure() {
        return heure;
    }

    public void setHeure(int heure) {
        this.heure = heure;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
