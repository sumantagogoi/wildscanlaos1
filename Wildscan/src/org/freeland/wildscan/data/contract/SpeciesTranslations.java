package org.freeland.wildscan.data.contract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freeland.wildscan.util.Util;
import org.freeland.wildscan.R;

import android.content.ContentValues;
import android.content.Context;

/**
 * @author Noam
 *
 */
public class SpeciesTranslations {
	public static final String TABLE_NAME = "species_translations";

	public static final String _S_SPECIES_ID = "species_id";

	// fields in summary info
	public static final String _S_LANGUAGE = "language";
//	public static final String _S_COMMON_NAME = "common_name";

	// fields for details view
//	public static final String _S_HABITAT = "habitat";
//	public static final String _S_BASIC_ID_CLUES = "basic_id_cues";
//	public static final String _S_CONSUMER_ADVICE = "consumer_advice";
//	public static final String _S_ENFORCEMENT_ADVICE = "enforcement_advice";
//	public static final String _S_SIMILAR_ANIMALS = "similar_animals";
//	public static final String _S_KNOWN_AS = "known_as";
//	public static final String _S_AVERAGE_SIZE_WEIGHT = "average_size_weight";
//	public static final String _S_FIRST_RESPONDER = "first_responder";
//	public static final String _S_TRADED_AS = "traded_as";
//	public static final String _S_COMMON_TRAFFICKING = "common_trafficking";
//	public static final String _S_NOTES = "notes";
//	public static final String _S_DISEASE_NAME = "disease_name";

	public static final Set<String> TRANSLATED_FIELDS;
	static {
		TRANSLATED_FIELDS = new HashSet<String>();
		TRANSLATED_FIELDS.add(Species._S_COMMON_NAME);
		TRANSLATED_FIELDS.add(Species._S_HABITAT);
		TRANSLATED_FIELDS.add(Species._S_BASIC_ID_CLUES);
		TRANSLATED_FIELDS.add(Species._S_CONSUMER_ADVICE);
		TRANSLATED_FIELDS.add(Species._S_ENFORCEMENT_ADVICE);
		TRANSLATED_FIELDS.add(Species._S_SIMILAR_ANIMALS);
		TRANSLATED_FIELDS.add(Species._S_KNOWN_AS);
		TRANSLATED_FIELDS.add(Species._S_AVERAGE_SIZE_WEIGHT);
		TRANSLATED_FIELDS.add(Species._S_FIRST_RESPONDER);
		TRANSLATED_FIELDS.add(Species._S_TRADED_AS);
		TRANSLATED_FIELDS.add(Species._S_COMMON_TRAFFICKING);
		TRANSLATED_FIELDS.add(Species._S_NOTES);
		TRANSLATED_FIELDS.add(Species._S_DISEASE_NAME);
	}

	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
			_S_SPECIES_ID + " INTEGER NOT NULL, " +
			_S_LANGUAGE + " TEXT NOT NULL," +
			Species._S_COMMON_NAME + " TEXT DEFAULT NULL," +
			Species._S_HABITAT + " TEXT DEFAULT NULL," +
			Species._S_BASIC_ID_CLUES + " TEXT DEFAULT NULL," +
			Species._S_CONSUMER_ADVICE + " TEXT DEFAULT NULL," +
			Species._S_ENFORCEMENT_ADVICE + " TEXT DEFAULT NULL," +
			Species._S_SIMILAR_ANIMALS + " TEXT DEFAULT NULL," +
			Species._S_KNOWN_AS + " TEXT DEFAULT NULL," +
			Species._S_AVERAGE_SIZE_WEIGHT + " TEXT DEFAULT NULL," +
			Species._S_FIRST_RESPONDER + " TEXT DEFAULT NULL," +
			Species._S_TRADED_AS + " TEXT DEFAULT NULL," +
			Species._S_COMMON_TRAFFICKING + " TEXT DEFAULT NULL," +
			Species._S_NOTES + " TEXT DEFAULT NULL," +
			Species._S_DISEASE_NAME + " TEXT DEFAULT NULL," +
			"PRIMARY KEY (" + _S_SPECIES_ID + "," + _S_LANGUAGE + "));";

