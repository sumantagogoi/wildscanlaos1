/**
 *
 */
package org.freeland.wildscan.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.vending.expansion.downloader.Helpers;

import org.freeland.wildscan.BuildConfig;
import org.freeland.wildscan.PrefsFragment;
import org.freeland.wildscan.R;
import org.freeland.wildscan.data.contract.Contacts;
import org.freeland.wildscan.data.contract.ContactsTranslations;
import org.freeland.wildscan.data.contract.Incidents;
import org.freeland.wildscan.data.contract.Species;
import org.freeland.wildscan.data.contract.SpeciesImages;
import org.freeland.wildscan.data.contract.SpeciesTranslations;
import org.freeland.wildscan.data.contract.StaticContent;
import org.freeland.wildscan.data.provider.WildscanDataProvider;
import org.freeland.wildscan.data.sync.WildscanSyncAdapter;
import org.freeland.wildscan.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.zip.ZipFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Noam
 */
public class WildscanDataManager {

    public static final String BASE_FOLDER = "Wildscan";
    public static final String REMOTE_BASE = "https://wildscan.org/api/";
    public static final String REMOTE_BASE_ENCODED = "URs2SAoIVktHXjhYQCZYHUVSIAUdIgY0XEMMDV5m";
    //	public static final String REMOTE_BASE = "https://test.vimi.co/test/wildscan/";
//	public static final String REMOTE_BASE_ENCODED =
// "URs2SAoIVktFUiZFQDNeX1gZMB4dIQorTB8YBVwtS1EsLBk=";
//	public static final String REMOTE_BASE = "http://wildscan.freeland.org/";
    public static final String REMOTE_PHP_QUERY_IDS = "get-ids.php";
    public static final String REMOTE_PHP_QUERY_UP_IDS = "get-updated-ids.php";
    public static final String REMOTE_PHP_QUERY_REPORT_PHOTO = "get-report-photo.php";
    public static final String REMOTE_PHP_QUERY_UPDATES = "get-updates.v2.php";
    public static final String REMOTE_PHP_SUBMIT_REPORT = "submit-report-json.php";
    public static final String REMOTE_PHP_LOGIN = "login.v2.php";
    public static final String REMOTE_PHP_SIGNUP = "signup.php";
//	public static final String REMOTE_PHP_RESET = "reset_user.php";

    public static final long APK_MAIN_EXPANSION_FILE_SIZE = 29358292L;//21522755L; //64187428L;
    // 174298624L; //174862247L;
    public static final int APK_MAIN_EXPANSION_FILE_VERSION = 106;//32; //3; //1;
    static final int LOCATION_UPDATES_MIN_TIME = 10 * 60 * 1000; // 10 minutes update interval
    static final int LOCATION_UPDATE_MIN_DISTANCE = 1000; // 1km travel minimum for location updates
    private static final String TAG = "WildscanDataManager";
    private final static String[] INFO_SECTION_KEYS = {
            StaticContent._ABOUT, StaticContent._TERMS, StaticContent._CONSUMER,
            StaticContent._RESPONDER, StaticContent._ENFORCEMENT, StaticContent._LEGAL,
            StaticContent._CREDITS
    };
    private static final long SYNC_FREQ = 7 * 60 * 60 * 24; // 1-week sync frequency
    private static String sPrevFilter = null;
    // the singleton instance
    private static WildscanDataManager sDataManager = null;
    public boolean isSyncing = false;
    private String mRemoteBaseUrl = null;
    private String[] mInfoSections = null;
    private ContentValues mDefaultValues = null;
    private SyncProgressReciever mSyncReciever;
    private Context mAppContext;
    //	private WildscanMainActivity mMainActivity = null;
    private LocationListener mLocationListener = null;
    private Location mLastLocation = null;
    private WildscanSQLiteOpenHelper mHelper;

//	public String calcUserPass(long uId, String secret, String cookie, String hash) {
//		// android installation id
//		final String androidId = Settings.Secure.getString(mAppContext.getContentResolver(),
// Settings.Secure.ANDROID_ID);
//		try {
//			Mac hmac = Mac.getInstance("HmacSHA1");
//			SecretKeySpec key = new SecretKeySpec(secret.getBytes("UTF-8"), hmac.getAlgorithm());
//			hmac.init(key);
//			
//			String string = String.valueOf(uId) + ":" + androidId + ":" + cookie + ":" + hash;
//			byte[] toHash = Base64.encode(string.getBytes("UTF-8"), Base64.NO_WRAP);
//			hmac.update(toHash);
//			
//			return Base64.encodeToString(hmac.doFinal(), Base64.NO_WRAP);
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
    //	private SQLiteDatabase mDb = null;
//	private SyncProgressDialog mSyncProgress;
    private ArrayList<Pair<String, Integer>> mSpeciesAutocompleteList = null;
    private ArrayList<Pair<String, Integer>> mContactsAutocompleteList = null;
    private ArrayList<Pair<String, Integer>> mSpeciesAutocompleteListAlsoKnownAs = null;
    private ZipResourceFile mApkExpSupport = null;
    private long mDexCrc = 0;

