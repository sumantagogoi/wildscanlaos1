package org.freeland.wildscan;

import android.app.Activity;
import android.content.Intent;
//import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationPickerActivity extends Activity implements OnMapReadyCallback {
	public static final String KEY_START_LAT = "org.freeland.wildscan.LocationPicker.startLat";
	public static final String KEY_START_LON = "org.freeland.wildscan.LocationPicker.startLon";
	public static final String KEY_TARGET_LAT = "org.freeland.wildscan.LocationPicker.markerLat";
	public static final String KEY_TARGET_LON = "org.freeland.wildscan.LocationPicker.markerLon";

	MarkerOptions mTargetMarkerOps;
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
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //map.setMyLocationEnabled(true);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LatLng myLoc = null, targetLoc = null;
		try {
			lat = getIntent().getExtras().getDouble(KEY_START_LAT, Double.NaN);
			lon = getIntent().getExtras().getDouble(KEY_START_LON, Double.NaN);
			if (!Double.isNaN(lat) && !Double.isNaN(lon))
				myLoc = new LatLng(lat, lon);
		} catch (Exception e) {
		}
		try {
			lat = getIntent().getExtras().getDouble(KEY_TARGET_LAT, Double.NaN);
			lon = getIntent().getExtras().getDouble(KEY_TARGET_LON, Double.NaN);
			if (!Double.isNaN(lat) && !Double.isNaN(lon))
				targetLoc = new LatLng(lat, lon);
		} catch (Exception e) {
		}

		setContentView(R.layout.activity_location_picker);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.locationPickerMap);
        mapFragment.getMapAsync(this);

		if (mMap!=null) {
			//mMap.setMyLocationEnabled(true);
			if (targetLoc != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, zoom));
			else if (myLoc != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, zoom));
			mTargetMarkerOps = new MarkerOptions().title(getText(R.string.location_picker_map_target_title).toString());
			if (targetLoc != null) {
				mTargetMarkerOps.position(targetLoc);
				mTargetMarker = mMap.addMarker(mTargetMarkerOps);
			}
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
