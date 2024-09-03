package org.freeland.wildscan.data.contract;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.freeland.wildscan.EventInfo;
import org.freeland.wildscan.data.WildscanDataManager;
import org.freeland.wildscan.models.EventsModel;
import org.freeland.wildscan.util.AppConstants;
import org.freeland.wildscan.util.AppPreferences;
import org.freeland.wildscan.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import org.apache.http.client.methods.HttpGet;

public class Incidents implements BaseColumns {
    // display fields: title, content, date/time, location, photo, suspect-name, route (origin, destination, logistics), method
    public static final String TABLE_NAME = "submit_report";

    // local fields
    public static final String _PHOTO = "_photo";
    public static final String _COUNTRY = "_country";
    public static final String _DATE = "_date";

    // summary
    public static final String _S_INCIDENT = "incident";
    public static final String _S_LOCATION_LAT = "location_lat";
    public static final String _S_LOCATION_LON = "location_lon";

    // details
    public static final String _S_INCIDENT_DATE = "incident_date";
    public static final String _S_LOCATION_ADDRESS = "location_address";
    public static final String _S_SPECIES = "species";
    public static final String _S_NUMBER = "number";
    public static final String _S_NUMBER_UNIT = "number_unit";
    public static final String _S_INCIDENT_CONDITION = "incident_condition";
    public static final String _S_OFFENSE_TYPE = "offense_type";
    public static final String _S_OFFENSE_DESCRIPTION = "offense_description";
    public static final String _S_METHOD = "method";
    public static final String _S_VALUE_ESTIMATED_USD = "value_estimated_usd";
    public static final String _S_ORIGIN_ADDRESS = "origin_address";
    public static final String _S_ORIGIN_COUNTRY = "origin_country";
    public static final String _S_ORIGIN_LAT = "origin_lat";
    public static final String _S_ORIGIN_LON = "origin_lon";
    public static final String _S_DESTINATION_ADDRESS = "destination_address";
    public static final String _S_DESTINATION_COUNTRY = "destination_country";
    public static final String _S_DESTINATION_LAT = "destination_lat";
    public static final String _S_DESTINATION_LON = "destination_lon";
    public static final String _S_VEHICLE_VESSEL_DESCRIPTION = "vehicle_vessel_description";
    public static final String _S_VEHICLE_VESSEL_LICENSE_NUMBER = "vehicle_vessel_license_number";
    public static final String _S_VESSEL_NAME = "vessel_name";
    public static final String _S_SHARE_WITH = "share_with";

    public static final String _S_PRIVATE = "status";

    // server-only
    public static final String _S_ID = "id";
    public static final String _S_SYNDICATE = "syndicate";
    public static final String _S_CREATED_BY = "created_by";
//	public static final String _S_INTERNET_INCIDENT = "internet_incident";
//	public static final String _S_WEB_ADDRESS = "web_address";
//	public static final String _S_CREATED_DATE = "created_date";
//	public static final String _S_UPDATED_BY = "updated_by";
//	public static final String _S_UPDATED_DATE = "updated_date";

    //	private static final String _S_IMAGES_TABLE_NAME = "submit_report_image";
//	private static final String _S_IMAGES_COL_NAME_REPORT_ID = "submit_report_id";
//	private static final String _S_IMAGES_COL_NAME_USER_ORDER = "user_order";
//	private static final String _S_IMAGES_COL_NAME_DEFAULT_ORDER = "default_order";
    private static final String _S_IMAGES_COL_NAME_PATH = "path_image";

