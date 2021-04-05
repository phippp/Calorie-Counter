package com.example.caloriecounter.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class ConnectionReceiver extends BroadcastReceiver {

    public ConnectionReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0){
            Toast.makeText(context, "Whilst Airplane Mode is enabled certain features will not be available", Toast.LENGTH_LONG).show();
        } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                Toast.makeText(context, "Wifi Connected, full functionality is available", Toast.LENGTH_LONG).show();
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected()) {
                Toast.makeText(context, "Wifi Disconnected, certain features will not be available", Toast.LENGTH_LONG).show();
            }
        }

    }
}