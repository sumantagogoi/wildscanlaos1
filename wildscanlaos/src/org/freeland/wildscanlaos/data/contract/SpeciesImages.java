package org.freeland.wildscanlaos.data.contract;

import android.content.ContentValues;
import android.content.Context;

import org.freeland.wildscanlaos.models.RegionSpeciesImages;

/**
 * @author Noam
 */
public class SpeciesImages {
    public static final String TABLE_NAME = "species_image";

    public static final String _S_SPECIES_ID = "species_id";
    public static final String _S_DEFAULT_ORDER = "default_order";
    public static final String _S_USER_ORDER = "user_order";
    public static final String _S_PATH_IMAGE = "path_image";
    public static final String _S_CREDIT = "credit";
    public static final String _S_LICENSE = "license";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _S_SPECIES_ID + " INTEGER NOT NULL, " +
            _S_DEFAULT_ORDER + " INTEGER NOT NULL, " +
            _S_USER_ORDER + " INTEGER DEFAULT NULL, " +
            _S_PATH_IMAGE + " TEXT DEFAULT NULL, " +
            _S_CREDIT + " TEXT DEFAULT NULL, " +
            _S_LICENSE + " INTEGER DEFAULT NULL, " +
            "PRIMARY KEY (" + _S_SPECIES_ID + "," + _S_DEFAULT_ORDER + "));";

//	public static final String[] LICENSE = { "",
//		 "Creative Commons Attribution-Share Alike 2.0 Generic",
//		 "Creative Commons Attribution-Share Alike 2.5 Generic",
//		 "Creative Commons Attribution-Share Alike 3.0 Unported",
//		 "GNU Free Documentation License",
//		 "Public Domain",
//		 "FREELAND photo license"
//		 };

    public static final ContentValues convertRemoteFieldsToLocal(Context context, RegionSpeciesImages speciesImages) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(_S_SPECIES_ID, speciesImages.getSpeciesId());
        contentValues.put(_S_DEFAULT_ORDER, speciesImages.getImageOrder());
//        contentValues.put(_S_USER_ORDER, speciesImages.getDefaultOrder());
        contentValues.put(_S_PATH_IMAGE, /*AppPreferences.getImageBaseLink(context)+*/speciesImages.getImagePath());
        contentValues.put(_S_CREDIT, speciesImages.getCredit());
        contentValues.put(_S_LICENSE, speciesImages.getLicense());

        return contentValues;
    }
}