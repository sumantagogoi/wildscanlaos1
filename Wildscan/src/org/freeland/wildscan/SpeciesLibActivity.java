/**
 *
 */
package org.freeland.wildscan;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.freeland.wildscan.data.WildscanDataManager;
import org.freeland.wildscan.data.contract.Incidents;
import org.freeland.wildscan.data.contract.Species;
import org.freeland.wildscan.data.contract.SpeciesTranslations;
import org.freeland.wildscan.data.provider.WildscanDataProvider;
import org.freeland.wildscan.imagecache.WildscanImageCache;
import org.freeland.wildscan.util.AppPreferences;
import org.freeland.wildscan.util.Util;
import org.freeland.wildscan.widget.SearchAutoCompleteView;

import java.util.ArrayList;


public class SpeciesLibActivity extends Activity
        implements OnItemClickListener {
    // bundle keys
    static final String KEY_TYPE_FILTER = "org.freeland.wildscan.SpeciesLib.TypeFilter";
    static final String KEY_PROD_ANI_FILTER = "org.freeland.wildscan.SpeciesLib.ProdAniFilter";
    static final String KEY_COVER_FILTER = "org.freeland.wildscan.SpeciesLib.CoveringFilter";
    static final String KEY_NUMLEGS_FILTER = "org.freeland.wildscan.SpeciesLib.NumLegsFilter";
    static final String KEY_COLOR_FILTER = "org.freeland.wildscan.SpeciesLib.ColorFilter";
    static final String KEY_SIZE_FILTER = "org.freeland.wildscan.SpeciesLib.SizeFilter";
    static final String KEY_SPEC_FILTER = "org.freeland.wildscan.SpeciesLib.SpecialFeaturesFilter";
    static final String KEY_PURPOSE_FILTER = "org.freeland.wildscan.SpeciesLib.PurposeFilter";
    static final String KEY_MATERIAL_FILTER = "org.freeland.wildscan.SpeciesLib.MaterialFilter";
    //	private static final Uri mImagesQueryUri = Uri.parse("content://" + WildscanDataProvider
// .AUTHORITY + "/" + WildscanDataManager.SpeciesImages.TABLE_NAME);
    static final String KEY_PART_FILTER = "org.freeland.wildscan.SpeciesLib.PartFilter";
    static final String KEY_SHOW_FAVS = "org.freeland.wildscan.SpeciesLib.ShowFavorites";
    static final String KEY_ISLIST = "org.freeland.wildscan.SpeciesLib.IsList";
    static final String KEY_LIST_POSITION = "org.freeland.wildscan.SpeciesLib.ListPosition";
    static final String KEY_LIST_OFFSET = "org.freeland.wildscan.SpeciesLib.ListOffset";
    static final String KEY_NEED_RESULT = "org.freeland.wildscan.SpeciesLib.NeedResult";
    static final String KEY_RESULT_SPECIES_ID = "org.freeland.wildscan.SpeciesLib.SpeciesId";
    static final String KEY_RESULT_SPECIES_NAME = "org.freeland.wildscan.SpeciesLib.SpeciesName";
    static final float DISTANCE_FOR_LOCATION_FILTER_IN_METERS = 100000.0f;
    static final int LOADER_ID = 3;
    private static final Uri sBaseQueryUri = Uri
            .parse("content://" + WildscanDataProvider.AUTHORITY + "/" + Species.TABLE_NAME);
    private static final String[] COLUMNS = {Species._MAIN_PHOTO, Species._S_COMMON_NAME, Species
            ._FAV};
    private static final int[] VIEWS = {R.id.speciesListThumbnail, R.id.speciesListName, R.id
            .speciesListBtnFav};
    LinearLayout mSearchPopup = null;
    //	private MatrixCursor mCursor;
    private Cursor mCursor;
    private boolean mIsList = false;
    //	private LinearLayout mSelectABLayout = null;
    private RelativeLayout mDisplayAreaLayout = null;
    private SearchAutoCompleteView mSearchPopupAutocompleteView = null;
    private SpeciesLibAdapter mAdapter = null;
    private WildscanImageCache mImageCache;
    private ContentObserver mObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
//		     showSpecies();
        }
    };
    private int mLastPosition = 0, mLastOffset = 0;
    // filter results
    private int mTypeFilter = 0; // no filter..
    private String mProdAniFilter = null;
    private String mCoveringFilter = null;
    private String mNumLegsFilter = null;
    private String mColorFilter = null;
    private String mSizeFilter = null;
    private String mSpecialFeaturesFilter = null;
    private String mPurposeFilter = null;
    private String mMaterialFilter = null;
    private String mPartFilter = null;
    private String mCurrFilterSelection = null;
    private boolean mShowFavorites = false;
    private boolean mShowAllFilter = false;
    //	static int mTmpFilter = -1;
    private boolean mIsFromWizard = false;
    private boolean mNeedResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        WildscanDataManager.setAppContext(this);
        mImageCache = WildscanImageCache.getInstance(this);

