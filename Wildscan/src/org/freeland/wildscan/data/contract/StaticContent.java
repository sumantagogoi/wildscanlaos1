package org.freeland.wildscan.data.contract;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.text.TextUtils;
import android.util.Base64;

import org.freeland.wildscan.R;
import org.freeland.wildscan.util.Util;

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
            _CREDITS + " TEXT DEFAULT NULL);";
    public static final String LANG_CODE_DEFAULT = "default";
    public static final String LANG_CODE_ENGLISH = "en";
    public static final String LANG_CODE_CHINESE = "ch";
    public static final String LANG_CODE_THAI = "th";
    public static final String LANG_CODE_VIETNAMESE = "vn";
    public static final String LANG_CODE_INDONESIAN = "in";
    public static final String LANG_CODE_HINDI = "hi";
    public static final String LANG_CODE_MALAY = "ms";
    public static final String LANG_CODE_KHMER = "km";
    public static final String LANG_CODE_FRENCH = "fr";
    public static final String LANG_CODE_SPANISH = "es";
    public static final String LANG_CODE_PORTUGUESE = "pt";
    public static final String LANG_CODE_SWAHILI = "sw";
    public static final List<String> AVAILABLE_LANGUAGES = new ArrayList<String>();
    public static final Map<String, Locale> LANG_CODE_TO_LOCALE;
    public static final Map<String, String> LOCALE_TO_LANG_CODE;

    static {
        AVAILABLE_LANGUAGES.add(LANG_CODE_DEFAULT);
        AVAILABLE_LANGUAGES.add(LANG_CODE_ENGLISH);
        AVAILABLE_LANGUAGES.add(LANG_CODE_THAI);
        AVAILABLE_LANGUAGES.add(LANG_CODE_INDONESIAN);
        AVAILABLE_LANGUAGES.add(LANG_CODE_VIETNAMESE);
        AVAILABLE_LANGUAGES.add(LANG_CODE_MALAY);
        AVAILABLE_LANGUAGES.add(LANG_CODE_KHMER);
        AVAILABLE_LANGUAGES.add(LANG_CODE_FRENCH);
        AVAILABLE_LANGUAGES.add(LANG_CODE_PORTUGUESE);
        AVAILABLE_LANGUAGES.add(LANG_CODE_SPANISH);
        AVAILABLE_LANGUAGES.add(LANG_CODE_SWAHILI);

    }

    static {
        LANG_CODE_TO_LOCALE = new HashMap<String, Locale>();
        LANG_CODE_TO_LOCALE.put(LANG_CODE_DEFAULT,
                Locale.getDefault()); // device language (original offline content)
        LANG_CODE_TO_LOCALE.put(LANG_CODE_ENGLISH, Locale.ENGLISH);//new Locale("en")); // english
        LANG_CODE_TO_LOCALE.put(LANG_CODE_CHINESE, Locale.CHINESE);//new Locale("zh")); // chinese
        LANG_CODE_TO_LOCALE.put(LANG_CODE_THAI, new Locale("th")); // thai
        LANG_CODE_TO_LOCALE.put(LANG_CODE_VIETNAMESE, new Locale("vi")); // vietnamese
        LANG_CODE_TO_LOCALE.put(LANG_CODE_INDONESIAN, new Locale("in")); // indonesian
        LANG_CODE_TO_LOCALE.put(LANG_CODE_HINDI, new Locale("hi")); // hindi
        LANG_CODE_TO_LOCALE.put(LANG_CODE_KHMER, new Locale("km")); // khmer
        LANG_CODE_TO_LOCALE.put(LANG_CODE_MALAY, new Locale("ms")); // malay
        LANG_CODE_TO_LOCALE.put(LANG_CODE_FRENCH, new Locale("fr")); // frensh
        LANG_CODE_TO_LOCALE.put(LANG_CODE_PORTUGUESE, new Locale("pt")); // portuguese
        LANG_CODE_TO_LOCALE.put(LANG_CODE_SPANISH, new Locale("es")); // spanish
        LANG_CODE_TO_LOCALE.put(LANG_CODE_SWAHILI, new Locale("sw")); // sawihili

    }

    static {
        LOCALE_TO_LANG_CODE = new HashMap<String, String>();
        LOCALE_TO_LANG_CODE.put("en", LANG_CODE_ENGLISH); // english
        LOCALE_TO_LANG_CODE.put("zh", LANG_CODE_CHINESE); // chinese
        LOCALE_TO_LANG_CODE.put("th", LANG_CODE_THAI); // thai
        LOCALE_TO_LANG_CODE.put("vi", LANG_CODE_VIETNAMESE); // vietnamese
        LOCALE_TO_LANG_CODE.put("in", LANG_CODE_INDONESIAN); // indonesian
        LOCALE_TO_LANG_CODE.put("hi", LANG_CODE_HINDI); // hindi
        LOCALE_TO_LANG_CODE.put("km", LANG_CODE_KHMER); // khmer
        LOCALE_TO_LANG_CODE.put("ms", LANG_CODE_MALAY); // malay
        LOCALE_TO_LANG_CODE.put("fr",LANG_CODE_FRENCH); // frensh
        LOCALE_TO_LANG_CODE.put("pt",LANG_CODE_PORTUGUESE); // portuguese
        LOCALE_TO_LANG_CODE.put("es",LANG_CODE_SPANISH); // spanish
        LOCALE_TO_LANG_CODE.put("sw",LANG_CODE_SWAHILI); // sawihili
    }

    public static String setupDefaultContent(Context context) {
        StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO " + TABLE_NAME + " VALUES(");

        insert.append("0,"); //_id
        insert.append("'" + LANG_CODE_DEFAULT + "',"); //lang
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
        insert.append(convertField(
                Base64.encodeToString(Util.readBytesFromResource(context, R.raw.credits),
                        Base64.DEFAULT)));//credits

        insert.append(");");

        return insert.toString();
    }

    public static final ContentValues convertRemoteFieldsToLocal(Context context,
                                                                 ContentValues remote) {
        ContentValues local = new ContentValues();

//		local.put(_ID, sLanguageNamesMap.get(remote.getAsString(_LANGUAGE)).intValue());
        local.put(_LANGUAGE, remote.getAsString(_LANGUAGE));
        local.put(_ABOUT, convertField(remote.getAsString(_ABOUT)));
        local.put(_TERMS, convertField(remote.getAsString(_TERMS)));
        local.put(_HELP, convertField(remote.getAsString(_HELP)));
        local.put(_LEGAL, convertField(remote.getAsString(_LEGAL)));
        local.put(_CONSUMER, convertField(remote.getAsString(_S_CONSUMER)));
        local.put(_RESPONDER, convertField(remote.getAsString(_S_RESPONDER)));
        local.put(_ENFORCEMENT, convertField(remote.getAsString(_S_ENFORCEMENT)));
        local.put(_CREDITS, convertField(remote.getAsString(_CREDITS)));

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
}
