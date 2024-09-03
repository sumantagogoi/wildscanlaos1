package org.freeland.wildscan.data.contract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freeland.wildscan.R;
import org.freeland.wildscan.data.provider.WildscanDataProvider;
import org.freeland.wildscan.util.Util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author Noam
 *
 */
public class ContactsTranslations {
	public static final String TABLE_NAME = "contacts_translations";

	public static final String _S_CONTACT_ID = "contact_id";
	public static final String _S_LANGUAGE = "language";
	
	public static final String _S_NAME = "name";
	public static final String _S_AGENCY = "agency";
	public static final String _S_SPECIALCAPACITY_NOTE = "specialcapacity_note";
	public static final String _S_ADDRESS1 = "address1";
	public static final String _S_ADDRESS2 = "address2";
//	public static final String _S_CITY = "city";

//	public static final String _DETAILS = "_details";
	// remote server field names
//	public static final String _S_ID = "id";
	
	public static final Set<String> TRANSLATED_FIELDS;
	static {
		TRANSLATED_FIELDS = new HashSet<String>();
		TRANSLATED_FIELDS.add(Contacts._NAME);
		TRANSLATED_FIELDS.add(Contacts._S_AGENCY);
		TRANSLATED_FIELDS.add(Contacts._S_SPECIALCAPACITY_NOTE);
		TRANSLATED_FIELDS.add(Contacts._S_ADDRESS1);
		TRANSLATED_FIELDS.add(Contacts._S_ADDRESS2);
		TRANSLATED_FIELDS.add(Contacts._DETAILS);
	}
	public static Map<String, String> sLangCodeToPhoneLabel = null;
	public static Map<String, String> sLangCodeToEmailLabel = null;

	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
			_S_CONTACT_ID + " INTEGER NOT NULL, " +
			_S_LANGUAGE + " TEXT NOT NULL," +
			Contacts._NAME + " TEXT DEFAULT NULL," + 
//			_S_AGENCY + " TEXT DEFAULT NULL," + 
			Contacts._S_SPECIALCAPACITY_NOTE + " TEXT DEFAULT NULL," + 
//			_S_ADDRESS1 + " TEXT DEFAULT NULL," + 
//			_S_ADDRESS2 + " TEXT DEFAULT NULL," + 
//			_S_CITY + " TEXT DEFAULT NULL," + 
			Contacts._DETAILS + " TEXT DEFAULT NULL," + 
			"PRIMARY KEY (" + _S_CONTACT_ID + "," + _S_LANGUAGE + "));";

	@SuppressLint("NewApi")
	public static final ContentValues convertRemoteFieldsToLocal(Context context, ContentValues remote) {
		if (sLangCodeToEmailLabel==null) {
			String[] langs = context.getResources().getStringArray(R.array.contact_labels_lang);
			String[] lab_email = context.getResources().getStringArray(R.array.contact_labels_email);
			String[] lab_phone = context.getResources().getStringArray(R.array.contact_labels_phone);
			sLangCodeToEmailLabel = new HashMap<String,String>(langs.length);
			sLangCodeToPhoneLabel = new HashMap<String,String>(langs.length);
			for (int i=0; i<langs.length; i++) {
				sLangCodeToEmailLabel.put(langs[i], lab_email[i]);
				sLangCodeToPhoneLabel.put(langs[i], lab_phone[i]);
			}
		}
		String lang = remote.getAsString(_S_LANGUAGE);
		String email_label = sLangCodeToEmailLabel.get(lang),
				phone_label = sLangCodeToPhoneLabel.get(lang);


		ContentValues local = new ContentValues();
		
		long id = remote.getAsLong(_S_CONTACT_ID);
		String email = null, phone = null, website = null;
		Uri uri = ContentUris.withAppendedId(WildscanDataProvider.getTableUri(Contacts.TABLE_NAME), id);
		Cursor c = context.getContentResolver().query(uri, new String[]{Contacts._EMAIL,Contacts._S_PHONE,Contacts._WEBSITE} , null, null, null);
		if (c.moveToFirst()) {
			email = c.getString(c.getColumnIndex(Contacts._EMAIL));
			phone = c.getString(c.getColumnIndex(Contacts._S_PHONE));
			website = c.getString(c.getColumnIndex(Contacts._WEBSITE));
		}
		c.close();

		String name = remote.getAsString(_S_NAME),
				capacity = remote.getAsString(_S_SPECIALCAPACITY_NOTE),
				agency = remote.getAsString(_S_AGENCY), 
				address1 = remote.getAsString(_S_ADDRESS1),
				address2 = remote.getAsString(_S_ADDRESS2);
		if (TextUtils.isEmpty(agency) || agency.equalsIgnoreCase("n/a") || agency.equalsIgnoreCase("not applicable"))
			agency = null;
		String details = ("<b>" + name + "</b>" +
				(Util.nullIfEmpty(capacity)==null?"":"<br>" + Util.escapeHtml(capacity)) + "<br>" +
				(TextUtils.isEmpty(email)?"":"<br>" + email_label + ": " + email) +
				(TextUtils.isEmpty(phone)?"":"<br>" + phone_label + ": " + phone) + "<br>" +
				/*"Agency: " + */(agency==null?"":"<br>" + Util.escapeHtml(agency)) +
				/*"Address: " + */(TextUtils.isEmpty(address1)?"": "<br>" + Util.escapeHtml(address1)) + 
				/*"         " + */(TextUtils.isEmpty(address2)?"": "<br>" + Util.escapeHtml(address2))/* +  
				"Website: " + (TextUtils.isEmpty(website)?"":"<br><br>" + website)*/
				).replaceAll("(<br>)+$","").replaceAll("<br><br>(<br>)+", "<br><br>");
		
		if (!TextUtils.isEmpty(website) && !website.startsWith("http://") && !website.startsWith("https://"))
			website = "http://" + website;
		
		local.put(_S_CONTACT_ID, remote.getAsLong(_S_CONTACT_ID));
		local.put(_S_LANGUAGE, Util.nullIfEmpty(remote.getAsString(_S_LANGUAGE)));
		local.put(Contacts._NAME, Util.nullIfEmpty(name));
//		local.put(_S_AGENCY, remote.getAsString(_S_AGENCY));
		local.put(Contacts._S_SPECIALCAPACITY_NOTE, Util.nullIfEmpty(capacity));
//		local.put(_S_ADDRESS1, Util.escapeHtml(remote.getAsString(_S_ADDRESS1)));
//		local.put(_S_ADDRESS2, Util.escapeHtml(remote.getAsString(_S_ADDRESS2)));
//		local.put(_S_CITY, remote.getAsString(_S_CITY));
		local.put(Contacts._DETAILS, details);

		return local;
	}
}