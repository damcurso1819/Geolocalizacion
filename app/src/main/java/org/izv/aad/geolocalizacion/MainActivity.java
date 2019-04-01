package org.izv.aad.geolocalizacion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    //https://developer.android.com/training/location
    //https://github.com/googlesamples/android-play-location/blob/master/LocationUpdates/app/src/main/java/com/google/android/gms/location/sample/locationupdates/MainActivity.java

    private static final int PERMISO_GPS = 1;
    public static final String TAG = MainActivity.class.getSimpleName();

    private AddressResultReceiver resultReceiver;
    private FusedLocationProviderClient fusedLocationClient;
    private Location ultimaPosicion = null;
    private LocationCallback callback;
    private LocationRequest request;

    private TextView tvUno;
    private TextView tvDos;

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                return;
            }
            String resultado = resultData.getString(ServicioGeocoder.Constants.RESULT_DATA_KEY);
            Log.v(TAG, resultado);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDos = findViewById(R.id.tvDos);
        tvUno = findViewById(R.id.tvUno);

        Log.v(TAG, "inicio");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.v(TAG, "aquí debe saltar un elemento que explique la razón por la que se necesita el permiso");
            } else {
                Log.v(TAG, "aquí se solicita el permiso (normalmente la primera vez)");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISO_GPS);
            }
        } else {
            Log.v(TAG, "se dispone del permiso por lo que se lanza directamente");
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        resultReceiver = new AddressResultReceiver(new Handler());
        callback = createLocationCallback();
        request = createLocationRequest();

        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                Log.v(TAG, "obteniendo última Location conocida");
                if (location != null) {
                    Log.v(TAG, "última Location no es null");
                    ultimaPosicion = location;
                    tvUno.setText("latitud: " + location.getLatitude());
                    tvDos.setText("longitud " + location.getLongitude());
                    ultimaPosicion.setLatitude(37.161262);
                    ultimaPosicion.setLongitude(-3.5922742);

                } else {
                    Log.v(TAG, "última Location es null");
                }
                }
            });
        fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                null /* Looper */);
    }

    private LocationCallback createLocationCallback() {
        Log.v(TAG, "creando objeto que se ejecuta después de cada nueva Location");
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.v(TAG, "se ha recibido nueva Location");
                for (Location location : locationResult.getLocations()) {
                    ultimaPosicion = location;
                    Log.v(TAG, location.getLatitude() + " " + location.getLongitude());
                }
                requestAddress(ultimaPosicion);
                fusedLocationClient.removeLocationUpdates(callback);
                tvUno.setText("latitud: " + ultimaPosicion.getLatitude());
                tvDos.setText("longitud " + ultimaPosicion.getLongitude());
            }
        };
        return locationCallback;
    }

    private void requestAddress(Location location) {
        Intent intent = new Intent(this, ServicioGeocoder.class);
        intent.putExtra(ServicioGeocoder.Constants.RECEIVER, resultReceiver);
        intent.putExtra(ServicioGeocoder.Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    protected LocationRequest createLocationRequest() {
        Log.v(TAG, "creando solicitud de Locations");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        //locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISO_GPS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "con permiso");
                    getLocation();
                } else {
                    Log.v(TAG, "sin permiso");
                }
            }
        }
    }
}