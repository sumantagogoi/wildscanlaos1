/**
 * 
 */
package org.freeland.wildscanlaos;

import java.util.Locale;

import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.CountryNameTranslations;
import org.freeland.wildscanlaos.data.contract.Incidents;
import org.freeland.wildscanlaos.data.provider.WildscanDataProvider;
import org.freeland.wildscanlaos.imagecache.WildscanImageCache;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * @author Noam
 *
 */
public class EventListFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>/*, OnTouchListener*/ {
	
	public static final String KEY_IS_ON_HOME = "org.freeland.wildscanlaos.EventListFragment.IsOnHome";

	
//    private static final String[] INTERNAL_CURSOR_COLUMNS = { "_ID", "user_id", "lat", "lon", "date", "description" };
//    private static final String[] INTERNAL_CURSOR_COLUMNS = { "_ID", "avatar", "user", "date", "description" };
    
    private final static int LOADER_ID = 1;

    private final static int MAX_ENTRIES_ON_HOME = 7;
    
    private final static int MAX_DESC_LEN = 40; 

	public static final String CACHE_FILENAME = "events_cache";
	private WildscanImageCache mImageCache;
	private EventsListAdapter mAdapter;

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		mPhotoBaseUri = "file://" + getActivity().getSharedPreferences(PrefsFragment.PREFS_FILENAME,0).getString(PrefsFragment.PREFS_KEY_DATA_FOLDER, null);

		mAdapter = new EventsListAdapter(null);
		setEmptyText(getString(R.string.events_list_loading_text));
		setListAdapter(mAdapter);
		
		ListView lv = getListView();
		
//		lv.setOnTouchListener(this);
		lv.setDivider(null);
		lv.setDividerHeight(0);
		
		getLoaderManager().initLoader(LOADER_ID, null, this);
		setListShown(false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//ensure DataManager is instantiated
		WildscanDataManager.setAppContext(getActivity());
		
		mImageCache = WildscanImageCache.getInstance(getActivity());
	}
		
	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
//		Toast t = Toast.makeText(getActivity(), "Not supported in this version", Toast.LENGTH_SHORT);
//		t.setGravity(Gravity.CENTER, 0, 0);
//		t.show();
		Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
		
		intent.putExtra(EventDetailsActivity.KEY_EVENT_ID, id);
//		intent.putExtra(EventDetailsActivity.KEY_EVENT_PHOTO, e.mPhotoUrl);
		startActivity(intent);		
	}

	@Override
	public CursorLoader onCreateLoader(int id, Bundle args) {
		final String orderBy = Incidents._S_INCIDENT_DATE + " DESC";
		final String[] projection = {
				Incidents._ID,
				Incidents._S_SPECIES,
				Incidents._S_INCIDENT,
				Incidents._COUNTRY,
				Incidents._DATE,
				Incidents._PHOTO,
				Incidents._S_LOCATION_LAT,
				Incidents._S_LOCATION_LON
		};
		//final Uri queryUri = Uri.parse("content://" + WildscanDataProvider.AUTHORITY + "/" + WildscanDataManager.Incidents.TABLE_NAME);
		final Uri queryUri = WildscanDataProvider.getTableUri(Incidents.TABLE_NAME);

		return new CursorLoader(getActivity(), queryUri, projection, null, null, orderBy);
//		return new EventListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//		mData = data;
		mAdapter.swapCursor(data);
		mAdapter.notifyDataSetChanged();
		setEmptyText(getString(R.string.events_list_empty_text));
//		if (isResumed()) {
//			setListShown(true);
//		} else {
			setListShownNoAnimation(true);
//		}
			
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
		mAdapter.notifyDataSetChanged();
	}

    @Override
    public void onResume() {
        super.onResume();
        mImageCache.resumeEventPhoto();
        //((EventsListAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageCache.pauseEventPhoto();
    }
    
//    public int getCount() {
//    	return mAdapter.getCount();
//    }
//
//    public EventInfo getItem(int i) {
//    	return mAdapter.getItem(i);
//    }
    
    public EventInfo[] getEventList() {
    	int count = mAdapter.getCount();
//		if (WildscanMainActivity.class.isInstance(getActivity()))
//			count = Math.min(count, MAX_ENTRIES_ON_HOME);

		Cursor c = mAdapter.getCursor();
    	EventInfo[] ret = new EventInfo[count];
    	for (int i=0; i<count; i++) {
    		c.moveToPosition(i);
    		long id = c.getLong(c.getColumnIndex(Incidents._ID));
    		long species = c.getLong(c.getColumnIndex(Incidents._S_SPECIES));
			String desc = c.getString(c.getColumnIndex(Incidents._S_INCIDENT));
			if (desc.length() > MAX_DESC_LEN) desc = desc.substring(0, MAX_DESC_LEN-3) + "...";
			String country = c.getString(c.getColumnIndex(Incidents._COUNTRY));
			String c_code = CountryNameTranslations.getLangCode(country), cn="";
			if (!TextUtils.isEmpty(c_code)) {
				Locale l = new Locale("", c_code);
				cn = l.getDisplayCountry();
			}
			if (!TextUtils.isEmpty(cn)) {
				country = cn;
			}
			String date = c.getString(c.getColumnIndex(Incidents._DATE));
			String photo = 	c.getString(c.getColumnIndex(Incidents._PHOTO));
			double lat = c.getDouble(c.getColumnIndex(Incidents._S_LOCATION_LAT));
			double lon = c.getDouble(c.getColumnIndex(Incidents._S_LOCATION_LON));
			ret[i] = new EventInfo(id, species, desc, country, date, photo, lat, lon);
    	}
    	return ret;
    }

