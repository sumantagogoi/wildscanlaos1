package org.freeland.wildscanlaos.data.contract;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.models.RegionSpecies;
import org.freeland.wildscanlaos.util.Util;

/**
 * @author Noam
 */
public class Species implements BaseColumns {
    public static final String TABLE_NAME = "species";

    // local fields
    public static final String _FAV = "_fav";
    public static final String _MAIN_PHOTO = "_main_photo";

    // fields in summary info
    public static final String _S_TYPE = "type";
    public static final String _S_REGION = "region";
    public static final String _S_COMMON_NAME = "common_name";
    public static final String _S_CITES = "cites";
    public static final String _S_KEYWORDS_TAGS = "keywords_tags";

    // fields for details view
    public static final String _S_SCIENTIFIC_NAME = "scientific_name";
    //	public static final String _S_CITES_OTHER = "cites_other";
    public static final String _S_EXTANT_COUNTRIES = "extant_countries";
    public static final String _S_STATUS = "status";
    public static final String _S_WARNINGS = "warnings";
    public static final String _S_HABITAT = "habitat";
    public static final String _S_BASIC_ID_CLUES = "basic_id_cues";
    public static final String _S_CONSUMER_ADVICE = "consumer_advice";
    public static final String _S_ENFORCEMENT_ADVICE = "enforcement_advice";
    public static final String _S_SIMILAR_ANIMALS = "similar_animals";
    public static final String _S_KNOWN_AS = "known_as";
    public static final String _S_AVERAGE_SIZE_WEIGHT = "average_size_weight";
    public static final String _S_FIRST_RESPONDER = "first_responder";
    public static final String _S_TRADED_AS = "traded_as";
    public static final String _S_COMMON_TRAFFICKING = "common_trafficking";
    public static final String _S_NOTES = "notes";
    //	public static final String _S_REFERENCE = "reference";
    public static final String _S_DISEASE_NAME = "disease_name";
    public static final String _S_DISEASE_RISK_LEVEL = "disease_risk_level";
//	public static final String _S_CREATED_BY = "created_by";
//	public static final String _S_CREATED_DATE = "created_date";
//	public static final String _S_UPDATED_BY = "updated_by";
//	public static final String _S_UPDATED_DATE = "updated_date";

    // remote server field names
    public static final String _S_ID = "id";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            _FAV + " BOOLEAN DEFAULT 0," +
            _S_REGION + " TEXT DEFAULT NULL," +
            _MAIN_PHOTO + " TEXT DEFAULT NULL," +
            _S_TYPE + " TEXT NOT NULL," +
            _S_COMMON_NAME + " TEXT NOT NULL," +
            _S_CITES + " TEXT DEFAULT NULL," +
            _S_SCIENTIFIC_NAME + " TEXT DEFAULT NULL," +
            _S_KEYWORDS_TAGS + " TEXT DEFAULT NULL," +
//			_S_CITES_OTHER + " TEXT DEFAULT NULL," +
            _S_EXTANT_COUNTRIES + " TEXT DEFAULT NULL," +
            _S_STATUS + " TEXT DEFAULT NULL," +
            _S_WARNINGS + " TEXT DEFAULT NULL," +
            _S_HABITAT + " TEXT DEFAULT NULL," +
            _S_BASIC_ID_CLUES + " TEXT DEFAULT NULL," +
            _S_CONSUMER_ADVICE + " TEXT DEFAULT NULL," +
            _S_ENFORCEMENT_ADVICE + " TEXT DEFAULT NULL," +
            _S_SIMILAR_ANIMALS + " TEXT DEFAULT NULL," +
            _S_KNOWN_AS + " TEXT DEFAULT NULL," +
            _S_AVERAGE_SIZE_WEIGHT + " TEXT DEFAULT NULL," +
            _S_FIRST_RESPONDER + " TEXT DEFAULT NULL," +
            _S_TRADED_AS + " TEXT DEFAULT NULL," +
            _S_COMMON_TRAFFICKING + " TEXT DEFAULT NULL," +
            _S_NOTES + " TEXT DEFAULT NULL," +
//			_S_REFERENCE + " TEXT DEFAULT NULL," +
            _S_DISEASE_NAME + " TEXT DEFAULT NULL," +
            _S_DISEASE_RISK_LEVEL + " TEXT DEFAULT NULL" +
//			_S_CREATED_BY + " INTEGER NOT NULL," +
//			_S_CREATED_DATE + " TEXT NOT NULL," +
//			_S_UPDATED_BY + " INTEGER DEFAULT NULL," +
//			_S_UPDATED_DATE + " TEXT DEFAULT NULL," +
            ");";

