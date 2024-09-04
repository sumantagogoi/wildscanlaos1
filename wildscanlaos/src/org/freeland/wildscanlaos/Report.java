package org.freeland.wildscanlaos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.JsonWriter;
import android.util.Pair;
import android.widget.Toast;

import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.Contacts;
import org.freeland.wildscanlaos.data.contract.ContactsTranslations;
import org.freeland.wildscanlaos.data.contract.Incidents;
import org.freeland.wildscanlaos.data.provider.WildscanDataProvider;
import org.freeland.wildscanlaos.util.AppPreferences;
import org.freeland.wildscanlaos.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class Report implements Parcelable {

    public static final Parcelable.Creator<Report> CREATOR = new Parcelable.Creator<Report>() {
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return null;
        }
    };
    //	final static String JSON_NAME_ID = "id";
    final static String[] JSON_NAME_PHOTOS = {"submit_report_image1", "submit_report_image2",
            "submit_report_image3"};
    final static String JSON_DATE_FORMAT = "dd/MM/yyyy HH:mm";
    final static int MAX_PHOTOS = 3;
    // TODO: remove for production ?
    private static final boolean EMAIL_CONTACTS_ACTIVE = false;
    Context mContext;
    String mReportId;
    long mUserId;
    int mHour, mMinute, mDay, mMonth, mYear;
    //	Date mIncidentDate;
    double mIncidentLat, mIncidentLon;
    String mOffenseDescription;
    String mSpeciesName;
    long mSpeciesId;
    float mAmount;
    int mAmountUnits;
    int mCondition;
    String mOffenseDetails;
    String mMethodDetails;
    int mValueEstimate;
    double mOriginLat, mOriginLon;
    String mOrigin;
    double mDestinationLat, mDestinationLon;
    String mDestination;
    String mVesselDescription;
    String mVesselId;
    String mVesselName;
    //	int mNumPhotos;
    String[] mPhotos;
    //	String mMainPhotoPath;
