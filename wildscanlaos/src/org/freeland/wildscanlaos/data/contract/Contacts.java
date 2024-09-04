package org.freeland.wildscanlaos.data.contract;

import org.freeland.wildscanlaos.R;
import org.freeland.wildscanlaos.data.provider.WildscanDataProvider;
import org.freeland.wildscanlaos.models.RegionContacts;
import org.freeland.wildscanlaos.models.response.ContactsResponse;
import org.freeland.wildscanlaos.util.Util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.text.TextUtils;
import android.view.textservice.TextInfo;

/**
 * @author Noam
 */
public class Contacts implements BaseColumns {

    public static ContactsResponse contactsResponse;

    public static final String TABLE_NAME = "contact";

    // local table fields
    public static final String _NAME = "_name";
    public static final String _EMAIL = "_email";
    public static final String _AVATAR = "_avatar";
    public static final String _DETAILS = "_details";
    public static final String _WEBSITE = "_website";
    public static final String _COUNTRY = "_country";
    public static final String _FAV = "_fav";

    // server-side fields
    public static final String _S_ID = "id";
    public static final String _S_REGION = "region";
    public static final String _S_NAME = "name";
    public static final String _S_AVATAR = "avatar";
    public static final String _S_TYPE = "type";
    public static final String _S_AGENCY = "agency";
    public static final String _S_JURISDICTION_SCOPE = "jurisdiction_scope";
    public static final String _S_SPECIALCAPACITY_NOTE = "specialcapacity_note";
    public static final String _S_EMAIL = "email";
    public static final String _S_PHONE = "phone";
    public static final String _S_ADDRESS1 = "address1";
    public static final String _S_ADDRESS2 = "address2";
    public static final String _S_CITY = "city";
    public static final String _S_COUNTRY = "country";
    public static final String _S_WEBSITE = "website";
    public static final String _S_AVAILABILITY = "availability";
    public static final String _S_LAT = "lat";
    public static final String _S_LON = "lon";
    public static final String _S_UTM = "utm";
    public static final String _S_CREATED_BY = "created_by";
    public static final String _S_CREATED_DATE = "created_date";
    public static final String _S_UPDATED_BY = "updated_by";
    public static final String _S_UPDATED_DATE = "updated_date";

    @SuppressLint("NewApi")
    public static final ContentValues convertRemoteFieldsToLocal(Context context, ContentValues remote) {
        ContentValues local = new ContentValues(6);

        String email_label = context.getResources().getString(R.string.contacts_list_label_email),
                phone_label = context.getResources().getString(R.string.contacts_list_label_phone),
                country_label = "Country",
                region_label = "Region",
                type_label = "Type";

        long id = remote.getAsLong(_S_ID);
        String name = remote.getAsString(_S_NAME),
                email = remote.getAsString(_S_EMAIL),
                type = remote.getAsString(_S_TYPE),
                country = remote.getAsString(_S_COUNTRY),
                region = remote.getAsString(_S_REGION),
                capacity = remote.getAsString(_S_SPECIALCAPACITY_NOTE),
                agency = remote.getAsString(_S_AGENCY),
                phone = remote.getAsString(_S_PHONE),
                address1 = remote.getAsString(_S_ADDRESS1),
                address2 = remote.getAsString(_S_ADDRESS2),
                website = remote.getAsString(_S_WEBSITE),
                lat = remote.getAsString(_S_LAT),
                lon = remote.getAsString(_S_LON);
        String region_string = new String();
        if (TextUtils.isEmpty(agency) || agency.equalsIgnoreCase("n/a") || agency.equalsIgnoreCase("not applicable"))
            agency = null;
        /*
        if (region.equals("1")) {
            region_string = "Global";
        }
        else if (region.equals("2")){
            region_string = "Southeast Asia";
        }
        else if (region.equals("3")) {
            region_string = "West Africa";
        }
        */
        String type_str;
        if (type.equals("ENV")) {
            type_str = "CITES/Wildlife/Environment";
        }
        else if (type.equals("VET")) {
            type_str = "Veterinarian";
        }
        else {
            type_str = type;
        }

        String details = ("<b>" + name + "</b>" +
                (TextUtils.isEmpty(type_str) ? "" : "<br>" + type_label + ": " + type_str) + "<br>" +
                (TextUtils.isEmpty(country) ? "" : "<br>" + country_label + ": " + country) + "<br>" +
                /*(TextUtils.isEmpty(region) ? "" : "<br>" + region_label + ": " + region_string) + "<br>" +*/
                (TextUtils.isEmpty(capacity) ? "" : "<br>" + Util.escapeHtml(capacity)) + "<br>" +
                (TextUtils.isEmpty(email) ? "" : "<br>" + email_label + ": " + email) +
                (TextUtils.isEmpty(phone) ? "" : "<br>" + phone_label + ": " + phone) + "<br>" +
                (TextUtils.isEmpty(agency) ? "" : "<br>" + Util.escapeHtml(agency)) + "<br>" +
                (TextUtils.isEmpty(address1) ? "" : "<br>" + Util.escapeHtml(address1)) +
                (TextUtils.isEmpty(address2) ? "" : "<br>" + Util.escapeHtml(address2)) +
                (TextUtils.isEmpty(lat) ? "" : "<br>Lat/Lon" + Util.escapeHtml(lat) + "," + Util.escapeHtml(lon))
                /* + "Website: " + (TextUtils.isEmpty(website)?"":"<br><br>" + website)*/
        ).replaceAll("(<br>)+$", "").replaceAll("<br><br>(<br>)+", "<br><br>");

        if (!TextUtils.isEmpty(website) && !website.startsWith("http://") && !website.startsWith("https://"))
            website = "http://" + website;

        Uri uri = ContentUris.withAppendedId(WildscanDataProvider.getTableUri(TABLE_NAME), id);
        Cursor c = context.getContentResolver().query(uri, new String[]{_ID, _FAV}, null, null, null);
        if (c.moveToFirst())
            DatabaseUtils.cursorRowToContentValues(c, local);
        else {
            local.put(_ID, id);
            local.put(_FAV, 0);
        }
        c.close();

        local.put(_NAME, name);
        local.put(_EMAIL, TextUtils.isEmpty(email) ? null : email);
        local.put(_COUNTRY, TextUtils.isEmpty(country) ? null : country);
        local.put(_S_TYPE, TextUtils.isEmpty(type) ? null : type);
        local.put(_S_AGENCY, TextUtils.isEmpty(agency) ? null : agency);
        String avatar = remote.getAsString(_S_AVATAR);
        local.put(_AVATAR, TextUtils.isEmpty(avatar) ? null : avatar);
        local.put(_DETAILS, TextUtils.isEmpty(details) ? null : details);
        local.put(_WEBSITE, TextUtils.isEmpty(website) ? null : website);
        //String country = remote.getAsString(_S_COUNTRY);
        local.put(_COUNTRY, TextUtils.isEmpty(country) ? null : country);
        local.put(_S_SPECIALCAPACITY_NOTE, TextUtils.isEmpty(capacity) ? null : capacity);
        local.put(_S_PHONE, TextUtils.isEmpty(phone) ? null : phone);

        return local;
    }

