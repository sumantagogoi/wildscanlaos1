package org.freeland.wildscan.util;

/**
 * Created by Noman on 3/6/2016.
 */
public class AppConstants {
//        public static final String REMOTE_SERVER_URL = "http://techmicro.co/wildscan-webapp/";
//    public static final String REMOTE_SERVER_URL = "http://wildscan-env-staging.elasticbeanstalk.com/";
public static final String REMOTE_SERVER_URL = "https://wildscan.org/api/";
    public static final String REMOTE_BASE_IMAGE_URL = "https://wildscan" +
            ".org/wildscan-uploads/";
    public static final String REMOTE_BASE_IMAGE_URL_WITHOUT_SLASH = "https://wildscan" +
            ".org/wildscan-uploads";
    public static final String CONTACTS_API = "api.php?r=get-contacts";
    public static final String SPECIES_API = "api.php?r=get-species";
    public static final String SPECIES_IMAGES_API = "api.php?r=get-species-images";
    public static final String REGION_API = "api.php?r=get-regions";
    public static final String REGION_STATS_API = "api.php?r=get-regions-stats";
    public static final String REGION_STATIC_CONTENT_API = "api.php?r=get-static-contents";
    public static final String API_SECRET_KEY = "OXUTzKm/rp5qCfotOhWj9Y600G/OIBNNNMNwGf7ZWNqXzF3N";


    public static final String REGION_AFRICA_ID = "1";
    public static final String REGION_GLOBAL_ID = "2";
    public static final String REGION_SOUTH_AMERICA_ID = "3";
    public static final String REGION_SOUTH_EAST_ASIA_ID = "4";

    public static final String REGION_AFRICA_CODE = "africa";
    public static final String REGION_GLOBAL_CODE = "global";
    public static final String REGION_SOUTH_AMERICA_CODE = "s_america";
    public static final String REGION_SOUTH_EAST_ASIA_CODE = "se_asia";

    public static final String SELECTED_REGIONS = "USER_SELECTED_REGIONS";
    public static final String REPORTED_REGION = "SELECT_REPORTED_REGION";
    public static final String REPORTED_REGION_CODE = "SELECT_REPORTED_REGION_CODE";
    public static final String GLOBAL_REGIONS = "IS_GLOBAL_SELECTED";
    public static final String SOUTH_ASIA_REGIONS = "IS_ASIA_SELECTED";
    public static final String SOUTH_AMERICA_REGIONS = "IS_AMERICA_SELECTED";
    public static final String AFRICA_REGIONS = "IS_AFRICA_SELECTED";


    public static final String CONTACTS_TIMESPAN = "CONTACT_TIMESPAN";
    public static final String SPECIES_TIMESPAN = "SPECIES_TIMESPAN";
    public static final String SPECIES_IMAGES_TIMESPAN = "SPECIES_IMAGES_TIMESPAN";
    public static final String REGIONS_TIMESPAN = "REGIONS_TIMESPAN";
    public static final String STATS_TIMESPAN = "STATS_TIMESPAN";
    public static final String EVENTS_TIMESPAN = "STATS_TIMESPAN";

    /*REGIONS STATS KEYS*/
    public static final String GLOBAL_CONTACTS = "GLOBAL_CONTACTS";
    public static final String ASIA_CONTACTS = "ASIA_CONTACTS";
    public static final String AMERICA_CONTACTS = "AMERICA_CONTACTS";
    public static final String AFRICA_CONTACTS = "AFRICA_CONTACTS";

    public static final String GLOBAL_SPECIES = "GLOBAL_SPECIES";
    public static final String ASIA_SPECIES = "ASIA_SPECIES";
    public static final String AMERICA_SPECIES = "AMERICA_SPECIES";
    public static final String AFRICA_SPECIES = "AFRICA_SPECIES";

    public static final String IS_CALLED_FROM_ACTIVITY = "IS_CALLED_FROM_ACTIVITY";

    public static final String SHOW_TUTORIAL = "SHOW_TUTORIAL";
}
