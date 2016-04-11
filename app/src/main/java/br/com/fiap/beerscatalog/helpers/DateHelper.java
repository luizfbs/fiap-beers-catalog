package br.com.fiap.beerscatalog.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static String Parse(long timeStamp){
        try{
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return dateFormat.format(new Date(timeStamp * 1000));
        } catch(Exception ex){
            return String.valueOf(timeStamp);
        }
    }

}
