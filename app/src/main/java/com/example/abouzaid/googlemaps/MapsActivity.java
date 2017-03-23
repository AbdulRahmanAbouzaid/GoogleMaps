package com.example.abouzaid.googlemaps;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,PlaceSelectionListener,
        OnConnectionFailedListener {

    //Handling MultiDex Error
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //create custom Info Window inner Class
    class MyWindow implements InfoWindowAdapter{
        private final View myContentsView;

        MyWindow(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            TextView tvTitle = (TextView)myContentsView.findViewById(R.id.title);
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = (TextView)myContentsView.findViewById(R.id.snippet);
            tvSnippet.setText(marker.getSnippet());
            ImageView img  = (ImageView)myContentsView.findViewById(R.id.imageView);
            img.setImageResource(R.drawable.scu);
            return myContentsView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    // Some Global Vars
    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //establishing auto Complete Search
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);


            mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();

        AddPlaceRequest place =
                new AddPlaceRequest(
                        "suez canal university", // Name
                        new LatLng(30.623109, 32.2729409), // Latitude and longitude
                        "W Esplanade, Manly NSW 2095", // Address
                        Collections.singletonList(Place.TYPE_SCHOOL), // Place types
                        "+61 1800 199 742", // Phone number
                        Uri.parse("http://www.manlysealifesanctuary.com.au/") // Website
                );

        Places.GeoDataApi.addPlace(mGoogleApiClient, place)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        Log.i(TAG, "Place add result: " + places.getStatus().toString());
                        Log.i(TAG, "Added place: " + places.get(0).getName().toString());
                        places.release();
                    }
                });


    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Adding styled map By style_json file in strings.xml
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
        // Add a marker & Position the map's camera to SCU .
        LatLng scu = new LatLng(30.623109, 32.2729409);
        final Marker mark = mMap.addMarker(new MarkerOptions()
                .position(scu));
        mMap.setInfoWindowAdapter(new MyWindow());
        mark.setTitle("SCU");
        mark.setSnippet("suez canal university");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(scu,14));

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mMap.getCameraPosition();
                if(cameraPosition.zoom > 14) {
                    mark.remove();
                }
            }
        });



    }

    /* autocomplete Search Bar --> Work on Selected Place */
        @Override
        public void onPlaceSelected(Place place) {
            // TODO: Get info about the selected place.
            Log.i(TAG, "Place: " + place.getName());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( place.getLatLng(),14));
        }

    /*  autocomplete Search Bar --> If an Error happens  */
        @Override
        public void onError(Status status) {
            // TODO: Handle the error.
            Log.i(TAG, "An error occurred: " + status);
            Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                    Toast.LENGTH_SHORT).show();
        }



}
