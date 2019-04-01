package org.izv.aad.geolocalizacion;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.izv.aad.geolocalizacion.MainActivity.TAG;

public class ServicioGeocoder extends IntentService {

    protected ResultReceiver receiver;

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }

    public ServicioGeocoder() {
        super("ServicioGeocoder");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, Thread.currentThread().getName());
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (intent == null) {
            return;
        }
        String errorMessage = "";

        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        receiver = intent.getParcelableExtra(Constants.RECEIVER);//duda

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    10);
        } catch (IOException ioException) {
            errorMessage = "servicio no disponible";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "geolocalización no válida";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no hay dirección";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            String resultado = "";
            for(Address address1: addresses) {
                resultado = "";
                for(int i = 0; i <= address1.getMaxAddressLineIndex(); i++) {
                    resultado += "\n" + address1.getAddressLine(i);
                }
                Log.v(TAG, resultado);
            }
            resultado = "";
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                resultado += "\n" + address.getAddressLine(i);
            }
            Log.v(TAG, resultado);
            deliverResultToReceiver(Constants.SUCCESS_RESULT, resultado);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
