/**
 * 
 */
package org.freeland.wildscanlaos;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.view.PagerAdapter;
import androidx.core.view.ViewPager;
import androidx.core.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.CountryNameTranslations;
import org.freeland.wildscanlaos.data.contract.Species;
import org.freeland.wildscanlaos.data.contract.SpeciesImages;
import org.freeland.wildscanlaos.data.contract.SpeciesTranslations;
import org.freeland.wildscanlaos.data.provider.WildscanDataProvider;
import org.freeland.wildscanlaos.imagecache.Utils;
import org.freeland.wildscanlaos.imagecache.WildscanImageCache;
import org.freeland.wildscanlaos.util.Util;
import org.freeland.wildscanlaos.widget.FixedAspectImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import static java.io.File.separator;
import static org.freeland.wildscanlaos.imagecache.ImageResizer.calculateInSampleSize;

/**
 * @author Noam
 *
 */
public class SpeciesDetailsActivity extends Activity 
		implements OnClickListener {
	
	static final String KEY_SPECIES_ID = "org.freeland.wildscanlaos.SpeciesDetails.Id";
	static final String KEY_NEED_RESULT = "org.freeland.wildscanlaos.SpeciesDetails.NeedResult";

	private WildscanImageCache mImageCache;
	
	private LinearLayout mImageThumbnailsLayout = null;

//	private ImageView mMainImage = null;
//	private TextView mMainImageCredit = null;
	private ViewPager mImagePager = null;
	private int mMainImageDim = 0;
	private int mThumbnailAutoScrollStep = 0;
	
	private String mCredits[] = null;
	private int mLicenses[] = null;
	private Uri mUris[] = null;
	
	private long mSpeciesId = 0;
	private boolean mFromReport = false;
	
	private int mIsFav = 0;
	
	private static final int NO_CURR = -1;
	private int mCurrMain = NO_CURR;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		
		Bundle b = getIntent().getExtras();
		if (b != null)
		{
			mSpeciesId = b.getLong(KEY_SPECIES_ID);
			mFromReport = b.getBoolean(KEY_NEED_RESULT, false);
		}
		
		setContentView(R.layout.activity_species_details);
		mImageThumbnailsLayout = (LinearLayout) findViewById(R.id.speciesDetailsImageThumbnails);
		mImagePager = (ViewPager) findViewById(R.id.speciesDetailsMain);

		mImageCache = WildscanImageCache.getInstance(this);
		
		ViewTreeObserver vto = mImagePager.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				if (Utils.hasJellyBean())
					mImagePager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else
					mImagePager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				mMainImageDim = mImagePager.getMeasuredWidth();
				int scrollWidth = mImageThumbnailsLayout.getMeasuredWidth();
				int n = mImageThumbnailsLayout.getChildCount()-1;
				if (scrollWidth>mMainImageDim && n>0)
					mThumbnailAutoScrollStep = Math.max(0, (scrollWidth-mMainImageDim)/n);
				setupPager();
			}
		});
		
		String[] columns = { SpeciesImages._S_PATH_IMAGE,	
				SpeciesImages._S_DEFAULT_ORDER,
				SpeciesImages._S_CREDIT,
				SpeciesImages._S_LICENSE };
		String selection = SpeciesImages._S_SPECIES_ID + "=?";
		String[] selectionArgs = { Long.toString(mSpeciesId) };
		String order = SpeciesImages._S_DEFAULT_ORDER + " ASC";

		
		final Uri imagesQueryUri = WildscanDataProvider.getTableUri(SpeciesImages.TABLE_NAME);
		Cursor c = getContentResolver().query(imagesQueryUri, columns, selection, selectionArgs, order);
		int thumb_dim = getResources().getDimensionPixelSize(R.dimen.species_details_thumbnail_height);//  mImageThumbnailsLayout.getLayoutParams().height;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(thumb_dim, thumb_dim);

		int n = c.getCount();

		if (c.moveToFirst()) {
			mCredits = new String[n];
			mLicenses = new int[n];
			mUris = new Uri[n];
			
			int col_idx_path = c.getColumnIndex(SpeciesImages._S_PATH_IMAGE);
			String im_path_base = getSharedPreferences(PrefsFragment.PREFS_FILENAME,0).getString(PrefsFragment.PREFS_KEY_DATA_FOLDER, null);
			n=0;
			while (true) {
				String path = c.getString(col_idx_path);
				File im_file = new File(im_path_base + path);
				mUris[n] = Uri.fromFile(im_file);

				int col_idx_credit = c.getColumnIndex(SpeciesImages._S_CREDIT),
						col_idx_lic = c.getColumnIndex(SpeciesImages._S_LICENSE);
				mLicenses[n] = c.getInt(col_idx_lic);
				mCredits[n] = c.getString(col_idx_credit);
				if (mCredits[n]!=null)
					mCredits[n] = "\u00A9 " + mCredits[n];
				ImageView v = new ImageView(this);
				v.setId(n);
				v.setLayoutParams(params);
				v.setScaleType(ScaleType.FIT_CENTER);
				v.setImageResource(R.drawable.empty_species);
				v.setOnClickListener(this);
				mImageThumbnailsLayout.addView(v, n, params);
				
				if (!c.moveToNext())
					break;
				n++;
			}
		}
		c.close();
		
		final Uri speciesQueryUri = WildscanDataProvider.getItemUri(Species.TABLE_NAME, mSpeciesId);
		selection = Util.addLangToSelection(getApplicationContext(), null, SpeciesTranslations._S_LANGUAGE);
		c = getContentResolver().query(speciesQueryUri, null, selection, null, null);
		if (c.moveToFirst()) {
			int col_idx_name = c.getColumnIndex(Species._S_COMMON_NAME),
				col_idx_sci_name = c.getColumnIndex(Species._S_SCIENTIFIC_NAME),
				col_idx_aka = c.getColumnIndex(Species._S_KNOWN_AS),
				col_idx_region = c.getColumnIndex(Species._S_EXTANT_COUNTRIES),
				col_idx_cites = c.getColumnIndex(Species._S_CITES),
				col_idx_status = c.getColumnIndex(Species._S_STATUS),
				col_idx_habitat = c.getColumnIndex(Species._S_HABITAT),
				col_idx_notes = c.getColumnIndex(Species._S_NOTES),
				col_idx_id_cues = c.getColumnIndex(Species._S_BASIC_ID_CLUES),
				col_idx_similar = c.getColumnIndex(Species._S_SIMILAR_ANIMALS),
				col_idx_size = c.getColumnIndex(Species._S_AVERAGE_SIZE_WEIGHT),
				col_idx_responder = c.getColumnIndex(Species._S_FIRST_RESPONDER),
				col_idx_enforcement = c.getColumnIndex(Species._S_ENFORCEMENT_ADVICE),
				col_idx_consumer = c.getColumnIndex(Species._S_CONSUMER_ADVICE),
				col_idx_trade = c.getColumnIndex(Species._S_TRADED_AS),
				col_idx_traffic = c.getColumnIndex(Species._S_COMMON_TRAFFICKING),
				col_idx_warn = c.getColumnIndex(Species._S_WARNINGS),
				col_idx_disease_risk = c.getColumnIndex(Species._S_DISEASE_RISK_LEVEL),
				col_idx_diseases = c.getColumnIndex(Species._S_DISEASE_NAME),
				col_idx_fav = c.getColumnIndex(Species._FAV);
			
			final ImageButton btnFav = (ImageButton)findViewById(R.id.speciesDetailsButtonFav);
			mIsFav = c.getInt(col_idx_fav);
			btnFav.setImageLevel(mIsFav);

			String sci_name = c.getString(col_idx_sci_name);
			if (sci_name.isEmpty())
				sci_name = null;
			String status = "",
					cites = "";
			Integer i_s = SpeciesTranslations.IUCN_ABBREVIATION_STRINGS.get(c.getString(col_idx_status)),
					i_c = SpeciesTranslations.CITES_APPENDIX_STRINGS.get(c.getString(col_idx_cites));
			if (i_s!=null)
				status = getResources().getString(i_s);
			if (i_c!=null)
				cites = getResources().getString(i_c);
			String warn = c.getString(col_idx_warn), 
					dis = c.getString(col_idx_disease_risk),
					disease = TextUtils.isEmpty(dis) ? "" : getResources().getString(SpeciesTranslations.DISEASE_LEVEL_STRINGS.get(dis));
			String warnings = "";
			if (!TextUtils.isEmpty(warn)) {
				int i=0;
				for (String w : warn.split(",")) {
					String ww = "";
					Integer i_w = SpeciesTranslations.WARNING_STRINGS.get(w.trim());
					if (i_w!=null) {
						ww = getResources().getString(i_w);
					}
					if (TextUtils.isEmpty(ww))
						continue;
					if (i>0)
						warnings += ",";
					warnings += ww;
					i++;
				}
			}
			if (!TextUtils.isEmpty(disease)) {
				if (!TextUtils.isEmpty(warnings))
					warnings += ", ";
				warnings += disease + " " + getString(R.string.species_details_label_warning_disease_risk);
			}
			String distrib_orig = c.getString(col_idx_region);
			String distrib = new String();
			if (!TextUtils.isEmpty(distrib_orig)) {
				for (String country : distrib_orig.split(",")) {
					String c_code = CountryNameTranslations.getLangCode(country), cn="";
					if (!distrib.isEmpty())
						distrib += ", ";
					if (!TextUtils.isEmpty(c_code)) {
						Locale l = new Locale("", c_code);
						cn = l.getDisplayCountry();
					}
					if (!TextUtils.isEmpty(cn)) {
						distrib += Util.escapeHtml(cn);
					} else {
						distrib += Util.escapeHtml(country);
					}
				}
			}

			String excerpt = 
				"<b>" + c.getString(col_idx_name) + "</b>" + (sci_name==null?"":" <i>(" + sci_name + ")</i>") + "<br>" +
				"<b>" + getString(R.string.species_details_label_cites) + ":</b> " + cites + "<br>" +
				"<b>" + getString(R.string.species_details_label_status) + ":</b> " + status + "<br>" +
				"<b>" + getString(R.string.species_details_label_warning) + ":</b> " + warnings;
			String details = 
				"<b><u>" + getString(R.string.species_details_label_id_clues) + ":</u></b><br>" + (c.isNull(col_idx_id_cues)?"":c.getString(col_idx_id_cues) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_similar) + ":</u></b><br>"+ (c.isNull(col_idx_similar)?"":c.getString(col_idx_similar) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_enforcement) + ":</u></b><br>" + (c.isNull(col_idx_enforcement)?"":c.getString(col_idx_enforcement) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_consumer) + ":</u></b><br>" + (c.isNull(col_idx_consumer)?"":c.getString(col_idx_consumer) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_responder) + ":</u></b><br>" + (c.isNull(col_idx_responder)?"":c.getString(col_idx_responder) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_traded_as) + ":</u></b><br>" + (c.isNull(col_idx_trade)?"":c.getString(col_idx_trade) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_common_methods) + ":</u></b><br>" + (c.isNull(col_idx_traffic)?"":c.getString(col_idx_traffic) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_aka) + ":</u></b><br>" + (c.isNull(col_idx_aka)?"":c.getString(col_idx_aka) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_distribution) + ":</u></b><br>" + (TextUtils.isEmpty(distrib)?"": distrib + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_size) + ":</u></b><br>" + (c.isNull(col_idx_size)?"":c.getString(col_idx_size) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_habitat) + ":</u></b><br>" + (c.isNull(col_idx_habitat)?"":c.getString(col_idx_habitat) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_diseases) + ":</u></b><br>" + (c.isNull(col_idx_diseases)?"":c.getString(col_idx_diseases) + "<br>") + "<br>" +
				"<b><u>" + getString(R.string.species_details_label_notes) + ":</u></b><br>" + (c.isNull(col_idx_notes)?"":c.getString(col_idx_notes) + "<br>");
			TextView tv_excerpt = (TextView) findViewById(R.id.speciesDetailsExcerpt),
					tv_details = (TextView) findViewById(R.id.speciesDetailsSpeciesInfo);
			tv_excerpt.setText(Html.fromHtml(excerpt));
			tv_details.setText(Html.fromHtml(details));
			tv_details.setMovementMethod(new ScrollingMovementMethod());
		}
		c.close();
		
		setupTumbnails();
	}

	public void showLicense(View v) {
		String license = getResources().getStringArray(R.array.species_photo_license)[mLicenses[mCurrMain]];
		Toast t = Toast.makeText(this, getString(R.string.species_details_photo_license, license), Toast.LENGTH_LONG);
		t.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 320);
		t.show();
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setMainImage(int thumbIdx) {
		if (mCurrMain==thumbIdx)
			return;

		mImagePager.setCurrentItem(thumbIdx, true);

		ImageView iv = (ImageView)mImageThumbnailsLayout.getChildAt(thumbIdx);
		if(iv == null){
			mImagePager.setVisibility(View.GONE);
			return;
		}
		iv.setBackgroundResource(R.drawable.frame_current_photo);
		iv.setPadding(3,3,3,3);
		//((LinearLayout.LayoutParams)iv.getLayoutParams()).setMargins(2,2,2,2);
		if (mCurrMain!=NO_CURR) {
			iv = (ImageView)mImageThumbnailsLayout.getChildAt(mCurrMain);
			if (Utils.hasJellyBean())
				iv.setBackground(null);
			else
				iv.setBackgroundDrawable(null);
			iv.setPadding(1,1,1,1);
			//((LinearLayout.LayoutParams)iv.getLayoutParams()).setMargins(0,0,0,0);
		}
		if (mThumbnailAutoScrollStep>0) {
			HorizontalScrollView sv = ((HorizontalScrollView)mImageThumbnailsLayout.getParent()); 
			sv.smoothScrollTo(mThumbnailAutoScrollStep*thumbIdx, sv.getScrollY());
		}
		mCurrMain = thumbIdx;
	}
	
	@Override
	public void onClick(View v) {
		setMainImage(v.getId());
	}
	
	public void onClickButtonFav(View v) {
		final Uri speciesQueryUri = WildscanDataProvider.getItemUri(Species.TABLE_NAME, mSpeciesId);
		mIsFav = 1 - mIsFav;
		((ImageButton)findViewById(R.id.speciesDetailsButtonFav)).setImageLevel(mIsFav);
		ContentValues values = new ContentValues(1);
		values.put(Species._FAV, mIsFav);
		int n = getContentResolver().update(speciesQueryUri, values, null, null);
		if (n!=1) {
			Toast.makeText(SpeciesDetailsActivity.this, R.string.species_details_failed_set_fav, Toast.LENGTH_SHORT).show();
		}
	}

	public void onClickButtonReport(View v) {
		if (mFromReport) {
			Intent res = new Intent();
			res.putExtra(ReportWizardActivity.KEY_SPECIES_ID, mSpeciesId);
			SpeciesDetailsActivity.this.setResult(Activity.RESULT_OK, res);
			finish();			
		} else {
			// start new report with this species
			Intent intent = new Intent(this, ReportWizardActivity.class);
			intent.putExtra(ReportWizardActivity.KEY_SPECIES_ID, mSpeciesId);
			startActivity(intent);
		}
	}
	
	private void setupPager() {
		mImagePager.setAdapter(new MainImagePagerAdapter());
//		mImagePager.setMinimumHeight(mMainImageDim);
		mImagePager.requestLayout();
		mImagePager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int page) {
				setMainImage(page);
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});

		setMainImage(0);
	}
	
	private void setupTumbnails() {
		for (int i=0; i<mImageThumbnailsLayout.getChildCount(); i++) {
			new CreateThumbnailTask().execute(i);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        mImageCache.resumeSpeciesMainPhoto();
	}

	@Override
	protected void onPause() {
        super.onPause();
        mImageCache.pauseSpeciesMainPhoto();
	}
	
	private class MainImagePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mUris == null? 0 : mUris.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view==obj;
		}

		@SuppressLint("InflateParams")
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LayoutInflater inf = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			FrameLayout frame = (FrameLayout) inf.inflate(R.layout.layout_species_details_image_frame, null);
			
			FixedAspectImageView image = (FixedAspectImageView)frame.findViewById(R.id.image);
			String path = mUris[position].toString();
			String decodedPath;
			try {
				decodedPath = URLDecoder.decode(path,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				decodedPath= path;
			}
			mImageCache.loadSpeciesMainPhoto(decodedPath, image);

            TextView credit = (TextView)frame.findViewById(R.id.credit);
            credit.setText(mCredits[position]);
            
            frame.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showLicense(v);
				}
			});
            
			container.addView(frame);
			return frame;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}
		
	}
	
	private static String TAG = "SpeciesDetails";
	private static String localStorageRoot = null;	
	private class CreateThumbnailTask extends AsyncTask<Integer, Void, Void> {
		
		public CreateThumbnailTask() {
			super();
			if (localStorageRoot==null) {
				localStorageRoot = getSharedPreferences(PrefsFragment.PREFS_FILENAME,0).getString(PrefsFragment.PREFS_KEY_DATA_FOLDER, null);
				if (localStorageRoot==null)
					localStorageRoot = getFilesDir().getAbsolutePath();
			
				localStorageRoot += separator;
			}
		}

		ImageView target;
		Bitmap thumb;

		@Override
		protected Void doInBackground(Integer... params) {
			int idx = params[0];
			target = (ImageView) mImageThumbnailsLayout.getChildAt(idx);
			String path = mUris[idx].getEncodedPath();
			String thumbPath = path.replace("/uploads/", "/thumbs/");
			File thumbFile = new File(thumbPath);
			if (thumbFile.exists()) {
				thumb = BitmapFactory.decodeFile(thumbPath);
			}
			else {
				createThumbnailBitmap(path);
			if (thumb!=null) {
					FileOutputStream fos;
				try {
						thumbFile.getParentFile().mkdirs();
					fos = new FileOutputStream(thumbFile);
					thumb.compress(CompressFormat.PNG, 100, fos);
					} catch (FileNotFoundException e) {
					}
				}
			}
			return null;
		}


		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (thumb!=null) {
					target.post(new Runnable() {
						@Override
						public void run() {
							target.setImageBitmap(thumb);
						}
					}); 
			}
		}


		private void createThumbnailBitmap(String origPath) {
			boolean inObb = true;
			int h = getResources().getDimensionPixelSize(R.dimen.species_details_thumbnail_height)*2, w = h;
			WildscanDataManager mng = WildscanDataManager.getInstance(getApplicationContext());

	       	try {
	    		String assetPath = origPath.substring(localStorageRoot.length());
    			File origFile = new File(origPath);
	            final BitmapFactory.Options options = new BitmapFactory.Options();
	            options.inSampleSize = 1;
	    		InputStream is = mng.expansionFileGetInputStream(assetPath);
	    		if (is==null) {
	    			inObb = false;
	    			if (origFile.exists())
	    				is = new FileInputStream(origFile);
	    		}

	    		if (is!=null) {
	    	        options.inJustDecodeBounds = true;
	    	        BitmapFactory.decodeStream(is, null, options);
	    	        // Calculate inSampleSize
	    	        options.inSampleSize = calculateInSampleSize(options, w, h);
	    	        is.close();
	    	        
	    	        // reopen stream..
	    			is = inObb ? mng.expansionFileGetInputStream(assetPath) : new FileInputStream(origFile);
		            // Decode bitmap with inSampleSize set
		            options.inJustDecodeBounds = false;
		            thumb = BitmapFactory.decodeStream(is, null, options);
	    		}
			} catch (IOException e) {
				Log.e(TAG, "createThumbnail - " + e);
			}
//	    	if (thumb==null)
//	    		thumb = BitmapFactory.decodeFile(origPath);
		}
		
		
	}
}
