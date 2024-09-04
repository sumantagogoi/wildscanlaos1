package org.freeland.wildscanlaos.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by nomankhan25dec on 3/9/2016.
 */
public class AppPreferences {


    public static void setImageBaseLink(Context context, String link) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("imagebaseLink", link);
        ed.commit();
    }

    public static String getImageBaseLink(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Util.logInfo("ImageUrl",TextUtils.isEmpty(sp.getString("imagebaseLink", "")) ? AppConstants.REMOTE_BASE_IMAGE_URL : sp.getString("imagebaseLink", ""));
        return TextUtils.isEmpty(sp.getString("imagebaseLink", "")) ? AppConstants.REMOTE_BASE_IMAGE_URL : sp.getString("imagebaseLink", "");
    }

    public static long getContactsTimeSpan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(AppConstants.CONTACTS_TIMESPAN, 0);
    }

    public static void setContactsTimespan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(AppConstants.CONTACTS_TIMESPAN, System.currentTimeMillis());
        ed.commit();
    }

    public static long getRegionsTimeSpan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(AppConstants.REGIONS_TIMESPAN, 0);
    }

    public static void setRegionsTimespan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(AppConstants.REGIONS_TIMESPAN, System.currentTimeMillis());
        ed.commit();
    }

    public static long getEventsTimeSpan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(AppConstants.EVENTS_TIMESPAN, 0);
    }

    public static void setEventsTimespan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(AppConstants.EVENTS_TIMESPAN, System.currentTimeMillis());
        ed.commit();
    }

    public static long getSpeciesTimeSpan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(AppConstants.SPECIES_TIMESPAN, 0);
    }

    public static void setSpeciesTimespan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(AppConstants.SPECIES_TIMESPAN, System.currentTimeMillis());
        ed.commit();
    }

    public static long getSpeciesImageTimeSpan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(AppConstants.SPECIES_IMAGES_TIMESPAN, 0);
    }

    public static void setSpeciesImageTimespan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(AppConstants.SPECIES_IMAGES_TIMESPAN, System.currentTimeMillis());
        ed.commit();
    }

    public static long getStatsTimeSpan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(AppConstants.STATS_TIMESPAN, 0);
    }

    public static void setStatsTimespan(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(AppConstants.STATS_TIMESPAN, System.currentTimeMillis());
        ed.commit();
    }
    /*
    public static String getSelectedRegions(Context context) {
        String regions = "";
        if (isAfricaRegion(context)) {
            if (regions == "") {
                regions = AppConstants.REGION_AFRICA_ID;
            }
            else {
                regions = regions + "," + AppConstants.REGION_AFRICA_ID;
            }
        }
        else if (isAmericanRegion(context)) {
            regions = regions + "," + AppConstants.REGION_SOUTH_AMERICA_ID;
        }
        else if (isAsiaRegion(context)) {
            if (regions == "") {
                regions = AppConstants.REGION_SOUTH_EAST_ASIA_ID;
            }
            else {
                regions = regions + "," + AppConstants.REGION_SOUTH_EAST_ASIA_ID;
            }
        }
        else if (isGlobalRegion(context)) {
            if (regions == "") {
                regions = AppConstants.REGION_GLOBAL_ID;
            }
            else {
                regions = regions + "," + AppConstants.REGION_GLOBAL_ID;
            }
        }
        return regions;
    }

    public static void setRegions(Context context,
                                  boolean global,
                                  boolean america,
                                  boolean asia,
                                  boolean africa) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(AppConstants.GLOBAL_REGIONS, global);
        ed.putBoolean(AppConstants.SOUTH_AMERICA_REGIONS, america);
        ed.putBoolean(AppConstants.SOUTH_ASIA_REGIONS, asia);
        ed.putBoolean(AppConstants.AFRICA_REGIONS, africa);
        ed.commit();
    }

    public static void setReportingRegion(Context context, String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(AppConstants.REPORTED_REGION, name);
        ed.commit();
    }
    */
    public static String getReportingRegion(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.REPORTED_REGION, AppConstants.REGION_GLOBAL_CODE);
    }
    /*
    public static boolean isAmericanRegion(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(AppConstants.SOUTH_AMERICA_REGIONS, false);
    }

    public static boolean isAsiaRegion(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(AppConstants.SOUTH_ASIA_REGIONS, false);
    }

    public static boolean isGlobalRegion(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(AppConstants.GLOBAL_REGIONS, false);
    }

    public static boolean isAfricaRegion(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(AppConstants.AFRICA_REGIONS, false);
    }


    /****************************************************
     * ***************REGION STATS METHODS****************
     ****************************************************/
    /*
    public static void setRegionStats(Context context,
                                      String globalContacts, String globalSpecies,
                                      String asiaContacts, String asiaSpecies) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(AppConstants.GLOBAL_CONTACTS, globalContacts);
        ed.putString(AppConstants.GLOBAL_SPECIES, globalSpecies);
        ed.putString(AppConstants.ASIA_CONTACTS, asiaContacts);
        ed.putString(AppConstants.ASIA_SPECIES, asiaSpecies);
        ed.commit();
    }
    */
    public static String getGlobalContacts(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.GLOBAL_CONTACTS, "0");
    }
    /*
    public static String getAsiaContacts(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.ASIA_CONTACTS, "0");
    }

    public static String getAfricaContacts(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.AFRICA_CONTACTS, "0");
    }

    public static String getAmericaContacts(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.AMERICA_CONTACTS, "0");
    }
    */
    public static String getGlobalSpecies(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.GLOBAL_SPECIES, "0");
    }
    /*
    public static String getAsiaSpecies(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.ASIA_SPECIES, "0");
    }

    public static String getAfricaSpecies(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.AFRICA_SPECIES, "0");
    }

    public static String getAmericaSpecies(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.AMERICA_SPECIES, "0");
    }

    public static boolean isFirstTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("isFirstTimeSync", true);
    }
    */
    public static void setFirstTimeSync(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("isFirstTimeSync", false);
        ed.commit();
    }
    public static boolean isCallFromActivity(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(AppConstants.IS_CALLED_FROM_ACTIVITY, false);
    }

    public static void setIsCallFromActivity(Context context,boolean call){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(AppConstants.IS_CALLED_FROM_ACTIVITY, call);
        ed.commit();
    }


    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(key, value);
        ed.commit();
    }
    public static String getString(Context context, String key){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key,"");
    }

    public static void setShowTutotial(Context context, boolean b) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(AppConstants.SHOW_TUTORIAL, b);
        ed.commit();
    }
    public static boolean showTutorial(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(AppConstants.SHOW_TUTORIAL, true);
    }
}
