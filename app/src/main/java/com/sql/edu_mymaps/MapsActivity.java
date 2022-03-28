package com.sql.edu_mymaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sql.edu_mymaps.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
// FragmentActivity

// view binding
// https://developer.android.com/topic/libraries/view-binding#java

// Publishing our APP
// https://play.google.com/console/about/releasewithconfidence/

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;


    private final static int PLACE_PICKER_REQUEST = 123;
    private static final long MIN_TIME = 1000;
    private static final float MIN_DISTANCE = 1000;


    private static final String TAG = "tag";

    private LocationManager locationManager;
    private LocationListener locationListener;

    //private GoogleApiClient mgoogleApiClient;
    private GoogleApiClient googleApiClient;

    //LocationRequest locationRequest;


    Geocoder geocoder;


    Marker marker;
    Circle circle;

    double travelSpeed = 0;


    LatLng finalAdddresssPos, addressPos;

    boolean setDestination = false;

    List<Address> address1;  // package..


    EditText editText, addresstext, finalAddressText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());

        if( GooglePlayServicesAvailable()) {
            setContentView(binding.getRoot());
        }
        else{
            Toast.makeText(this, "Please Install Google Play services to run this app", Toast.LENGTH_SHORT).show();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(12.9778739, 77.5904463);
        mMap.addMarker(new MarkerOptions().position(location).title("Marker in Bangalore"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        mMap.getUiSettings().setZoomControlsEnabled(true);  // + / -
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        getLocationUpdates();  // All run time permission for the location manger..

    }



    private boolean GooglePlayServicesAvailable() {


/**   Lets check for the installed play service ...  */

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Oops!! cant load the play services...", Toast.LENGTH_SHORT).show();
        }

        return false;



    }

    /** Permissions for location manager */

    private void getLocationUpdates() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);

                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
                } else {
                    Toast.makeText(this, "No Provider Enabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        }
    }

    public void getDirections(View view) {

       // Toast.makeText(this, "At Get Directions", Toast.LENGTH_SHORT).show();


//        // Mysore 12.3106368,76.5656492
//        // Ooty 11.4118505,76.658402
//                String uri = "http://maps.google.com/maps?f=d&hl=en&sMysore="+12.3106368+","+76.5656492
//                        +"&dOoty="+11.4118505+","+76.658402;
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
//                startActivity(Intent.createChooser(intent, "Select an application"));

        addresstext       = (EditText) findViewById(R.id.addressEditText);
        finalAddressText  = (EditText) findViewById(R.id.finalAddressEditText);


        showMapFromLocation(addresstext.getText().toString(), finalAddressText.getText().toString());
    }

    ////////////////////////////
    private void showMapFromLocation(String src, String dest) {

        double srcLat = 0, srcLng = 0, destLat = 0, destLng = 0;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            if (true) {   //mMaps
                List<Address> srcAddresses = geocoder.getFromLocationName(src,
                        1);
                if (srcAddresses.size() > 0) {
                    Address location = srcAddresses.get(0);
                    srcLat = location.getLatitude();
                    srcLng = location.getLongitude();
                }
                List<Address> destAddresses = geocoder.getFromLocationName(
                        dest, 1);
                if (destAddresses.size() > 0) {
                    Address location = destAddresses.get(0);
                    destLat = location.getLatitude();
                    destLng = location.getLongitude();
                }



                // Get the distance from PointA to PointB

                /// gets the distance in meeters so convert that to KM
                //  LatLng myLocation = new LatLng(12.9778791, 77.5904463);
                Location locationA = new Location("PointA ");//LocationManager.GPS_PROVIDER);
                locationA.setLatitude(srcLat);
                locationA.setLongitude(srcLng);


                // mysore zoo 12.3022057,76.6619995
                Location locationB = new Location("Point B");//LocationManager.GPS_PROVIDER);
                locationB.setLatitude(destLat);
                locationB.setLongitude(destLng);

                float distance = locationA.distanceTo(locationB)/1000;
                Toast.makeText(this, "Distance Between "+srcAddresses+" - "+destAddresses, Toast.LENGTH_SHORT).show();
                Log.d("tag"," -> "+"Distance Between "+srcAddresses+" - "+destAddresses);
                Toast.makeText(this, " The aerial distance in KM  : "+new DecimalFormat("#.##").format(distance) ,
                        Toast.LENGTH_SHORT).show();
                Log.d("tag"," ->  KM  : "+new DecimalFormat("#.##").format(distance));


                ////////////// End of finding the distance
                String desLocation = "&daddr=" + Double.toString(destLat) + ","
                        + Double.toString(destLng);
                String currLocation = "saddr=" + Double.toString(srcLat) + ","
                        + Double.toString(srcLng);
                // "d" means driving car, "w" means walking "r" means by bus
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?" + currLocation
                                + desLocation + "&dirflg=d"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.setClassName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity");
                startActivity(intent);



                String whatsAppMessage = "http://maps.google.com/maps?" + currLocation + desLocation + "&dirflg=d";

                // PHASE II Sending My travel src , dest location to whats ap
                //
                //
                // sendWhatsAppMessage(whatsAppMessage);
                // Send the data to server...( x, y )
                // Any reg user can reterive the data from server....

                //////  Send a message or location to  A PARTICULAR CONTACT FROM UR CONTACTS AS FOLLOWS

//                                        try {
//
//
//                                            Intent i = new Intent(Intent.ACTION_VIEW);
//
//                                            String url = "https://api.whatsapp.com/send?phone="+ "+919448384716" +"&text=" +
//                                                    URLEncoder.encode("My MSG for u", "UTF-8");
//                                            i.setPackage("com.whatsapp");
//                                            i.setData(Uri.parse(url));
//
//                                                startActivity(i);
//
//                                        } catch (Exception e){
//                                            e.printStackTrace();
//                                        }

                ////////


            }


        } catch (IOException e) {
            Log.e(TAG, "Error when showing google map directions, E: " + e);
        } catch (Exception e) {
            Log.e(TAG, "Error when showing google map directions, E: " + e);
        }
    }
    ///////// End of ShowMap


    // Whats app program receiving the src and dest to display the travelling point...
    public void sendWhatsAppMessage(String whatsAppMessage) {
        // String whatsAppMessage = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;
        Intent sendIntent = new Intent();

//        Uri uri = Uri.parse("smsto:" + "1234567890");
//        Intent i = new Intent(Intent.ACTION_SENDTO, uri);

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);

        /*
        Uri uri = Uri.parse("smsto:" + number);
    Intent i = new Intent(Intent.ACTION_SENDTO, uri);
    i.setPackage("com.whatsapp");
    startActivity(Intent.createChooser(i, ""));
         */
    }

    public void LocationToReach(View view) {

        //  editText = (EditText) findViewById(R.id.locationToReach);
        //  String locationtoRaech = editText.getText().toString();

        // Ohio..  / Ooty
        String locationtoRaech1 = binding.locationToReach.getText().toString();
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = null;

        try {
            list = geocoder.getFromLocationName(locationtoRaech1, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Address address = list.get(0);
        String locality = address.getLocality();
        Toast.makeText(this, "Locality : " + locality, Toast.LENGTH_SHORT).show();

        // x,y - Ooty, 17( Zoom )
        double lat = address.getLatitude();
        double lng = address.getLongitude();

        GoToLocationZoom(lat, lng);

        // setMarker

        setMarker(locality, lat, lng);


//        String locationToReach = binding.locationToReach.getText().toString();
//        Toast.makeText(this, locationToReach, Toast.LENGTH_SHORT).show();
    }

    private void setMarker(String locality, double lat, double lng) {
//
//        if( marker != null){
//            marker.remove();
//            marker = null;
//            circle.remove();
//            circle = null;
//        }


        MarkerOptions markerOptions = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng))
                .snippet("My favourite place")
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.giftrap));
        marker = mMap.addMarker(markerOptions);
        marker.showInfoWindow();

        circle = DrawCircle(new LatLng(lat, lng));

        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {

                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }
        });

        ////////


