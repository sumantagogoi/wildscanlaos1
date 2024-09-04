package org.freeland.wildscanlaos;

import java.util.HashMap;
import java.util.Map;

import org.freeland.wildscanlaos.imagecache.WildscanImageCache;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
//import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class EventMapActivity extends Activity implements OnMapReadyCallback {
	public static final String KEY_EVENTS_LIST = "org.freeland.wildscanlaos.EventsMap.eventsList";

	Parcelable[] mEvents;
	Marker[] mMarkers = null;
	LatLngBounds mBounds;
	GoogleMap mMap;
	
	Map<String, Integer> mMarkerIdToEventIdx;
	Marker mSelectedMarker = null;
	
	View mDetailsLayout = null;
	
	static final float DEFAULT_ALPHA = 0.75f;
	static final float HIGHLIGHT_ALPHA = 1.0f;

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
	public void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		
		mEvents = getIntent().getExtras().getParcelableArray(KEY_EVENTS_LIST);
		mMarkerIdToEventIdx = new HashMap<String, Integer>(mEvents.length);

		setContentView(R.layout.activity_events_map);

		MapFragment frag = (MapFragment) getFragmentManager().findFragmentById(R.id.eventsMapFragment); 

		frag.getMapAsync(this);
		if (mMap!=null) {
			//mMap.setMyLocationEnabled(true);
			mMap.getUiSettings().setZoomControlsEnabled(false);
			mMap.getUiSettings().setMyLocationButtonEnabled(true);
			
			MarkerOptions markerOps = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

			mMarkers = new Marker[mEvents.length];
			double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
			double maxLat = Double.MIN_VALUE, maxLon = Double.MIN_VALUE;
			
			for (int i=0; i<mEvents.length; i++) {
				EventInfo event = (EventInfo)mEvents[i];
				
				if (event.mIncidentLat > maxLat) maxLat = event.mIncidentLat;
				else if (event.mIncidentLat < minLat) minLat = event.mIncidentLat;
				if (event.mIncidentLon > maxLon) maxLon = event.mIncidentLon;
				else if (event.mIncidentLon < minLon) minLon = event.mIncidentLon;
				
				float alpha = mSelectedMarker!=null && Integer.valueOf(i).equals(mMarkerIdToEventIdx.get(mSelectedMarker.getId())) ? HIGHLIGHT_ALPHA : DEFAULT_ALPHA;
				LatLng loc = new LatLng(event.mIncidentLat, event.mIncidentLon);
				String snippet =  TextUtils.isEmpty(event.mIncidentText) ? "Empty description.." : event.mIncidentText;
				int len = Math.min(10, snippet.length());
				mMarkers[i] = mMap.addMarker(markerOps.title(snippet.substring(0, len))
														.snippet(snippet)
														.position(loc)
														.alpha(alpha));
				mMarkerIdToEventIdx.put(mMarkers[i].getId(), i);
			}
			
			mBounds = new LatLngBounds(new LatLng(minLat,minLon), new LatLng(maxLat, maxLon));
			
	        // Pan to see all markers in view.
	        // Cannot zoom to bounds until the map has a size.
	        final View mapView = frag.getView();
	        if (mapView.getViewTreeObserver().isAlive()) {
	            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	                @SuppressWarnings("deprecation") // We use the new method when supported
	                @SuppressLint("NewApi") // We check which build version we are using.
	                @Override
	                public void onGlobalLayout() {
	                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
	                      mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	                    } else {
	                      mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	                    }
	                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 100));
	                }
	            });
	        }
	        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				
				@Override
				public boolean onMarkerClick(Marker marker) {
					setSelectedMarker(marker);
					return false;
				}
			});
//			
//			mMap.setOnMapClickListener(new OnMapClickListener() {
//	
//				@Override
//				public void onMapClick(LatLng position) {
//					markerOps.position(position);
//					if (mTargetMarker==null)
//						mTargetMarker = mMap.addMarker(markerOps);
//					else
//						mTargetMarker.setPosition(position);
//				}
//			});
	        
		}
	}

	public void setSelectedMarker(Marker marker) {
		// update marker alpha
		if (mSelectedMarker!=null) {
			// remove selected marker highlight
			mSelectedMarker.setAlpha(DEFAULT_ALPHA);
		}
		mSelectedMarker = marker;
		marker.setAlpha(HIGHLIGHT_ALPHA);
		int idx=mMarkerIdToEventIdx.get(marker.getId());
		EventInfo ei = (EventInfo)mEvents[idx];
		
		// update bottom event-details layout
		if (mDetailsLayout==null) {
			ViewStub stub = (ViewStub) findViewById(R.id.stub_currentEvent);
			stub.setLayoutResource(R.layout.item_event_list);
			mDetailsLayout = stub.inflate();
			mDetailsLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(EventMapActivity.this, EventDetailsActivity.class);
					
					intent.putExtra(EventDetailsActivity.KEY_EVENT_ID, (Long)v.getTag());
//					intent.putExtra(EventDetailsActivity.KEY_EVENT_PHOTO, e.mPhotoUrl);
					startActivity(intent);		
				}
			});
		}
		WildscanImageCache cache = WildscanImageCache.getInstance(EventMapActivity.this);
		((TextView)mDetailsLayout.findViewById(R.id.eventCountry)).setText(ei.mIncidentLocation);
		((TextView)mDetailsLayout.findViewById(R.id.eventSpecies)).setText(ei.mSpeciesName==null?null:Html.fromHtml(ei.mSpeciesName));
		((TextView)mDetailsLayout.findViewById(R.id.eventDate)).setText(ei.mIncidentDate);
		((TextView)mDetailsLayout.findViewById(R.id.eventDescription)).setText(ei.mIncidentText);
		cache.loadEventPhoto(ei.mPhotoUrl, (ImageView)mDetailsLayout.findViewById(R.id.eventPhoto));
		mDetailsLayout.setTag(ei.mEventId);
	}
	
	public void onClickHeaderBtnShowList(View v) {
    	Intent intent = new Intent(this, EventListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		finish();
    }
	
    public void onClickHeaderBtnNext(View v) {
    	int next = mSelectedMarker==null ? 0 : (mMarkerIdToEventIdx.get(mSelectedMarker.getId())+1)%mMarkers.length;
    	setSelectedMarker(mMarkers[next]);
    	
    	mMarkers[next].showInfoWindow();
    	mMap.animateCamera(CameraUpdateFactory.newLatLng(mMarkers[next].getPosition()), 250, null);
    }

	@Override
	protected void onResume() {
		super.onResume();
		WildscanImageCache.getInstance(EventMapActivity.this).resumeEventPhoto();
	}

	@Override
	protected void onPause() {
		super.onPause();
		WildscanImageCache.getInstance(EventMapActivity.this).pauseEventPhoto();
	}
}
