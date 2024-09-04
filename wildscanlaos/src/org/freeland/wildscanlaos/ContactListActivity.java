/**
 *
 */
package org.freeland.wildscanlaos;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.Contacts;
import org.freeland.wildscanlaos.data.contract.ContactsTranslations;
import org.freeland.wildscanlaos.data.provider.WildscanDataProvider;
import org.freeland.wildscanlaos.imagecache.WildscanImageCache;
import org.freeland.wildscanlaos.util.AppPreferences;
import org.freeland.wildscanlaos.util.Util;
import org.freeland.wildscanlaos.widget.SearchAutoCompleteView;

import java.util.ArrayList;

/**
 * @author Noam
 */
public class ContactListActivity extends Activity
        implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnItemSelectedListener, OnClickListener {

    public static String KEY_RESULT_CONTACT_ID = "org.freeland.wildscanlaos.ContactList.ContactId";
    public static String KEY_RESULT_CONTACT_NAME = "org.freeland.wildscanlaos.ContactList.ContactName";
    public static String KEY_RESULT_CONTACT_EMAIL = "org.freeland.wildscanlaos.ContactList.ContactEmail";
    public static String KEY_RESULT_CONTACT_ID_LIST = "org.freeland.wildscanlaos.ContactList.ContactIdList";

    public static String KEY_REQUEST_RESULT = "org.freeland.wildscanlaos.ContactList.RequestResult";

    private final static int LOADER_ID = 2;

    private WildscanImageCache mImageCache;
    private ListView mListView = null;
    private boolean mReturnRequested = false;
    private ArrayList<Long> mReturnIdList = new ArrayList<Long>();
    private ContactsListAdapter mAdapter;
    private boolean mShowFavs = false;
    private SearchAutoCompleteView mSearchAutocompleteView;

    private static final Uri mBaseQueryUri = Uri.parse("content://" + WildscanDataProvider.AUTHORITY + "/" + Contacts.TABLE_NAME);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        try {
            mReturnRequested = getIntent().getExtras().getBoolean(KEY_REQUEST_RESULT, false);
        } catch (Exception e) {
            // do nothing
        }
        mReturnIdList.clear();

        WildscanDataManager.setAppContext(this);

        mImageCache = WildscanImageCache.getInstance(this);

        setContentView(R.layout.activity_contacts_list);

        //mDisplayAreaLayout = (RelativeLayout) findViewById(R.id.contactsListDisplayLayout);
        mListView = (ListView) findViewById(R.id.contactsListDisplayLayout); //new ListView(this);
        mListView.setOnItemClickListener(this);

		/*SHOW CONTACTS ACCORDING TO THE SELECTED REGIONS*/
        String[] columns_id = {Contacts._ID, Contacts._S_REGION, Contacts._COUNTRY, Contacts._AVATAR, Contacts._WEBSITE, Contacts._DETAILS, Contacts._FAV};
        String where = null;
        String orderBy = Contacts._NAME + " ASC";
        //Log.e("TAG", where + " ");
        /*
        if (null == where) {
            where = "region = 1 ";
            if (AppPreferences.isAsiaRegion(ContactListActivity.this))
                where = where + " OR region = 2";
            if (AppPreferences.isAfricaRegion(ContactListActivity.this))
                where = where + " OR region = 3";
            if (AppPreferences.isAmericanRegion(ContactListActivity.this))
                where = where + " OR region = 4";

        }
        */
        //String orderBy = Contacts._COUNTRY + ", " + Contacts._NAME + " ASC";
        //String orderBy = Contacts._ID + " ASC";

        Cursor mCursor = getContentResolver().query(mBaseQueryUri, columns_id, where, null, orderBy);


        mAdapter = new ContactsListAdapter(mCursor);
        mListView.setAdapter(mAdapter);
        mListView.setDivider(null);
        mListView.setDividerHeight(0);

        //Display Total
        //Integer number = mListView.getCount();

        //TextView header = findViewById(R.id.contactsListHeader);
        //header.setText(number);

        ((ImageButton) findViewById(R.id.contactsListHeaderBtnFavorites)).getDrawable().setLevel(mShowFavs ? 1 : 0);
        getLoaderManager().initLoader(LOADER_ID, null, this);

//		showDisplay();

//		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		mDisplayAreaLayout.addView(mListView, params);

    }

    private void refreshDisplay() {
        ((ImageButton) findViewById(R.id.contactsListHeaderBtnFavorites)).getDrawable().setLevel(mShowFavs ? 1 : 0);
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mSearchAutocompleteView != null) {
            dismissSearchPopup();
            return;
        }

        if (!mReturnRequested)
            return;

        //SQLiteDatabase db = WildscanDataManager.getInstance(this).getReadableDatabase();
        String[] columns = {Contacts._ID, Contacts._S_REGION, Contacts._AVATAR, Contacts._WEBSITE, Contacts._DETAILS, Contacts._FAV};
        //String[] columns = {Contacts._ID, Contacts._NAME, Contacts._EMAIL};
        String selection = Util.addLangToSelection(getApplicationContext(), null, ContactsTranslations._S_LANGUAGE);
        //String orderBy = Contacts._NAME + " ASC";
        String orderBy = Contacts._COUNTRY + ", " + Contacts._NAME + " ASC";
        //Cursor c = db.query(WildscanDataManager.Contacts.TABLE_NAME, columns, selection, null, null, null, null);
        Uri uri = ContentUris.withAppendedId(mBaseQueryUri, id);
        Cursor c = getContentResolver().query(uri, columns, selection, null, orderBy);
        c.moveToFirst();
        int col_idx_name = c.getColumnIndex(Contacts._NAME),
                col_idx_email = c.getColumnIndex(Contacts._EMAIL);
        String name = c.getString(col_idx_name),
                email = c.getString(col_idx_email);
        c.close();

        Intent intent = new Intent();
        intent.putExtra(KEY_RESULT_CONTACT_ID, id);
        intent.putExtra(KEY_RESULT_CONTACT_NAME, name);
        intent.putExtra(KEY_RESULT_CONTACT_EMAIL, email);

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = Util.addLangToSelection(getApplicationContext(), null, ContactsTranslations._S_LANGUAGE);
//		String orderBy = WildscanDataManager.Contacts._NAME + " ASC";
        String orderBy = Contacts._COUNTRY + ", " + Contacts._NAME + " ASC";
        //String orderBy = Contacts._NAME + " ASC";
        if (mShowFavs)
            selection = Contacts._FAV + "=1";
        /*
        else {
            selection = "region = 1";
            if (AppPreferences.isAsiaRegion(ContactListActivity.this))
                selection = selection + " OR region = 2";
            if (AppPreferences.isAfricaRegion(ContactListActivity.this))
                selection = selection + " OR region = 3";
            if (AppPreferences.isAmericanRegion(ContactListActivity.this))
                selection = selection + " OR region = 4";

        }
        */
        return new CursorLoader(this, mBaseQueryUri, null, selection, null, orderBy);
