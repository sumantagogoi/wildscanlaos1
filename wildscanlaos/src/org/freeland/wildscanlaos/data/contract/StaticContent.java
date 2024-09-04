package org.freeland.wildscanlaos.data.contract;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import org.freeland.wildscanlaos.R;
import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.models.RegionSpecies;
import org.freeland.wildscanlaos.models.StaticContentModel;
import org.freeland.wildscanlaos.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StaticContent implements BaseColumns {
    public static final String TABLE_NAME = "static_content";

    public static final String _S_TYPE = "type";
    public static final String _S_CONTENT = "content";
    public static final String _S_CONSUMER = "consumer-advice";
    public static final String _S_RESPONDER = "first-responder";
    public static final String _S_ENFORCEMENT = "enforcement-advice";

    public static final String _LANGUAGE = "language";
    public static final String _ABOUT = "about";
    public static final String _TERMS = "terms";
    public static final String _HELP = "help";
    public static final String _LEGAL = "legal";
    public static final String _CONSUMER = "consumer_advice";
    public static final String _RESPONDER = "first_responder";
    public static final String _ENFORCEMENT = "enforcement_advice";
    public static final String _CONTRIBUTOR = "contributor";
    public static final String _CREDITS = "credits";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            _LANGUAGE + " TEXT UNIQUE NOT NULL, " +
            _ABOUT + " TEXT DEFAULT NULL, " +
            _TERMS + " TEXT DEFAULT NULL, " +
            _HELP + " TEXT DEFAULT NULL, " +
            _LEGAL + " TEXT DEFAULT NULL, " +
            _CONSUMER + " TEXT DEFAULT NULL, " +
            _RESPONDER + " TEXT DEFAULT NULL, " +
            _ENFORCEMENT + " TEXT DEFAULT NULL, " +
            _CONTRIBUTOR + " TEXT DEFAULT NULL, " +
            _CREDITS + " TEXT DEFAULT NULL);";
    public static final String LANG_CODE_DEFAULT = "default";
    public static final String LANG_CODE_ENGLISH = "en";
    public static final String LANG_CODE_LAO = "la";
    public static final List<String> AVAILABLE_LANGUAGES = new ArrayList<String>();
    public static final Map<String, Locale> LANG_CODE_TO_LOCALE;
    public static final Map<String, String> LOCALE_TO_LANG_CODE;

    static {
        AVAILABLE_LANGUAGES.add(LANG_CODE_DEFAULT);
        AVAILABLE_LANGUAGES.add(LANG_CODE_ENGLISH);
        AVAILABLE_LANGUAGES.add(LANG_CODE_LAO);
    }

    static {
        LANG_CODE_TO_LOCALE = new HashMap<String, Locale>();
        LANG_CODE_TO_LOCALE.put(LANG_CODE_DEFAULT,
                Locale.getDefault()); // device language (original offline content)
        LANG_CODE_TO_LOCALE.put(LANG_CODE_ENGLISH, Locale.ENGLISH);//new Locale("en")); // english
        LANG_CODE_TO_LOCALE.put(LANG_CODE_LAO, new Locale("la")); // lao
    }

    static {
        LOCALE_TO_LANG_CODE = new HashMap<String, String>();
        LOCALE_TO_LANG_CODE.put("en", LANG_CODE_ENGLISH); // english
        LOCALE_TO_LANG_CODE.put("la", LANG_CODE_LAO); // lao
    }

    public static String setupDefaultContent(Context context) {
        StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO " + TABLE_NAME + "(" +  _ID + ", language, about, terms, help, legal, consumer_advice, first_responder, enforcement_advice, contributor, credits) VALUES(");

        insert.append("0,"); //_id
        insert.append('"' + LANG_CODE_DEFAULT + '"' + ','); //lang
        //insert.append("'en',"); //lang
        insert.append(convertField(
                Base64.encodeToString(Util.readBytesFromResource(context, R.raw.about),
                        Base64.DEFAULT)) + ",");//about
        insert.append(convertField(
                Base64.encodeToString(Util.readBytesFromResource(context, R.raw.eula),
                        Base64.DEFAULT)) + ",");//terms
        insert.append(convertField(
                Base64.encodeToString(Util.readBytesFromResource(context, R.raw.help),
                        Base64.DEFAULT)) + ",");//help
        insert.append(convertField(
                Base64.encodeToString(Util.readBytesFromResource(context, R.raw.national_laws),
                        Base64.DEFAULT)) + ",");//legal
        insert.append(convertField(
                Base64.encodeToString(Util.readBytesFromResource(context, R.raw.consumer_advice),
                        Base64.DEFAULT)) + ",");//consumer
        insert.append(convertField(Base64.encodeToString(
                Util.readBytesFromResource(context, R.raw.first_responder_advice),
                Base64.DEFAULT)) + ",");//responder
        insert.append(convertField(Base64.encodeToString(
                Util.readBytesFromResource(context, R.raw.law_enforcement_advice),
                Base64.DEFAULT)) + ",");//enforcements
        insert.append(convertField(Base64.encodeToString(
                Util.readBytesFromResource(context, R.raw.contributors),
                Base64.DEFAULT)) + ","); //contributors
        insert.append(convertField(
                Base64.encodeToString(Util.readBytesFromResource(context, R.raw.credits),
                Base64.DEFAULT)));//credits

        insert.append(");");

        return insert.toString();
    }

    public static final ContentValues convertRemoteFieldsToLocal(Context context,
                                                                 ContentValues remote) {
        ContentValues local = new ContentValues();

		//local.put(_ID, sLanguageNamesMap.get(remote.getAsString(_LANGUAGE)).intValue());
        local.put(_LANGUAGE, remote.getAsString(_LANGUAGE));
        local.put(_ABOUT, convertField(remote.getAsString(_ABOUT)));
        local.put(_TERMS, convertField(remote.getAsString(_TERMS)));
        local.put(_HELP, convertField(remote.getAsString(_HELP)));
        local.put(_LEGAL, convertField(remote.getAsString(_LEGAL)));
        local.put(_CONSUMER, convertField(remote.getAsString(_S_CONSUMER)));
        local.put(_RESPONDER, convertField(remote.getAsString(_S_RESPONDER)));
        local.put(_ENFORCEMENT, convertField(remote.getAsString(_S_ENFORCEMENT)));
        local.put(_CREDITS, convertField(remote.getAsString(_CREDITS)));
        local.put(_CONTRIBUTOR, convertField(remote.getAsString(_CONTRIBUTOR)));

        return local;
    }

    private static final String convertField(String value) {
//		String out = Util.escapeHtml(value);
        if (TextUtils.isEmpty(value))
            return null;
        return DatabaseUtils.sqlEscapeString(value);
    }

    public static String getLanguageKey(Locale locale) {
        String langCode = locale.getLanguage();
        String name = LOCALE_TO_LANG_CODE.get(langCode);
        if (name == null)
            name = LANG_CODE_ENGLISH; // default = english

        return name;
    }
/*
    public static final ContentValues convertRemoteStaticContentToLocal(Context context,
                                                                  StaticContentModel staticContent) {
        ContentValues local = new ContentValues();

        local.put(_LANGUAGE, staticContent.getLanguage());

        if (staticContent.getType().equalsIgnoreCase("about")) {
            local.put(_ABOUT, staticContent.getContent());
        } else if (staticContent.getType().equalsIgnoreCase("terms")) {
            local.put(_TERMS, staticContent.getContent());
        } else if (staticContent.getType().equalsIgnoreCase("help")) {
            local.put(_HELP, staticContent.getContent());
        } else if (staticContent.getType().equalsIgnoreCase("legal")) {
            local.put(_LEGAL, staticContent.getContent());
        } else if (staticContent.getType().equalsIgnoreCase("consumer-advice")) {
            local.put(_CONSUMER, staticContent.getContent());
        } else if (staticContent.getType().equalsIgnoreCase("first-responder")) {
            local.put(_RESPONDER, staticContent.getContent());
        } else if (staticContent.getType().equalsIgnoreCase("enforcement")) {
            local.put(_ENFORCEMENT, staticContent.getContent());
        } else {
            local.put(_CREDITS, staticContent.getContent());
        }
        return local;
    }
*/
}
