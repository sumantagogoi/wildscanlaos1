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
import org.freeland.wildscanlaos.util.Util;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Noam
 *
 */
public class EventDetailsActivity extends Activity {
	
	static final String KEY_EVENT_ID = "org.freeland.wildscanlaos.EventDetails.Id";
	static final String KEY_EVENT_PHOTO = "org.freeland.wildscanlaos.EventDetails.PhotoPath";

	private WildscanImageCache mImageCache = null;
	private ImageView mMainImage = null;
	private int mMainImageDim = 0;
	
	private long mEventId = 0;
	private String mImageUrl = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		
		Bundle b = getIntent().getExtras();
		if (b != null)
		{
			mEventId = b.getLong(KEY_EVENT_ID);
//			mImageUrl = b.getString(KEY_EVENT_PHOTO);
		}
		
		mImageCache = WildscanImageCache.getInstance(this);
		
		setContentView(R.layout.activity_event_details);
		mMainImage = (ImageView) findViewById(R.id.eventDetailsMainImage);

		ViewTreeObserver vto = mMainImage.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				if (Build.VERSION.SDK_INT>=16)
					mMainImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else
					mMainImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				mMainImageDim = findViewById(R.id.eventDetailsInfo).getMeasuredWidth();
				mMainImage.setMinimumHeight(mMainImageDim);
				mMainImage.setMaxHeight(mMainImageDim);
			}
		});
		
		WildscanDataManager.setAppContext(this);
		final Uri queryUri = ContentUris.withAppendedId(Uri.parse("content://" + WildscanDataProvider.AUTHORITY + "/" + Incidents.TABLE_NAME), mEventId);
		Cursor c = getContentResolver().query(queryUri, null, null, null, null);
		if (c.moveToFirst()) {
			int col_idx_desc = c.getColumnIndex(Incidents._S_INCIDENT),
				col_idx_country = c.getColumnIndex(Incidents._COUNTRY),
				col_idx_address = c.getColumnIndex(Incidents._S_LOCATION_ADDRESS),
				col_idx_date = c.getColumnIndex(Incidents._S_INCIDENT_DATE),
				col_idx_species = c.getColumnIndex(Incidents._S_SPECIES),
				col_idx_photo = c.getColumnIndex(Incidents._PHOTO),
				col_idx_amount = c.getColumnIndex(Incidents._S_NUMBER),
				col_idx_unit = c.getColumnIndex(Incidents._S_NUMBER_UNIT),
				col_idx_cond = c.getColumnIndex(Incidents._S_INCIDENT_CONDITION);
			
			mImageUrl = c.getString(col_idx_photo);
			if (TextUtils.isEmpty(mImageUrl))
				mMainImage.setImageResource(R.drawable.missing_photo);
			else
				mImageCache.loadEventPhoto(mImageUrl, mMainImage);
			
			int spId = c.getInt(col_idx_species);
			String spName = WildscanDataManager.getSpeciesName(spId);
			String amount = c.getString(col_idx_amount);
			String country = c.getString(col_idx_country);
			String c_code = CountryNameTranslations.getLangCode(country), cn="";
			if (!TextUtils.isEmpty(c_code)) {
				Locale l = new Locale("", c_code);
				cn = l.getDisplayCountry();
			}
			if (!TextUtils.isEmpty(cn)) {
				country = Util.escapeHtml(cn);
			}


			String details = 
				"<b>"+getString(R.string.event_details_offense_label)+":</b><br>" + c.getString(col_idx_desc) + "<br>" +
				"<b>"+getString(R.string.event_details_address_label)+":</b><br>" + (c.isNull(col_idx_address)?"":c.getString(col_idx_address)) + "<br>" +
				"<b>"+getString(R.string.event_details_country_label)+":</b><br>" + country + "<br>" +
				"<b>"+getString(R.string.event_details_datetime_label)+":</b><br>" + c.getString(col_idx_date) + "<br>" +
				"<b>"+getString(R.string.event_details_species_label)+":</b><br>" + (TextUtils.isEmpty(spName)?getString(R.string.event_details_unknown):spName) + "<br>" + 
				"<b>"+getString(R.string.event_details_amount_label)+":</b><br>"+ (TextUtils.isEmpty(amount)?getString(R.string.event_details_unknown):amount + " " + getResources().getStringArray(R.array.report_units)[c.getInt(col_idx_unit)]) + "<br>" +
				"<b>"+getString(R.string.event_details_condition_label)+":</b><br>"+ (c.isNull(col_idx_cond)?getString(R.string.event_details_unknown):getResources().getStringArray(R.array.report_condition)[c.getInt(col_idx_cond)]);
			TextView tv_details = (TextView) findViewById(R.id.eventDetailsInfo);
			tv_details.setText(Html.fromHtml(details));
			
//			tv_details.setMovementMethod(new ScrollingMovementMethod());
		}
		c.close();
	}
}