//	String mExtraPhoto2;
//	String mExtraPhoto3;
    ArrayList<RecipientInfo> mRecipients;
    boolean mIsPrivate;

    public Report(Context context) {
        mContext = context;
        mUserId = WildscanDataManager.getInstance(context).getUserId();
        mIncidentLat = Double.NaN;
        mIncidentLon = Double.NaN;
        mAmount = Float.NaN;
        mAmountUnits = ReportWizardActivity.UNIT_PIECES;
        mCondition = -1;
        mValueEstimate = -1;
        mOriginLat = Double.NaN;
        mOriginLon = Double.NaN;
        mDestinationLat = Double.NaN;
        mDestinationLon = Double.NaN;
        mPhotos = new String[MAX_PHOTOS];
        for (int i = 0; i < MAX_PHOTOS; i++) mPhotos[i] = null;
        mRecipients = new ArrayList<RecipientInfo>();
        Set<String> autoContacts = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getStringSet(PrefsFragment.PREFS_KEY_AUTO_CONTACTS, null);
        if (autoContacts != null) {
            for (String c : autoContacts) {
                long id = Long.valueOf(c);
                Uri uri = WildscanDataProvider.getItemUri(Contacts.TABLE_NAME, id);
                String[] projection = {Contacts._EMAIL, Contacts._NAME};
                String selection = Util
                        .addLangToSelection(context, null, ContactsTranslations._S_LANGUAGE);
                Cursor cc = mContext.getContentResolver()
                        .query(uri, projection, selection, null, null);
                if (cc.moveToFirst()) {
                    String email = cc.getString(cc.getColumnIndex(Contacts._EMAIL));
                    if (TextUtils.isEmpty(email)) {
                        String name = cc.getString(cc.getColumnIndex(Contacts._NAME));
                        Toast.makeText(context,
                                context.getString(R.string.msg_report_contact_no_email, name),
                                Toast.LENGTH_SHORT).show();
                    }
                    mRecipients.add(new RecipientInfo(id, email));
                }
                cc.close();
            }
        }
        mIsPrivate = true;
    }

    private Report(Parcel in) {
        mReportId = in.readString();
        mUserId = in.readLong();
        mHour = in.readInt();
        mMinute = in.readInt();
        mDay = in.readInt();
        mMonth = in.readInt();
        mYear = in.readInt();
        mIncidentLat = in.readDouble();
        mIncidentLon = in.readDouble();
        mOffenseDescription = in.readString();
        mSpeciesName = in.readString();
        mSpeciesId = in.readLong();
        mAmount = in.readFloat();
        mAmountUnits = in.readInt();
        mCondition = in.readInt();
        mOffenseDetails = in.readString();
        mMethodDetails = in.readString();
        mValueEstimate = in.readInt();
        mOriginLat = in.readDouble();
        mOriginLon = in.readDouble();
        mOrigin = in.readString();
        mDestinationLat = in.readDouble();
        mDestinationLon = in.readDouble();
        mDestination = in.readString();
        mVesselDescription = in.readString();
        mVesselId = in.readString();
        mVesselName = in.readString();
        mPhotos = new String[MAX_PHOTOS];
        for (int i = 0; i < MAX_PHOTOS; i++)
            mPhotos[i] = in.readString();
        mRecipients = in.createTypedArrayList(RecipientInfo.CREATOR);
        mIsPrivate = in.readByte() != 0;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @SuppressWarnings("unused")
    public void emailReport(Context context) {
//		if (mRecipients.size()==1 && TextUtils.isEmpty(mRecipients.get(0).second))
//			return;

        StringWriter dest = new StringWriter();
        dest.write(context.getString(R.string.report_email_section_name_datetime) + ":\n");
        Calendar c = Calendar.getInstance();
        c.set(mYear, mMonth, mDay, mHour, mMinute);
        dest.write(DateFormat.getDateTimeInstance().format(c.getTime()) + "\n\n");
        dest.write(
                context.getString(R.string.report_email_section_name_location_lat) + ":\n" + String
                        .valueOf(mIncidentLat) + "\n\n");
        dest.write(
                context.getString(R.string.report_email_section_name_location_lon) + ":\n" + String
                        .valueOf(mIncidentLon) + "\n\n");
        if (mOffenseDescription != null && !mOffenseDescription.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_description) + ":\n" + mOffenseDescription
                    + "\n\n");
        if (mSpeciesName != null && !mSpeciesName.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_species) + ":\n" + mSpeciesName + "\n\n");
        if (!Float.isNaN(mAmount))
            dest.write(context.getString(R.string.report_email_section_name_amount) + ":\n" + String
                    .valueOf(mAmount) + " " + context.getResources()
                    .getStringArray(R.array.report_units)[mAmountUnits] + "\n\n");
        if (mCondition >= 0)
            dest.write(context.getString(
                    R.string.report_email_section_name_condition) + ":\n" + context.getResources()
                    .getStringArray(R.array.report_condition)[mCondition] + "\n\n");
        if (mOffenseDetails != null && !mOffenseDetails.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_details) + ":\n" + mOffenseDetails + "\n\n");
        if (mMethodDetails != null && !mMethodDetails.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_method) + ":\n" + mMethodDetails + "\n\n");
        if (mValueEstimate >= 0)
            dest.write(context.getString(R.string.report_email_section_name_value) + ":\n" + String
                    .valueOf(mValueEstimate) + "\n\n");

        if (!Double.isNaN(mOriginLat)) {
            dest.write(context.getString(
                    R.string.report_email_section_name_origin_lat) + ":\n" + String
                    .valueOf(mOriginLat) + "\n\n");
            dest.write(context.getString(
                    R.string.report_email_section_name_origin_lon) + ":\n" + String
                    .valueOf(mOriginLon) + "\n\n");
        }
        if (mOrigin != null && !mOrigin.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_origin_address) + ":\n" + mOrigin + "\n\n");

        if (!Double.isNaN(mDestinationLat)) {
            dest.write(
                    context.getString(R.string.report_email_section_name_dest_lat) + ":\n" + String
                            .valueOf(mDestinationLat) + "\n\n");
            dest.write(
                    context.getString(R.string.report_email_section_name_dest_lon) + ":\n" + String
                            .valueOf(mDestinationLon) + "\n\n");
        }
        if (mDestination != null && !mDestination.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_dest_address) + ":\n" + mDestination +
                    "\n\n");

        if (mVesselDescription != null && !mVesselDescription.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_vessel_description) + ":\n" +
                    mVesselDescription + "\n\n");
        if (mVesselId != null && !mVesselId.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_vessel_id) + ":\n" + mVesselId + "\n\n");
        if (mVesselName != null && !mVesselName.isEmpty())
            dest.write(context.getString(
                    R.string.report_email_section_name_vessel_name) + ":\n" + mVesselName + "\n\n");

        if (mIsPrivate)
            dest.write(
                    "This report was marked as private and will not be syndicated via WildScan " +
                            "app.\n");

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE, Uri.fromParts(
                "mailto", AppPreferences.getString(context, AppPreferences.getReportingRegion
                        (context) + "-email"), null));
        intent.setType("message/rfc822");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String[] emails = new String[mRecipients.size()];
        int i = 0;
        /*for (RecipientInfo rec : mRecipients) {
            if (!TextUtils.isEmpty(rec.second))
                emails[i++] = rec.second;
        }*/
        //if (i > 0 && EMAIL_CONTACTS_ACTIVE)
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, AppPreferences.getString(context,
                AppPreferences.getReportingRegion(context) + "-email").split(","));
        intent.putExtra(Intent.EXTRA_SUBJECT,
                context.getString(R.string.report_email_title, mReportId));
        intent.putExtra(Intent.EXTRA_TEXT, dest.toString());
        ArrayList<Uri> photos = new ArrayList<Uri>();
        for (i = 0; i < MAX_PHOTOS; i++) {
            if (mPhotos[i] == null)
                continue;
            File f = new File(mPhotos[i]);
            Uri uri = FileProvider
                    .getUriForFile(mContext, mContext.getString(R.string.file_provider_authorities),
                            f);
            photos.add(uri);
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, photos);
        GrantReadPermissions(intent, photos);

        //mContext.startActivity(Intent.createChooser(intent, "Send report via e-mail"));
        mContext.startActivity(intent);
    }

    public void writeJson(Context context, Writer out) throws IOException {
        JsonWriter jOut = new JsonWriter(out);

        jOut.beginArray();
        jOut.beginObject();

//		final static String JSON_NAME_PHOTO = "Image 1";
        Calendar c = Calendar.getInstance();
        TimeZone tz = c.getTimeZone();
        c.set(mYear, mMonth, mDay, mHour, mMinute);
        SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT, Locale.US);
        sdf.setTimeZone(tz);

        String date = sdf.format(c.getTime());
        jOut.name(Incidents._S_INCIDENT_DATE).value(date);
        if (Double.isNaN(mIncidentLat)) {
            jOut.name(Incidents._S_LOCATION_LAT).value("NaN");
            jOut.name(Incidents._S_LOCATION_LON).value("NaN");
        } else {
            jOut.name(Incidents._S_LOCATION_LAT).value(mIncidentLat);
            jOut.name(Incidents._S_LOCATION_LON).value(mIncidentLon);
        }

        if (mOffenseDescription != null && !mOffenseDescription.isEmpty())
            jOut.name(Incidents._S_INCIDENT).value(mOffenseDescription);
