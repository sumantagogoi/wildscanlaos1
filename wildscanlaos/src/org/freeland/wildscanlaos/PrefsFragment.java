package org.freeland.wildscanlaos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.util.DisplayMetrics;

import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.Contacts;
import org.freeland.wildscanlaos.data.contract.ContactsTranslations;
import org.freeland.wildscanlaos.data.contract.StaticContent;
import org.freeland.wildscanlaos.data.provider.WildscanDataProvider;
import org.freeland.wildscanlaos.util.Util;

import java.util.Iterator;
import java.util.Locale;

public class PrefsFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    public static final String PREFS_FILENAME = "wildscanlaos.shared.prefs";

    public static final String PREFS_KEY_LAST_SUCCESSFUL_SYNC = "org.freeland.wildscanlaos.Prefs.LastSuccessfullSynch";
    public static final String PREFS_KEY_LAST_EVENTS_SYNC = "org.freeland.wildscanlaos.Prefs.LastSuccessfullEventsSynch";
    public static final String PREFS_KEY_DATA_FOLDER = "org.freeland.wildscanlaos.Prefs.DataFolder";
    public static final String PREFS_KEY_USER_ID = "org.freeland.wildscanlaos.Prefs.UserId";
    public static final String PREFS_KEY_USER_ORIG_CODE_HASH = "org.freeland.wildscanlaos.Prefs.UserSecret1"; // google-auth-code-hash
    public static final String PREFS_KEY_USER_SERVER_COOKIE = "org.freeland.wildscanlaos.Prefs.UserSecret2"; // server cookie
    public static final String PREFS_KEY_USER_SECRET = "org.freeland.wildscanlaos.Prefs.UserSecret3"; // random client key
    public static final String PREFS_KEY_USER_XOR_KEY = "org.freeland.wildscanlaos.Prefs.UserSecret4"; // random client key
    public static final String PREFS_KEY_DEFAULT_DATA_FOLDER = "Wildscan";

    // settings:
    public static final String PREFS_KEY_SHOW_WARNING = "org.freeland.wildscanlaos.Prefs.ShowWarning";
    //boolean mShowReportWarning = true;

    public static final String PREFS_KEY_SYNC_USE_WIFI_ONLY = "org.freeland.wildscanlaos.Prefs.SyncWifiOnly";
    //boolean mSyncWifiOnly = true;

    public static final String PREFS_KEY_DEFAULT_GALLERY_VIEW = "org.freeland.wildscanlaos.Prefs.SpeciesDefaultView";
    public static final int PREFS_DEFAULT_GALLERY_VIEW_GALLERY = 0;
    public static final int PREFS_DEFAULT_GALLERY_VIEW_LIST = 1;
    public static final int PREFS_DEFAULT_GALLERY_VIEW_LAST = 2;
    //int mGalleryDefaultView = PREFS_DEFAULT_GALLERY_VIEW_GALLERY;

    public static final String PREFS_KEY_AUTO_CONTACTS = "org.freeland.wildscanlaos.Prefs.AutoContacts";
    // List<Long> mAutoContacts = null;

    public static final String PREFS_KEY_CONTENT_LANGUAGE = "org.freeland.wildscanlaos.Prefs.ContentLanguage";
	public static final String PREFS_KEY_LANGUAGE = "org.freeland.wildscanlaos.Prefs.Language";
    // String mLanguage = "en";

    public static final String PREFS_KEY_SAVE_REPORT_BAK = "org.freeland.wildscanlaos.Prefs.SaveReport";
    //boolean mSaveReport = false;

    public static final String PREFS_KEY_CURRECT_REGION = "org.freelanc.wildscanlaos.Prefs" +
            ".CurrentRegion";

    // Required fields:
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREFS_KEY_DEFAULT_GALLERY_VIEW.equals(key)) {
            ListPreference pref = (ListPreference) findPreference(PREFS_KEY_DEFAULT_GALLERY_VIEW);
            pref.setSummary(pref.getEntry());
            getActivity().getSharedPreferences(PREFS_FILENAME, 0).edit().putString(PREFS_KEY_DEFAULT_GALLERY_VIEW, pref.getValue()).commit();
        } else if (PREFS_KEY_CONTENT_LANGUAGE.equals(key)) {
            ListPreference pref = (ListPreference) findPreference(PREFS_KEY_CONTENT_LANGUAGE);
            pref.setSummary(pref.getEntry());
            getActivity().getSharedPreferences(PREFS_FILENAME, 0).edit().putString(PREFS_KEY_CONTENT_LANGUAGE, pref.getValue()).commit();
            WildscanDataManager.getInstance(getActivity()).resetInfoLanguage();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        /*
        MultiSelectListPreference contactsPref = (MultiSelectListPreference) findPreference(PREFS_KEY_AUTO_CONTACTS);
        Uri uri = WildscanDataProvider.getTableUri(Contacts.TABLE_NAME);
        String[] projection = {Contacts._ID, Contacts._NAME};
        String sortOrder = Contacts._NAME + " ASC";
        String selection = Util.addLangToSelection(getActivity().getApplicationContext(), Contacts._EMAIL + " NOT NULL", ContactsTranslations._S_LANGUAGE);
        //Cursor contacts = getActivity().getContentResolver().query(uri, projection, selection, null, sortOrder);
        Cursor contacts = getActivity().getContentResolver().query(uri, projection, selection, null, null);
        if (contacts.moveToFirst()) {
            int n = contacts.getCount();
            int id = contacts.getColumnIndex(Contacts._ID),
                    name = contacts.getColumnIndex(Contacts._NAME);
            CharSequence[] names = new CharSequence[n];
            CharSequence[] ids = new CharSequence[n];
            for (int i = 0; !contacts.isAfterLast(); contacts.moveToNext(), i++) {
                names[i] = contacts.getString(name);
                ids[i] = contacts.getString(id);
            }
            contactsPref.setEntries(names);
            contactsPref.setEntryValues(ids);
        } else
            contactsPref.setEnabled(false);
        contacts.close();
        */

        ListPreference langPref = (ListPreference) findPreference(PREFS_KEY_CONTENT_LANGUAGE);
        int nLang = StaticContent.AVAILABLE_LANGUAGES.size(), i=0;
        CharSequence[] names = new CharSequence[nLang];
        CharSequence[] ids = new CharSequence[nLang];
		for (Iterator<String> it = StaticContent.AVAILABLE_LANGUAGES.iterator(); it.hasNext(); i++) {
			String lang = it.next();
			Locale locale = StaticContent.LANG_CODE_TO_LOCALE.get(lang);
			names[i] = locale.getDisplayLanguage(locale).toUpperCase();
			ids[i] = lang;
		}
		nLang = i;
        //names[0] = getString(R.string.pref_entry_device_language);
        //ids[0] = StaticContent.LANG_CODE_DEFAULT;
        if (nLang > 1) {
            langPref.setEntries(names);
            langPref.setEntryValues(ids);
            langPref.setEnabled(true);
        } else {
            langPref.setEnabled(false);
            //langPref.setSummary(getString(R.string.pref_entry_device_language, names[0]));
        }
        if (langPref.getEntry() != null)
            langPref.setSummary(langPref.getEntry());

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // disconnect google account
    // species gallery view
    // list of contacts to be auto-added to reports
    // allow sync over wi-fi only
    // language
    // save report backup after send
    // optional profile details

}