    public static final ContentValues convertRemoteFieldsToLocal(Context context,
                                                                 ContentValues remote) {
        SQLiteDatabase db = WildscanDataManager.getInstance(context).getReadableDatabase();
        ContentValues local = new ContentValues();

        long id = remote.getAsLong(_S_ID);
        String select = _ID + "==" + String.valueOf(id);
        Cursor c = db.query(TABLE_NAME, null, select, null, null, null, null);
        if (c.moveToFirst())
            DatabaseUtils.cursorRowToContentValues(c, local);
        else {
            local.put(_ID, id);
            local.put(_FAV, 0);
        }
        c.close();
        String[] imCols = {SpeciesImages._S_PATH_IMAGE};
        String imSelect = SpeciesImages._S_SPECIES_ID + "=" + String.valueOf(
                id);//? AND " + WildscanDataManager.SpeciesImages.COL_NAME_DEFAULT_ORDER + "=?";
        String imOrder = SpeciesImages._S_DEFAULT_ORDER + "," + SpeciesImages._S_USER_ORDER;
        //String[] imSelArgs = { Integer.toString(spec_id), "0" };

        c = db.query(SpeciesImages.TABLE_NAME, imCols, imSelect, null, null, null, imOrder);
        if (c.getCount() > 0) {
            c.moveToFirst();
            int col_idx_image = c.getColumnIndex(SpeciesImages._S_PATH_IMAGE);
            local.put(_MAIN_PHOTO, c.getString(col_idx_image));
        }
        c.close();

        String distrib = remote.getAsString(_S_EXTANT_COUNTRIES);
        if (TextUtils.isEmpty(distrib))
            distrib = null;

        local.put(_S_EXTANT_COUNTRIES, distrib);
        local.put(_S_TYPE, remote.getAsString(_S_TYPE));
//        local.put(_S_REGION, remote.getAsString(_S_REGION));
        local.put(_S_COMMON_NAME, Util.escapeHtml(remote.getAsString(_S_COMMON_NAME)));
        local.put(_S_CITES, remote.getAsString(_S_CITES));
        local.put(_S_KEYWORDS_TAGS, remote.getAsString(_S_KEYWORDS_TAGS));
        local.put(_S_SCIENTIFIC_NAME, Util.escapeHtml(remote.getAsString(_S_SCIENTIFIC_NAME)));
        local.put(_S_STATUS, remote.getAsString(_S_STATUS));
        local.put(_S_WARNINGS, Util.escapeHtml(remote.getAsString(_S_WARNINGS)));
        local.put(_S_HABITAT, Util.escapeHtml(remote.getAsString(_S_HABITAT)));
        local.put(_S_BASIC_ID_CLUES, Util.escapeHtml(remote.getAsString(_S_BASIC_ID_CLUES)));
        local.put(_S_CONSUMER_ADVICE, Util.escapeHtml(remote.getAsString(_S_CONSUMER_ADVICE)));
        local.put(_S_ENFORCEMENT_ADVICE,
                Util.escapeHtml(remote.getAsString(_S_ENFORCEMENT_ADVICE)));
        local.put(_S_SIMILAR_ANIMALS, Util.escapeHtml(remote.getAsString(_S_SIMILAR_ANIMALS)));
        local.put(_S_KNOWN_AS, Util.escapeHtml(remote.getAsString(_S_KNOWN_AS)));
        local.put(_S_AVERAGE_SIZE_WEIGHT,
                Util.escapeHtml(remote.getAsString(_S_AVERAGE_SIZE_WEIGHT)));
        local.put(_S_FIRST_RESPONDER, Util.escapeHtml(remote.getAsString(_S_FIRST_RESPONDER)));
        local.put(_S_TRADED_AS, Util.escapeHtml(remote.getAsString(_S_TRADED_AS)));
        local.put(_S_COMMON_TRAFFICKING,
                Util.escapeHtml(remote.getAsString(_S_COMMON_TRAFFICKING)));
        local.put(_S_NOTES, Util.escapeHtml(remote.getAsString(_S_NOTES)));
        local.put(_S_DISEASE_NAME, Util.escapeHtml(remote.getAsString(_S_DISEASE_NAME)));
        local.put(_S_DISEASE_RISK_LEVEL,
                Util.escapeHtml(remote.getAsString(_S_DISEASE_RISK_LEVEL)));

        return local;
    }