//		if (mSpeciesName!=null && !mSpeciesName.isEmpty())
//			jOut.name(WildscanDataManager.Incidents._S_SPECIES).value(mSpeciesName);
        if (mSpeciesId != -1L) {
            jOut.name(Incidents._S_SPECIES).value(mSpeciesId);
        }
        if (!Float.isNaN(mAmount) && mAmount > 0) {
            jOut.name(Incidents._S_NUMBER).value(mAmount);
            jOut.name(Incidents._S_NUMBER_UNIT).value(context.getResources()
                    .getStringArray(R.array.report_units)[mAmountUnits]);
        }
        if (mCondition >= 0)
            jOut.name(Incidents._S_INCIDENT_CONDITION).value(context.getResources()
                    .getStringArray(R.array.report_condition)[mCondition]);
        if (mOffenseDetails != null && !mOffenseDetails.isEmpty())
            jOut.name(Incidents._S_OFFENSE_DESCRIPTION).value(mOffenseDetails);
        if (mMethodDetails != null && !mMethodDetails.isEmpty())
            jOut.name(Incidents._S_METHOD).value(mMethodDetails);
        if (mValueEstimate >= 0)
            jOut.name(Incidents._S_VALUE_ESTIMATED_USD).value(mValueEstimate);
        if (!Double.isNaN(mOriginLat)) {
            jOut.name(Incidents._S_ORIGIN_LAT).value(mOriginLat);
            jOut.name(Incidents._S_ORIGIN_LON).value(mOriginLon);
        }
        if (mOrigin != null && !mOrigin.isEmpty())
            jOut.name(Incidents._S_ORIGIN_ADDRESS).value(mOrigin);
        if (!Double.isNaN(mDestinationLat)) {
            jOut.name(Incidents._S_DESTINATION_LAT).value(mDestinationLat);
            jOut.name(Incidents._S_DESTINATION_LON).value(mDestinationLon);
        }
        if (mDestination != null && !mDestination.isEmpty())
            jOut.name(Incidents._S_DESTINATION_ADDRESS).value(mDestination);
        if (mVesselDescription != null && !mVesselDescription.isEmpty())
            jOut.name(Incidents._S_VEHICLE_VESSEL_DESCRIPTION).value(mVesselDescription);
        if (mVesselId != null && !mVesselId.isEmpty())
            jOut.name(Incidents._S_VEHICLE_VESSEL_LICENSE_NUMBER).value(mVesselId);
        if (mVesselName != null && !mVesselName.isEmpty())
            jOut.name(Incidents._S_VESSEL_NAME).value(mVesselName);

        jOut.name("region").value(Util.getRegionId(AppPreferences.getReportingRegion(context)));
        boolean hasContactRecipients = false;
        String share_with = "Wildscan apps";
        for (RecipientInfo recipient : mRecipients) {
            if (recipient.first != null && recipient.first.longValue() > 0L) {
                share_with += "," + String.valueOf(recipient.first);
                hasContactRecipients = true;
            }
        }
        if (hasContactRecipients) {
            jOut.name(Incidents._S_SHARE_WITH).value(share_with);
        }

        jOut.name(Incidents._S_PRIVATE).value(mIsPrivate ? "1" : "0");

        // long myUserId = mUserId;//WildscanDataManager.getUserId();
        jOut.name(Incidents._S_CREATED_BY).value(-2);

        byte[] imageBytes, buffer = new byte[8192];// = new byte[Util.MAX_IMAGE_FILE_SIZE];
        for (int i = 0; i < MAX_PHOTOS; i++) {
            if (mPhotos[i] == null)
                continue;
            FileInputStream imageIn = new FileInputStream(mPhotos[i]);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int nBytes;
            while ((nBytes = imageIn.read(buffer)) != -1) {
                bos.write(buffer, 0, nBytes);
            }
            imageIn.close();
            imageBytes = bos.toByteArray();
            jOut.name(JSON_NAME_PHOTOS[i]).value(Base64.encodeToString(imageBytes, Base64.NO_WRAP));
            bos.close();
        }

        jOut.endObject();
        jOut.endArray();

        jOut.close();
    }

    private void GrantReadPermissions(Intent intent, ArrayList<Uri> uris) {
        List<ResolveInfo> activities = mContext.getPackageManager()
                .queryIntentActivities(intent, 0);
        for (ResolveInfo a : activities) {
            for (Uri uri : uris)
                mContext.grantUriPermission(a.activityInfo.packageName, uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mReportId);
        dest.writeLong(mUserId);
        dest.writeInt(mHour);
        dest.writeInt(mMinute);
        dest.writeInt(mDay);
        dest.writeInt(mMonth);
        dest.writeInt(mYear);
        dest.writeDouble(mIncidentLat);
        dest.writeDouble(mIncidentLon);
        dest.writeString(mOffenseDescription);
        dest.writeString(mSpeciesName);
        dest.writeLong(mSpeciesId);
        dest.writeFloat(mAmount);
        dest.writeInt(mAmountUnits);
        dest.writeInt(mCondition);
        dest.writeString(mOffenseDetails);
        dest.writeString(mMethodDetails);
        dest.writeInt(mValueEstimate);
        dest.writeDouble(mOriginLat);
        dest.writeDouble(mOriginLon);
        dest.writeString(mOrigin);
        dest.writeDouble(mDestinationLat);
        dest.writeDouble(mDestinationLon);
        dest.writeString(mDestination);
        dest.writeString(mVesselDescription);
        dest.writeString(mVesselId);
        dest.writeString(mVesselName);
        for (int i = 0; i < MAX_PHOTOS; i++)
            dest.writeString(mPhotos[i]);
        dest.writeTypedList(mRecipients);
        dest.writeByte((byte) (mIsPrivate ? 1 : 0));
    }

    static class RecipientInfo extends Pair<Long, String> implements Parcelable {

        public static final Parcelable.Creator<RecipientInfo> CREATOR = new Parcelable.Creator<RecipientInfo>() {
            public RecipientInfo createFromParcel(Parcel in) {
                return new RecipientInfo(in);
            }

            @Override
            public RecipientInfo[] newArray(int size) {
                return null;
            }
        };

        public RecipientInfo(long first, String second) {
            super(first, second);
        }

        private RecipientInfo(Parcel in) {
            super(in.readLong(), in.readString());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(first);
            dest.writeString(second);
        }

    }


}