/*****  Set on map drag listener  Begining of the Drag marker  **********/

        if (mMap != null) {


            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    Log.d("tag", "DRAG Start ");
                }

                @Override
                public void onMarkerDrag(Marker marker) {

                    Geocoder geoCoder = new Geocoder(MapsActivity.this);
                    //  geoCoder = new Geocoder(MapsActivity.this);
                    LatLng latlng = marker.getPosition();
                    double lat = latlng.latitude;
                    double lng = latlng.longitude;

                    List<android.location.Address> list = null;

                    try {
                        list = geoCoder.getFromLocation(lat, lng, 1);

                        //////
                        if( latlng != null){
                            circle.remove();
                            circle = null;
                        }
                        circle = DrawCircle(new LatLng(lat, lng));
                        //////
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    android.location.Address address = list.get(0);
                    //  address.getAddressLine()
//                    marker.setTitle(address.getLocality()+address.getSubLocality());
                    marker.setTitle(address.getLocality()+"\n"+address.getAddressLine(0));
                    marker.showInfoWindow();
                    Log.d("tag", "onMarkerDragStart..." + marker.getPosition().latitude + "..." +
                            marker.getPosition().longitude + "Marker title " + marker.getTitle());

                    Toast.makeText(MapsActivity.this, "Locality : " + address.getLocality(), Toast.LENGTH_SHORT).show();


                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    // Refractor this function to MoveMarkerInfo add this function in onMarkerDragStart and try it...
                    Geocoder geoCoder = new Geocoder(MapsActivity.this);
                    //  geoCoder = new Geocoder(MapsActivity.this);
                    LatLng latlng = marker.getPosition();
                    double lat = latlng.latitude;
                    double lng = latlng.longitude;

                    Log.d("tag", "Drag END lat " + lat + " lng " + lng);


                    List<android.location.Address> list = null;

                    try {
                        list = geoCoder.getFromLocation(lat, lng, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    android.location.Address address = list.get(0);

                    marker.setTitle(address.getLocality());
                    marker.showInfoWindow();


                }
            });


/****  setInfoWindowAdapter ******/


            // impliment methods
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {


                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {


                    View view = getLayoutInflater().inflate(R.layout.marker_info, null);


                    TextView viewLocality = (TextView) view.findViewById(R.id.textViewLocality);
                    TextView viewLat = (TextView) view.findViewById(R.id.textViewLat);
                    TextView viewLng = (TextView) view.findViewById(R.id.textViewLng);
                    TextView viewSnippet = (TextView) view.findViewById(R.id.textViewSnippet);

                    LatLng latLng = marker.getPosition();

                    // address1.get(0).getAddressLine(0)


                    viewLocality.setText(marker.getTitle());
                    viewLat.setText("Latitude : " + latLng.latitude);
                    viewLng.setText("Longitude : " + latLng.longitude);
                    viewSnippet.setText(marker.getSnippet());

                    Log.d("tag", "setInfoWindowAdapter  " + marker.getTitle() + " snippet  " + marker.getSnippet());

                    return view;
                }
            });

        }


        /////////////  map != null
        //////// End of my drag marker//////////////////////////////////////////////////////////////////
    }

    private Circle DrawCircle(LatLng latLng) {

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(1000)
                .strokeWidth(7)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(120, 255, 255, 255))
                .clickable(true);

        return mMap.addCircle(circleOptions);
    }

    private void GoToLocationZoom(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        mMap.moveCamera(cameraUpdate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;

            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                break;
            case R.id.mapTypeSatellite:

                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                break;

            case R.id.mapTypeybrid:

                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                break;

            case R.id.mapTypeTerrain:

                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                break;

            case R.id.locationManagerStart:

                EnableLocationManager();


                break;

            case R.id.locationManagerStop:


                DestroyLocationManager();

//                 Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://maps.googleapis.com/maps/api/place/textsearch/json?query=123+main+street&location=42.3675294,-71.186966&radius=10000"));
//
//                 startActivity(intent);

                break;

            case R.id.findPlace:


                // PlacePicker();

                break;


            case R.id.listPlace:
                // Search for restaurants in Bangalore
                Uri gmmIntentUri = Uri.parse("geo:12.9778791, 77.5904463?q=restaurants");//hospital");//restaurants");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;

        }


        return super.onOptionsItemSelected(item);
    }

    private void DestroyLocationManager() {


        if( locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    private void EnableLocationManager() {


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph

                travelSpeed = location.getSpeed() * 3.6;  //  or // 18 / 5;
                editText.setText(" Travel Speed : " + new DecimalFormat("#.##").format(travelSpeed) + " km/hr");


                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                // Force you to turn on the locations...
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);

    }
}