    // blocked constructor
    private WildscanDataManager(Context c) {
        mAppContext = c.getApplicationContext();
        mHelper = new WildscanSQLiteOpenHelper(mAppContext);
        ZipFile zf = null;
        try {
            zf = new ZipFile(mAppContext.getPackageCodePath());
            mDexCrc = zf.getEntry("classes.dex").getCrc();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zf != null) zf.close();
            } catch (IOException e) {
            }
        }

        mLocationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;
            }
        };
        LocationManager lm = (LocationManager) mAppContext
                .getSystemService(Context.LOCATION_SERVICE);
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_COARSE);
        crit.setAltitudeRequired(false);
        crit.setBearingRequired(false);

        //lm.requestLocationUpdates(LOCATION_UPDATES_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, crit,
        //        mLocationListener, null);
        //mLastLocation = lm.getLastKnownLocation(lm.getBestProvider(crit, true));
    }


    public static void setAppContext(Context c) {
        if (sDataManager != null && sDataManager.mAppContext != c.getApplicationContext()) {
            if (sDataManager.mHelper != null)
                sDataManager.mHelper.close();
            sDataManager = null;
        }
        if (sDataManager == null) {
            sDataManager = new WildscanDataManager(c);
        }
//		if (WildscanMainActivity.class.isInstance(c))
//			sDataManager.mMainActivity = (WildscanMainActivity)c;
    }

    public static WildscanDataManager getInstance(Context c) {
        setAppContext(c);
        return sDataManager;
    }

    public static ArrayList<Pair<String, Integer>> getSpeciesAutocompleteList(String filter) {
        if (sDataManager != null) {
            if (sDataManager.mSpeciesAutocompleteList == null || filter != sPrevFilter)
                sDataManager.setupSpeciesList(filter);
            sPrevFilter = filter;
            return sDataManager.mSpeciesAutocompleteList;
        }
        return null;
    }

    public static ArrayList<Pair<String, Integer>> getSpecieAutoCompleteKnownAsList(String filter) {
        if (sDataManager != null) {
            if (sDataManager.mSpeciesAutocompleteListAlsoKnownAs == null || filter != sPrevFilter)
                sDataManager.setupSpeciesList(filter);
            sPrevFilter = filter;
            return sDataManager.mSpeciesAutocompleteListAlsoKnownAs;
        }
        return null;
    }

    public static ArrayList<Pair<String, Integer>> getContactsAutocompleteList() {
        if (sDataManager != null) {
            if (sDataManager.mContactsAutocompleteList == null)
                sDataManager.setupContactsList();
            return sDataManager.mContactsAutocompleteList;
        }
        return null;
    }

    public static void clearSpeciesAutocompleteList() {
        if (sDataManager != null)
            sDataManager.mSpeciesAutocompleteList = null;

    }

    public static void clearContactsAutocompleteList() {
        if (sDataManager != null)
            sDataManager.mContactsAutocompleteList = null;

    }

    public static Location getLastLocation() {
        if (sDataManager != null)
            return sDataManager.mLastLocation;
        return null;
    }