    public static final ContentValues convertRemoteSpeciesToLocal(Context context,
                                                                  RegionSpecies regionSpecies) {
        SQLiteDatabase db = WildscanDataManager.getInstance(context).getReadableDatabase();
        ContentValues local = new ContentValues();

        long id = Long.parseLong(regionSpecies.getId());
        String select = _ID + "==" + String.valueOf(id);
        Cursor c = db.query(TABLE_NAME, null, select, null, null, null, null);
        if (c.moveToFirst())
            DatabaseUtils.cursorRowToContentValues(c, local);
        else {
            local.put(_ID, id);
            local.put(_FAV, 0);
        }
        c.close();
        String[] imCols = {SpeciesImages._S_PATH_IMAGE};
        String imSelect = SpeciesImages._S_SPECIES_ID + "=" + String.valueOf(
                id);//? AND " + WildscanDataManager.SpeciesImages.COL_NAME_DEFAULT_ORDER + "=?";
        String imOrder = SpeciesImages._S_DEFAULT_ORDER + "," + SpeciesImages._S_USER_ORDER;
        //String[] imSelArgs = { Integer.toString(spec_id), "0" };

        c = db.query(SpeciesImages.TABLE_NAME, imCols, imSelect, null, null, null, imOrder);
        if (c.getCount() > 0) {
            c.moveToFirst();
            int col_idx_image = c.getColumnIndex(SpeciesImages._S_PATH_IMAGE);
            local.put(_MAIN_PHOTO, c.getString(col_idx_image));
        }
        c.close();

        String distrib = regionSpecies.getExtant_countries();
        if (TextUtils.isEmpty(distrib))
            distrib = null;

        local.put(_S_EXTANT_COUNTRIES, distrib);
        local.put(_S_TYPE, regionSpecies.getType());
        local.put(_S_REGION, regionSpecies.getRegion());
        local.put(_S_COMMON_NAME, Util.escapeHtml(regionSpecies.getCommon_name()));
        local.put(_S_CITES, regionSpecies.getCites());
        local.put(_S_KEYWORDS_TAGS, regionSpecies.getKeywords_tags());
        local.put(_S_SCIENTIFIC_NAME, Util.escapeHtml(regionSpecies.getScientific_name()));
        local.put(_S_STATUS, regionSpecies.getStatus());
        local.put(_S_WARNINGS, Util.escapeHtml(regionSpecies.getWarnings()));
        local.put(_S_HABITAT, Util.escapeHtml(regionSpecies.getHabitat()));
        local.put(_S_BASIC_ID_CLUES, Util.escapeHtml(regionSpecies.getBasic_id_cues()));
        local.put(_S_CONSUMER_ADVICE, Util.escapeHtml(regionSpecies.getConsumer_advice()));
        local.put(_S_ENFORCEMENT_ADVICE, Util.escapeHtml(regionSpecies.getEnforcement_advice()));
        local.put(_S_SIMILAR_ANIMALS, Util.escapeHtml(regionSpecies.getSimilar_animals()));
        local.put(_S_KNOWN_AS, Util.escapeHtml(regionSpecies.getKnown_as()));
        local.put(_S_AVERAGE_SIZE_WEIGHT, Util.escapeHtml(regionSpecies.getAverage_size_weight()));
        local.put(_S_FIRST_RESPONDER, Util.escapeHtml(regionSpecies.getFirst_responder()));
        local.put(_S_TRADED_AS, Util.escapeHtml(regionSpecies.getTraded_as()));
        local.put(_S_COMMON_TRAFFICKING, Util.escapeHtml(regionSpecies.getCommon_trafficking()));
        local.put(_S_NOTES, Util.escapeHtml(regionSpecies.getNotes()));
        local.put(_S_DISEASE_NAME, Util.escapeHtml(regionSpecies.getDisease_name()));
        local.put(_S_DISEASE_RISK_LEVEL, Util.escapeHtml(regionSpecies.getDisease_risk_level()));

        return local;
    }

}