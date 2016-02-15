package fr.univ_savoie.mamiejeanne.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import fr.univ_savoie.mamiejeanne.beans.Temperature;


public class DBManager {

    public static final int DEFAULT_TEMPERATURE = 20;
    private final SQLiteDatabase db;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                       Constructor                                         //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public DBManager(SQLiteDatabase _db) {

        db = _db;

        db.execSQL("CREATE TABLE IF NOT EXISTS Temperatures("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, " //column 0
                + "heure INTEGER," //column 1
                + "value INTEGER)" //column 2
        );
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                          Delete All                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public void cleanTable() {

        db.execSQL("DELETE FROM Temperatures");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                Get temperatures from Database                             //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public ArrayList<Temperature> getTemperatures() {

        Cursor resultSet = db.rawQuery("SELECT * FROM Temperatures", null);
        ArrayList<Temperature> temperatures = new ArrayList<>();

        while (resultSet.moveToNext()) {
            temperatures.add(new Temperature(resultSet.getInt(0), resultSet.getInt(1), resultSet.getInt(2)));
        }
        return temperatures;
    }

    public int getTemperatureMoyenneByTime(int time) {

        Cursor resultSet = db.rawQuery("SELECT * FROM Temperatures", null);

        int S = 0;
        int count = 0;
        while (resultSet.moveToNext()) {
            S += resultSet.getInt(2);
            count++;
        }

        if (S != 0) {
            return S / count;
        }
        return DEFAULT_TEMPERATURE;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                   Add temperature                                         //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public Temperature add(Temperature temperature) {

        temperature.setId(getMaxId());
        db.execSQL("INSERT INTO Temperatures VALUES("
                        + temperature.getId() + ","
                        + temperature.getHeure() + ","
                        + temperature.getValue()
                        + ")"
        );

        return temperature;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                          Tools                                            //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public int getMaxId() {

        Cursor resultSet = db.rawQuery("SELECT * FROM Temperatures ORDER BY id DESC LIMIT 1",null);
        if (!resultSet.moveToNext()) { return 1; }

        resultSet.moveToFirst();
        return resultSet.getInt(0) + 1;

    }
}