	public static final ContentValues convertRemoteFieldsToLocal(Context context, ContentValues remote) {
		ContentValues local = new ContentValues();

		local.put(_S_SPECIES_ID, remote.getAsLong(_S_SPECIES_ID));
		local.put(_S_LANGUAGE, remote.getAsString(_S_LANGUAGE));
		local.put(Species._S_COMMON_NAME, Util.escapeHtmlOrNull(remote.getAsString(Species._S_COMMON_NAME)));
		local.put(Species._S_HABITAT, Util.escapeHtmlOrNull(remote.getAsString(Species._S_HABITAT)));
		local.put(Species._S_BASIC_ID_CLUES, Util.escapeHtmlOrNull(remote.getAsString(Species._S_BASIC_ID_CLUES)));
		local.put(Species._S_CONSUMER_ADVICE, Util.escapeHtmlOrNull(remote.getAsString(Species._S_CONSUMER_ADVICE)));
		local.put(Species._S_ENFORCEMENT_ADVICE, Util.escapeHtmlOrNull(remote.getAsString(Species._S_ENFORCEMENT_ADVICE)));
		local.put(Species._S_SIMILAR_ANIMALS, Util.escapeHtmlOrNull(remote.getAsString(Species._S_SIMILAR_ANIMALS)));
		local.put(Species._S_KNOWN_AS, Util.escapeHtmlOrNull(remote.getAsString(Species._S_KNOWN_AS)));
		local.put(Species._S_AVERAGE_SIZE_WEIGHT, Util.escapeHtmlOrNull(remote.getAsString(Species._S_AVERAGE_SIZE_WEIGHT)));
		local.put(Species._S_FIRST_RESPONDER, Util.escapeHtmlOrNull(remote.getAsString(Species._S_FIRST_RESPONDER)));
		local.put(Species._S_TRADED_AS, Util.escapeHtmlOrNull(remote.getAsString(Species._S_TRADED_AS)));
		local.put(Species._S_COMMON_TRAFFICKING, Util.escapeHtmlOrNull(remote.getAsString(Species._S_COMMON_TRAFFICKING)));
		local.put(Species._S_NOTES, Util.escapeHtmlOrNull(remote.getAsString(Species._S_NOTES)));
		local.put(Species._S_DISEASE_NAME, Util.escapeHtmlOrNull(remote.getAsString(Species._S_DISEASE_NAME)));

		return local;
	}
	
	public static final Map<String,Integer> DISEASE_LEVEL_STRINGS;
	static {
		DISEASE_LEVEL_STRINGS = new HashMap<String,Integer>();
		DISEASE_LEVEL_STRINGS.put("Low", R.string.species_details_label_warning_disease_risk_low);
		DISEASE_LEVEL_STRINGS.put("Medium", R.string.species_details_label_warning_disease_risk_medium);
		DISEASE_LEVEL_STRINGS.put("High", R.string.species_details_label_warning_disease_risk_high);
	}

	public static final Map<String,Integer> WARNING_STRINGS;
	static {
		WARNING_STRINGS = new HashMap<String,Integer>();
		WARNING_STRINGS.put("Dangerous", R.string.species_details_label_warning_dangerous);
		WARNING_STRINGS.put("Poisonous", R.string.species_details_label_warning_poisonous);
		WARNING_STRINGS.put("Nocturnal", R.string.species_details_label_warning_nocturnal);
	}

	public static final Map<String,Integer> CITES_APPENDIX_STRINGS;
	static {
		CITES_APPENDIX_STRINGS = new HashMap<String,Integer>();
		CITES_APPENDIX_STRINGS.put("Appendix I", R.string.species_details_label_cites_appendix_1);
		CITES_APPENDIX_STRINGS.put("Appendix II", R.string.species_details_label_cites_appendix_2);
		CITES_APPENDIX_STRINGS.put("Appendix III", R.string.species_details_label_cites_appendix_3);
	}

	public static final Map<String,Integer> IUCN_ABBREVIATION_STRINGS;
	static {
		IUCN_ABBREVIATION_STRINGS = new HashMap<String,Integer>();
		IUCN_ABBREVIATION_STRINGS.put("LC",R.string.species_details_label_status_lc);
		IUCN_ABBREVIATION_STRINGS.put("NT",R.string.species_details_label_status_nt);
		IUCN_ABBREVIATION_STRINGS.put("VU",R.string.species_details_label_status_vu);
		IUCN_ABBREVIATION_STRINGS.put("EN",R.string.species_details_label_status_en);
		IUCN_ABBREVIATION_STRINGS.put("CR",R.string.species_details_label_status_cr);
		IUCN_ABBREVIATION_STRINGS.put("EW",R.string.species_details_label_status_ew);
		IUCN_ABBREVIATION_STRINGS.put("EX",R.string.species_details_label_status_ex);
		IUCN_ABBREVIATION_STRINGS.put("NA",R.string.species_details_label_status_na);
	}

}