    public static final ContentValues convertRemoteFieldsToLocal(Context context, ContentValues remote) {
        SQLiteDatabase db = WildscanDataManager.getInstance(context).getReadableDatabase();
        ContentValues local = new ContentValues(26);

        long id = remote.getAsLong(_S_ID);
        String photo = null, address = remote.getAsString(_S_LOCATION_ADDRESS), country = "Unknown";
        double lat = remote.getAsDouble(_S_LOCATION_LAT),
                lon = remote.getAsDouble(_S_LOCATION_LON);
        String datestring = remote.getAsString(_S_INCIDENT_DATE), date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date d = sdf.parse(datestring);
            DateFormat df = DateFormat.getDateInstance();
            date = df.format(d);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        String select = Incidents._ID + "==" + String.valueOf(id);
        Cursor c = db.query(TABLE_NAME, null, select, null, null, null, null);
        if (c.moveToFirst())
            DatabaseUtils.cursorRowToContentValues(c, local);
        c.close();
        try {
            // retrieve photo url:
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
            requestParams.add(new BasicNameValuePair("report_id", String.valueOf(id)));

            HttpClient httpClient = Util.getHttpClient(context);
            HttpPost httpRequest = new HttpPost(AppConstants.REMOTE_SERVER_URL + WildscanDataManager
                    .REMOTE_PHP_QUERY_REPORT_PHOTO);
            httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
            httpRequest.addHeader("Cache-Control", "no-cache");
            HttpResponse response = httpClient.execute(httpRequest);

            if (response.getStatusLine().getStatusCode() / 100 == 2) {
                InputStream is = response.getEntity().getContent();
                String res = Util.readResponse(is);
                is.close();
                StringReader isr = new StringReader(res);

                JsonReader reader = new JsonReader(isr);
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        // skip null
                        if (reader.peek() == JsonToken.NULL)
                            reader.nextNull();
                        else if (name.equals(_S_IMAGES_COL_NAME_PATH))
                            photo = reader.nextString();
                        else
                            // skip token
                            reader.nextString();
                    }
                    reader.endObject();
                }
                reader.endArray();
                reader.close();
                isr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Locale en = new Locale("en");
            Geocoder geocoder = new Geocoder(context.getApplicationContext(), en);
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && addresses.size() > 0)
                country = addresses.get(0).getCountryName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(country)) {
            country = address;
        }
        int start = country.length() - EventInfo.MAX_CHARS_COUNTRY;
        if (start > 0)
            country = ".." + country.substring(start + 2);
        if (TextUtils.isEmpty(photo))
            photo = null;
        else
            photo = AppPreferences.getImageBaseLink(context) + photo;

        local.put(_ID, id);
        local.put(_PHOTO, photo);
        local.put(_COUNTRY, country);
        local.put(_DATE, date);
        local.put(_S_INCIDENT_DATE, datestring);
        local.put(_S_INCIDENT, Util.escapeHtml(remote.getAsString(_S_INCIDENT)));
        local.put(_S_LOCATION_ADDRESS, address);
        local.put(_S_LOCATION_LAT, lat);
        local.put(_S_LOCATION_LON, lon);
        local.put(_S_SPECIES, remote.getAsString(_S_SPECIES));
        local.put(_S_NUMBER, remote.getAsString(_S_NUMBER));
        local.put(_S_NUMBER_UNIT, remote.getAsString(_S_NUMBER_UNIT));
        local.put(_S_INCIDENT_CONDITION, remote.getAsString(_S_INCIDENT_CONDITION));
        local.put(_S_OFFENSE_TYPE, remote.getAsString(_S_OFFENSE_TYPE));
        local.put(_S_OFFENSE_DESCRIPTION, Util.escapeHtml(remote.getAsString(_S_OFFENSE_DESCRIPTION)));
        local.put(_S_METHOD, remote.getAsString(_S_METHOD));
        local.put(_S_VALUE_ESTIMATED_USD, remote.getAsString(_S_VALUE_ESTIMATED_USD));
        local.put(_S_ORIGIN_ADDRESS, remote.getAsString(_S_ORIGIN_ADDRESS));
        local.put(_S_ORIGIN_COUNTRY, remote.getAsString(_S_ORIGIN_COUNTRY));
        local.put(_S_ORIGIN_LAT, remote.getAsString(_S_ORIGIN_LAT));
        local.put(_S_ORIGIN_LON, remote.getAsString(_S_ORIGIN_LON));
        local.put(_S_DESTINATION_ADDRESS, remote.getAsString(_S_DESTINATION_ADDRESS));
        local.put(_S_DESTINATION_COUNTRY, remote.getAsString(_S_DESTINATION_COUNTRY));
        local.put(_S_DESTINATION_LAT, remote.getAsString(_S_DESTINATION_LAT));
        local.put(_S_DESTINATION_LON, remote.getAsString(_S_DESTINATION_LON));
        local.put(_S_VEHICLE_VESSEL_DESCRIPTION, Util.escapeHtml(remote.getAsString(_S_VEHICLE_VESSEL_DESCRIPTION)));
        local.put(_S_VEHICLE_VESSEL_LICENSE_NUMBER, Util.escapeHtml(remote.getAsString(_S_VEHICLE_VESSEL_LICENSE_NUMBER)));
        local.put(_S_VESSEL_NAME, Util.escapeHtml(remote.getAsString(_S_VESSEL_NAME)));

        return local;
    }

    public static final ContentValues convertIncidentFieldsToLocal(Context context, EventsModel remote) {
        SQLiteDatabase db = WildscanDataManager.getInstance(context).getReadableDatabase();
        ContentValues local = new ContentValues(26);

        long id = Long.parseLong(remote.getId());
        String photo = null, address = remote.getLocationAddress(), country = "Unknown";
        double lat = Double.parseDouble(remote.getLocationLat()),
                lon = Double.parseDouble(remote.getLocationLon());
        String datestring = remote.getIncidentDate(), date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date d = sdf.parse(datestring);
            DateFormat df = DateFormat.getDateInstance();
            date = df.format(d);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        String select = Incidents._ID + "==" + String.valueOf(id);
        Cursor c = db.query(TABLE_NAME, null, select, null, null, null, null);
        if (c.moveToFirst())
            DatabaseUtils.cursorRowToContentValues(c, local);
        c.close();
     /*   try {
            // retrieve photo url:
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
            requestParams.add(new BasicNameValuePair("report_id", String.valueOf(id)));

            HttpClient httpClient = Util.getHttpClient(context);
            HttpPost httpRequest = new HttpPost(WildscanDataManager.getInstance(context).getRemoteBaseUrl() + WildscanDataManager.REMOTE_PHP_QUERY_REPORT_PHOTO);
            httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
            httpRequest.addHeader("Cache-Control", "no-cache");
            HttpResponse response = httpClient.execute(httpRequest);

            if (response.getStatusLine().getStatusCode() / 100 == 2) {
                InputStream is = response.getEntity().getContent();
                String res = Util.readResponse(is);
                is.close();
                StringReader isr = new StringReader(res);

                JsonReader reader = new JsonReader(isr);
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        // skip null
                        if (reader.peek() == JsonToken.NULL)
                            reader.nextNull();
                        else if (name.equals(_S_IMAGES_COL_NAME_PATH))
                            photo = reader.nextString();
                        else
                            // skip token
                            reader.nextString();
                    }
                    reader.endObject();
                }
                reader.endArray();
                reader.close();
                isr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        try {
            Locale en = new Locale("en");
            Geocoder geocoder = new Geocoder(context.getApplicationContext(), en);
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && addresses.size() > 0)
                country = addresses.get(0).getCountryName();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(country)) {
            country = address;
        }
        int start = country.length() - EventInfo.MAX_CHARS_COUNTRY;
        if (start > 0)
            country = ".." + country.substring(start + 2);

        photo = AppConstants.REMOTE_BASE_IMAGE_URL_WITHOUT_SLASH + remote.getImage();
        local.put(_ID, id);
        local.put(_PHOTO, photo);
        local.put(_COUNTRY, country);
        local.put(_DATE, date);
        local.put(_S_INCIDENT_DATE, datestring);
        local.put(_S_INCIDENT, Util.escapeHtml(remote.getIncident()));
        local.put(_S_LOCATION_ADDRESS, address);
        local.put(_S_LOCATION_LAT, lat);
        local.put(_S_LOCATION_LON, lon);
        local.put(_S_SPECIES, remote.getSpecies());
        local.put(_S_NUMBER, remote.getNumber());
        local.put(_S_NUMBER_UNIT, remote.getNumberUnit());
        local.put(_S_INCIDENT_CONDITION, remote.getIncidentCondition());
        local.put(_S_OFFENSE_TYPE, remote.getOffenseType());
        local.put(_S_OFFENSE_DESCRIPTION, Util.escapeHtml(remote.getOffenseDescription()));
        local.put(_S_METHOD, remote.getMethod());
        local.put(_S_VALUE_ESTIMATED_USD, remote.getValueEstimatedUsd());
        local.put(_S_ORIGIN_ADDRESS, remote.getOriginAddress());
        local.put(_S_ORIGIN_COUNTRY, remote.getOriginCountry());
        local.put(_S_ORIGIN_LAT, remote.getOriginLat());
        local.put(_S_ORIGIN_LON, remote.getOriginLon());
        local.put(_S_DESTINATION_ADDRESS, remote.getDestinationAddress());
        local.put(_S_DESTINATION_COUNTRY, remote.getDestinationCountry());
        local.put(_S_DESTINATION_LAT, remote.getDestinationLat());
        local.put(_S_DESTINATION_LON, remote.getDestinationLon());
        local.put(_S_VEHICLE_VESSEL_DESCRIPTION, Util.escapeHtml(remote.getVehicleVesselDescription()));
        local.put(_S_VEHICLE_VESSEL_LICENSE_NUMBER, Util.escapeHtml(remote.getVehicleVesselLicenseNumber()));
        local.put(_S_VESSEL_NAME, Util.escapeHtml(remote.getVesselName()));

        return local;
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            _PHOTO + " TEXT DEFAULT NULL," +
            _COUNTRY + " TEXT DEFAULT NULL," +
            _DATE + " TEXT NOT NULL," +
            _S_INCIDENT_DATE + " TEXT NOT NULL," +
            //		        _S_INTERNET_INCIDENT + " TEXT DEFAULT NULL," +
            _S_LOCATION_ADDRESS + " TEXT DEFAULT NULL," +
            //		        _S_WEB_ADDRESS + " TEXT DEFAULT NULL," +
            _S_LOCATION_LAT + " REAL DEFAULT NULL," +
            _S_LOCATION_LON + " REAL DEFAULT NULL," +
            _S_INCIDENT + " TEXT DEFAULT NULL," +
            _S_SPECIES + " INTEGER NOT NULL," +
            _S_NUMBER + " TEXT DEFAULT NULL," +
            _S_NUMBER_UNIT + " TEXT DEFAULT NULL," +
            _S_INCIDENT_CONDITION + " TEXT DEFAULT NULL," +
            _S_OFFENSE_TYPE + " TEXT DEFAULT NULL," +
            _S_OFFENSE_DESCRIPTION + " TEXT DEFAULT NULL," +
            _S_METHOD + " TEXT DEFAULT NULL," +
            _S_VALUE_ESTIMATED_USD + " TEXT DEFAULT NULL," +
            _S_ORIGIN_ADDRESS + " TEXT DEFAULT NULL," +
            _S_ORIGIN_COUNTRY + " TEXT DEFAULT NULL," +
            _S_ORIGIN_LAT + " REAL DEFAULT NULL," +
            _S_ORIGIN_LON + " REAL DEFAULT NULL," +
            _S_DESTINATION_ADDRESS + " TEXT DEFAULT NULL," +
            _S_DESTINATION_COUNTRY + " TEXT DEFAULT NULL," +
            _S_DESTINATION_LAT + " REAL DEFAULT NULL," +
            _S_DESTINATION_LON + " REAL DEFAULT NULL," +
            _S_VEHICLE_VESSEL_DESCRIPTION + " TEXT DEFAULT NULL," +
            _S_VEHICLE_VESSEL_LICENSE_NUMBER + " TEXT DEFAULT NULL," +
            _S_VESSEL_NAME + " TEXT DEFAULT NULL" +
//		        _S_VESSEL_NAME + " TEXT DEFAULT NULL," +
//		        _S_SHARE_WITH + " TEXT DEFAULT NULL," +
//		        _S_SYNDICATE + " TEXT DEFAULT 'H'," +
//		        _S_CREATED_BY + " INTEGER NOT NULL," +
//		        _S_CREATED_DATE + " TEXT NOT NULL," +
//		        _S_UPDATED_BY + " INTEGER DEFAULT NULL," +
//		        _S_UPDATED_DATE + " TEXT DEFAULT NULL" +
            ");";

    public static final String REMOTE_SELECT_COLUMNS =
            _S_ID + "," +
                    _S_INCIDENT_DATE + "," +
//	        _S_INTERNET_INCIDENT + "," +
                    _S_LOCATION_ADDRESS + "," +
//	        _S_WEB_ADDRESS + "," +
                    _S_LOCATION_LAT + "," +
                    _S_LOCATION_LON + "," +
                    _S_INCIDENT + "," +
                    _S_SPECIES + "," +
                    _S_NUMBER + "," +
                    _S_NUMBER_UNIT + "," +
                    _S_INCIDENT_CONDITION + "," +
                    _S_OFFENSE_TYPE + "," +
                    _S_OFFENSE_DESCRIPTION + "," +
                    _S_METHOD + "," +
                    _S_VALUE_ESTIMATED_USD + "," +
                    _S_ORIGIN_ADDRESS + "," +
                    _S_ORIGIN_COUNTRY + "," +
                    _S_ORIGIN_LAT + "," +
                    _S_ORIGIN_LON + "," +
                    _S_DESTINATION_ADDRESS + "," +
                    _S_DESTINATION_COUNTRY + "," +
                    _S_DESTINATION_LAT + "," +
                    _S_DESTINATION_LON + "," +
                    _S_VEHICLE_VESSEL_DESCRIPTION + "," +
                    _S_VEHICLE_VESSEL_LICENSE_NUMBER + "," +
                    _S_VESSEL_NAME;
//	        _S_VESSEL_NAME + "," +
//	        _S_SHARE_WITH + "," +
//	        _S_SYNDICATE + "" +
//	        _S_CREATED_BY + "," +
//	        _S_CREATED_DATE + "," +
//	        _S_UPDATED_BY + "," +
//	        _S_UPDATED_DATE;

}