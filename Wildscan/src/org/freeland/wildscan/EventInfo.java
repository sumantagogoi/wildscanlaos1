package org.freeland.wildscan;

import java.io.Serializable;

import org.freeland.wildscan.data.WildscanDataManager;

import android.os.Parcel;
import android.os.Parcelable;

public class EventInfo implements Serializable, Parcelable {

	public static final int MAX_CHARS_COUNTRY = 15;
	private static final long serialVersionUID = 2L;
	
	long mEventId;
//	String mAvatarUrl, mUserText, mDateText, mDescriptionText;
	// TODO: fields: title, content, date/time, location, photo, suspect-name, route (origin, destination, logistics), method
	String mIncidentText, mContent, mIncidentDate, mIncidentLocation, mPhotoUrl, mOriginLocation, mDestinationLocation, mMethod;
	String mSpeciesName;
	long mSpeciesId;
	double mIncidentLat, mIncidentLon, mOriginLat, mOriginLon, mDestinationLat, mDestinationLon;
	// TODO: Pull info from 'submit_report' and 'submit_report_image' tables
	
	public EventInfo() {
//		mAvatarUrl = mUserText = mDateText = mDescriptionText = null;
		
		mEventId = -1;
		mSpeciesId = -1;
		mIncidentLat = mIncidentLon = Double.NaN;
		mIncidentText = mContent = mIncidentDate = mIncidentLocation = mPhotoUrl = mOriginLocation = mDestinationLocation = mMethod = null;
		mOriginLat = mOriginLon = mDestinationLat = mDestinationLon = Double.NaN;
	}
	
	public EventInfo(long id, String text, String content, String date, String location, String photo, String orig, String dest, String method,
						long species, double lon, double lat, double origLon, double origLat, double destLon, double destLat) {
		mEventId = id;
		mSpeciesId = species;
		mIncidentText = text; mContent = content; mIncidentDate = date; mIncidentLocation = location; mPhotoUrl = photo;
		mOriginLocation = orig; mDestinationLocation = dest; mMethod = method;
		mIncidentLon = lon; mIncidentLat = lat;
		mOriginLon = origLon; mOriginLat = origLat;
		mDestinationLon = destLon; mDestinationLat = destLat;
	}
	
	public EventInfo(long id, long species, String text, String location, String date, String photo, double lat, double lon) {
		this();
		mEventId = id;
		mSpeciesId = species;
		mSpeciesName = WildscanDataManager.getSpeciesName(mSpeciesId);
		mIncidentText = text;
		mIncidentLocation = location;
		mIncidentDate = date;
		mPhotoUrl = photo;
		mIncidentLat = lat;
		mIncidentLon = lon;
	}
					
	
//	public EventInfo(long id, String avatar, String user, String date, String desc, double lat, double lon) {
//		mEventId = id;
//		mAvatarUrl = avatar;
//		mUserText = user;
//		mDateText = date;
//		mDescriptionText = desc;
//		mIncidentLat = lat;
//		mIncidentLon = lon;
//	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(this);
	}

	public static final Parcelable.Creator<EventInfo> CREATOR = new Parcelable.Creator<EventInfo>() {
		public EventInfo createFromParcel(Parcel in) {
			return (EventInfo) in.readSerializable();
		}

		@Override
		public EventInfo[] newArray(int size) {
			return new EventInfo[size];
		}
	};
	
}