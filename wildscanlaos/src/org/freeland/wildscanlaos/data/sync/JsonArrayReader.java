package org.freeland.wildscanlaos.data.sync;

import android.content.ContentValues;
import android.content.Context;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.freeland.wildscanlaos.App;
import org.freeland.wildscanlaos.PrefsFragment;
import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.Contacts;
import org.freeland.wildscanlaos.data.contract.Incidents;
import org.freeland.wildscanlaos.data.contract.Species;
import org.freeland.wildscanlaos.data.contract.SpeciesImages;
import org.freeland.wildscanlaos.data.contract.SpeciesTranslations;
//import org.freeland.wildscanapp.data.contract.StaticContent;
import org.freeland.wildscanlaos.models.EventsModel;
import org.freeland.wildscanlaos.models.RegionContacts;
import org.freeland.wildscanlaos.models.RegionSpecies;
import org.freeland.wildscanlaos.models.RegionSpeciesImages;
import org.freeland.wildscanlaos.models.StaticContentModel;
import org.freeland.wildscanlaos.util.AppConstants;
import org.freeland.wildscanlaos.util.AppPreferences;
import org.freeland.wildscanlaos.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

//import org.apache.http.client.methods.HttpGet;

public class JsonArrayReader {
    static final String SELECT_FORMAT = "(UNIX_TIMESTAMP(created_date)>" + "%d" + " OR UNIX_TIMESTAMP(updated_date)>" + "%d)";
    //		static final String SELECT_SP_IMAGES_FORMAT = SpeciesImages._S_SPECIES_ID + " IN (SELECT " + Species._S_ID + " FROM "
//				+ Species.TABLE_NAME + " WHERE " + SELECT_FORMAT + ")";
    private static ArrayList<RegionContacts> contactsArrayList;
    private static ArrayList<RegionSpecies> speciesArrayList;
    private static ArrayList<RegionSpeciesImages> regionSpeciesImages;
    private static ArrayList<StaticContentModel> staticContents;
    private static ArrayList<EventsModel> eventsList;
    JsonReader mReader = null;
    HttpResponse mResponse;
    String mUpdatedSpecies = null;
    HttpClient mClient;