//		ActionBar bar = getActionBar();
//		bar.setSubtitle("Species");

        if (savedInstanceState != null) {
            mLastPosition = savedInstanceState.getInt(KEY_LIST_POSITION, 0);
            mLastOffset = savedInstanceState.getInt(KEY_LIST_OFFSET, 0);
        }
        String v = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PrefsFragment.PREFS_KEY_DEFAULT_GALLERY_VIEW, "0");
        int defView = Integer.valueOf(v);
        mIsList = PrefsFragment.PREFS_DEFAULT_GALLERY_VIEW_LIST == defView ||
                (PrefsFragment.PREFS_DEFAULT_GALLERY_VIEW_LAST == defView &&
                        getSharedPreferences(PrefsFragment.PREFS_FILENAME, MODE_PRIVATE)
                                .getBoolean(KEY_ISLIST, false));
        mShowFavorites = getSharedPreferences(PrefsFragment.PREFS_FILENAME, MODE_PRIVATE)
                .getBoolean(KEY_SHOW_FAVS, false);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mTypeFilter = b.getInt(KEY_TYPE_FILTER, -1);
            mProdAniFilter = b.getString(KEY_PROD_ANI_FILTER, null);
            mCoveringFilter = b.getString(KEY_COVER_FILTER, null);
            mNumLegsFilter = b.getString(KEY_NUMLEGS_FILTER, null);
            mColorFilter = b.getString(KEY_COLOR_FILTER, null);
            mSizeFilter = b.getString(KEY_SIZE_FILTER, null);
            mSpecialFeaturesFilter = b.getString(KEY_SPEC_FILTER, null);
            mPurposeFilter = b.getString(KEY_PURPOSE_FILTER, null);
            mMaterialFilter = b.getString(KEY_MATERIAL_FILTER, null);
            mPartFilter = b.getString(KEY_PART_FILTER, null);

            mNeedResult = b.getBoolean(KEY_NEED_RESULT, false);
        }

        setContentView(R.layout.activity_species_lib);

        mDisplayAreaLayout = (RelativeLayout) findViewById(R.id.speciesLibDisplayLayout);

        showSpecies();
    }

    private void showSpecies() {

        String[] columns_id = {Species._ID, Species._MAIN_PHOTO, Species._S_COMMON_NAME, Species
                ._S_CITES, Species._FAV};
        String where = null;
        mCurrFilterSelection = null;
        String fav = mShowFavorites ? Species._FAV + "=1" : null;
        String[] types = getResources()
                .getStringArray(R.array.species_types);//WildscanDataManager.Species.TYPES;
        if (mTypeFilter > 0 && mTypeFilter < types.length) {
            mCurrFilterSelection = Species._S_TYPE + " = \'" + types[mTypeFilter] + "\'";
        } else if (mTypeFilter == types.length) {
            // location-based filter (close-by species)
            Location here = WildscanDataManager.getLastLocation();
            if (here != null) {
                double here_lat = here.getLatitude(), here_lon = here.getLongitude();
                String[] cols = new String[]{Incidents._S_SPECIES, Incidents._S_LOCATION_LAT,
                        Incidents._S_LOCATION_LON};
                Uri uri = Uri
                        .parse("content://" + WildscanDataProvider.AUTHORITY + "/" + Incidents
                                .TABLE_NAME);
                Cursor cursor = getContentResolver()
                        .query(uri, cols, mCurrFilterSelection, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    double lat = cursor.getDouble(cursor.getColumnIndex(Incidents._S_LOCATION_LAT)),
                            lon = cursor
                                    .getDouble(cursor.getColumnIndex(Incidents._S_LOCATION_LON));
                    float[] dist = new float[1];
                    Location.distanceBetween(here_lat, here_lon, lat, lon, dist);
                    if (dist[0] <= DISTANCE_FOR_LOCATION_FILTER_IN_METERS) {
                        String id = String.valueOf(
                                cursor.getLong(cursor.getColumnIndex(Incidents._S_SPECIES)));
                        mCurrFilterSelection = (mCurrFilterSelection == null) ? id :
                                mCurrFilterSelection + "," + id;
                    }
                    cursor.moveToNext();
                }
                cursor.close();

                if (mCurrFilterSelection != null) {
                    mCurrFilterSelection = Species._ID + " IN (" + mCurrFilterSelection + ")";
                } else {
                    Toast t = Toast.makeText(this, R.string.msg_species_lib_no_close_events,
                            Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                    mTypeFilter = 0;
                }
            } else {
                Toast t = Toast
                        .makeText(this, R.string.msg_species_lib_no_location, Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                mTypeFilter = 0;
            }
        } else {
            Log.e("NOMAN", "3");

            String field = Species._S_KEYWORDS_TAGS;
            if (mProdAniFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mProdAniFilter);
            }
            if (mCoveringFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mCoveringFilter);
            }
            if (mColorFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mColorFilter);
            }
            if (mNumLegsFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mNumLegsFilter);
            }
            if (mSizeFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mSizeFilter);
            }
            if (mSpecialFeaturesFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field,
                        mSpecialFeaturesFilter);
            }
            if (mPurposeFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mPurposeFilter);
            }
            if (mMaterialFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mMaterialFilter);
            }
            if (mPartFilter != null) {
                mCurrFilterSelection = addFilter(mCurrFilterSelection, field, mPartFilter);
            }
            if (mCurrFilterSelection != null) {
                mIsFromWizard = true;
            }
        }
        if (mShowFavorites) {
            if (mCurrFilterSelection == null)
                where = fav;
            else
                where = mCurrFilterSelection + " AND " + fav;
        } else {
            where = mCurrFilterSelection;
        }
        String orderBy = Species._S_COMMON_NAME + " ASC";