//    private static class EventListLoader extends CursorLoader {
//    	
////    	private Cursor mData;
//    	
//		public EventListLoader(Context context) {
//			super(context);
////			mData = null;
//		}
//
////		@Override
////		public void deliverResult(Cursor data) {
////			
////			if (isReset()) {
////				return;
////			}
//////			EventInfo[] oldData = mData;
////			mData = data;
////			if (isStarted()) {
////				super.deliverResult(data);
////			}
////		}
////
////		@Override
////		protected void onStartLoading() {
////			if (mData==null) {
////				forceLoad();
////			} else {
////				deliverResult(mData);
////			}
////		}
//
//		@Override
//		public Cursor loadInBackground() {
//			// synchronize events table...
////			WildscanDataManager.getInstance(getContext()).synchronize(true);
//			
//			final String orderBy = WildscanDataManager.Incidents._S_INCIDENT_DATE + " DESC";
//			final String[] projection = {
//					WildscanDataManager.Incidents._ID,
//					WildscanDataManager.Incidents._S_INCIDENT,
//					WildscanDataManager.Incidents._COUNTRY,
//					WildscanDataManager.Incidents._DATE,
//					WildscanDataManager.Incidents._PHOTO,
//					WildscanDataManager.Incidents._S_LOCATION_LAT,
//					WildscanDataManager.Incidents._S_LOCATION_LON
//			};
//			final Uri queryUri = Uri.parse("content://" + WildscanDataProvider.AUTHORITY + "/" + WildscanDataManager.Incidents.TABLE_NAME);
//			
//			return getContext().getContentResolver().query(queryUri, projection, null, null, orderBy);
//		}
//    	
//    }
    
	public static final String[] ADAPTER_COLUMNS = { Incidents._PHOTO, Incidents._COUNTRY, Incidents._DATE, Incidents._S_SPECIES, Incidents._S_INCIDENT };
	public static final int[] ADAPTER_VIEWS = { R.id.eventPhoto, R.id.eventCountry, R.id.eventDate, R.id.eventSpecies, R.id.eventDescription };
    
    private class EventsListAdapter extends SimpleCursorAdapter {

//		@Override
//		public Cursor swapCursor(Cursor c) {
//			// TODO Auto-generated method stub
//			return super.swapCursor(c);
//		}

		@Override
		public int getCount() {
			int count = super.getCount();
			if (WildscanMainActivity.class.isInstance(getActivity()))
				count = Math.min(count, MAX_ENTRIES_ON_HOME);
			return count;
		}

		private EventsListAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		public EventsListAdapter(Cursor c) {
			super(getActivity(), R.layout.item_event_list, c, ADAPTER_COLUMNS, ADAPTER_VIEWS, 0);
		}

		@Override
		public void setViewImage(ImageView v, String value) {
			if (TextUtils.isEmpty(value)) 
				v.setImageResource(R.drawable.missing_photo);
			else
				mImageCache.loadEventThumbnail(value, v);
		}

		@Override
		public void setViewText(TextView v, String text) {
			if (v.getId()==R.id.eventDescription) {
				String desc = text.length() > MAX_DESC_LEN ? text.substring(0, MAX_DESC_LEN-3) + "..." : text;
				super.setViewText(v, desc);
			}
			else if (v.getId()==R.id.eventSpecies) {
				String name = WildscanDataManager.getSpeciesName(Long.valueOf(text));
				if (!TextUtils.isEmpty(name))
					//name = Html.fromHtml(name);//.toString();
					v.setText(Html.fromHtml(name));
				//super.setViewText(v, name);
			} else if (v.getId()==R.id.eventCountry) {
				String c_code = CountryNameTranslations.getLangCode(text), cn="";
				if (!TextUtils.isEmpty(c_code)) {
					Locale l = new Locale("", c_code);
					cn = l.getDisplayCountry();
				}
				if (!TextUtils.isEmpty(cn)) {
					v.setText(cn);
				} else
					v.setText(text);
			} else {
				super.setViewText(v, text);
			}
		}

//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View view = super.getView(position, convertView, parent);
//			
//			Cursor c = getCursor();
//			int id = c.getInt(c.getColumnIndex(WildscanDataManager.Incidents._ID));
//			view.setId(id);
//
//			return view;
//		}
    }
}