//	public static int getSpeciesId(CharSequence name) {
//		int ret = -1;
//		if (sDataManager!=null) {
//			Integer id = sDataManager.mSpeciesNameToId.get(name);
//			if (id!=null)
//				ret = id.intValue();
//		}
//		return ret; 	
//	}

    private static String getSha256Hash(String in) {
        MessageDigest sha256;
        try {
            byte[] tokenBytes = in.getBytes("UTF-8");//Base64.decode(in, Base64.NO_WRAP);
            sha256 = MessageDigest.getInstance("SHA-256");
            sha256.reset();
            byte[] hashBytes = sha256.digest(tokenBytes);
            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getSpeciesId(String name) {
        int ret = -1;
        if (sDataManager != null) {
            String escaped = DatabaseUtils.sqlEscapeString(name);
            Uri uri = WildscanDataProvider.getTableUri(Species.TABLE_NAME);
            Cursor c = sDataManager.mAppContext.getContentResolver()
                    .query(uri, new String[]{Species._ID}, Species._S_COMMON_NAME + "=" + escaped,
                            null, null);
            if (c.moveToFirst())
                ret = c.getInt(c.getColumnIndex(Species._ID));
            c.close();
        }
        return ret;
    }

//	public static int getContactId(CharSequence name) {
//		int ret = -1;
//		if (sDataManager!=null) {
//			Integer id = sDataManager.mContactCapToId.get(name);
//			if (id==null)
//				id = sDataManager.mContactCapToId.get(name);
//			if (id!=null)
//				ret = id.intValue();
//		}
//		return ret;
//	}

    public static String getSpeciesName(long id) {
        String ret = null;
        if (sDataManager != null) {
            String selection = Util.addLangToSelection(sDataManager.mAppContext,
                    Species._ID + "=" + String.valueOf(id), SpeciesTranslations._S_LANGUAGE);
            Uri uri = WildscanDataProvider.getItemUri(Species.TABLE_NAME, id);
            Cursor c = sDataManager.mAppContext.getContentResolver()
                    .query(uri, new String[]{Species._S_COMMON_NAME}, selection, null, null);
            if (c.moveToFirst())
                ret = c.getString(c.getColumnIndex(Species._S_COMMON_NAME));
            c.close();
        }
        return ret;
    }

    public static int getContactId(String name) {
        int ret = -1;
        if (sDataManager != null) {
            String escaped = name;//DatabaseUtils.sqlEscapeString(name);
            Uri uri = WildscanDataProvider.getTableUri(Contacts.TABLE_NAME);
            Cursor c = sDataManager.mAppContext.getContentResolver()
                    .query(uri, new String[]{Contacts._ID}, Contacts._NAME + "=?",
                            new String[]{escaped}, null);
            if (c.moveToFirst())
                ret = c.getInt(c.getColumnIndex(Contacts._ID));
            else {
                c.close();
                c = sDataManager.mAppContext.getContentResolver()
                        .query(uri, new String[]{Contacts._ID},
                                Contacts._S_SPECIALCAPACITY_NOTE + "=?", new String[]{escaped},
                                null);
                if (c.moveToFirst())
                    ret = c.getInt(c.getColumnIndex(Contacts._ID));
            }
            c.close();
        }
        return ret;
    }

    public static String getContactName(long id) {
        String ret = null;
        if (sDataManager != null) {
            Uri uri = WildscanDataProvider.getTableUri(Contacts.TABLE_NAME);
            String selection = Util.addLangToSelection(sDataManager.mAppContext,
                    Contacts._ID + "=" + String.valueOf(id), ContactsTranslations._S_LANGUAGE);
            Cursor c = sDataManager.mAppContext.getContentResolver()
                    .query(uri, new String[]{Contacts._S_NAME}, selection, null, null);
            if (c.moveToFirst())
                ret = Util.escapeHtml(c.getString(c.getColumnIndex(Contacts._S_NAME)));
            c.close();
        }
        return ret;
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        return sDataManager.mHelper.getReadableDatabase();
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        return sDataManager.mHelper.getWritableDatabase();
    }

    public void close() {
        sDataManager.mHelper.close();
        LocationManager lm = (LocationManager) sDataManager.mAppContext
                .getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(sDataManager.mLocationListener);
    }


    public long getUserId() {
        return mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0)
                .getLong(PrefsFragment.PREFS_KEY_USER_ID, -1L);
    }

    public String getUserSig(long time, String secret, String cookie, String hash) {
        long id = getUserId();
        final String androidId = Settings.Secure
                .getString(mAppContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == -1L || TextUtils.isEmpty(secret) || TextUtils.isEmpty(cookie))
            return null;
        try {
            Mac hmac = Mac.getInstance("HmacSHA1");
            String key_str = secret + ":" + cookie;
            String str = String.valueOf(id) + ":" + androidId + ":" + String
                    .valueOf(time) + ":" + hash;
//			String str64 = Base64.encodeToString(str.getBytes("UTF-8"), Base64.NO_WRAP);

            SecretKeySpec key = new SecretKeySpec(key_str.getBytes("UTF-8"), hmac.getAlgorithm());
            hmac.init(key);

            byte[] toHash = Base64.encode(str.getBytes("UTF-8"), Base64.NO_WRAP);
            hmac.update(toHash);

            String sig = Base64.encodeToString(hmac.doFinal(), Base64.NO_WRAP);
            return sig;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
//	private long mDownloadId = 0L;

    public String generateAuthString() {
        long id = getUserId();
        SharedPreferences pref = mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0);
        String secret = pref.getString(PrefsFragment.PREFS_KEY_USER_SECRET, null);
        String cookie = pref.getString(PrefsFragment.PREFS_KEY_USER_SERVER_COOKIE, null);
        String hash = pref.getString(PrefsFragment.PREFS_KEY_USER_ORIG_CODE_HASH, null);
        long time = System.currentTimeMillis();
        String sig = getUserSig(time, secret, cookie, hash);
        if (id == -1L || sig == null || TextUtils.isEmpty(secret) || TextUtils.isEmpty(cookie))
            return null;
        try {
            String auth = String.valueOf(id) + ":" + hash + ":" + String.valueOf(time) + ":" + sig;
            String auth64 = "Basic " + Base64
                    .encodeToString(auth.getBytes("UTF-8"), Base64.NO_WRAP);

            return auth64;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void resetUserCredentials() {
        mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit()
                .putLong(PrefsFragment.PREFS_KEY_USER_ID, -1L)
                .putString(PrefsFragment.PREFS_KEY_USER_ORIG_CODE_HASH, null)
                .putString(PrefsFragment.PREFS_KEY_USER_SERVER_COOKIE, null)
                .putString(PrefsFragment.PREFS_KEY_USER_SECRET, null)
                .commit();
    }

    public void setUserCredentials(long id, String cookie, String authCode, String secret) {
//		if (BuildConfig.DEBUG)
//			authCode = "1234567890";
        String codeHash = getSha256Hash(authCode);
//		String pass = calcUserPass(id, secret, cookie, codeHash);
        mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit()
                .putLong(PrefsFragment.PREFS_KEY_USER_ID, id)
                .putString(PrefsFragment.PREFS_KEY_USER_ORIG_CODE_HASH, codeHash)
                .putString(PrefsFragment.PREFS_KEY_USER_SERVER_COOKIE, cookie)
                .putString(PrefsFragment.PREFS_KEY_USER_SECRET, secret)
//			.putString(PrefsFragment.PREFS_KEY_USER_XOR_KEY, pass)
                .commit();
    }

    public long dexCrc() {
        // fix dex_crc to version 23 (no access to backend code for now..)
        return 0xBE3E2E02L;

        //return mDexCrc;
    }

    public String getRemoteBaseUrl() {
        if (mRemoteBaseUrl == null) {
            byte[] p = mAppContext.getString(R.string.pass1).getBytes();
            byte[] dec = Base64.decode(REMOTE_BASE_ENCODED, Base64.NO_WRAP);
            int n = Math.min(dec.length, p.length);
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < n; i++) {
                out.append((char) (dec[i] ^ p[i]));
            }
            mRemoteBaseUrl = out.toString();
            mRemoteBaseUrl = REMOTE_BASE;
        }
        Log.e(TAG, mRemoteBaseUrl);
        return mRemoteBaseUrl;
    }

    public String getLanguage() {
        String lang = mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0)
                .getString(PrefsFragment.PREFS_KEY_CONTENT_LANGUAGE,
                        StaticContent.LANG_CODE_DEFAULT);
        if (lang.equals(StaticContent.LANG_CODE_DEFAULT)) {
            lang = StaticContent.getLanguageKey(Locale.getDefault());
            if (!StaticContent.AVAILABLE_LANGUAGES.contains(lang))
                lang = StaticContent.LANG_CODE_ENGLISH;
        }
        return lang;
    }

    public String[] getInfoTitles() {
        return mAppContext.getResources().getStringArray(R.array.info_section_titles);
    }
//	private static final String LOG_TAG = WildscanDataManager.class.getSimpleName();

    public String[] getInfoSections() {
        if (mInfoSections == null) {
            ContentValues langValues = null;
            Uri uri = null;
            final String selection = StaticContent._LANGUAGE + "=?";
            String[] selectionArgs = new String[1];
            Cursor c = null;
            if (mDefaultValues == null) {
                uri = WildscanDataProvider.getTableUri(StaticContent.TABLE_NAME);
                selectionArgs[0] = StaticContent.LANG_CODE_DEFAULT;
                c = mAppContext.getContentResolver()
                        .query(uri, null, selection, selectionArgs, null);
                if (c.moveToFirst()) {
                    mDefaultValues = new ContentValues(c.getColumnCount());
                    DatabaseUtils.cursorRowToContentValues(c, mDefaultValues);
                }
                c.close();
            }
            String lang = getLanguage();
            if (!lang.equals(StaticContent.LANG_CODE_DEFAULT)) {
                uri = WildscanDataProvider.getTableUri(StaticContent.TABLE_NAME);
                selectionArgs[0] = lang;
                c = mAppContext.getContentResolver()
                        .query(uri, null, selection, selectionArgs, null);
                if (c.moveToFirst()) {
                    langValues = new ContentValues(c.getColumnCount());
                    DatabaseUtils.cursorRowToContentValues(c, langValues);
                }
                c.close();
            }

            ArrayList<String> res = new ArrayList<String>(mDefaultValues.size());
            for (String key : INFO_SECTION_KEYS) {
                String val = langValues == null ? null : langValues.getAsString(key);
                if (TextUtils.isEmpty(val))
                    val = mDefaultValues.getAsString(key);
                if (!TextUtils.isEmpty(val)) {
                    try {
                        val = new String(Base64.decode(val, Base64.DEFAULT), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                res.add(val);
            }
            if (!res.isEmpty()) {
                mInfoSections = new String[res.size()];
                res.toArray(mInfoSections);
            }
        }
        return mInfoSections;
    }

    public void resetInfoLanguage() {
        final Uri queryUri = WildscanDataProvider.getTableUri(Incidents.TABLE_NAME);
        mAppContext.getContentResolver().notifyChange(queryUri, null, false);

        mInfoSections = null;
    }

    public boolean expansionFilesDelivered() {
        String state = Environment.getExternalStorageState();
        if (state
                .equals(Environment.MEDIA_MOUNTED)) {  // || state.equals(Environment
			// .MEDIA_MOUNTED_READ_ONLY)) {
            String fileName = Helpers
                    .getExpansionAPKFileName(mAppContext, true, APK_MAIN_EXPANSION_FILE_VERSION);
            if (Helpers.doesFileExist(mAppContext, fileName, APK_MAIN_EXPANSION_FILE_SIZE, false))
                return true;
        }
        return false;
    }

    public synchronized InputStream expansionFileGetInputStream(String path) throws IOException {
        if (mApkExpSupport == null) {
            mApkExpSupport = APKExpansionSupport
                    .getAPKExpansionZipFile(mAppContext, APK_MAIN_EXPANSION_FILE_VERSION, -1);
        }
        if (mApkExpSupport != null) {
            return mApkExpSupport.getInputStream(path);
        }
        return null;
    }

    public synchronized AssetFileDescriptor expansionFileGetFD(String path) throws IOException {
        if (mApkExpSupport == null) {
            mApkExpSupport = APKExpansionSupport
                    .getAPKExpansionZipFile(mAppContext, APK_MAIN_EXPANSION_FILE_VERSION, -1);
        }
        if (mApkExpSupport != null) {
            return mApkExpSupport.getAssetFileDescriptor(path);
        }
        return null;
    }

    private Account getSyncAccount() {
        final String authority = mAppContext.getString(R.string.sync_provider_authorities);
        Account account = new Account("SyncAccount", "org.freeland.wildscan.data.sync");
        AccountManager am = (AccountManager) mAppContext.getSystemService(Context.ACCOUNT_SERVICE);
        if (am.addAccountExplicitly(account, null, null)) {
            ContentResolver.setIsSyncable(account, authority, 1);
        }
        /*
        if (ContentResolver.getPeriodicSyncs(account, authority).isEmpty()) {
            Bundle extras = new Bundle();
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true);
            ContentResolver.addPeriodicSync(account, authority, extras, SYNC_FREQ);
        }
        */
        return account;
    }

    public void requestSync(SyncProgressListener caller, boolean eventsOnly, boolean firstTime) {

        mSyncReciever = new SyncProgressReciever(caller);
        IntentFilter f = new IntentFilter(WildscanSyncAdapter.SYNC_PROGRESS_ACTION);
        LocalBroadcastManager.getInstance(mAppContext).registerReceiver(mSyncReciever, f);

        final String authority = mAppContext.getString(R.string.sync_provider_authorities);
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true);
        extras.putBoolean(WildscanSyncAdapter.KEY_EVENTS_ONLY, eventsOnly);
        extras.putBoolean(WildscanSyncAdapter.KEY_FIRST_TIME, firstTime);
        Account account = getSyncAccount();

        ContentResolver.requestSync(account, authority, extras);
    }

    //private HashMap<CharSequence,Integer> mSpeciesNameToId = new HashMap<CharSequence,Integer>();
    private void setupSpeciesList(String filter) {
//    	String[] columns = { Species._ID, Species._S_COMMON_NAME, Species._S_SCIENTIFIC_NAME };
        String[] columns = {Species._ID, Species._S_COMMON_NAME, Species._S_KNOWN_AS};

        if (mSpeciesAutocompleteList == null)
            mSpeciesAutocompleteList = new ArrayList<Pair<String, Integer>>();
        if (mSpeciesAutocompleteListAlsoKnownAs == null)
            mSpeciesAutocompleteListAlsoKnownAs = new ArrayList<Pair<String, Integer>>();

        mSpeciesAutocompleteList.clear();
        mSpeciesAutocompleteListAlsoKnownAs.clear();
        //mSpeciesNameToId.clear();

        String selection = Util.addLangToSelection(sDataManager.mAppContext, filter,
                SpeciesTranslations._S_LANGUAGE);
        selection = Util.addRegionSelection(sDataManager.mAppContext, selection);
        final Uri queryUri = WildscanDataProvider.getTableUri(Species.TABLE_NAME);
        Cursor c = mAppContext.getContentResolver().query(queryUri, columns, selection, null, null);

        if (c.moveToFirst()) {
            int col_idx_id = c.getColumnIndex(Species._ID),
                    col_idx_name = c.getColumnIndex(Species._S_COMMON_NAME),
                    col_idx_known_as = c.getColumnIndex(Species._S_KNOWN_AS);
            //,col_idx_sc_name = c.getColumnIndex(Species._S_SCIENTIFIC_NAME);
            do {
                String name = c.getString(col_idx_name);
                String knownAs = c.getString(col_idx_known_as);
                Integer id = c.getInt(col_idx_id);
                if (name != null) {
                    mSpeciesAutocompleteList.add(new Pair<String, Integer>(name, id));
                    //mSpeciesNameToId.put(name, id);
                    if (knownAs != null)
                        mSpeciesAutocompleteListAlsoKnownAs
                                .add(new Pair<String, Integer>(knownAs, id));
                    else
                        mSpeciesAutocompleteListAlsoKnownAs.add(new Pair<String, Integer>("", id));

                }


//    			mSpeciesAutocompleteList.add(c.getString(col_idx_sc_name));
            } while (c.moveToNext());
        }
        c.close();

        ArrayList<Pair<String,Integer>> tempSpeciesList = new ArrayList<>(mSpeciesAutocompleteList);
        ArrayList<Pair<String,Integer>> tempSpeciesKnownAsList = new ArrayList<>(mSpeciesAutocompleteListAlsoKnownAs);
        mSpeciesAutocompleteListAlsoKnownAs.clear();

        Collections.sort(mSpeciesAutocompleteList, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> lhs, Pair<String, Integer> rhs) {
                return lhs.first.compareTo(rhs.first);
            }
        });

        for(Pair<String,Integer> item : mSpeciesAutocompleteList){
            int index = tempSpeciesList.indexOf(item);
            mSpeciesAutocompleteListAlsoKnownAs.add(tempSpeciesKnownAsList.get(index));
        }

    }

    //private HashMap<String,Integer> mContactNameToId = new HashMap<String,Integer>();
    //private HashMap<String,Integer> mContactCapToId = new HashMap<String,Integer>();
    private void setupContactsList() {
        String[] columns = {Contacts._ID, Contacts._NAME, Contacts._S_SPECIALCAPACITY_NOTE,
				Contacts._COUNTRY};
//    	String[] columns = { Contacts._ID, Contacts._NAME };

        if (mContactsAutocompleteList == null)
            mContactsAutocompleteList = new ArrayList<Pair<String, Integer>>();
        mContactsAutocompleteList.clear();
        //mContactNameToId.clear();
        //mContactCapToId.clear();

        final Uri queryUri = WildscanDataProvider.getTableUri(Contacts.TABLE_NAME);
        String selection = Util.addLangToSelection(sDataManager.mAppContext, null,
                ContactsTranslations._S_LANGUAGE);
        selection = Util.addRegionSelection(sDataManager.mAppContext, selection);
        Cursor c = mAppContext.getContentResolver().query(queryUri, columns, selection, null, null);

        if (c.moveToFirst()) {
            int col_idx_id = c.getColumnIndex(Contacts._ID),
                    col_idx_name = c.getColumnIndex(Contacts._NAME),
                    col_idx_capacity = c.getColumnIndex(Contacts._S_SPECIALCAPACITY_NOTE);
            //, col_idx_country = c.getColumnIndex(Contacts._COUNTRY);
            do {
                String name = c.getString(col_idx_name),
                        cap = c.getString(col_idx_capacity);
                Integer id = c.getInt(col_idx_id);
                if (name != null) {
                    mContactsAutocompleteList.add(new Pair<String, Integer>(name, id));
                    //mContactNameToId.put(Html.fromHtml(name), id);
                }
                if (cap != null) {
                    mContactsAutocompleteList.add(new Pair<String, Integer>(cap, id));
                    //mContactCapToId.put(Html.fromHtml(cap), id);
                }
//    			mContactsAutocompleteList.add(c.getString(col_idx_country));
            } while (c.moveToNext());
        }
        c.close();

        Collections.sort(mContactsAutocompleteList, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> lhs, Pair<String, Integer> rhs) {
                return lhs.first.compareTo(rhs.first);
            }
        });
    }