//        String orderBy = Species._S_TYPE + " , " + Species._S_COMMON_NAME + " ASC";

        where = Util.addLangToSelection(getApplicationContext(), where,
                SpeciesTranslations._S_LANGUAGE);
        if (null == where) {
            where = "region = 2";
            if (AppPreferences.isAsiaRegion(SpeciesLibActivity.this))
                where = where + " OR region = 4";
            if (AppPreferences.isAfricaRegion(SpeciesLibActivity.this))
                where = where + "OR region = 1";
            if (AppPreferences.isAmericanRegion(SpeciesLibActivity.this))
                where = where + " OR region = 3";
        }
        mCursor = getContentResolver().query(sBaseQueryUri, columns_id, where, null, orderBy);

        if (mCursor.getCount() == 0) {
            int message;
            if (mIsFromWizard)
                message = R.string.msg_species_lib_wizard_empty;
            else if (mTypeFilter != -1)
                message = R.string.msg_species_lib_filter_empty;
            else
                message = R.string.msg_species_lib_list_empty;
            Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }

        mAdapter = new SpeciesLibAdapter(mCursor);
        setupLibView();
    }

    private static String addFilter(String select, String field, String filter) {
        String ret;
        if (select == null)
            ret = "";
        else
            ret = select + " AND ";
        int s2, s = filter.indexOf('/');
        if (s == -1) {
            ret += field + " LIKE \'%" + filter + "%\'";
        } else {
            ret += "( " + field + " LIKE \'%" + filter.substring(0, s - 1) + "%\'";
            while ((s2 = filter.indexOf('/', s + 1)) != -1) {
                ret += " OR " + field + " LIKE \'%" + filter.substring(s + 1, s2 - 1) + "%\'";
                s = s2;
            }
            ret += " )";
        }
        return ret;
    }

    private void setupLibView() {
        AbsListView v = (AbsListView) ((ViewGroup) findViewById(R.id.speciesLibDisplayLayout))
                .getChildAt(0);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        int padding = mIsList ? 0 : getResources()
                .getDimensionPixelSize(R.dimen.species_gallery_grid_spacing);

        boolean haveV = v != null;
        boolean vMatch = haveV && (mIsList == ListView.class.isInstance(v));

        if (mIsList) {
            ListView lv;

            if (vMatch) {
                lv = (ListView) v;
            } else {
                lv = new ListView(this);
                lv.setOnItemClickListener(this);
                lv.setDivider(null);
                lv.setDividerHeight(0);
            }

            lv.setAdapter(mAdapter);

            if (vMatch) {
                lv.setSelectionFromTop(mLastPosition, mLastOffset);
            }

            v = lv;
        } else {
            GridView gv;

            if (vMatch) {
                gv = (GridView) v;
                //position = gv.getScrollY();
            } else {
                gv = new GridView(this);
                gv.setOnItemClickListener(this);
                gv.setNumColumns(2);
                gv.setHorizontalSpacing(padding);
                gv.setVerticalSpacing(padding);
                gv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            }

            gv.setAdapter(mAdapter);

            if (vMatch) {
                gv.setSelection(mLastPosition);
                gv.smoothScrollToPositionFromTop(mLastPosition, mLastOffset);
            }

            v = gv;
        }

        ((ImageButton) findViewById(R.id.speciesLibHeaderBtnGallery)).getDrawable()
                .setLevel(mIsList ? 0 : 1);
        ((ImageButton) findViewById(R.id.speciesLibHeaderBtnFavorites)).getDrawable()
                .setLevel(mShowFavorites ? 1 : 0);

        mDisplayAreaLayout.setPadding(padding, padding, padding, padding);

        if (!vMatch) {
            if (haveV)
                mDisplayAreaLayout.removeAllViews();
            mDisplayAreaLayout.addView(v, params);
        }
        mDisplayAreaLayout.postInvalidate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AbsListView alv = (AbsListView) ((ViewGroup) findViewById(R.id.speciesLibDisplayLayout))
                .getChildAt(0);

        mLastPosition = alv.getFirstVisiblePosition();
        mLastOffset = (alv.getChildAt(0) == null) ? 0 : alv.getChildAt(0).getTop();
        mImageCache.pauseSpeciesThumbnail();
        dismissSearchPopup();
        WildscanDataManager.clearSpeciesAutocompleteList();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    private void dismissSearchPopup() {
        if (mSearchPopup != null) {
            final InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            mSearchPopup.setVisibility(View.INVISIBLE);
        }
        mSearchPopup = null;
        mSearchPopupAutocompleteView = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageCache.resumeSpeciesThumbnail();
        getContentResolver().registerContentObserver(sBaseQueryUri, true, mObserver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_LIST_POSITION, mLastPosition);
        outState.putInt(KEY_LIST_OFFSET, mLastOffset);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mLastPosition = savedInstanceState.getInt(KEY_LIST_POSITION, 0);
        mLastOffset = savedInstanceState.getInt(KEY_LIST_OFFSET, 0);

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mSearchPopup != null)
            dismissSearchPopup();
        else if (mShowAllFilter) {
            mShowAllFilter = false;
            showSpecies();
        } else
            super.onBackPressed();
    }

    public void onClickHeaderBtnFavorites(View view) {
        if (mSearchPopup != null) {
            dismissSearchPopup();
            return;
        }
        mShowFavorites = !mShowFavorites;
        int count = 1;
        if (mShowFavorites) {// && count<1) {
            String selection = Util.addLangToSelection(getApplicationContext(), Species._FAV + "=1",
                    SpeciesTranslations._S_LANGUAGE);
            String orderBy = Species._S_COMMON_NAME + " ASC";
            Cursor c = getContentResolver().query(sBaseQueryUri, null, selection, null, orderBy);
            count = c.getCount();
            c.close();
            if (count < 1) {
                Toast.makeText(this, R.string.msg_species_lib_fav_empty, Toast.LENGTH_LONG).show();
                mShowFavorites = false;
            }
        }
        if (count >= 1) {
            getSharedPreferences(PrefsFragment.PREFS_FILENAME, MODE_PRIVATE).edit()
                    .putBoolean(KEY_SHOW_FAVS, mShowFavorites).commit();
//			refreshDisplay();
            showSpecies();
        }
    }

    public void onClickHeaderBtnGallery(View view) {
        if (mSearchPopup != null) {
            dismissSearchPopup();
            return;
        }
        mIsList = !mIsList;
        getSharedPreferences(PrefsFragment.PREFS_FILENAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_ISLIST, mIsList).commit();
//		Intent i = new Intent(this, this.getClass());
//		i.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
//		startActivity(i);
//		finish();
//		refreshDisplay();
        showSpecies();
    }

    public void onClickHeaderBtnSearch(View view) {
        if (mSearchPopup != null)
            return;

        final InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mSearchPopup = (LinearLayout) findViewById(R.id.speciesLibSearchPopup);

        ArrayList<Pair<String, Integer>> acList = WildscanDataManager
                .getSpeciesAutocompleteList(mCurrFilterSelection);
        ArrayList<Pair<String, Integer>> knownAsList = WildscanDataManager
                .getSpecieAutoCompleteKnownAsList(mCurrFilterSelection);
        mSearchPopupAutocompleteView = (SearchAutoCompleteView) mSearchPopup
                .findViewById(R.id.searchAutocomplete);
        mSearchPopupAutocompleteView.getText().clear();
        mSearchPopupAutocompleteView
                .setHintTextColor(getResources().getColor(android.R.color.white));
        mSearchPopupAutocompleteView.shouldReplaceText(false);
        mSearchPopupAutocompleteView.setAdapter(
                new SearchAutoCompleteView.Adapter(getApplicationContext(),
                        R.layout.item_search_dropdown, acList, knownAsList));
        mSearchPopupAutocompleteView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView v = (TextView) view.findViewById(android.R.id.text1);
                String searchedText = mSearchPopupAutocompleteView.getText().toString();
                //CharSequence name = v.getText();
                int sid = (Integer) v.getTag();//WildscanDataManager.getSpeciesId(name);
                imm.hideSoftInputFromWindow(v.getWindowToken(),
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
                dismissSearchPopup();
                AbsListView listView = (AbsListView) ((ViewGroup) findViewById(
                        R.id.speciesLibDisplayLayout)).getChildAt(0);
                int pos = listView.getAdapter().getCount();
                while (--pos >= 0 && listView.getAdapter().getItemId(pos) != sid) ;
                if (pos != -1) {
                    if (mIsList)
                        ((ListView) listView).setSelectionFromTop(pos, 0);
                    else {
                        listView.setSelection(pos);
                        ((GridView) listView).smoothScrollToPositionFromTop(pos, 0);
                    }
                } else if (sid == -2) {
                    showAllFilterSpecies(searchedText);
                } else {
                    Toast.makeText(SpeciesLibActivity.this,
                            R.string.msg_species_lib_search_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSearchPopupAutocompleteView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    mSearchPopupAutocompleteView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imm.showSoftInput(mSearchPopupAutocompleteView,
                                    InputMethodManager.SHOW_IMPLICIT);
                        }
                    }, 100);
                else
                    imm.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        Spinner filterSpinner = (Spinner) mSearchPopup.findViewById(R.id.searchFilter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getApplicationContext(), R.array.species_filters,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.item_species_filter_dropdown);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setSelection(mTypeFilter);
        filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            boolean firstTime = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!firstTime) {
                    dismissSearchPopup();
                    if (position != mTypeFilter) {
                        mTypeFilter = position;
                        showSpecies();
//						getLoaderManager().getLoader(LOADER_ID).onContentChanged();
//						refreshDisplay();
                    }
                }
                firstTime = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSearchPopup.setVisibility(View.VISIBLE);
    }

    private void showAllFilterSpecies(String keyword) {

        mShowAllFilter = true;

        String[] columns_id = {Species._ID, Species._MAIN_PHOTO, Species._S_COMMON_NAME, Species
                ._S_CITES, Species._FAV};
        String where = null;
        mCurrFilterSelection = null;

        String orderBy = Species._S_COMMON_NAME + " ASC";

        where = Util.addLangToSelection(getApplicationContext(), where,
                SpeciesTranslations._S_LANGUAGE);
        if (null == where) {
            where = "( region = 2";
            if (AppPreferences.isAsiaRegion(SpeciesLibActivity.this))
                where = where + " OR region = 4";
            if (AppPreferences.isAfricaRegion(SpeciesLibActivity.this))
                where = where + "OR region = 1";
            if (AppPreferences.isAmericanRegion(SpeciesLibActivity.this))
                where = where + " OR region = 3";

            where += ") ";
        }
        where += " AND " + Species._S_COMMON_NAME + " like ? ";
        mCursor = getContentResolver().query(sBaseQueryUri, columns_id,
                where, new String[]{"%" + keyword + "%"}, orderBy);

        if (mCursor.getCount() == 0) {
            int message;
            if (mIsFromWizard)
                message = R.string.msg_species_lib_wizard_empty;
            else if (mTypeFilter != -1)
                message = R.string.msg_species_lib_filter_empty;
            else
                message = R.string.msg_species_lib_list_empty;
            Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }

        mAdapter = new SpeciesLibAdapter(mCursor);
        setupLibView();
    }

    public void onClickItemFav(View v) {
        if (mSearchPopup != null) {
            dismissSearchPopup();
            return;
        }

        final ImageView fv = (ImageView) v;

        long id = (Long) fv.getTag();
        int isFav = fv.getDrawable().getLevel();
        isFav = 1 - isFav;

        // update mark
        fv.getDrawable().setLevel(isFav);
        fv.invalidate();

        // update db
        ContentValues values = new ContentValues();
        values.put(Species._FAV, isFav);
        Uri uri = ContentUris.withAppendedId(sBaseQueryUri, id);
        getContentResolver().update(uri, values, null, null);
    }

    public void onClickItemReport(View v) {
        if (mSearchPopup != null) {
            dismissSearchPopup();
            return;
        }

        final ImageView fv = (ImageView) v;

        long id = (Long) fv.getTag();
        if (mNeedResult) {
            Intent res = new Intent();
            res.putExtra(KEY_RESULT_SPECIES_ID, id);
            SpeciesLibActivity.this.setResult(Activity.RESULT_OK, res);
            finish();
        } else {
            // start new report with this species
            Intent intent = new Intent(this, ReportWizardActivity.class);
            intent.putExtra(ReportWizardActivity.KEY_SPECIES_ID, id);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int pos, final long id) {
        if (mSearchPopup != null) {
            dismissSearchPopup();
            return;
        }
        if (mNeedResult) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage(R.string.species_lib_select_dlg_text);
            b.setNegativeButton(R.string.species_lib_select_dlg_btn_details,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            launchSpeciesDetails(id);
                        }
                    });
            b.setPositiveButton(R.string.species_lib_select_dlg_btn_select,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent res = new Intent();
                            res.putExtra(KEY_RESULT_SPECIES_ID, id);
                            SpeciesLibActivity.this.setResult(Activity.RESULT_OK, res);
                            dialog.dismiss();
                            finish();
                        }
                    });
            b.show();
        } else {
            launchSpeciesDetails(id);
        }
    }

    private void launchSpeciesDetails(long id) {
        Intent intent = new Intent(this, SpeciesDetailsActivity.class);
        intent.putExtra(SpeciesDetailsActivity.KEY_SPECIES_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (mNeedResult) {
            intent.putExtra(SpeciesDetailsActivity.KEY_NEED_RESULT, mNeedResult);
            startActivityForResult(intent, 0);
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 0) {
            Intent res = new Intent();
            res.putExtra(KEY_RESULT_SPECIES_ID,
                    data.getLongExtra(ReportWizardActivity.KEY_SPECIES_ID, -1L));
            SpeciesLibActivity.this.setResult(Activity.RESULT_OK, res);
            finish();

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private class SpeciesLibAdapter extends SimpleCursorAdapter {
        final String mPhotoBaseUri = "file://" + SpeciesLibActivity.this
                .getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0)
                .getString(PrefsFragment.PREFS_KEY_DATA_FOLDER, null);

        @Deprecated
        public SpeciesLibAdapter(Context context, int layout, Cursor c,
                                 String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        public SpeciesLibAdapter(Context context, int layout, Cursor c,
                                 String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        public SpeciesLibAdapter(Cursor c) {
            super(SpeciesLibActivity.this,
                    mIsList ? R.layout.item_species_list : R.layout.item_species_gallery, c,
                    COLUMNS, VIEWS, 0);
        }

        @Override
        public void setViewText(TextView v, String text) {
            text = "<b>" + text + "</b>";
//			if (!mIsList) {
            Cursor cursor = getCursor();
            int col_idx_cites = cursor.getColumnIndex(Species._S_CITES);
            String cites = null;
            Integer i_c = SpeciesTranslations.CITES_APPENDIX_STRINGS
                    .get(cursor.getString(col_idx_cites));
            if (i_c != null)
                cites = getResources().getString(i_c);
            if (!TextUtils.isEmpty(cites))
                text += "<br>CITES " + cites;//.toUpperCase(Locale.ENGLISH);
//			}
            v.setText(Html.fromHtml(text));
        }

        @Override
        public void setViewImage(ImageView v, String value) {
            if (v.getId() == R.id.speciesListBtnFav) {
                v.getDrawable().setLevel(Integer.valueOf(value));
            } else {
                if (TextUtils.isEmpty(value))
                    v.setImageResource(R.drawable.empty_species);
                else
                    mImageCache.loadSpeciesThumbnail(mPhotoBaseUri + value, v);
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            final ImageButton btnFav = (ImageButton) v.findViewById(R.id.speciesListBtnFav),
                    btnReport = (ImageButton) v.findViewById(R.id.speciesListBtnReport);
            final Cursor c = getCursor();
            final long id = c.getLong(c.getColumnIndex(Species._ID));
            if (btnFav != null) {
                btnFav.setTag(id);
                btnFav.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickItemFav(btnFav);
                    }
                });
                btnReport.setTag(id);
                btnReport.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickItemReport(btnReport);
                    }
                });
            }
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(((AbsListView) mDisplayAreaLayout.getChildAt(0)), v, position, id);
                }
            });
            return v;
        }
    }

}
