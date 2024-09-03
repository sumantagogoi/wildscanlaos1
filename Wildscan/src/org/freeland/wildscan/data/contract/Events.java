package org.freeland.wildscan.data.contract;

/**
 * @author Noam
 *
 */
public class Events {
	public static final String TABLE_NAME = "species_event";		
	public static final String COL_NAME_ID = "id";
	static final String COL_NAME_EVENT_DESCRIPTION = "event_description";
	static final String COL_NAME_KNOWN_LOCATION = "known_location";
	static final String COL_NAME_LAT = "lat";
	static final String COL_NAME_LON = "lon";
	static final String COL_NAME_REPORTING_BY = "reporting_by";
	static final String COL_NAME_REPORTING_DATE = "reporting_date";
	static final String COL_NAME_SPECIES_ID = "species_id";
	static final String COL_NAME_STATUS_HIDE = "status_hide";
	static final String COL_NAME_STATUS_VERIFIED = "status_verified";
	static final String COL_NAME_STATUS_PUBLISH = "status_publish";
	static final String COL_NAME_STATUS_STAR = "status_star";
	static final String COL_NAME_STATUS_SHARE_VERFIED = "status_share_verfied";
	static final String COL_NAME_STATUS_SHARE_NONVERIFIED = "status_share_nonverified";
	static final String COL_NAME_CREATED_BY = "created_by";
	static final String COL_NAME_CREATED_DATE = "created_date";
	static final String COL_NAME_UPDATED_BY = "updated_by";
	static final String COL_NAME_UPDATED_DATE = "updated_date";
	  
	static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
			COL_NAME_ID + " INTEGER PRIMARY KEY, " + 
			COL_NAME_EVENT_DESCRIPTION + " TEXT DEFAULT NULL, " + 
			COL_NAME_KNOWN_LOCATION + " TEXT DEFAULT NULL, " + 
			COL_NAME_LAT + " REAL DEFAULT NULL, " + 
			COL_NAME_LON + " REAL DEFAULT NULL, " + 
			COL_NAME_REPORTING_BY + " INTEGER NOT NULL, " + 
			COL_NAME_REPORTING_DATE + " TEXT NOT NULL, " + 
			COL_NAME_SPECIES_ID + " INTEGER NOT NULL, " + 
			COL_NAME_STATUS_HIDE + " BOOLEAN NOT NULL DEFAULT 1, " +
			COL_NAME_STATUS_VERIFIED + " BOOLEAN NOT NULL DEFAULT 0, " +
			COL_NAME_STATUS_PUBLISH + " BOOLEAN NOT NULL DEFAULT 0, " +
			COL_NAME_STATUS_STAR+ " BOOLEAN NOT NULL DEFAULT 0, " +
			COL_NAME_STATUS_SHARE_VERFIED + " BOOLEAN NOT NULL DEFAULT 0, " +
			COL_NAME_STATUS_SHARE_NONVERIFIED + " BOOLEAN NOT NULL DEFAULT 0, " +
			COL_NAME_CREATED_BY + " INTEGER NOT NULL, " + 
			COL_NAME_CREATED_DATE + " TEXT NOT NULL, " + 
			COL_NAME_UPDATED_BY + " INTEGER DEFAULT NULL, " + 
			COL_NAME_UPDATED_DATE + " TEXT DEFAULT NULL" +
			");";

}