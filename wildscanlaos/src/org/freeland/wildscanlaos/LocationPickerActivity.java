package org.freeland.wildscanlaos;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class LocationPickerActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
	public static final String KEY_START_LAT = "org.freeland.wildscanlaos.LocationPicker.startLat";
	public static final String KEY_START_LON = "org.freeland.wildscanlaos.LocationPicker.startLon";
	public static final String KEY_TARGET_LAT = "org.freeland.wildscanlaos.LocationPicker.markerLat";
	public static final String KEY_TARGET_LON = "org.freeland.wildscanlaos.LocationPicker.markerLon";
	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	FusedLocationProviderClient fusedLocationClient;
	GoogleApiClient googleApiClient;
	LocationRequest mLocationRequest;
	public static final int LOCATION_REQUEST = 101;

	MarkerOptions mTargetMarkerOps;
	Location mLastLocation;
	Marker mTargetMarker = null;
	GoogleMap mMap;
    MapFragment mapFragment;
    double lat,lon;
    float zoom = 10.0f;

//	private LatLng fromLocation(Location l) {
//		return new LatLng(l.getLatitude(), l.getLongitude());
//	}

    @Override
    public void onMapReady(GoogleMap map) {
        // DO WHATEVER YOU WANT WITH GOOGLEMAP
		mMap = map;
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(120000); // two minute interval
		mLocationRequest.setFastestInterval(120000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		else {
			fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
			mMap.setMyLocationEnabled(true);
		}
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		//mMap.setMyLocationEnabled(true);
		mMap.setTrafficEnabled(false);
		mMap.setIndoorEnabled(true);
		mMap.setBuildingsEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);


		LatLng myLoc = null, targetLoc = null;

		if (mMap!=null) {
			LatLng here = new LatLng(lat,lon);
			//mMap.setMyLocationEnabled(true);
			/*
			if (targetLoc != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, zoom));
			else if (myLoc != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, zoom));

			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, zoom));
			mTargetMarkerOps = new MarkerOptions().title(getText(R.string.location_picker_map_target_title).toString());

			if (targetLoc != null) {
				mTargetMarkerOps.position(targetLoc);
				mTargetMarker = mMap.addMarker(mTargetMarkerOps);
			}

			mTargetMarkerOps.position(here);
			mTargetMarker = mMap.addMarker(mTargetMarkerOps);
			*/
			mMap.setOnMapClickListener(new OnMapClickListener() {
				@Override
				public void onMapClick(LatLng position) {
					mTargetMarkerOps.position(position);
					if (mTargetMarker==null)
						mTargetMarker = mMap.addMarker(mTargetMarkerOps);
					else
						mTargetMarker.setPosition(position);
				}
			});
		}

    }

	LocationCallback mLocationCallback = new LocationCallback() {
		@Override
		public void onLocationResult(LocationResult locationResult) {
			List<Location> locationList = locationResult.getLocations();
			if (locationList.size() > 0) {
				//The last location in the list is the newest
				Location location = locationList.get(locationList.size() - 1);
				Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
				mLastLocation = location;
				if (mTargetMarker != null) {
					mTargetMarker.remove();
				}

				//Place current location marker
				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.position(latLng);
				markerOptions.title("Current Position");
				markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
				mTargetMarker = mMap.addMarker(markerOptions);

				//move map camera
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		//Building a instance of Google Api Client
		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addOnConnectionFailedListener(this)
				.addConnectionCallbacks(this)
				.build();


		setContentView(R.layout.activity_location_picker);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.locationPickerMap);
        mapFragment.getMapAsync(this);

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
	}

	public void onStart() {
		super.onStart();
		// Initiating the GoogleApiClient Connection when the activity is visible
		googleApiClient.connect();
	}
	public void onStop() {
		super.onStop();
		//Disconnecting the GoogleApiClient when the activity goes invisible
		googleApiClient.disconnect();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
	}
	
	public void onClickBtnSet(View v) {
		if (mTargetMarker==null)
			return;
		Intent i = new Intent();
		i.putExtra(KEY_TARGET_LAT, mTargetMarker.getPosition().latitude);
		i.putExtra(KEY_TARGET_LON, mTargetMarker.getPosition().longitude);
		
		if (getParent() == null) {
		    setResult(Activity.RESULT_OK, i);
		} else {
		    getParent().setResult(Activity.RESULT_OK, i);
		}
		finish();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Toast.makeText(this, "Connection was suspended", Toast.LENGTH_SHORT);
	}
	//Callback invoked if the GoogleApiClient connection fails
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT);
	}
	
	public void onClickBtnCancel(View v) {
		Intent i = new Intent();
		if (getParent() == null) {
		    setResult(Activity.RESULT_CANCELED, i);
		} else {
		    getParent().setResult(Activity.RESULT_CANCELED, i);
		}
		finish();
	}

}
