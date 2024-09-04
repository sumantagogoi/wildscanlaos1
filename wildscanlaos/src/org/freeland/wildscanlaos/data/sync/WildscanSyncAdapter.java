package org.freeland.wildscanlaos.data.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.core.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.freeland.wildscanlaos.PrefsFragment;
import org.freeland.wildscanlaos.R;
import org.freeland.wildscanlaos.ReportWizardActivity;
import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.Contacts;
import org.freeland.wildscanlaos.data.contract.ContactsTranslations;
import org.freeland.wildscanlaos.data.contract.Incidents;
import org.freeland.wildscanlaos.data.contract.Species;
import org.freeland.wildscanlaos.data.contract.SpeciesImages;
import org.freeland.wildscanlaos.data.contract.SpeciesTranslations;
import org.freeland.wildscanlaos.data.contract.StaticContent;
import org.freeland.wildscanlaos.data.provider.WildscanDataProvider;
import org.freeland.wildscanlaos.util.AppConstants;
import org.freeland.wildscanlaos.util.AppPreferences;
import org.freeland.wildscanlaos.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.freeland.wildscanlaos.data.provider.WildscanDataProvider.AUTHORITY;

//import org.apache.http.client.methods.HttpGet;

public class WildscanSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String KEY_EVENTS_ONLY = "org.freeland.wildscanlaos.data.sync.EventsOnly";
    public static final String KEY_FIRST_TIME = "org.freeland.wildscanlaos.data.sync.FirstTime";
    public static final String KEY_SYNC_MARKER = "org.freeland.wildscanlaos.data.sync.SyncMarker";
    public static final String SYNC_PROGRESS_ACTION = "org.freeland.wildscanlaos.data.sync.SyncProgress";
    public static final String KEY_SYNC_PROGRESS_MESSAGE = "org.freeland.wildscanlaos.data.sync.SyncStatus";
    public static final int SYNC_UPLOADING_REPORTS = 0;
    public static final int SYNC_SYNCING_STATIC = 1;
    public static final int SYNC_SYNCING_CONTACTS = 2;
    public static final int SYNC_SYNCING_SPECIES = 3;
    public static final int SYNC_SYNCING_SPECIES_IMAGES = 4;
    public static final int SYNC_SYNCING_INCIDENTS = 5;
    public static final int SYNC_SYNCING_TRANSLATIONS = 6;
    public static final int SYNC_SYNCING_PHOTOS = 7;
    public static final int SYNC_COMPLETED = 8;
    public static final String SYNC_DONE = "@@done@@";
    public static final String SYNC_ERROR = "@@error@@";
    final static String LOG_TAG = "WildscanSyncAdapter";
    static final int SYNC_MAX_INCIDENTS_TO_SYNC = 50;
    static final String MSG_FORMAT = "%s (%d%%)";
    private static final int SYNC_TBL_CONTACTS = 0;
    private static final int SYNC_TBL_SPECIES_IMAGES = 1;
    private static final int SYNC_TBL_SPECIES = 2;
    private static final int SYNC_TBL_INCIDENTS = 3;
    private static final int SYNC_TBL_TRANSLATIONS_SPECIES = 4;
    private static final int SYNC_TBL_TRANSLATIONS_CONTACTS = 5;
    //private static final int SYNC_TBL_STATIC_CONTENTS = 6;
    private static final int SYNC_N_TBLS = 6;
    private static final String[] SYNC_TBL_NAMES = {Contacts.TABLE_NAME, SpeciesImages.TABLE_NAME, Species.TABLE_NAME, Incidents.TABLE_NAME, SpeciesTranslations.TABLE_NAME, ContactsTranslations.TABLE_NAME};
    private static final String[] SYNC_TBL_ID_FIELD = {Contacts._ID, SpeciesImages._S_SPECIES_ID, Species._ID, Incidents._ID, SpeciesTranslations._S_SPECIES_ID, ContactsTranslations._S_CONTACT_ID};
    private static final String[] SYNC_TBL_LANG_FIELD = {ContactsTranslations._S_LANGUAGE, null, SpeciesTranslations._S_LANGUAGE, null, null, null};
    private static final String[] SYNC_TBL_REMOTE_NAMES = {Contacts.TABLE_NAME, Species.TABLE_NAME, Species.TABLE_NAME, Incidents.TABLE_NAME, Species.TABLE_NAME, Contacts.TABLE_NAME};
    private static final String[] SYNC_TBL_REMOTE_ID_FIELD = {Contacts._S_ID, Species._S_ID, Species._S_ID, Incidents._S_ID, Species._S_ID, Contacts._S_ID};
    public final String[] SYNC_PROGRESS_MESSAGES;
    private final Context mContext;
    private final AccountManager mAccountManager;
    boolean firstTime = false;
    private JsonArrayReader reader = null;
    private SQLiteDatabase mDatabase;
    private WildscanDataManager mDataManager;
    private boolean mShouldBraodcastProgress = false;
    //private final String TRUSTSTORE_PASS = "9oB8y2yd17U1nE7217Sq2UoX80ol0I82MB617E2e";
    //private final String KEYSTORE_PASS = "97Q75IbCd47sWY3awF7662IH235243937z1tR74V";
    //private ThreadSafeClientConnManager mConnManager;
    private HttpClient mHttpClient;
    private String mCurrSpeciesIds = null;
    public WildscanSyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public WildscanSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
        SYNC_PROGRESS_MESSAGES = mContext.getResources().getStringArray(R.array.sync_progress);
        mHttpClient = Util.getHttpClient(mContext);
    }

    public String retrieveCurrentIdsList(String table, String field) throws ClientProtocolException, IOException {
//		final String s = "SELECT " + field,
//				f = "FROM " + table,
//				w = Incidents.TABLE_NAME.equals(table) ?
//						" ORDER BY " + Incidents._S_INCIDENT_DATE + " DESC LIMIT " + String.valueOf(WildscanSyncAdapter.SYNC_MAX_INCIDENTS_TO_SYNC) :
//						"";


//		String url = Uri.parse(WildscanDataManager.REMOTE_QUERY_IDS).buildUpon()
//				.appendQueryParameter("table", table).build().toString();
//				.appendQueryParameter("s", s)
//				.appendQueryParameter("f", f)
//				.appendQueryParameter("w", w).build().toString();
        String res = "";

        List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
        requestParams.add(new BasicNameValuePair("table", table));
        HttpPost httpRequest = new HttpPost(AppConstants.REMOTE_SERVER_URL + WildscanDataManager.REMOTE_PHP_QUERY_IDS);
        httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
        httpRequest.addHeader("Cache-Control", "no-cache");
        HttpResponse response = mHttpClient.execute(httpRequest);
        int sc = response.getStatusLine().getStatusCode();
        InputStream is = response.getEntity().getContent();
        String ret = Util.readResponse(is);
        is.close();
        if (sc == 200) {
            StringReader isr = new StringReader(ret);//  InputStreamReader(new BufferedInputStream(response.getEntity().getContent()), "utf-8");
            JsonReader reader = new JsonReader(isr);
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                reader.nextName();
                if (reader.peek() != JsonToken.NULL) {
                    res += reader.nextString();
                } else {
                    reader.nextNull();
                }
                reader.endObject();
                if (reader.hasNext())
                    res += ",";
            }
            reader.endArray();
            reader.close();
            isr.close();
        }
        Log.i("UpdatedIds: ", res);
        return res;
    }


    private void clearcache() {

    }

    private void uploadPendingReports() {
//		long id = WildscanDataManager.getInstance(mContext).getUserId();
//		String pass = WildscanDataManager.getInstance(mContext).getUserPass();
        // TODO: construct authorization header with id and pass
//		if (id==-1L)
//			return;
        // Disable to solve pending report upload issue

        boolean saveReport = mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getBoolean(PrefsFragment.PREFS_KEY_SAVE_REPORT_BAK, false);

        String dir = Environment.getExternalStorageDirectory()
                + File.separator + "Wildscan"
                + File.separator + ReportWizardActivity.REPORTS_FOLDER
                + File.separator + ReportWizardActivity.REPORTS_PENDING_FOLDER;
        String bakdir = Environment.getExternalStorageDirectory()
                + File.separator + "Wildscan"
                + File.separator + ReportWizardActivity.REPORTS_FOLDER
                + File.separator + ReportWizardActivity.REPORTS_BACKUP_FOLDER;
        File folder = new File(dir);

        if (folder.exists()) {
            char[] buffer = new char[1024];
            for (File pending : folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".json");
                }
            })) {
                try {
                    StringWriter sw = new StringWriter();
                    FileReader fr = new FileReader(pending);
                    int count = 0;
                    while ((count = fr.read(buffer)) != -1) {
                        sw.write(buffer, 0, count);
                    }
                    fr.close();
                    if (Util.uploadReportJson(mContext, sw.toString())) {
                        if (saveReport) {
                            File bakFolder = new File(bakdir);
                            bakFolder.mkdirs();
                            FileWriter fw = new FileWriter(new File(bakFolder, pending.getName()));
                            fw.write(sw.toString());
                            fw.close();
                        }
                        pending.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ContentValues convertRemoteFieldsToLocal(ContentValues remote, int tblIdx) {
        switch (tblIdx) {
            case SYNC_TBL_CONTACTS:
                return Contacts.convertRemoteFieldsToLocal(mContext, remote);
            case SYNC_TBL_SPECIES:
                return Species.convertRemoteFieldsToLocal(mContext, remote);
            case SYNC_TBL_SPECIES_IMAGES:
                return remote;
            case SYNC_TBL_INCIDENTS:
                return Incidents.convertRemoteFieldsToLocal(mContext, remote);
            case SYNC_TBL_TRANSLATIONS_SPECIES:
                return SpeciesTranslations.convertRemoteFieldsToLocal(mContext, remote);
            case SYNC_TBL_TRANSLATIONS_CONTACTS:
                return ContactsTranslations.convertRemoteFieldsToLocal(mContext, remote);
        }

        return null;
    }

    private String retrieveCurrentIdsList(int tblIdx) throws ClientProtocolException, IOException {
        String list = null;
        switch (tblIdx) {
            case SYNC_TBL_CONTACTS:
            case SYNC_TBL_INCIDENTS:
            case SYNC_TBL_TRANSLATIONS_SPECIES:
            case SYNC_TBL_TRANSLATIONS_CONTACTS:
                list = retrieveCurrentIdsList(SYNC_TBL_REMOTE_NAMES[tblIdx], SYNC_TBL_REMOTE_ID_FIELD[tblIdx]);
                break;
            case SYNC_TBL_SPECIES:
            case SYNC_TBL_SPECIES_IMAGES:
                if (mCurrSpeciesIds == null) {
                    list = retrieveCurrentIdsList(SYNC_TBL_REMOTE_NAMES[tblIdx], SYNC_TBL_REMOTE_ID_FIELD[tblIdx]);
                    mCurrSpeciesIds = list;
                } else {
                    list = mCurrSpeciesIds;
                    mCurrSpeciesIds = null;
                }
        }
        return list;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        boolean ok = true, eventsOnly = false;
        Uri tableUri = null;

        //sendProgressBroadcast(SYNC_UPLOADING_REPORTS);
        //uploadPendingReports();

        if (extras != null) {
            eventsOnly = extras.getBoolean(KEY_EVENTS_ONLY, false);
            firstTime = extras.getBoolean(KEY_FIRST_TIME, false);
            mShouldBraodcastProgress = extras.containsKey(ContentResolver.SYNC_EXTRAS_MANUAL);
        }

        tableUri = WildscanDataProvider.getTableUri(StaticContent.TABLE_NAME);

        mDataManager = WildscanDataManager.getInstance(mContext);

//		synchronized(mDataManager) { mDataManager.isSyncing = true; }

        mDatabase = mDataManager.getWritableDatabase();

        long lastSync = firstTime ? 0 : getSyncMarker(account);//mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME,0).getLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0L);


//        if (eventsOnly) {
        /*long lastEventsSync = mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getLong(PrefsFragment.PREFS_KEY_LAST_EVENTS_SYNC, lastSync);
        sendProgressBroadcast(SYNC_SYNCING_INCIDENTS);
        ok = syncTable(lastEventsSync, SYNC_TBL_INCIDENTS, syncResult);
        if (ok)
            lastEventsSync = System.currentTimeMillis() / 1000L;
        mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit().putLong(PrefsFragment.PREFS_KEY_LAST_EVENTS_SYNC, lastEventsSync).commit();
//        } else {
*/
        ok = syncTables(lastSync, syncResult, firstTime);

        if (ok) {
            lastSync = System.currentTimeMillis() / 1000L;
            mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit().putLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, lastSync).commit();
            mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit().putLong(PrefsFragment.PREFS_KEY_LAST_EVENTS_SYNC, lastSync).commit();
        }

        sendProgressBroadcast(SYNC_SYNCING_PHOTOS);
        ok = ok && syncImages(syncResult, firstTime);
//        }
        setSyncMarker(account, lastSync);
        sendProgressBroadcast(SYNC_COMPLETED);

        mDatabase = null;
        mDataManager = null;
//		synchronized(mDataManager) { mDataManager.isSyncing = false; }
    }

    private boolean syncTables(long lastSync, SyncResult syncResult, boolean firstTime) {
        int status = SYNC_SYNCING_STATIC;
        sendProgressBroadcast(status++);
        boolean ret = syncStaticContent(lastSync, syncResult, firstTime);
        for (int tbl = 0; tbl < SYNC_N_TBLS; tbl++) {
            sendProgressBroadcast(status++);
            ret = ret && syncTable(lastSync, tbl, syncResult, firstTime);
        }
        return ret;
    }

    private boolean syncStaticContent(long lastSync, SyncResult syncResult, boolean firstTime) {
        Uri tableUri = null;
        JsonArrayReader reader;
        boolean changed = false, ok = true;

        tableUri = WildscanDataProvider.getTableUri(StaticContent.TABLE_NAME);

        //int err =

        try {
            Map<String, ContentValues> content = new HashMap<String, ContentValues>();

            reader = new JsonArrayReader(mContext, StaticContent.TABLE_NAME, lastSync, firstTime);
            while (reader.hasNext()) {
                ContentValues cv = reader.readObject();
                String lang = cv.getAsString(StaticContent._LANGUAGE);
                ContentValues row = content.get(lang);
                if (row == null) {
                    row = new ContentValues();
                    row.put(StaticContent._LANGUAGE, lang);
                }
                row.put(cv.getAsString(StaticContent._S_TYPE), cv.getAsString(StaticContent._S_CONTENT));
                content.put(lang, row);
            }
            reader.close();
            if (!content.isEmpty()) {
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                //ArrayList<String> langs = new ArrayList<String>(content.size());
                for (ContentValues remote : content.values()) {
                    ContentValues toInsert = StaticContent.convertRemoteFieldsToLocal(mContext, remote);
                    //langs.add(toInsert.getAsString(StaticContent._LANGUAGE));
                    ops.add(ContentProviderOperation.newInsert(tableUri).withValues(toInsert).build());
                    syncResult.stats.numInserts++;
                }
                ContentProviderResult[] results = mContext.getContentResolver().applyBatch(WildscanDataProvider.AUTHORITY, ops);
                for (int i = 0; i < results.length; i++) {
                    if (results[i].uri == null)
                        ok = false;
                    else {
                        changed = true;
                        //StaticContent.sLanguageNamesMap.put(langs.get(i), Integer.valueOf(results[i].uri.getLastPathSegment()));
                    }
                }
            }
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            ok = false;
            e.printStackTrace();
        } catch (RemoteException e) {
            syncResult.databaseError = true;
            ok = false;
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            syncResult.databaseError = true;
            ok = false;
            e.printStackTrace();
        }
        if (changed) {
            mDataManager.resetInfoLanguage();
            mContext.getContentResolver().notifyChange(tableUri, null, false);
        }
        return ok;
    }


    private boolean syncTable(long lastSync, int tblIdx, SyncResult syncResult, boolean firstTime) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String toDelete;
        Uri tableUri = null;
        boolean changed = false, ok = true;

        //long lastSync = mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME,0).getLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0L);

        try {
            toDelete = SYNC_TBL_ID_FIELD[tblIdx] + " NOT IN (" + retrieveCurrentIdsList(tblIdx) + ")";
            // TODO: need to consider deleted photos per species - verify existence in remote (no _id field..)
            tableUri = WildscanDataProvider.getTableUri(SYNC_TBL_NAMES[tblIdx]);
            ops.add(ContentProviderOperation.newDelete(tableUri).withSelection(toDelete, null).build());
            syncResult.stats.numDeletes++;


            //Todo: get ContentValues here...............

            reader = new JsonArrayReader(mContext, SYNC_TBL_NAMES[tblIdx], lastSync, firstTime);

            if (SYNC_TBL_NAMES[tblIdx].equalsIgnoreCase(Contacts.TABLE_NAME)) {
                int size = reader.getContactsList().size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        ContentValues toInsert = Contacts.convertRemoteContactsToLocal(mContext, reader.getContactsList().get(i));
                        toInsert.put("_avatar", "/" + toInsert.getAsString("_avatar"));
                        if (SYNC_TBL_LANG_FIELD[tblIdx] != null)
                            toInsert.put(SYNC_TBL_LANG_FIELD[tblIdx], StaticContent.LANG_CODE_ENGLISH);
                        ops.add(ContentProviderOperation.newInsert(tableUri).withValues(toInsert).build());
                        syncResult.stats.numInserts++;
                    }
                    AppPreferences.setContactsTimespan(mContext);
                }
            } else if (SYNC_TBL_NAMES[tblIdx].equalsIgnoreCase(Incidents.TABLE_NAME)) {
                int size = reader.getEventsList().size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        ContentValues toInsert = Incidents.convertIncidentFieldsToLocal(mContext, reader.getEventsList().get(i));
                        if (SYNC_TBL_LANG_FIELD[tblIdx] != null)
                            toInsert.put(SYNC_TBL_LANG_FIELD[tblIdx], StaticContent.LANG_CODE_ENGLISH);
                        ops.add(ContentProviderOperation.newInsert(tableUri).withValues(toInsert).build());
                        syncResult.stats.numInserts++;
                    }
                    AppPreferences.setEventsTimespan(mContext);
                }
            } else if (SYNC_TBL_NAMES[tblIdx].equalsIgnoreCase(Species.TABLE_NAME)) {
                int size = reader.getSpeciesList().size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        ContentValues toInsert = Species.convertRemoteSpeciesToLocal(mContext, reader.getSpeciesList().get(i));
                        if (SYNC_TBL_LANG_FIELD[tblIdx] != null)
                            toInsert.put(SYNC_TBL_LANG_FIELD[tblIdx], StaticContent.LANG_CODE_ENGLISH);
                        ops.add(ContentProviderOperation.newInsert(tableUri).withValues(toInsert).build());
                        Log.i("i: ", String.valueOf(i));
                        syncResult.stats.numInserts++;
                    }
                    AppPreferences.setSpeciesTimespan(mContext);
                }
            } else if (SYNC_TBL_NAMES[tblIdx].equalsIgnoreCase(SpeciesImages.TABLE_NAME)) {
                int size = reader.getSpeciesImagesList().size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        ContentValues toInsert = SpeciesImages.convertRemoteFieldsToLocal(mContext, reader.getSpeciesImagesList().get(i));
                        if (SYNC_TBL_LANG_FIELD[tblIdx] != null)
                            toInsert.put(SYNC_TBL_LANG_FIELD[tblIdx], StaticContent.LANG_CODE_ENGLISH);
                        ops.add(ContentProviderOperation.newInsert(tableUri).withValues(toInsert).build());
                        syncResult.stats.numInserts++;
                    }
                }
                AppPreferences.setSpeciesImageTimespan(mContext);
            } else {
                while (reader.hasNext()) {
                    ContentValues cv = reader.readObject();
                    int count = cv.size();
                    int i = 0;
                    ContentValues toInsert = convertRemoteFieldsToLocal(cv, tblIdx);
                    if (SYNC_TBL_NAMES[tblIdx].equalsIgnoreCase(SpeciesImages.TABLE_NAME)) {
                        toInsert.put("path_image", "/uploads" + toInsert.getAsString("path_image"));
                    }
                    if (SYNC_TBL_LANG_FIELD[tblIdx] != null)
                        toInsert.put(SYNC_TBL_LANG_FIELD[tblIdx], StaticContent.LANG_CODE_ENGLISH);
                    ops.add(ContentProviderOperation.newInsert(tableUri).withValues(toInsert).build());
                    syncResult.stats.numInserts++;
                    /*
                    if (++i % 10 == 0) {
                        sendProgressBroadcast(tblIdx, 0.999 * i / count);
                    }
                    */
                }
                reader.close();
            }

            ContentProviderResult[] results = mContext.getContentResolver().applyBatch(AUTHORITY, ops);
            changed = results[0].count != 0;
            for (int i = 1; i < results.length && (ok && !changed); i++) {
                if (results[i].uri == null)
                    ok = false;
                else
                    changed = true;
            }
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            ok = false;
            e.printStackTrace();
        } catch (RemoteException e) {
            syncResult.databaseError = true;
            ok = false;
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            syncResult.databaseError = true;
            ok = false;
            e.printStackTrace();
        }
        if (changed)
            mContext.getContentResolver().notifyChange(tableUri, null, false);

        return ok;
    }

    private boolean syncImages(String table, String field, SyncResult syncResult, boolean isFirstTime) {
        final String localFolder = mContext.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getString(PrefsFragment.PREFS_KEY_DATA_FOLDER, null);
        Util.logInfo("localFolderPath", localFolder);
        String name = null;
        boolean ok = true, changed = false;
        int idx, i = 0, count;
        double done = 0;



        Cursor c = mDatabase.query(table, new String[]{field}, null, null, null, null, null);
        if (c.moveToFirst()) {
            count = c.getCount();
            idx = c.getColumnIndex(field);
            do {
                File f = null;
                String local = null;
                name = c.getString(idx);
                if (name != null) {
                    if (name.startsWith(File.separator))
                        name = name.substring(1);// omit initial '/'
                    AssetFileDescriptor fd = null;
                    //InputStream is = null;
                    try {
                        fd = mDataManager.expansionFileGetFD(name);
                        //is = mDataManager.expansionFileGetInputStream(name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fd == null || !fd.getFileDescriptor().valid()) {
                            //if (is==null) {
                            local = localFolder + File.separator + name;
                            f = new File(local);
                        }
                        if (fd != null)
                            try {
                                fd.close();
                            } catch (IOException e) {
                            }
                        //if (is!=null)
                        //	try { is.close(); } catch (IOException e) {}
                    }
                }
                if (f != null && !f.exists()) {
                    File p = f.getParentFile();
                    if (!p.exists())
                        p.mkdirs();
                 /*   if (Util.httpDownloadFile(mDataManager.getRemoteBaseUrl() + name, local)) {
                        changed = true;
                    } else {
                        syncResult.stats.numIoExceptions++;
                        ok = false;
                    }*/
                    if (i < reader.getContactsList().size() && table.equalsIgnoreCase(Contacts.TABLE_NAME)) {
                        if (Util.httpDownloadFile(reader.getContactsList().get(i).getS3Url() +
                                reader.getContactsList().get(i).getAvatar(), local)) {
                            changed = true;
                        } else {
                            syncResult.stats.numIoExceptions++;
                            ok = false;
                        }
                    }

                    if ((table.equalsIgnoreCase(SpeciesImages
                            .TABLE_NAME) )) {
                        if (Util.httpDownloadFile(AppConstants.REMOTE_BASE_IMAGE_URL +
                                name.replace("uploads/",""), local)) {
                            changed = true;
                        } else {
                            syncResult.stats.numIoExceptions++;
                            ok = false;
                        }
                    }

                   /* if ( i < reader.getSpeciesImagesList().size() && table.equalsIgnoreCase(SpeciesImages.TABLE_NAME)) {
                        if (Util.httpDownloadFile(AppPreferences.getImageBaseLink(mContext) +
                                reader.getSpeciesImagesList().get(i).getImagePath(), local)) {
                            changed = true;
                        } else {
                            syncResult.stats.numIoExceptions++;
                            ok = false;
                        }
                    }*/

                }
                if (++i % 10 == 0) {
                    sendProgressBroadcast(SYNC_SYNCING_PHOTOS, 0.999 * i / count);
                }

            } while (c.moveToNext());
        }
        c.close();
        if (changed) {
            mContext.getContentResolver().notifyChange(WildscanDataProvider.getTableUri(table), null, false);
        }
        return ok;
    }

    private boolean syncImages(SyncResult syncResult, boolean isFirstTime) {
        boolean ok = syncImages(Contacts.TABLE_NAME, Contacts._AVATAR, syncResult, isFirstTime);
        ok = syncImages(SpeciesImages.TABLE_NAME, SpeciesImages._S_PATH_IMAGE, syncResult, isFirstTime) && ok;
        return ok;
    }

    private long getSyncMarker(Account account) {
        String markerString = mAccountManager.getUserData(account, KEY_SYNC_MARKER);
        if (!TextUtils.isEmpty(markerString)) {
            Util.logInfo("LastSync: ", markerString);
            return Long.parseLong(markerString);
        }
        Util.logInfo("LastSync: ", "0");
        return 0;
    }

    private void setSyncMarker(Account account, long marker) {
        mAccountManager.setUserData(account, KEY_SYNC_MARKER, Long.toString(marker));
    }

    private void sendProgressBroadcast(int progress) {
        if (!mShouldBraodcastProgress)
            return;

        final String msg = progress < SYNC_COMPLETED ? SYNC_PROGRESS_MESSAGES[progress] : SYNC_DONE;

        Intent intent = new Intent();
        intent.setAction(SYNC_PROGRESS_ACTION);
        intent.putExtra(KEY_SYNC_PROGRESS_MESSAGE, msg);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void sendProgressBroadcast(int progress, double done) {
        if (!mShouldBraodcastProgress)
            return;

        final String msg = progress < SYNC_COMPLETED ? String.format(Locale.getDefault(), MSG_FORMAT, SYNC_PROGRESS_MESSAGES[progress], (int) (done * 100)) : SYNC_DONE;

        Intent intent = new Intent();
        intent.setAction(SYNC_PROGRESS_ACTION);
        intent.putExtra(KEY_SYNC_PROGRESS_MESSAGE, msg);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public static class SyncService extends Service {
//        private static final String TAG = "SyncService";

        private static final Object sSyncAdapterLock = new Object();
        private static WildscanSyncAdapter sSyncAdapter = null;

        /**
         */
        @Override
        public void onCreate() {
            super.onCreate();
//            Log.i(TAG, "Service created");
            synchronized (sSyncAdapterLock) {
                if (sSyncAdapter == null) {
                    sSyncAdapter = new WildscanSyncAdapter(getApplicationContext(), true);
                }
            }
        }

        @Override
        /**
         * Logging-only destructor.
         */
        public void onDestroy() {
            super.onDestroy();
//            Log.i(TAG, "Service destroyed");
        }

        /**
         * <p/>
         * <p>New sync requests will be sent directly to the SyncAdapter using this channel.
         *
         * @param intent Calling intent
         */
        @Override
        public IBinder onBind(Intent intent) {
            return sSyncAdapter.getSyncAdapterBinder();
        }
    }

}