    @SuppressLint("NewApi")
    public static final ContentValues convertRemoteContactsToLocal(Context context, RegionContacts regionContacts) {
        ContentValues local = new ContentValues(6);

        String email_label = context.getResources().getString(R.string.contacts_list_label_email),
                phone_label = context.getResources().getString(R.string.contacts_list_label_phone),
                country_label = "Country",
                region_label = "Region",
                type_label = "Type",
                latlon;

        long id = Long.parseLong(regionContacts.getId());
        String name = regionContacts.getName(),
                email = regionContacts.getEmail(),
                type = regionContacts.getType(),
                country = regionContacts.getCountry(),
                region = regionContacts.getRegion(),
                capacity = regionContacts.getSpecialcapacity_note(),
                agency = regionContacts.getAgency(),
                phone = regionContacts.getPhone(),
                address1 = regionContacts.getAddress1(),
                address2 = regionContacts.getAddress2(),
                website = regionContacts.getWebsite(),
                lat = regionContacts.getLat(),
                lon = regionContacts.getLon();
        String region_string = new String();
        if (region.equals("1")) {
            region_string = "Global";
        } else if (region.equals("2")) {
            region_string = "Southeast Asia";
        } else {
            region_string = "West Africa";
        }
        if (TextUtils.isEmpty(agency) || agency.equalsIgnoreCase("n/a") || agency.equalsIgnoreCase("not applicable")) {
            agency = null;
        }
        if (lat!="null") {
            latlon = "<br>Lat/Lon : " + lat + "," +lon;
        }
        else {
            latlon = "";
        }
        String type_str;

        if (type.equals("ENV")) {
            type_str = "CITES/Wildlife/Environment";
        }
        else if (type.equals("VET")) {
            type_str = "Veterinarian";
        }
        else {
            type_str = type;
        }

        String details = ("<b>" + name  + "</b>" +
                (TextUtils.isEmpty(type) ? "" : "<br>" + type_label + ": " + type_str) + "<br>" +
                (TextUtils.isEmpty(country) ? "" : "<br>" + country_label + ": " + country) + "<br>" +
                /*(TextUtils.isEmpty(region) ? "" : "<br>" + region_label + ": " + region_string) + "<br>" +*/
                (TextUtils.isEmpty(capacity) ? "" : "<br>" + Util.escapeHtml(capacity)) + "<br>" +
                (TextUtils.isEmpty(email) ? "" : "<br>" + email_label + ": " + email) +
                (TextUtils.isEmpty(phone) ? "" : "<br>" + phone_label + ": " + phone) + "<br>" +
					/*"Agency: " + */(agency == null ? "" : "<br>" + Util.escapeHtml(agency)) +
					/*"Address: " + */(TextUtils.isEmpty(address1) ? "" : "<br>" + Util.escapeHtml(address1)) +
					/*"         " + */(TextUtils.isEmpty(address2) ? "" : "<br>" + Util.escapeHtml(address2)) +  latlon
                    /* + Website: " + (TextUtils.isEmpty(website)?"":"<br><br>" + website)*/
        ).replaceAll("(<br>)+$", "").replaceAll("<br><br>(<br>)+", "<br><br>");

        if (!TextUtils.isEmpty(website) && !website.startsWith("http://") && !website.startsWith("https://"))
            website = "http://" + website;

        Uri uri = ContentUris.withAppendedId(WildscanDataProvider.getTableUri(TABLE_NAME), id);
        Cursor c = context.getContentResolver().query(uri, new String[]{_ID, _FAV}, null, null, null);
        if (c.moveToFirst())
            DatabaseUtils.cursorRowToContentValues(c, local);
        else {
            local.put(_ID, id);
            local.put(_FAV, 0);
        }
        c.close();

        local.put(_NAME, name);
        local.put(_S_REGION, regionContacts.getRegion());
        local.put(_EMAIL, TextUtils.isEmpty(email) ? null : email);
        String avatar = regionContacts.getAvatar();
        local.put(_AVATAR, TextUtils.isEmpty(avatar) ? null : avatar);
        local.put(_DETAILS, TextUtils.isEmpty(details) ? null : details);
        local.put(_WEBSITE, TextUtils.isEmpty(website) ? null : website);
        //String country = regionContacts.getCountry();
        local.put(_COUNTRY, TextUtils.isEmpty(country) ? null : country);
        local.put(_S_SPECIALCAPACITY_NOTE, TextUtils.isEmpty(capacity) ? null : capacity);
        local.put(_S_PHONE, TextUtils.isEmpty(phone) ? null : phone);

        return local;
    }
//

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            _NAME + " TEXT NOT NULL," +
            _S_REGION + " TEXT NOT NULL," +
            _AVATAR + " TEXT DEFAULT NULL," +
            _EMAIL + " TEXT DEFAULT NULL," +
            _DETAILS + " TEXT DEFAULT NULL," +
            _COUNTRY + " TEXT DEFAULT NULL," +
            _WEBSITE + " TEXT DEFAULT NULL," +
            _FAV + " BOOLEAN DEFAULT 0," +
            _S_SPECIALCAPACITY_NOTE + " TEXT DEFAULT NULL," +
            _S_PHONE + " TEXT DEFAULT NULL" +
            ");";
//				_TYPE + " TEXT DEFAULT NULL," + 
//				_AGENCY + " TEXT DEFAULT NULL," + 
//				_JURISDICTION_SCOPE + " TEXT DEFAULT NULL," + 
//				_SPECIALCAPACITY_NOTE + " TEXT DEFAULT NULL," + 
//				_PHONE + " TEXT DEFAULT NULL," + 
//				_ADDRESS1 + " TEXT DEFAULT NULL," + 
//				_ADDRESS2 + " TEXT DEFAULT NULL," + 
//				_CITY + " TEXT DEFAULT NULL," + 
//				_WEBSITE + " TEXT DEFAULT NULL," + 
//				_AVAILABILITY + " BOOLEAN DEFAULT 0," + 
//				_LAT + " REAL DEFAULT NULL," + 
//				_LON + " REAL DEFAULT NULL," + 
//				_UTM + " TEXT DEFAULT NULL," + 
//				_CREATED_BY + " INTEGER DEFAULT NULL," + 
//				_CREATED_DATE + " TEXT NOT NULL," + 
//				_UPDATED_BY + " INTEGER DEFAULT NULL," + 
//				_UPDATED_DATE + " TEXT DEFAULT NULL," +
//		
//		static final String SELECT_FOR_UPDATE = "SELECT " +
//				_S_ID + " AS " + _ID + "," + 
//				_NAME + "," + _AVATAR + "," + _TYPE + "," + _AGENCY + "," + 
//				_JURISDICTION_SCOPE + "," + _SPECIALCAPACITY_NOTE + "," + 
//				_EMAIL + "," + _PHONE + "," + _ADDRESS1 + "," + _ADDRESS2 + "," + 
//				_CITY + "," + _COUNTRY + "," + _WEBSITE + "," + 
//				_AVAILABILITY + "," + _LAT + "," + _LON + "," + _UTM + "," + 
//				_CREATED_BY + "," + _CREATED_DATE + "," + 
//				_UPDATED_BY + "," + _UPDATED_DATE + "," +
//				_FAV + " FROM " + TABLE_NAME + "_tmp";		
}