    public JsonArrayReader(Context context, String table) throws ClientProtocolException, IOException {
        this(context, table, context.getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0L), false);
    }

    public JsonArrayReader(Context context, String table, long lastSync, boolean firstTime) throws ClientProtocolException, IOException {
        mClient = Util.getHttpClient(context);
        String param;
        //if (table.equalsIgnoreCase(SpeciesTranslations.TABLE_NAME)) {
        //    Util.logInfo(Contacts.TABLE_NAME, "Ignore Species Translation Sync");
        //}
        if (table.equalsIgnoreCase(Contacts.TABLE_NAME)) {
            Util.logInfo(Contacts.TABLE_NAME, ".......Synchronizing........................................................");
            contactsArrayList = new ArrayList<>();
           /* List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
            requestParams.add(new BasicNameValuePair("t", AppConstants.API_SECRET_KEY));
            requestParams.add(new BasicNameValuePair("later_than", Util.lastSyncTime(AppPreferences.getContactsTimeSpan(context))));*/

//            HttpPost httpRequest = new HttpPost(AppConstants.REMOTE_SERVER_URL + AppConstants.CONTACTS_API);
//            httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
            param = "&t=" + AppConstants.API_SECRET_KEY;
            /*
            param = "&t=" + AppConstants.API_SECRET_KEY + "&later_than=" +
                            Util.lastSyncTime(AppPreferences.getContactsTimeSpan(context));

            */
            String url = AppConstants.REMOTE_SERVER_URL + AppConstants.CONTACTS_API + param;
            Util.logInfo("ContactResponseUrl: ", url);
            HttpGet httpRequest = new HttpGet(url);


            httpRequest.addHeader("Cache-Control", "no-cache");
            mResponse = mClient.execute(httpRequest);
            if (mResponse.getStatusLine().getStatusCode() / 100 == 2) {
                InputStream is = mResponse.getEntity().getContent();
                String resp = convertInputStreamToString(is);
                Log.i("ContactsRespones:", resp);
                try {
                    JSONObject rootObject = new JSONObject(resp);
                    JSONObject dataObject;
                    JSONArray contactsArray;
                    if (rootObject.getString("success").equalsIgnoreCase("true")) {
                        dataObject = rootObject.getJSONObject("data");
                        AppPreferences.setImageBaseLink(context, dataObject.getString("s3Url") + "/");
                        contactsArray = dataObject.getJSONArray("contacts");
                        int size = contactsArray.length();
                        Log.i("ContactSize: ", " " + size);
                        contactsArrayList.clear();
                        for (int i = 0; i < size; i++) {
                            RegionContacts regionContact = new RegionContacts();
                            regionContact.setS3Url(dataObject.getString("s3Url"));
                            regionContact.setName(contactsArray.getJSONObject(i).getString("name"));
                            regionContact.setId(contactsArray.getJSONObject(i).getString("id"));
                            regionContact.setAvatar(contactsArray.getJSONObject(i).getString("avatar"));
                            regionContact.setType(contactsArray.getJSONObject(i).getString("type"));
                            regionContact.setAgency(contactsArray.getJSONObject(i).getString("agency"));
                            regionContact.setJurisdiction_scope(contactsArray.getJSONObject(i).getString("jurisdiction_scope"));
                            regionContact.setSpecialcapacity_note(contactsArray.getJSONObject(i).getString("specialcapacity_note"));
                            regionContact.setEmail(contactsArray.getJSONObject(i).getString("email"));
                            regionContact.setPhone(contactsArray.getJSONObject(i).getString("phone"));
                            regionContact.setAddress1(contactsArray.getJSONObject(i).getString("address1"));
                            regionContact.setAddress2(contactsArray.getJSONObject(i).getString("address2"));
                            regionContact.setCity(contactsArray.getJSONObject(i).getString("city"));
                            regionContact.setCountry(contactsArray.getJSONObject(i).getString("country"));
                            regionContact.setRegion(contactsArray.getJSONObject(i).getString("region"));
                            regionContact.setWebsite(contactsArray.getJSONObject(i).getString("website"));
                            regionContact.setAvailability(contactsArray.getJSONObject(i).getString("availability"));
                            regionContact.setLat(contactsArray.getJSONObject(i).getString("lat"));
                            regionContact.setLon(contactsArray.getJSONObject(i).getString("lon"));
                            regionContact.setUtm(contactsArray.getJSONObject(i).getString("utm"));
                            regionContact.setCreated_by(contactsArray.getJSONObject(i).getString("created_by"));
                            regionContact.setCreated_date(contactsArray.getJSONObject(i).getString("created_date"));
                            regionContact.setUpdated_by(contactsArray.getJSONObject(i).getString("updated_by"));
                            contactsArrayList.add(regionContact);

                        }
                    } else {
                        Toast.makeText(context, "Contacts empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } else if (table.equalsIgnoreCase(SpeciesImages.TABLE_NAME)) {
            Util.logInfo(SpeciesImages.TABLE_NAME, ".......Synchronizing........................................................");
            regionSpeciesImages = new ArrayList<>();

            if (firstTime) {
                param = "&t=" + AppConstants.API_SECRET_KEY + "&later_than=" +
                        Util.lastSyncTime(AppPreferences.getSpeciesTimeSpan(context)) +
                        "&region=1";
            } else {
                param = "&t=" + AppConstants.API_SECRET_KEY + "&later_than=" +
                        Util.lastSyncTime(AppPreferences.getSpeciesTimeSpan(context));
                        //+ "&region=" + AppPreferences.getSelectedRegions(context);
            }
            String url = AppConstants.REMOTE_SERVER_URL + AppConstants.SPECIES_IMAGES_API + param;
            Util.logInfo("SpeciesResponseUrl: ", url);
            HttpGet httpRequest = new HttpGet(url);

//            HttpPost httpRequest = new HttpPost(AppConstants.REMOTE_SERVER_URL + "api.php");
//            httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
            httpRequest.addHeader("Cache-Control", "no-cache");
            mResponse = mClient.execute(httpRequest);
            if (mResponse.getStatusLine().getStatusCode() / 100 == 2) {
                InputStream is = mResponse.getEntity().getContent();
                String resp = convertInputStreamToString(is);
                Log.i("SpeciesImagesRespones:", resp);
                try {
                    JSONObject rootObject = new JSONObject(resp);
                    JSONObject dataObject;
                    JSONArray speciesImagesArray;
                    if (rootObject.getString("success").equalsIgnoreCase("true")) {
                        dataObject = rootObject.getJSONObject("data");
                        speciesImagesArray = dataObject.getJSONArray("speciesImages");
                        int size = speciesImagesArray.length();
                        Log.i("SpeciesImagesSize: ", " " + size);
                        regionSpeciesImages.clear();
                        for (int i = 0; i < size; i++) {
                            JSONObject imageArray = speciesImagesArray.getJSONObject(i);

                            for (int j = 0; j < imageArray.getJSONArray("images").length(); j++) {
                                RegionSpeciesImages regionSpeciesImage = new RegionSpeciesImages();
                                regionSpeciesImage.setSpeciesId(imageArray.getString("species_id"));
                                JSONObject imageObj = imageArray.getJSONArray("images").getJSONObject(j);
                                regionSpeciesImage.setCredit(imageObj.getString("credit"));
                                regionSpeciesImage.setImageOrder(imageObj.getString("image_order"));
                                regionSpeciesImage.setLicense(imageObj.getString("license"));
                                regionSpeciesImage.setImagePath("/uploads" + imageObj.getString("image_path"));
                                regionSpeciesImage.setDefaultOrder("0");
                                Util.logInfo("Image Info ", imageArray.getString("species_id") + " : " + imageObj.toString());
                                regionSpeciesImages.add(regionSpeciesImage);

                            }
                        }
                    } else {
                        Toast.makeText(context, "Species Images empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        } else if (table.equalsIgnoreCase(Species.TABLE_NAME)) {
            Util.logInfo(Species.TABLE_NAME, ".......Synchronizing........................................................");
            speciesArrayList = new ArrayList<>();
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
            requestParams.add(new BasicNameValuePair("t", AppConstants.API_SECRET_KEY));
            requestParams.add(new BasicNameValuePair("later_than", Util.lastSyncTime(AppPreferences.getSpeciesTimeSpan(context))));
            //requestParams.add(new BasicNameValuePair("region", AppPreferences.getSelectedRegions(context)));

//            HttpPost httpRequest = new HttpPost(AppConstants.REMOTE_SERVER_URL + AppConstants.SPECIES_API);
            if (firstTime) {
                param = "&t=" + AppConstants.API_SECRET_KEY + "&later_than=" +
                        Util.lastSyncTime(AppPreferences.getSpeciesTimeSpan(context)) + "&region=1";
            } else {
                param = "&t=" + AppConstants.API_SECRET_KEY + "&later_than=" +
                        Util.lastSyncTime(AppPreferences.getSpeciesTimeSpan(context)); // + "&region=" + AppPreferences.getSelectedRegions(context);
            }
            String url = AppConstants.REMOTE_SERVER_URL + AppConstants.SPECIES_API + param;
            Util.logInfo("SpeciesResponseUrl: ", url);
            HttpGet httpRequest = new HttpGet(url);
//            httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
            httpRequest.addHeader("Cache-Control", "no-cache");
            mResponse = mClient.execute(httpRequest);
            if (mResponse.getStatusLine().getStatusCode() / 100 == 2) {
                InputStream is = mResponse.getEntity().getContent();
                String resp = convertInputStreamToString(is);
                Log.i("SpeciesRespones:", resp);
                try {
                    JSONObject rootObject = new JSONObject(resp);
                    JSONObject dataObject;
                    JSONArray speciesArray;
                    if (rootObject.getString("success").equalsIgnoreCase("true")) {
                        dataObject = rootObject.getJSONObject("data");
                        speciesArray = dataObject.getJSONArray("species");
                        int size = speciesArray.length();
                        Log.i("SpeciesSize: ", " " + size);
                        speciesArrayList.clear();
                        for (int i = 0; i < size; i++) {
                            RegionSpecies regionSpecies = new RegionSpecies();
                            regionSpecies.setS3Url(dataObject.getString("s3Url"));
                            regionSpecies.setCommon_name(speciesArray.getJSONObject(i).getString("common_name"));
                            regionSpecies.setId(speciesArray.getJSONObject(i).getString("id"));
                            regionSpecies.setRegion(speciesArray.getJSONObject(i).getString("region"));
                            regionSpecies.setType(speciesArray.getJSONObject(i).getString("type"));
                            regionSpecies.setIs_global(speciesArray.getJSONObject(i).getString("is_global"));
                            regionSpecies.setScientific_name(speciesArray.getJSONObject(i).getString("scientific_name"));
                            regionSpecies.setCites(speciesArray.getJSONObject(i).getString("cites"));
                            regionSpecies.setCites_other(speciesArray.getJSONObject(i).getString("cites_other"));
                            regionSpecies.setExtant_countries(speciesArray.getJSONObject(i).getString("extant_countries"));
                            regionSpecies.setStatus(speciesArray.getJSONObject(i).getString("status"));
                            regionSpecies.setWarnings(speciesArray.getJSONObject(i).getString("warnings"));
                            regionSpecies.setHabitat(speciesArray.getJSONObject(i).getString("habitat"));
                            regionSpecies.setBasic_id_cues(speciesArray.getJSONObject(i).getString("basic_id_cues"));
                            regionSpecies.setConsumer_advice(speciesArray.getJSONObject(i).getString("consumer_advice"));
                            regionSpecies.setEnforcement_advice(speciesArray.getJSONObject(i).getString("enforcement_advice"));
                            regionSpecies.setSimilar_animals(speciesArray.getJSONObject(i).getString("similar_animals"));
                            regionSpecies.setKnown_as(speciesArray.getJSONObject(i).getString("known_as"));
                            regionSpecies.setAverage_size_weight(speciesArray.getJSONObject(i).getString("average_size_weight"));
                            regionSpecies.setFirst_responder(speciesArray.getJSONObject(i).getString("first_responder"));
                            regionSpecies.setTraded_as(speciesArray.getJSONObject(i).getString("traded_as"));
                            regionSpecies.setCommon_trafficking(speciesArray.getJSONObject(i).getString("common_trafficking"));
                            regionSpecies.setNotes(speciesArray.getJSONObject(i).getString("notes"));
                            regionSpecies.setKeywords_tags(speciesArray.getJSONObject(i).getString("keywords_tags"));
                            regionSpecies.setReference(speciesArray.getJSONObject(i).getString("reference"));
                            regionSpecies.setDisease_name(speciesArray.getJSONObject(i).getString("disease_name"));
                            regionSpecies.setDisease_risk_level(speciesArray.getJSONObject(i).getString("disease_risk_level"));
                            regionSpecies.setCreated_by(speciesArray.getJSONObject(i).getString("created_by"));
                            regionSpecies.setCreated_date(speciesArray.getJSONObject(i).getString("created_date"));
                            regionSpecies.setUpdated_by(speciesArray.getJSONObject(i).getString("updated_by"));
                            regionSpecies.setUpdated_date(speciesArray.getJSONObject(i).getString("updated_date"));
                            regionSpecies.setImage(speciesArray.getJSONObject(i).getString("image"));
                            speciesArrayList.add(regionSpecies);

                        }
                    } else {
                        Toast.makeText(context, "Contacts empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } else if (table.equalsIgnoreCase(Incidents.TABLE_NAME)) {
            Util.logInfo(Incidents.TABLE_NAME, ".......Synchronizing........................................................");
            eventsList = new ArrayList<>();
          /*  List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
            requestParams.add(new BasicNameValuePair("r", "get-submit-reports"));//api.php?r=get-species-images
            requestParams.add(new BasicNameValuePair("t", AppConstants.API_SECRET_KEY));
            requestParams.add(new BasicNameValuePair("status", "public"));
            requestParams.add(new BasicNameValuePair("later_than", Util.lastSyncTime(AppPreferences.getEventsTimeSpan(context))));*/

            param = "r=get-submit-reports"/*&region=" + AppPreferences.getSelectedRegions(context)*/
                    + "&t=" + AppConstants.API_SECRET_KEY + "&status=public&later_than=" +
                    Util.lastSyncTime(AppPreferences.getEventsTimeSpan(context));
            String url = AppConstants.REMOTE_SERVER_URL + "api.php?" + param;
            Util.logInfo("EventsUrl: ", url);
            HttpGet httpRequest = new HttpGet(url);
//            httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));

            httpRequest.addHeader("Cache-Control", "no-cache");
            mResponse = mClient.execute(httpRequest);
            if (mResponse.getStatusLine().getStatusCode() / 100 == 2) {
                InputStream is = mResponse.getEntity().getContent();
                String resp = convertInputStreamToString(is);
                Log.i("EventsRespones:", resp);
                try {
                    JSONObject rootObject = new JSONObject(resp);
                    JSONObject dataObject;
                    JSONArray eventsArray;
                    if (rootObject.getString("success").equalsIgnoreCase("true")) {
                        dataObject = rootObject.getJSONObject("data");
                        eventsArray = dataObject.getJSONArray("submitReports");
                        int size = eventsArray.length();
                        Log.i("EventListSize: ", " " + size);
                        eventsList.clear();
                        Gson gson = new Gson();
                        for (int i = 0; i < size; i++) {
                            EventsModel eventsModel;
                            eventsModel = gson.fromJson(eventsArray.getJSONObject(i).toString(), EventsModel.class);
                            eventsList.add(eventsModel);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
/*
        else if (table.equalsIgnoreCase(StaticContent.TABLE_NAME)) {
            Util.logInfo(StaticContent.TABLE_NAME, ".......Synchronizing........................................................");
            staticContents = new ArrayList<>();

            param = "&t=" + AppConstants.API_SECRET_KEY; // + "&later_than=" + Util.lastSyncTime(AppPreferences.getSpeciesTimeSpan(context));

            String url = AppConstants.REMOTE_SERVER_URL + AppConstants.REGION_STATIC_CONTENT_API + param;
            Util.logInfo("StaticContentResponseUrl: ", url);
            HttpGet httpRequest = new HttpGet(url);

            httpRequest.addHeader("Cache-Control", "no-cache");
            mResponse = mClient.execute(httpRequest);
            if (mResponse.getStatusLine().getStatusCode() / 100 == 2) {
                InputStream is = mResponse.getEntity().getContent();
                String resp = convertInputStreamToString(is);
                Log.i("StaticConentRespones:", resp);
                try {
                    JSONObject rootObject = new JSONObject(resp);
                    JSONObject dataObject;
                    JSONArray staticContentArray;
                    if (rootObject.getString("success").equalsIgnoreCase("true")) {
                        dataObject = rootObject.getJSONObject("data");
                        staticContentArray = dataObject.getJSONArray("staticContents");
                        int size = staticContentArray.length();
                        staticContents.clear();
                        for (int i = 0; i < size; i++) {
                            JSONObject contentArray = staticContentArray.getJSONObject(i);
                            StaticContentModel staticContentModel = new StaticContentModel();
                            staticContentModel.setType(contentArray.getString("type"));
                            staticContentModel.setLanguage(contentArray.getString("language"));
                            staticContentModel.setContent(contentArray.getString("content"));
                            staticContentModel.setCreated_by(contentArray.getString("created_by"));
                            staticContentModel.setCreated_date(contentArray.getString("created_date"));
                            staticContentModel.setUpdated_by(contentArray.getString("updated_by"));
                            staticContentModel.setUpdated_date(contentArray.getString("updated_date"));
                            staticContents.add(staticContentModel);
                        }
                    } else {
                        Toast.makeText(context, "Static Content empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
 */
        else {
            if (table.equals(SpeciesTranslations.TABLE_NAME) && firstTime) {
                //Ignore Translation Sync at first time launch.
            } else {
                if (AppPreferences.isCallFromActivity(App.getInstance())) {
                    lastSync = 0;
                }
                List<NameValuePair> requestParams1 = new ArrayList<NameValuePair>(1);
                requestParams1.add(new BasicNameValuePair("table", table));
                requestParams1.add(new BasicNameValuePair("time", String.valueOf(lastSync)));
                /*
                if (firstTime) {
                    if (table.equals(SpeciesImages.TABLE_NAME))
                        requestParams1.add(new BasicNameValuePair("region", "1"));
                } else {
                    if (table.equals(SpeciesImages.TABLE_NAME))
                        requestParams1.add(new BasicNameValuePair("region", AppPreferences.getSelectedRegions(context)));
                }
                */
                HttpPost httpRequest1 = new HttpPost(AppConstants.REMOTE_SERVER_URL + WildscanDataManager.REMOTE_PHP_QUERY_UPDATES);
                httpRequest1.setEntity(new UrlEncodedFormEntity(requestParams1));
                httpRequest1.addHeader("Cache-Control", "no-cache");
                mResponse = mClient.execute(httpRequest1);

                if (mResponse.getStatusLine().getStatusCode() / 100 == 2) {
                    InputStream is = mResponse.getEntity().getContent();
                    String ret = Util.readResponse(is);
                    if (table.equalsIgnoreCase(SpeciesImages.TABLE_NAME))
                        Log.i("SpeciesImages: ", ret);
                    is.close();

                    StringReader isr = new StringReader(ret);
                    mReader = new JsonReader(isr);
                    mReader.beginArray();

                }
            }
        }
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean hasNext() throws IOException {
        return mReader != null && mReader.hasNext();
    }

    public ArrayList<RegionContacts> getContactsList() {
        return contactsArrayList;
    }

    public ArrayList<RegionSpecies> getSpeciesList() {
        return speciesArrayList;
    }

    public ArrayList<RegionSpeciesImages> getSpeciesImagesList() {
        return regionSpeciesImages;
    }

    public ArrayList<EventsModel> getEventsList() {
        return eventsList;
    }

    public ArrayList<StaticContentModel> getStaticContentsList() {
        return staticContents;
    }

    // read next row, return ContentValues object
    public ContentValues readObject() throws IOException {
        ContentValues res = new ContentValues();

        if (mReader == null)
            return res;

        String name;
        boolean b;
        String s;
        long l;
        double d;
        mReader.beginObject();
        while (mReader.hasNext()) {
            name = mReader.nextName();
            switch (mReader.peek()) {
                case NULL:
                    mReader.nextNull();
                    res.putNull(name);
                    break;
                case BOOLEAN:
                    b = mReader.nextBoolean();
                    res.put(name, b);
                    break;
                case STRING:
                    s = mReader.nextString().replaceAll("\\r\\n", " ").replaceAll("\\\"", "").replaceAll("\\\\+", "");
                    res.put(name, s);
                    break;
                case NUMBER:
                    s = mReader.nextString().replaceAll("\\r\\n", " ").replaceAll("\\\"", "").replaceAll("\\\\+", "");
                    try {
                        l = Long.parseLong(s);
                        res.put(name, l);
                    } catch (NumberFormatException e) {
                        try {
                            d = Double.parseDouble(s);
                            res.put(name, d);
                        } catch (NumberFormatException e1) {
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        mReader.endObject();

        return res;
    }

    public void close() throws IOException {
        if (mReader != null)
            mReader.close();
    }

}