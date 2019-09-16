package com.pk.vigyaan.currencydetector;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class Utils {
    private Utils utils;

    public Utils getInstance(){
        if(utils ==null)
            utils = new Utils();
        return utils;
    }

    public static void showShortToast(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public static AlertDialog showDialog(Context context,boolean cancellable, String title, String message){
        if (context!=null){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(cancellable);
            return builder.create();
        }
        return null;
    }
}