//
//    // lets have a geo fencing around the marker...
//
//
//        circle = DrawCircle(LatLng(lat, lng))
//        mMap!!.setOnCircleClickListener { circle ->
//            val strokeColor = circle.strokeColor xor 0x00ffffff
//            circle.strokeColor = strokeColor
//        }
//
//        // FIREBASE starts here
//// here we are only changing the latitude and longitude we can also change the
////        val database = Firebase.database
////        val myRef = database.getReference("UserLocation")
//
//        lateinit var  ref :DatabaseReference
//        ref=Firebase.database.reference
//
//
////        myRef.setValue("Hello, World!")
//        ////////////setOnMarkerDragListener(object : OnMarkerDragListener
//        if (mMap != null) {
//            mMap!!.setOnMarkerDragListener(object : OnMarkerDragListener {
//                override fun onMarkerDragStart(marker: Marker) {
//                    Log.d("tag", "DRAG Start ")
////
//                }
//
//                override fun onMarkerDrag(marker: Marker) {
//                    val geoCoder = Geocoder(this@MapsActivity)
//                    //  geoCoder = new Geocoder(MapsActivity.this);
//                    val latlng = marker.position
//
//                    val lat = latlng.latitude
//                    val lng = latlng.longitude
//                    ref.child("UserLocation").setValue(latlng)
//
//          // here i m retriving data...from the fire base
//
//
//
//                    var list: List<Address>? = null
//                    try {
//                        list = geoCoder.getFromLocation(lat, lng, 1)
//// taken care of geo fencing it is moving with the changing in location
//
//                        if( latlng != null){
//                            circle!!.remove()
//                            circle = null
//                        }
//                        circle = DrawCircle(LatLng(lat, lng))
//
//                        ref.addValueEventListener(object: ValueEventListener{
//
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                // This method is called once with the initial value and again
//                                // whenever data at this location is updated.
//                                val value = snapshot.getValue()
//                                Log.d(TAG, "Value is: " + value)
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                Log.w(TAG, "Failed to read value.", error.toException())
//                            }
//
//                        })
//
////                        mMap!!.addMarker(MarkerOptions().position( latlng).title("My favourite place!"))
////                lots of green color line is getting generated making it messy
//
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    val address = list!![0]
//
////                    Log.d("tag123","viewing the list data ${list[0]}")
//
//                    // marker.setTitle(address.getLocality());
//                    marker.title = address.getAddressLine(0)
//                    marker.showInfoWindow()
//                    Log.d(
//                        "tag", ("onMarkerDragStart..." + marker.position.latitude + "..." +
//                                marker.position.longitude + "Marker title " + marker.title)
//                    )
//                    Toast.makeText(
//                        this@MapsActivity,
//                        "Locality : " + address.locality,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                override fun onMarkerDragEnd(marker: Marker) {
//                    // Refractor this function to MoveMarkerInfo add this function in onMarkerDragStart and try it...
//                    val geoCoder = Geocoder(this@MapsActivity)
//                    //  geoCoder = new Geocoder(MapsActivity.this);
//                    val latlng = marker.position
//                    val lat = latlng.latitude
//                    val lng = latlng.longitude
//                    Log.d("tag", "Drag END lat $lat lng $lng")
//                    var list: List<Address>? = null
//                    try {
//                        list = geoCoder.getFromLocation(lat, lng, 1)
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    val address = list!![0]
//                    marker.title = address.locality
//                    marker.showInfoWindow()
//                }
//            })
//
//
//
// impliment methods
//            mMap!!.setInfoWindowAdapter(object : InfoWindowAdapter {
//                    override fun getInfoWindow(marker: Marker): View? {
//                    return null
//                    }
//
//                    override fun getInfoContents(marker: Marker): View? {
//                    val view: View = getLayoutInflater().inflate(R.layout.marker_info, null)
//                    val viewLocality = view.findViewById<View>(R.id.textViewLocality) as TextView
//        val viewLat = view.findViewById<View>(R.id.textViewLat) as TextView
//        val viewLng = view.findViewById<View>(R.id.textViewLng) as TextView
//        val viewSnippet = view.findViewById<View>(R.id.textViewSnippet) as TextView
//        val latLng = marker.position
//
//        // address1.get(0).getAddressLine(0)
//        viewLocality.text = marker.title
//        viewLat.text = "Latitude : " + latLng.latitude
//        viewLng.text = "Longitude : " + latLng.longitude
//        viewSnippet.text = marker.snippet
//        Log.d(
//        "tag",
//        "setInfoWindowAdapter  " + marker.title + " snippet  " + marker.snippet
//        )
//        return view
//        }
//        })
//        // setInfoWindowAdapter Ends here.....
//        }
//
//
//        /////////////  map != null
//
//
//
//        ///////////////////////////
//