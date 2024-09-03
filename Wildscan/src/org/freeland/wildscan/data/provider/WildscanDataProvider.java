package org.freeland.wildscan.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import org.freeland.wildscan.data.WildscanDataManager;
import org.freeland.wildscan.data.contract.Contacts;
import org.freeland.wildscan.data.contract.ContactsTranslations;
import org.freeland.wildscan.data.contract.Incidents;
import org.freeland.wildscan.data.contract.Species;
import org.freeland.wildscan.data.contract.SpeciesImages;
import org.freeland.wildscan.data.contract.SpeciesTranslations;
import org.freeland.wildscan.data.contract.StaticContent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WildscanDataProvider extends ContentProvider {

    public static final String AUTHORITY = "org.freeland.wildscan.data.provider";
    static final String URI_TYPE_FORMAT = "vnd.android.cursor.%s/vnd." + AUTHORITY + ".%s";
    static final String URI_TYPE_ITEM = "item";
    static final String URI_TYPE_DIR = "dir";
    static final String CONTENT_SELECT_ROW = File.separator + "#";

    static final Map<String, Uri> sTableUris;
    private static final int STATIC = 0;
    private static final int STATIC_ID = 10;
    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 11;
    private static final int CONTACT_TRANSLATIONS = 111;
    private static final int SPECIES = 2;
    private static final int SPECIES_TRANSLATIONS = 211;
    private static final int SPECIES_ID = 12;
    private static final int SPECIES_IMAGES = 3;
    private static final int SPECIES_IMAGE_ID = 13;
    private static final int EVENTS = 4;
    private static final int EVENT_ID = 14;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sTableUris = new HashMap<String, Uri>();
        sTableUris.put(StaticContent.TABLE_NAME,
                Uri.parse("content://" + AUTHORITY + "/" + StaticContent.TABLE_NAME));
        sTableUris.put(Contacts.TABLE_NAME,
                Uri.parse("content://" + AUTHORITY + "/" + Contacts.TABLE_NAME));
        sTableUris.put(ContactsTranslations.TABLE_NAME,
                Uri.parse("content://" + AUTHORITY + "/" + ContactsTranslations.TABLE_NAME));
        sTableUris.put(Species.TABLE_NAME,
                Uri.parse("content://" + AUTHORITY + "/" + Species.TABLE_NAME));
        sTableUris.put(SpeciesImages.TABLE_NAME,
                Uri.parse("content://" + AUTHORITY + "/" + SpeciesImages.TABLE_NAME));
        sTableUris.put(SpeciesTranslations.TABLE_NAME,
                Uri.parse("content://" + AUTHORITY + "/" + SpeciesTranslations.TABLE_NAME));
        sTableUris.put(Incidents.TABLE_NAME,
                Uri.parse("content://" + AUTHORITY + "/" + Incidents.TABLE_NAME));
    }

    static {
        sUriMatcher.addURI(AUTHORITY, StaticContent.TABLE_NAME, STATIC);
        sUriMatcher.addURI(AUTHORITY, StaticContent.TABLE_NAME + CONTENT_SELECT_ROW, STATIC_ID);
        sUriMatcher.addURI(AUTHORITY, Contacts.TABLE_NAME, CONTACTS);
        sUriMatcher.addURI(AUTHORITY, Contacts.TABLE_NAME + CONTENT_SELECT_ROW, CONTACT_ID);
        sUriMatcher.addURI(AUTHORITY, ContactsTranslations.TABLE_NAME, CONTACT_TRANSLATIONS);
        sUriMatcher.addURI(AUTHORITY, Species.TABLE_NAME, SPECIES);
        sUriMatcher.addURI(AUTHORITY, Species.TABLE_NAME + CONTENT_SELECT_ROW, SPECIES_ID);
        sUriMatcher.addURI(AUTHORITY, SpeciesImages.TABLE_NAME, SPECIES_IMAGES);
        sUriMatcher
                .addURI(AUTHORITY, SpeciesImages.TABLE_NAME + CONTENT_SELECT_ROW, SPECIES_IMAGE_ID);
        sUriMatcher.addURI(AUTHORITY, SpeciesTranslations.TABLE_NAME, SPECIES_TRANSLATIONS);
        sUriMatcher.addURI(AUTHORITY, Incidents.TABLE_NAME, EVENTS);
        sUriMatcher.addURI(AUTHORITY, Incidents.TABLE_NAME + CONTENT_SELECT_ROW, EVENT_ID);
    }

    private WildscanDataManager mDataManager;
    private String mTable;
    private String mSelection;
    private int mMatch;

    public static Uri getTableUri(String table) {
        return sTableUris.get(table);
    }

    public static Uri getItemUri(String table, long id) {
        return ContentUris.withAppendedId(getTableUri(table), id);
    }

    @Override
    public String getType(Uri uri) {
        String type, table;
        switch (sUriMatcher.match(uri)) {
            case (STATIC):
                type = URI_TYPE_DIR;
                table = StaticContent.TABLE_NAME;
                break;
            case (STATIC_ID):
                type = URI_TYPE_ITEM;
                table = StaticContent.TABLE_NAME;
                break;
            case (CONTACTS):
                type = URI_TYPE_DIR;
                table = Contacts.TABLE_NAME;
                break;
            case (CONTACT_ID):
                type = URI_TYPE_ITEM;
                table = Contacts.TABLE_NAME;
                break;
            case (CONTACT_TRANSLATIONS):
                type = URI_TYPE_DIR;
                table = ContactsTranslations.TABLE_NAME;
                break;
            case (SPECIES):
                type = URI_TYPE_DIR;
                table = Species.TABLE_NAME;
                break;
            case (SPECIES_ID):
                type = URI_TYPE_ITEM;
                table = Species.TABLE_NAME;
                break;
            case (SPECIES_TRANSLATIONS):
                type = URI_TYPE_DIR;
                table = SpeciesTranslations.TABLE_NAME;
                break;
            case (SPECIES_IMAGES):
                type = URI_TYPE_DIR;
                table = Species.TABLE_NAME;
                break;
            case (SPECIES_IMAGE_ID):
                type = URI_TYPE_ITEM;
                table = Species.TABLE_NAME;
                break;
            case (EVENTS):
                type = URI_TYPE_DIR;
                table = Incidents.TABLE_NAME;
                break;
            case (EVENT_ID):
                type = URI_TYPE_ITEM;
                table = Incidents.TABLE_NAME;
                break;
            default:
                return null;
        }
        return String.format(URI_TYPE_FORMAT, type, table);
    }

    @Override
    public boolean onCreate() {
        mDataManager = WildscanDataManager.getInstance(getContext());
        if (mDataManager == null) {
            throw new IllegalStateException(
                    "Attempt to create WildscanDataProvider before DataManager initialized");
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDataManager.getReadableDatabase();
        performMatch(uri, selection);

        Cursor c;
//		String lang = mDataManager.getLanguage();
//		synchronized(mDataManager) { 
//			if (mDataManager.isSyncing) 
//				lang = StaticContent.LANG_CODE_ENGLISH; 
//		} 

        if (!TextUtils.isEmpty(mSelection) && ((mTable == Contacts.TABLE_NAME && mSelection
                .contains(ContactsTranslations._S_LANGUAGE)) ||
                (mTable == Species.TABLE_NAME && mSelection
                        .contains(SpeciesTranslations._S_LANGUAGE)))) {
//		if (lang != StaticContent.LANG_CODE_ENGLISH && (mTable==Contacts.TABLE_NAME ||
// mTable==Species.TABLE_NAME)) {
//		if (mTable==Contacts.TABLE_NAME || mTable==Species.TABLE_NAME) {
            c = queryWithTranslation(mTable, projection, mSelection, selectionArgs, null, null,
                    sortOrder);
        } else {
            c = db.query(mTable, projection, mSelection, selectionArgs, null, null, sortOrder);
        }
        // Note: Notification URI must be manually set here for loaders to correctly
        // register ContentObservers.
        Context ctx = getContext();
        assert ctx != null;
        c.setNotificationUri(ctx.getContentResolver(), uri);
        return c;
    }

    private synchronized Cursor queryWithTranslation(String table, String[] columns,
                                                     String selection, String[] selectionArgs,
                                                     String groupBy, String having,
                                                     String orderBy) {
        SQLiteDatabase db = mDataManager.getReadableDatabase();
        Cursor c = null;
        String rawQuery = getRawQueryForTranslations(columns, selection);
        if (rawQuery != null) {
            c = db.rawQuery(rawQuery, selectionArgs);
        }
        if (c == null)
            c = db.query(mTable, columns, mSelection, selectionArgs, null, null, orderBy);
        return c;
    }

    private String getRawQueryForTranslations(String[] columns, String selection) {
        SQLiteDatabase db = mDataManager.getReadableDatabase();
//		String lang = mDataManager.getLanguage();

        if (columns == null) {
            Cursor c = db.rawQuery("SELECT * FROM " + mTable + " LIMIT 0", null);
            if (c != null)
                columns = c.getColumnNames();
            c.close();
        }
        if (columns == null)
            return null;

        final Set<String> FIELDS = mTable == Contacts.TABLE_NAME ? ContactsTranslations
                .TRANSLATED_FIELDS :
                mTable == Species.TABLE_NAME ? SpeciesTranslations.TRANSLATED_FIELDS : null;

        if (FIELDS == null)
            return null;

        String rawQuery = "SELECT";
        boolean needTranslation = false;
        for (int i = 0; i < columns.length; i++) {
            String col = columns[i];
            if (FIELDS.contains(col)) {
                needTranslation = true;
                rawQuery += " IFNULL(t." + col + ", o." + col + ") AS " + col + ",";
            } else {
                rawQuery += " o." + col + " AS " + col + ",";
            }
//			if (i<columns.length-1) rawQuery += ",";
        }
        if (!needTranslation)
            return null;

        String transTable = null, idField = null, refIdField = null, langField = null;
        if (mTable == Contacts.TABLE_NAME) {
            transTable = ContactsTranslations.TABLE_NAME;
            idField = Contacts._ID;
            refIdField = ContactsTranslations._S_CONTACT_ID;
            langField = ContactsTranslations._S_LANGUAGE;
        } else if (mTable == Species.TABLE_NAME) {
            transTable = SpeciesTranslations.TABLE_NAME;
            idField = Species._ID;
            refIdField = SpeciesTranslations._S_SPECIES_ID;
            langField = SpeciesTranslations._S_LANGUAGE;
        }
        rawQuery += " t." + langField + " AS " + langField;

        rawQuery += " FROM " + mTable + " o LEFT OUTER JOIN (SELECT * FROM " + transTable + ") t " +
				"ON o." +
                idField + "=t." + refIdField;// + " WHERE t." + langField + "=" + DatabaseUtils
		// .sqlEscapeString(lang);
        if (!TextUtils.isEmpty(selection)) {
            if (selection.contains(Species._S_COMMON_NAME))
                selection=selection.replace(Species._S_COMMON_NAME, "t." + Species._S_COMMON_NAME);
            rawQuery += " WHERE " + selection;
//			rawQuery += " AND " + selection;
        }
        return rawQuery;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newUri = null;
        SQLiteDatabase db = mDataManager.getWritableDatabase();
        performMatch(uri, null);
        switch (mMatch) {
            case (STATIC_ID):
            case (CONTACT_ID):
            case (SPECIES_ID):
            case (SPECIES_IMAGE_ID):
            case (EVENT_ID):
                throw new UnsupportedOperationException("Insert not supported for uri: " + uri);
            case (SPECIES_TRANSLATIONS):
            case (CONTACT_TRANSLATIONS):
            case (STATIC):
                break;
            default:
                if (values.containsKey(ContactsTranslations._S_LANGUAGE))
                    values.remove(ContactsTranslations._S_LANGUAGE);
                if (values.containsKey(SpeciesTranslations._S_LANGUAGE))
                    values.remove(SpeciesTranslations._S_LANGUAGE);
                break;
        }
        long id = db.insertWithOnConflict(mTable, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (id != -1) {
            Context ctx = getContext();
            assert ctx != null;
            ctx.getContentResolver().notifyChange(uri, null, false);
            newUri = ContentUris.withAppendedId(uri, id);
        }
        return newUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDataManager.getWritableDatabase();
        performMatch(uri, selection);
        switch (mMatch) {
            case (CONTACT_ID):
            case (SPECIES_ID):
                if (values.containsKey(ContactsTranslations._S_LANGUAGE))
                    values.remove(ContactsTranslations._S_LANGUAGE);
                if (values.containsKey(SpeciesTranslations._S_LANGUAGE))
                    values.remove(SpeciesTranslations._S_LANGUAGE);
            case (STATIC_ID):
                break;
            default:
                throw new UnsupportedOperationException("Update not supported for uri: " + uri);
        }
        int count = db.update(mTable, values, mSelection, null);
        if (count > 0) {
            Context ctx = getContext();
            assert ctx != null;
            ctx.getContentResolver().notifyChange(uri, null, false);
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        performMatch(uri, selection);
        SQLiteDatabase db = mDataManager.getWritableDatabase();
        int count = db.delete(mTable, mSelection, null);
        if (count > 0) {
            Context ctx = getContext();
            assert ctx != null;
            ctx.getContentResolver().notifyChange(uri, null, false);
        }
        return count;
    }

    private void performMatch(Uri uri, String selection) {
        mTable = null;
        mSelection = selection;
        mMatch = sUriMatcher.match(uri);
        switch (mMatch) {
            case (STATIC_ID):
                if (mSelection != null && !mSelection.isEmpty())
                    mSelection += " AND ";
                else
                    mSelection = "";
                mSelection += StaticContent._ID + " = " + uri.getLastPathSegment();
            case (STATIC):
                mTable = StaticContent.TABLE_NAME;
                break;
            case (CONTACT_ID):
                if (mSelection != null && !mSelection.isEmpty())
                    mSelection += " AND ";
                else
                    mSelection = "";
                mSelection += Contacts._ID + " = " + uri.getLastPathSegment();
            case (CONTACTS):
                mTable = Contacts.TABLE_NAME;
                break;
            case (CONTACT_TRANSLATIONS):
                mTable = ContactsTranslations.TABLE_NAME;
                break;
            case (SPECIES_ID):
                if (mSelection != null && !mSelection.isEmpty())
                    mSelection += " AND ";
                else
                    mSelection = "";
                mSelection += Species._ID + " = " + uri.getLastPathSegment();
            case (SPECIES):
                mTable = Species.TABLE_NAME;
                break;
            case (SPECIES_TRANSLATIONS):
                mTable = SpeciesTranslations.TABLE_NAME;
                break;
            case (SPECIES_IMAGE_ID):
                if (mSelection != null && !mSelection.isEmpty())
                    mSelection += " AND ";
                else
                    mSelection = "";
                mSelection += "ID = " + uri.getLastPathSegment();
            case (SPECIES_IMAGES):
                mTable = SpeciesImages.TABLE_NAME;
                break;
            case (EVENT_ID):
                if (mSelection != null && !mSelection.isEmpty())
                    mSelection += " AND ";
                else
                    mSelection = "";
                mSelection += Incidents._ID + " = " + uri.getLastPathSegment();
            case (EVENTS):
                mTable = Incidents.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


}