//	return new ContactsListLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        mAdapter.changeCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissSearchPopup();
//		mDbHelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageCache.resumeContactAvatar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mImageCache.pauseContactAvatar();
        dismissSearchPopup();
        WildscanDataManager.clearContactsAutocompleteList();
    }

    private void dismissSearchPopup() {
        if (mSearchAutocompleteView != null) {
            mSearchAutocompleteView.setVisibility(View.INVISIBLE);
            mSearchAutocompleteView = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mSearchAutocompleteView != null)
            dismissSearchPopup();
        else
            super.onBackPressed();
    }

    public void onClickHeaderBtnFavorites(View v) {
        if (mSearchAutocompleteView != null) {
            dismissSearchPopup();
            return;
        }
        mShowFavs = !mShowFavs;
        int count = 1;
        if (mShowFavs) {// && count<1) {
            String selection = mShowFavs ? Contacts._FAV + "=1" : "";
            selection = Util.addLangToSelection(getApplicationContext(), selection, ContactsTranslations._S_LANGUAGE);
            //String orderBy = Contacts._NAME + " ASC";
            String orderBy = Contacts._COUNTRY + ", " + Contacts._NAME + " ASC";
            Cursor c = getContentResolver().query(mBaseQueryUri, null, selection, null, orderBy);
            count = c.getCount();
            c.close();
            if (mShowFavs && count < 1) {
                Toast.makeText(this, R.string.msg_contact_fav_empty, Toast.LENGTH_LONG).show();
                mShowFavs = false;
            }
        }
        if (count >= 1) {
            refreshDisplay();
//			showDisplay();
        }
    }

    public void onClickHeaderBtnSearch(View button) {
        if (mSearchAutocompleteView != null)
            return;

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ArrayList<Pair<String, Integer>> acList = WildscanDataManager.getContactsAutocompleteList();
        mSearchAutocompleteView = (SearchAutoCompleteView) findViewById(R.id.searchAutocomplete);
        mSearchAutocompleteView.setText(null);
        mSearchAutocompleteView.setHintTextColor(getResources().getColor(android.R.color.white));
//	    mSearchAutocompleteView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_search_dropdown, acList));
        mSearchAutocompleteView.setAdapter(new SearchAutoCompleteView.Adapter(this, R.layout.item_search_dropdown, acList,null));
        mSearchAutocompleteView.setDropDownWidth(LayoutParams.WRAP_CONTENT);
        mSearchAutocompleteView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView v = (TextView) view.findViewById(android.R.id.text1);
                //String name = (String)v.getTag();//getText().toString();
                int cid = (Integer) v.getTag();//WildscanDataManager.getContactId(name);
                imm.hideSoftInputFromWindow(mSearchAutocompleteView.getWindowToken(), 0);// InputMethodManager.HIDE_IMPLICIT_ONLY);
                dismissSearchPopup();
                int pos = mListView.getAdapter().getCount();
                while (--pos >= 0 && mListView.getAdapter().getItemId(pos) != cid) ;
                if (pos != -1) {
                    mListView.requestFocusFromTouch();
                    mListView.setSelectionFromTop(pos, 0);
                }
            }
        });
        mSearchAutocompleteView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            //imm.showSoftInput(mSearchAutocompleteView, InputMethodManager.SHOW_IMPLICIT);
                            imm.showSoftInput(mSearchAutocompleteView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }
                    }, 100);
                } else
                    //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    imm.hideSoftInputFromWindow(mSearchAutocompleteView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        mSearchAutocompleteView.setVisibility(View.VISIBLE);
        mSearchAutocompleteView.requestFocus();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mSearchAutocompleteView != null) {
            dismissSearchPopup();
            return;
        }
        if (!mReturnRequested)
            return;

        mReturnIdList.add(Long.valueOf(id));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mReturnIdList.clear();
    }

    @Override
    public void onClick(View v) {
        if (mSearchAutocompleteView != null) {
            dismissSearchPopup();
            return;
        }
        final ImageView fv = (ImageView) v;

        int isFav = 1 - fv.getDrawable().getLevel();
        int id = (Integer) fv.getTag();
        fv.getDrawable().setLevel(isFav);
        fv.invalidate();

        // update db
        ContentValues values = new ContentValues();
        values.put(Contacts._FAV, isFav);
        Uri uri = ContentUris.withAppendedId(mBaseQueryUri, id);
        getContentResolver().update(uri, values, null, null);
    }

    private static final String[] COLUMNS = {Contacts._AVATAR, Contacts._WEBSITE, Contacts._DETAILS, Contacts._FAV};
    private static final int[] VIEWS = {R.id.contactsListAvatar, R.id.contactsListWebsite, R.id.contactsListDetails, R.id.listBtnFav};

    private class ContactsListAdapter extends SimpleCursorAdapter {

        final String mAvatarBaseUri = "file://" + ContactListActivity.this.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getString(PrefsFragment.PREFS_KEY_DATA_FOLDER, null);

        @Deprecated
        private ContactsListAdapter(Context context, int layout, Cursor c,
                                    String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        private ContactsListAdapter(Context context, int layout, Cursor c,
                                    String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        public ContactsListAdapter(Cursor cursor) {
            super(ContactListActivity.this, R.layout.item_contact_list, cursor, COLUMNS, VIEWS, 0);
        }

        @Override
        public void setViewText(TextView v, String text) {
            if (v.getId() == R.id.contactsListWebsite) {
                v.setPaintFlags(v.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
            v.setText(Html.fromHtml(text));
        }

        @Override
        public void setViewImage(ImageView v, String value) {
            if (v.getId() == R.id.listBtnFav) {
                v.getDrawable().setLevel(Integer.valueOf(value));
                Util.logInfo("Contact Link: ", mAvatarBaseUri + value);
            }
//			else if (mImageFetcher == null ) {
//				super.setViewImage(v, value);
//			}
            else {
                mImageCache.loadContactAvatar(mAvatarBaseUri + value, v);
                Util.logInfo("Contact Link: ", mAvatarBaseUri + value);
            }

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            Cursor c = getCursor();
            final int id = c.getInt(c.getColumnIndex(Contacts._ID));

            ImageButton btn = (ImageButton) v.findViewById(R.id.listBtnFav);
            if (btn != null) {
                btn.setTag(id);
                btn.setFocusable(false);
                btn.setFocusableInTouchMode(false);
                btn.setOnClickListener(ContactListActivity.this);
            }
            v.findViewById(R.id.contactsListWebsite).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSearchAutocompleteView != null) {
                        dismissSearchPopup();
                        return;
                    }
                    if (!TextUtils.isEmpty(((TextView) v).getText())) {
                        Uri website = Uri.parse(((TextView) v).getText().toString());
                        Intent intent = new Intent(Intent.ACTION_VIEW, website);
                        startActivity(intent);
                    }
                }
            });
            v.findViewById(R.id.contactsListDetails).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(mListView, v, position, id);
                }
            });
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(mListView, v, position, id);
                }
            });
            return v;
        }
    }

}