//	private static class CompCharSequence implements Comparator<CharSequence> {
//
//		@Override
//		public int compare(CharSequence lhs, CharSequence rhs) {
//			return lhs.toString().compareTo(rhs.toString());
//		}
//		
//	}

    public interface SyncProgressListener {
        public void onSyncComplete(boolean canceled);

        public void onSyncProgress(String msg);
    }

    public static class AppReplacedReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                return;
            if (intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                context.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0)
                        .edit()
                        .putLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0L)
                        .putLong(PrefsFragment.PREFS_KEY_LAST_EVENTS_SYNC, 0L)
                        .commit();
                //WildscanDataManager.getInstance(context).requestSync(null, false, true);
            }
        }
    }

    private class SyncProgressReciever extends BroadcastReceiver implements OnCancelListener {
        final SyncProgressListener mCaller;

        public SyncProgressReciever(SyncProgressListener mCaller) {
            super();
            this.mCaller = mCaller;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            {
                String msg = extras.getString(WildscanSyncAdapter.KEY_SYNC_PROGRESS_MESSAGE);
                if (msg.equals(WildscanSyncAdapter.SYNC_DONE))
                    finish(false);
                else if (mCaller != null)
                    mCaller.onSyncProgress(msg);
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            final String authority = mAppContext.getString(R.string.sync_provider_authorities);
            Account account = getSyncAccount();
            ContentResolver.cancelSync(account, authority);
            finish(true);
        }

        private void finish(boolean canceled) {
            if (!canceled && mSpeciesAutocompleteList != null)
                setupSpeciesList(null);
            if (!canceled && mContactsAutocompleteList != null)
                setupContactsList();
//			if (!canceled && mAvailableLanguageIds!=null)
//				setupAvailableLanguages();
            if (mCaller != null)
                mCaller.onSyncComplete(canceled);
            LocalBroadcastManager.getInstance(mAppContext).unregisterReceiver(mSyncReciever);
//			mSyncProgress = null;
            mSyncReciever = null;
        }
    }

    /**
     * @author Noam
     */
    public class WildscanSQLiteOpenHelper extends SQLiteOpenHelper {


        private static final String DATABASE_NAME = "wildscan";
        private static final int DATABASE_VERSION = 5;
        protected SQLiteDatabase database;
        private boolean mInitialized = true;

        public WildscanSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mInitialized = (mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0)
                    .getLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0L) > 0);
        }

        /* (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            database = db;
            database.execSQL(Species.CREATE_TABLE);
            database.execSQL(SpeciesImages.CREATE_TABLE);
            database.execSQL(Incidents.CREATE_TABLE);
            database.execSQL(Contacts.CREATE_TABLE);
            database.execSQL(StaticContent.CREATE_TABLE);
            database.execSQL(SpeciesTranslations.CREATE_TABLE);
            database.execSQL(ContactsTranslations.CREATE_TABLE);
            database.execSQL(StaticContent.setupDefaultContent(mAppContext));
        }

        /* (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            database = db;
            if (oldVersion == 1 && newVersion == 2) {
                database.execSQL(StaticContent.CREATE_TABLE);
                database.execSQL(StaticContent.setupDefaultContent(mAppContext));
                mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit()
                        .putLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0L).commit();
            } else if (oldVersion == 2 && (newVersion == 3 || newVersion == 4)) {
                database.execSQL("DROP TABLE " + Contacts.TABLE_NAME);
                database.execSQL(Contacts.CREATE_TABLE);
                database.execSQL(SpeciesTranslations.CREATE_TABLE);
                database.execSQL(ContactsTranslations.CREATE_TABLE);
                mAppContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit()
                        .putLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0L).commit();
            } else if (oldVersion == 3 && newVersion == 4) {
                database.execSQL("DROP TABLE " + ContactsTranslations.TABLE_NAME);
                database.execSQL(ContactsTranslations.CREATE_TABLE);
                database.execSQL("DROP TABLE " + SpeciesTranslations.TABLE_NAME);
                database.execSQL(SpeciesTranslations.CREATE_TABLE);
            } else if (oldVersion == 4 && newVersion == 5) {
                database.execSQL("DROP TABLE " + Species.TABLE_NAME);
                database.execSQL(Species.CREATE_TABLE);
                database.execSQL("DROP TABLE " + SpeciesImages.TABLE_NAME);
                database.execSQL(SpeciesImages.CREATE_TABLE);
                database.execSQL("DROP TABLE " + Incidents.TABLE_NAME);
                database.execSQL(Incidents.CREATE_TABLE);
                database.execSQL("DROP TABLE " + Contacts.TABLE_NAME);
                database.execSQL(Contacts.CREATE_TABLE);

            } else {
                throw new IllegalArgumentException(
                        String.format("DB upgrade not supported from v %d to v %d", oldVersion,
                                newVersion));
//				database.execSQL("DROP TABLE IF EXISTS " + Species.TABLE_NAME + ";");
//				database.execSQL(Species.CREATE_TABLE);
//				database.execSQL("DROP TABLE IF EXISTS " + SpeciesImages.TABLE_NAME + ";");
//	            database.execSQL(SpeciesImages.CREATE_TABLE);
//				database.execSQL("DROP TABLE IF EXISTS " + Incidents.TABLE_NAME + ";");
//	            database.execSQL(Incidents.CREATE_TABLE);
//				database.execSQL("DROP TABLE IF EXISTS " + Contacts.TABLE_NAME + ";");
//	            database.execSQL(Contacts.CREATE_TABLE);
//				database.execSQL("DROP TABLE IF EXISTS " + StaticContent.TABLE_NAME + ";");
//	            database.execSQL(StaticContent.CREATE_TABLE);
            }
        }

        public SQLiteDatabase getDB() {
            return database;
        }

        public boolean initialized() {
            return mInitialized;
        }

        public void doneInit() {
            mInitialized = true;
        }

        public void InitialData() {
        }
    }
}
