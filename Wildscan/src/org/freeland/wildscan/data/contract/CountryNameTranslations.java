package org.freeland.wildscan.data.contract;

import java.util.HashMap;
import java.util.Map;

public class CountryNameTranslations {
	
	public static final Map<String,String> COUNTRY_NAME_TO_ISO_CODE;
	
	static {
		COUNTRY_NAME_TO_ISO_CODE = new HashMap<String,String>();
		COUNTRY_NAME_TO_ISO_CODE.put("Cambodia","KH");
		COUNTRY_NAME_TO_ISO_CODE.put("Indonesia","ID");
		COUNTRY_NAME_TO_ISO_CODE.put("Lao PDR","LA");
		COUNTRY_NAME_TO_ISO_CODE.put("Malaysia","MY");
		COUNTRY_NAME_TO_ISO_CODE.put("Myanmar","MM");
		COUNTRY_NAME_TO_ISO_CODE.put("Philippines","PH");
		COUNTRY_NAME_TO_ISO_CODE.put("Singapore","SG");
		COUNTRY_NAME_TO_ISO_CODE.put("Thailand","TH");
		COUNTRY_NAME_TO_ISO_CODE.put("Vietnam","VN");
	}
	
	public static String getLangCode(String country) {
		return COUNTRY_NAME_TO_ISO_CODE.get(country.trim());
	}

}
