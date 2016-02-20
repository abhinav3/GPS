package com.example.omsairam.gps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String LOG_TAG = "Debug";
    private TextView txtinfo;
    private TextView txtAdd;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    String errorMessage="err";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Build the mGoogleApiClient.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();

        txtinfo = (TextView) findViewById(R.id.location_lat_view);
        txtAdd = (TextView) findViewById(R.id.location_address_view);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //connect the mGoogleApiClient
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {

        //Disconnect the mGoogleApiClient
        mGoogleApiClient.disconnect();
        super.onStop();
    }




    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);//Update location every 10 second.
        mLocationRequest.setFastestInterval(5000);

        /*Since SDK 23, you should/need to check the permission before you call Location API functionality. Here is an example of how to do it:*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "GoogleApiClient has been suspended", Toast.LENGTH_LONG).show();//not able to connect this time only.
        Log.i(LOG_TAG, "GoogleApiClient has been suspended");

    }




    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, location.toString());
        Toast.makeText(getApplicationContext(), "HI "+location.toString(), Toast.LENGTH_LONG).show();
        txtinfo.setText("HI " + location.toString());

        //We only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && location != null) {


            ////////////////////////////////////////////////////////

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;


            try {
                // Using getFromLocation() returns an array of Addresses for the area immediately
                // surrounding the given latitude and longitude. The results are a best guess and are
                // not guaranteed to be accurate.
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, we get just a single address.
                        5);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = getString(R.string.service_not_available);
                Log.e(LOG_TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = getString(R.string.invalid_lat_long_used);
                Log.e(LOG_TAG, errorMessage + ". " +
                        "Lat = " + location.getLatitude() +
                        ", Long = " + location.getLongitude(), illegalArgumentException);
            }
            ArrayList<String> addressFragments = new ArrayList<String>();
            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = getString(R.string.no_address_found);
                    Log.e(LOG_TAG, errorMessage);
                }

            } else {
                for(Address add: addresses) {
                   // Address address = addresses.get(0);

                    String s = add.getAddressLine(0)+","+add.getAddressLine(1)+","
                            +add.getAddressLine(2)+","+add.getAddressLine(3);

                        addressFragments.add(s);

                    Log.i(LOG_TAG, getString(R.string.address_found));
                }
            }

            txtAdd.setText(TextUtils.join(System.getProperty("line.separator"), addressFragments));
            ////////////////////////////////////////////////////////////


        }

        ////////////////////////////////////////////////////
       /* String cityName=null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses=null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location
                    .getLongitude(), 5);
            if (addresses.size() > 0)
                System.out.println(addresses.get(0).getLocality());
            cityName=addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String s = location.getLongitude()+","+location.getLatitude() +
                "\n\n"+addresses.get(0).getAddressLine(0)+","+addresses.get(0).getAddressLine(1)+","
                +addresses.get(0).getAddressLine(2)+","+addresses.get(0).getAddressLine(3);
        txtAdd1.setText(s);*/

    }
 @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "GoogleApiClient connection has failed", Toast.LENGTH_LONG).show();//not able to connect this time only.
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }
}
