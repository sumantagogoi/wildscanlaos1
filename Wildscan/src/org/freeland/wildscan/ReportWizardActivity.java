/**
 * 
 */
package org.freeland.wildscan;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.freeland.wildscan.Report.RecipientInfo;
import org.freeland.wildscan.data.WildscanDataManager;
import org.freeland.wildscan.data.contract.Contacts;
import org.freeland.wildscan.data.contract.ContactsTranslations;
import org.freeland.wildscan.data.contract.StaticContent;
import org.freeland.wildscan.data.provider.WildscanDataProvider;
import org.freeland.wildscan.util.Util;
import org.freeland.wildscan.widget.SearchAutoCompleteView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author Noam
 *
 */
public class ReportWizardActivity extends Activity implements 
	DateTimePickerDialog.OnTimeSetListener, DateTimePickerDialog.OnDateSetListener {

	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_IMAGE_BROWSE = 2;
	static final int REQUEST_PICK_LOCATION = 3;
	static final int REQUEST_PICK_ORIGIN = 4;
	static final int REQUEST_PICK_DESTINATION = 5;
	static final int REQUEST_SELECT_CONTACT = 6;
	static final int REQUEST_SELECT_SPECIES = 7;
	
	static final int STATE_AQUIRE_PHOTO = 0;
	static final int STATE_BASIC_INFO = 1;
	static final int STATE_MORE_DETAILS = 2;
	static final int STATE_SUBMIT_REPORT = 3;
	
	static final int UNIT_PIECES = 0;
	static final int UNIT_KG = 1;
//	static final String[] UNIT_NAMES = { "individuals", "kg" };
	
	static final int CONDITION_ALIVE = 0;
	static final int CONDITION_DEAD = 1;
	static final int CONDITION_MIXED = 2;
//	static final String[] CONDITION_NAMES = { "alive", "dead", "mixed" };
	
	public static final String KEY_REPORT = "org.freeland.wildscan.ReportWizard.Report";
	public static final String KEY_STATE = "org.freeland.wildscan.ReportWizard.State";
	public static final String KEY_SPECIES_ID= "org.freeland.wildscan.ReportWizard.SpeciesId";
	
	public static final String REPORTS_FOLDER = "reports";
	public static final String REPORTS_BACKUP_FOLDER = "sent";
	public static final String REPORTS_PENDING_FOLDER = "pending";
	static final String TMP_IMAGE_FILE = "report_tmp.jpg";
		
	int mCurrentState;
	Report mReport;
	private boolean mStarted = false;

	FrameLayout mDisplayLayout;
	
	AlertDialog mSubmitProgress;
	AlertDialog mWarningDialog;
	String mReportsFolderPath;
	OnFocusChangeListener mOnFocus;

	// screen 1 - photo capture
	String mCurrentPhotoPath = null;
	ImageView mPhotoImageView;
	int mPhotoPreviewDim;
	boolean mIsTmpFile = false;
	
	// screen 2 - basic details
	ImageButton mBtnEditDateTime, mBtnEditLocation, mBtnIdSpecies;
	Location mStartLocation;
	TextView mDateTime, mAddress;
	boolean mHasTargetLocation = mReport!=null && !Double.isNaN(mReport.mIncidentLat);
	EditText mEditOffense;
	SearchAutoCompleteView mEditSpecies;
	EditText mEditAmount;
	
	//screen 3 - more details
	EditText mEditOffenseDetails, mEditMethod, mEditValue;
	ImageButton mBtnEditOrigin, mBtnEditDestination;
	boolean mHasOrigin = mReport!=null && !Double.isNaN(mReport.mOriginLat), 
			mHasDestination = mReport!=null && !Double.isNaN(mReport.mDestinationLat);
	EditText mEditOrigin, mEditDestination;
	EditText mEditVesselDescription, mEditVesselId, mEditVesselName;
	
	//screen 4 - submit
	Button mBtnAddImageCapture, mBtnAddImageBrowse, mBtnFinalSubmit;
	CheckBox mShareViaEmailChk, mPrivateChk;
	ListView mContactsList;
	ContactListAdapter mContactsAdapter;
	LinearLayout mLayoutThumbnails;
	final int[] mDeletePhotoBtnIds = { R.id.reportWizardBtnDeletePhoto1, R.id.reportWizardBtnDeletePhoto2, R.id.reportWizardBtnDeletePhoto3 };
	final int[] mPhotoThumbIds = { R.id.reportWizardPhotoThumb1, R.id.reportWizardPhotoThumb2, R.id.reportWizardPhotoThumb3 };

	@SuppressLint("InflateParams")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		boolean showWarning = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PrefsFragment.PREFS_KEY_SHOW_WARNING, true); 
		
		if (savedInstanceState != null) {
			mReport = (Report) savedInstanceState.getParcelable(KEY_REPORT);
			mReport.setContext(this);
			mCurrentState = savedInstanceState.getInt(KEY_STATE);
			mStarted = true;
			showWarning = false;
		}
		else {
			mReport = new Report(this);
			final Calendar c = Calendar.getInstance();
		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(c.getTime());
		    mReport.mReportId = "Report_" + timeStamp;
		    setupTime(c);
		    mCurrentState = STATE_AQUIRE_PHOTO;
		}
		
		mReport.mSpeciesId = getIntent().getLongExtra(KEY_SPECIES_ID, -1L);
		if (mReport.mSpeciesId != -1L) {
			mStarted = true;
			mReport.mSpeciesName = WildscanDataManager.getSpeciesName(mReport.mSpeciesId);
		}
		setContentView(R.layout.activity_report_wizard);
		
		if (showWarning) {
			// display warning dialog with 'do not show again' checkbox
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_report_warning, null))
				.setTitle(R.string.report_wizard_warning_dialog_title)
				.setMessage(R.string.report_wizard_warning_dialog_message)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (((CheckBox) mWarningDialog.findViewById(R.id.checkBoxDontShow)).isChecked()) {
							PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(PrefsFragment.PREFS_KEY_SHOW_WARNING, false).commit();
						}
						startReport(savedInstanceState);
					}
				}).setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
				});
			mWarningDialog = builder.show();
		}
		else
			startReport(savedInstanceState);
	}
	
	private void startReport(Bundle savedInstanceState) {	
				
		try {
			mReportsFolderPath = Environment.getExternalStorageDirectory().getCanonicalPath() + File.separator + "Wildscan" + File.separator + REPORTS_FOLDER;
		} catch (IOException e) {
			e.printStackTrace();
		}
		File folder = new File(mReportsFolderPath);
		if (!folder.mkdirs() && !folder.isDirectory()) {
			throw new RuntimeException("Failed to create reports folder");
		} else {
			File f = new File(folder, ".nomedia");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		}
	
		mDisplayLayout = (FrameLayout) findViewById(R.id.reportWizardDisplayLayout);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mPhotoPreviewDim = dm.widthPixels; // scale image to fit full width
	    
	    setupIncidentLocation();
//	    checkPlayServices();
	    
	    initOnFocus();
	    
	    showDisplay();
	}
	
	private void initOnFocus() {
		mOnFocus = new OnFocusChangeListener() {
			final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
				else
					imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
			}
		};
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	outState.putParcelable(KEY_REPORT, mReport);
    	outState.putInt(KEY_STATE, mCurrentState);
    }
        
    private void setupIncidentLocation() {
    	mStartLocation = WildscanDataManager.getLastLocation();
    	if (mStartLocation!=null && Double.isNaN(mReport.mIncidentLat)) {
	    	mReport.mIncidentLat = mStartLocation.getLatitude();
	    	mReport.mIncidentLon = mStartLocation.getLongitude();
    	}
    }
    
    private void setupTime(Calendar c) {
		mReport.mYear = c.get(Calendar.YEAR);
		mReport.mMonth = c.get(Calendar.MONTH);
		mReport.mDay = c.get(Calendar.DAY_OF_MONTH);
		mReport.mHour = c.get(Calendar.HOUR_OF_DAY);
		mReport.mMinute = c.get(Calendar.MINUTE);  	
    }
    
    private void cleanupState() {
		switch(mCurrentState) {
		case STATE_AQUIRE_PHOTO:
			mPhotoImageView.setImageBitmap(null);
			mPhotoImageView.setVisibility(View.GONE);
			deleteTempImageFile();
			break;
		case STATE_BASIC_INFO:
			if (mEditOffense.getText() != null)
				mReport.mOffenseDescription = mEditOffense.getText().toString();
			if (mEditSpecies.getText() != null) {
				mReport.mSpeciesName = mEditSpecies.getText().toString();
				Object tag = mEditSpecies.getTag();
				mReport.mSpeciesId = tag!=null?(Integer)tag:WildscanDataManager.getSpeciesId(mReport.mSpeciesName);
			}
			if (mEditAmount.getText() != null) {
				try {
					mReport.mAmount = Float.parseFloat(mEditAmount.getText().toString());
				} catch (NumberFormatException nfe) {
					mReport.mAmount = Float.NaN;
				}
			}
			break;
		case STATE_MORE_DETAILS:
			if (mEditOffenseDetails.getText() != null)
				mReport.mOffenseDetails = mEditOffenseDetails.getText().toString();
			if (mEditMethod.getText() != null)
				mReport.mMethodDetails = mEditMethod.getText().toString();
			if (mEditValue.getText() != null) {
				try {
					mReport.mValueEstimate = Integer.valueOf(mEditValue.getText().toString());
				} catch (NumberFormatException nfe) {
					mReport.mValueEstimate = -1;
				}
			}
			if (mEditOrigin.getText() != null)
				mReport.mOrigin = mEditOrigin.getText().toString();
			if (mEditDestination.getText() != null)
				mReport.mDestination = mEditDestination.getText().toString();
			if (mEditVesselDescription.getText() != null)
				mReport.mVesselDescription = mEditVesselDescription.getText().toString();
			if (mEditVesselId.getText() != null)
				mReport.mVesselId = mEditVesselId.getText().toString();
			if (mEditVesselName.getText() != null)
				mReport.mVesselName = mEditVesselName.getText().toString();
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mCurrentState==STATE_AQUIRE_PHOTO)
			onClickBtnCancel(null);
		else 
			previousScreen();
	}
	
	private void showDisplay() {
		mDisplayLayout.removeAllViews();
		switch(mCurrentState) {
		case STATE_AQUIRE_PHOTO:
			showDisplay1();
			break;
		case STATE_BASIC_INFO:
			showDisplay2();
			break;
		case STATE_MORE_DETAILS:
			showDisplay3();
			break;
		case STATE_SUBMIT_REPORT:
			showDisplay4();
			break;
		}
	}

	private void showDisplay1() {
		LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.layout_report_wizard_1, mDisplayLayout);

		((TextView)findViewById(R.id.reportWizardHeader)).setText(R.string.report_wizard_screen_1_title);

		mPhotoImageView = new ImageView(this);
		mPhotoImageView.setAdjustViewBounds(true);
		mPhotoImageView.setVisibility(View.GONE);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		mDisplayLayout.addView(mPhotoImageView, params);
		
		boolean hasPhoto = !TextUtils.isEmpty(mReport.mPhotos[0]);
		
		if (hasPhoto) {
			updatePhotoPreview();
			mPhotoImageView.setVisibility(View.VISIBLE);
			mStarted = true;
		}

	    Button b = (Button) findViewById(R.id.reportWizardBtnCancel);
	    b.setVisibility(View.GONE);

	    b = (Button) findViewById(R.id.reportWizardBtn2);
	    b.setText(hasPhoto ? R.string.recapture : R.string.capture);
	    b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickBtnCapture(v);
			}
		});
        b.setVisibility(View.VISIBLE);

	    b = (Button) findViewById(R.id.reportWizardBtn3);
	    b.setText(hasPhoto ? R.string.rebrowse : R.string.browse);
	    b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickBtnBrowse(v);
			}
		});
        b.setVisibility(View.VISIBLE);

	    b = (Button) findViewById(R.id.reportWizardBtnNext);
	    if (hasPhoto) {
		    ((LinearLayout.LayoutParams)b.getLayoutParams()).weight = 0.5f;
		    b.setText(R.string.next);
		    b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					nextScreen();
				}
			});
		    b.setVisibility(View.VISIBLE);
	    } else {
		    b.setVisibility(View.GONE);	    	
	    }
	}

	private void showDisplay2() {
		LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.layout_report_wizard_2, mDisplayLayout);
		
		((TextView)findViewById(R.id.reportWizardHeader)).setText(R.string.report_wizard_screen_2_title);
		
		mDateTime = (TextView) findViewById(R.id.reportWizardDateTime);
		mBtnEditDateTime = (ImageButton)findViewById(R.id.reportWizardBtnEditDateTime);
		updateDateTime();
	    mBtnEditDateTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchDateTimePicker();
			}
		});
	    
	    mBtnEditLocation = (ImageButton)findViewById(R.id.reportWizardBtnEditLocation);
	    mBtnEditLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLng target = null;
				if (mHasTargetLocation)
					target = new LatLng(mReport.mIncidentLat, mReport.mIncidentLon);
				dispatchLocationPicker(REQUEST_PICK_LOCATION, target);
			}
		});
	    mAddress = (TextView) findViewById(R.id.reportWizardLocation);
	    updateAddress(true, mAddress, true, mReport.mIncidentLat, mReport.mIncidentLon);
	    
	    mEditOffense = (EditText)findViewById(R.id.reportWizardOffense);
	    if (!TextUtils.isEmpty(mReport.mOffenseDescription))
	    	mEditOffense.setText(mReport.mOffenseDescription);
	    mEditOffense.setOnFocusChangeListener(mOnFocus);
	    
	    mEditSpecies = (SearchAutoCompleteView)findViewById(R.id.reportWizardSpecies);
	    if (!TextUtils.isEmpty(mReport.mSpeciesName))
	    	mEditSpecies.setText(mReport.mSpeciesName);
	    ArrayList<Pair<String,Integer>> autocomplete = WildscanDataManager.getSpeciesAutocompleteList(null);
	    mEditSpecies.setAdapter(new SearchAutoCompleteView.Adapter(this, android.R.layout.simple_dropdown_item_1line,
				autocomplete,WildscanDataManager.getSpecieAutoCompleteKnownAsList(null)));
	    mEditSpecies.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mEditSpecies.setTag((Integer)view.getTag());
			}
		});
	    mEditSpecies.setOnFocusChangeListener(mOnFocus);
	    mBtnIdSpecies = (ImageButton) findViewById(R.id.reportWizardBtnIdSpecies);
	    mBtnIdSpecies.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), SpeciesIdWizardActivity.class);
				intent.putExtra(SpeciesIdWizardActivity.KEY_NEED_RESULT, true);
				startActivityForResult(intent, REQUEST_SELECT_SPECIES);
			}
		});
	    
	    mEditAmount = (EditText)findViewById(R.id.reportWizardAmount);
	    if (!Float.isNaN(mReport.mAmount)) {
	    	mEditAmount.setText(String.valueOf(mReport.mAmount));
	    }
	    mEditAmount.setOnFocusChangeListener(mOnFocus);
	    RadioButton rb = ((RadioButton)findViewById(R.id.reportWizardAmountUnits));
	    rb.setChecked(mReport.mAmountUnits == UNIT_PIECES);
	    rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mReport.mAmountUnits = UNIT_PIECES;
			}
		});
	    rb = ((RadioButton)findViewById(R.id.reportWizardAmountKg));
	    rb.setChecked(mReport.mAmountUnits == UNIT_KG);
	    rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mReport.mAmountUnits = UNIT_KG;
			}
		});
	    
	    rb = ((RadioButton)findViewById(R.id.reportWizardConditionAlive));
	    rb.setChecked(mReport.mCondition == CONDITION_ALIVE);
	    rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mReport.mCondition = CONDITION_ALIVE;
			}
		});
	    rb = ((RadioButton)findViewById(R.id.reportWizardConditionDead));
	    rb.setChecked(mReport.mCondition == CONDITION_DEAD);
	    rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mReport.mCondition = CONDITION_DEAD;
			}
		});
	    rb = ((RadioButton)findViewById(R.id.reportWizardConditionMixed));
	    rb.setChecked(mReport.mCondition == CONDITION_MIXED);
	    rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mReport.mCondition = CONDITION_MIXED;
			}
		});
	    
	    Button b = (Button) findViewById(R.id.reportWizardBtnCancel);
	    b.setVisibility(View.VISIBLE);

        b = (Button) findViewById(R.id.reportWizardBtn2);
        b.setText(R.string.details);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// date and time saved into mCurrentReport in their callbacks
				// location saved into mCurrentReport in its callback
				nextScreen();
			}
		});
        b.setVisibility(View.VISIBLE);

        b = (Button) findViewById(R.id.reportWizardBtn3);
        b.setVisibility(View.GONE);
        
        b = (Button) findViewById(R.id.reportWizardBtnNext);
        ((LinearLayout.LayoutParams)b.getLayoutParams()).weight = 1.0f;
        b.setText(R.string.submit);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				skipToSumbitReport();
			}
		});
        b.setVisibility(View.VISIBLE);

        
        mDisplayLayout.clearFocus();
		
	}

	private void showDisplay3() {
		LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.layout_report_wizard_3, mDisplayLayout);

		((TextView)findViewById(R.id.reportWizardHeader)).setText(R.string.report_wizard_screen_3_title);
		
	    mEditOffenseDetails = (EditText)findViewById(R.id.reportWizardOffenseDetails);
	    if (mReport.mOffenseDetails!=null && ! mReport.mOffenseDetails.isEmpty())
	    	mEditOffenseDetails.setText(mReport.mOffenseDetails);
	    mEditOffenseDetails.setOnFocusChangeListener(mOnFocus);
	    
	    mEditMethod = (EditText)findViewById(R.id.reportWizardMethod);
	    if (mReport.mMethodDetails!=null && ! mReport.mMethodDetails.isEmpty())
	    	mEditMethod.setText(mReport.mMethodDetails);
	    mEditMethod.setOnFocusChangeListener(mOnFocus);
	    
	    mEditValue = (EditText)findViewById(R.id.reportWizardValue);
	    if (mReport.mValueEstimate > 0)
	    	mEditValue.setText(String.valueOf(mReport.mValueEstimate));
	    mEditValue.setOnFocusChangeListener(mOnFocus);

	    mEditOrigin = (EditText)findViewById(R.id.reportWizardOrigin);
	    mEditOrigin.setOnFocusChangeListener(mOnFocus);
	    mBtnEditOrigin = (ImageButton)findViewById(R.id.reportWizardBtnEditOrigin);
	    mBtnEditOrigin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLng target = null;
				if (mHasOrigin)
					target = new LatLng(mReport.mOriginLat, mReport.mOriginLon);
				dispatchLocationPicker(REQUEST_PICK_ORIGIN, target);
			}
		});
	    if (mReport.mOrigin!=null && ! mReport.mOrigin.isEmpty())
	    	mEditOrigin.setText(mReport.mOrigin);
	    else
	    	updateAddress(true, mEditOrigin, false, mReport.mOriginLat, mReport.mOriginLon);
	    
	    mEditDestination = (EditText)findViewById(R.id.reportWizardDestination);
	    mEditDestination.setOnFocusChangeListener(mOnFocus);
	    mBtnEditDestination = (ImageButton)findViewById(R.id.reportWizardBtnEditDestination);
	    mBtnEditDestination.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLng target = null;
				if (mHasDestination)
					target = new LatLng(mReport.mDestinationLat, mReport.mDestinationLon);
				dispatchLocationPicker(REQUEST_PICK_DESTINATION, target);
			}
		});
	    if (mReport.mOrigin!=null && ! mReport.mOrigin.isEmpty())
	    	mEditDestination.setText(mReport.mDestination);
	    else
	    	updateAddress(true, mEditDestination, false, mReport.mDestinationLat, mReport.mDestinationLon);
	    
	    mEditVesselDescription = (EditText)findViewById(R.id.reportWizardVesselDescription);
	    if (mReport.mVesselDescription!=null && ! mReport.mVesselDescription.isEmpty())
	    	mEditVesselDescription.setText(mReport.mVesselDescription);
	    mEditVesselDescription.setOnFocusChangeListener(mOnFocus);
		
	    mEditVesselId = (EditText)findViewById(R.id.reportWizardVesselId);
	    if (mReport.mVesselId!=null && ! mReport.mVesselId.isEmpty())
	    	mEditVesselId.setText(mReport.mVesselId);
	    mEditVesselId.setOnFocusChangeListener(mOnFocus);
	    
	    mEditVesselName = (EditText)findViewById(R.id.reportWizardVesselName);
	    if (mReport.mVesselName!=null && ! mReport.mVesselName.isEmpty())
	    	mEditVesselName.setText(mReport.mVesselName);
	    mEditVesselName.setOnFocusChangeListener(mOnFocus);
	    
	    Button b = (Button) findViewById(R.id.reportWizardBtnCancel);
        b.setVisibility(View.VISIBLE);
        
        b = (Button) findViewById(R.id.reportWizardBtn2);
        b.setVisibility(View.GONE);
	    
        b = (Button) findViewById(R.id.reportWizardBtn3);
	    b.setVisibility(View.GONE);
	    
	    b = (Button) findViewById(R.id.reportWizardBtnNext);
	    ((LinearLayout.LayoutParams)b.getLayoutParams()).weight = 0.5f;
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextScreen();
			}
		});
        b.setVisibility(View.VISIBLE);

//	    Button b = (Button) findViewById(R.id.reportWizardBtn1);
//	    b.setText(R.string.cancel);
//	    b.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onClickBtnCancel(v);
//			}
//		});
	}	

	@SuppressLint("ClickableViewAccessibility")
	private void showDisplay4() {
		LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.layout_report_wizard_4, mDisplayLayout);
		
		((TextView)findViewById(R.id.reportWizardHeader)).setText(R.string.report_wizard_screen_4_title);
		
		for (int i=0; i<Report.MAX_PHOTOS; i++) {
			updateThumbnail(i);
		}

	    mBtnAddImageCapture = (Button) findViewById(R.id.reportWizardBtnAddPhotoCapture);
		mBtnAddImageCapture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});
		
		mBtnAddImageBrowse = (Button) findViewById(R.id.reportWizardBtnAddPhotoBrowse);
		mBtnAddImageBrowse.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dispatchBrowseIntent();
			}
		});
		
		mShareViaEmailChk = (CheckBox) findViewById(R.id.reportWizardChkShareViaEmail);
		
		mContactsList = (ListView) findViewById(R.id.reportWizardContactsList);
		mContactsAdapter = new ContactListAdapter(this);
		mContactsList.setAdapter(mContactsAdapter);
		mContactsList.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) 
            {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
		((ScrollView)findViewById(R.id.root)).setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                mContactsList.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
		
		mPrivateChk = (CheckBox) findViewById(R.id.reportWizardChkPrivate);
		mPrivateChk.setChecked(mReport.mIsPrivate);
		mPrivateChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mReport.mIsPrivate = mPrivateChk.isChecked();
			}
		});

	    Button b = (Button) findViewById(R.id.reportWizardBtnCancel);
        b.setVisibility(View.VISIBLE);
        
        b = (Button) findViewById(R.id.reportWizardBtn2);
        b.setVisibility(View.GONE);
	    
        b = (Button) findViewById(R.id.reportWizardBtn3);
	    b.setVisibility(View.GONE);
	    
	    b = (Button) findViewById(R.id.reportWizardBtnNext);
	    ((LinearLayout.LayoutParams)b.getLayoutParams()).weight = 0.5f;
		b.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			submitReport(mShareViaEmailChk.isChecked());
		}
	});
        b.setVisibility(View.VISIBLE);
		
//		Button b = (Button) findViewById(R.id.reportWizardBtnCancel);
//	    b.setText(R.string.cancel);
//	    b.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onClickBtnCancel(v);
//			}
//		});
//	    b = (Button) findViewById(R.id.reportWizardBtn2);
//	    b.setText(R.string.submit);
//	    b.setBackgroundColor(getResources().getColor(R.color.btn_submit_bg));
//		b.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				submitReport(mShareViaEmailChk.isChecked());
//			}
//		});
		
	}
	
	private class ContactListAdapter extends ArrayAdapter<Report.RecipientInfo> {

		public ContactListAdapter(Context context) {
			super(context, R.layout.item_report_contacts, mReport.mRecipients);
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {			
			if (convertView==null) {
				LayoutInflater i = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = i.inflate(R.layout.item_report_contacts, null);
			}
			if (position<super.getCount()) {
				long id = getItem(position).first;
				Uri uri = WildscanDataProvider.getItemUri(Contacts.TABLE_NAME, id);
				String[] projection = { Contacts._NAME };
				String selection = Util.addLangToSelection(getApplicationContext(), null, ContactsTranslations._S_LANGUAGE);
				Cursor cc = getContext().getContentResolver().query(uri, projection, selection, null, null);
				if (cc.moveToFirst()) {
					String name = cc.getString(cc.getColumnIndex(Contacts._NAME));
					((TextView)convertView.findViewById(android.R.id.text1)).setText(name);
					((ImageButton)convertView.findViewById(android.R.id.button1)).setImageResource(R.drawable.ic_action_remove_contact);
					((ImageButton)convertView.findViewById(android.R.id.button1)).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							mReport.mRecipients.remove(position);
							//remove(getItem(position));
							notifyDataSetChanged();
						}
					});
				}
				cc.close();			
			}
			else {
				((TextView)convertView.findViewById(android.R.id.text1)).setText(getResources().getString(R.string.report_wizard_screen_4_add_contact));
				((ImageButton)convertView.findViewById(android.R.id.button1)).setImageResource(R.drawable.ic_action_add_contact);
				((ImageButton)convertView.findViewById(android.R.id.button1)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dispatchContactPicker();
					}
				});			}
			return convertView;
		}

		@Override
		public int getCount() {
			return super.getCount()+1;
		}

		@Override
		public RecipientInfo getItem(int position) {
			if (position>=super.getCount())
				return null;
			return super.getItem(position);
		}

		@Override
		public long getItemId(int position) {
			if (position>=super.getCount())
				return -1L;
			return super.getItemId(position);
		}
		
	}
	
	private void dispatchTakePictureIntent() {
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (intent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	    	mCurrentPhotoPath = mReportsFolderPath + File.separator + "tmp" + File.separator + TMP_IMAGE_FILE;
	    	//mCurrentPhotoPath = getExternalCacheDir() + File.separator + TMP_IMAGE_FILE;
	        File photoFile = new File(mCurrentPhotoPath);
	        //File photoFile = new File(getExternalCacheDir(), TMP_IMAGE_FILE);
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	        	File dir = photoFile.getParentFile(); 
	        	if (dir.isDirectory() || dir.mkdirs()) {
		            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		            intent.putExtra("return-data", true);
		            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	        	} else {
	        		Toast.makeText(this, "Cannot create directory: " + photoFile.getParent(), Toast.LENGTH_LONG).show();
	        	}
	        }
	    }
	}
	
	private void dispatchBrowseIntent() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
//		intent.setAction(Intent.ACTION_PICK);
//		intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivityForResult(intent, REQUEST_IMAGE_BROWSE);
	}
	
	private void dispatchLocationPicker(int requestId, LatLng target) {
		Intent intent = new Intent(this,LocationPickerActivity.class);
		if (mStartLocation != null) {
			intent.putExtra(LocationPickerActivity.KEY_START_LAT, mStartLocation.getLatitude());
			intent.putExtra(LocationPickerActivity.KEY_START_LON, mStartLocation.getLongitude());
		}
		if (target!=null) {
			intent.putExtra(LocationPickerActivity.KEY_TARGET_LAT, target.latitude);
			intent.putExtra(LocationPickerActivity.KEY_TARGET_LON, target.longitude);
		}

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, requestId);
	}

	private void dispatchContactPicker() {
		Intent intent = new Intent(this,ContactListActivity.class);
		intent.putExtra(ContactListActivity.KEY_REQUEST_RESULT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivityForResult(intent, REQUEST_SELECT_CONTACT);
	}

	private void updateGalleryPhotoPreview() {
		if (mCurrentPhotoPath==null)
			return;
		if (mCurrentPhotoPath.startsWith("content://")) {
			new downloadPhotoFromPicasaTask().execute(mCurrentPhotoPath, "0");
		} else if (mCurrentPhotoPath!=null && mCurrentPhotoPath.startsWith("http")) {
			new downloadPhotoTask().execute(mCurrentPhotoPath, "0");
		} else {
			saveGalleryFileToReport(0);
			showDisplay1();
			//updatePhotoPreview();
		}
		mStarted = true;
	}
	
	private void saveCameraFileToReport(Bitmap bitmap, int photo) {
		try {
			String outFile = mReportsFolderPath + File.separator + mReport.mReportId + "_" + String.valueOf(photo) + ".jpg";
			if (bitmap!=null) {
				Util.compressPhoto(bitmap, outFile);
		    	mReport.mPhotos[photo] = outFile;
			}
			else {
	    		mCurrentPhotoPath = mReportsFolderPath + File.separator + "tmp" + File.separator + TMP_IMAGE_FILE;
//				mCurrentPhotoPath = getExternalCacheDir() + File.separator + TMP_IMAGE_FILE;
		    	Util.compressPhoto(mCurrentPhotoPath, outFile);
		    	mReport.mPhotos[photo] = outFile;
				new File(mCurrentPhotoPath).delete();
			}
			mCurrentPhotoPath = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveGalleryFileToReport(int photo) {
		if (mCurrentPhotoPath!=null && !mCurrentPhotoPath.startsWith("content://")) {
			try {
				String outFile = mReportsFolderPath + File.separator + mReport.mReportId + "_" + String.valueOf(photo) + ".jpg";
		    	Util.compressPhoto(mCurrentPhotoPath, outFile);
		    	mReport.mPhotos[photo] = outFile;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mCurrentPhotoPath = null;
	}

	private void updateCameraPhotoPreview(Bitmap bitmap) {
		saveCameraFileToReport(bitmap, 0);
		showDisplay1();
		//updatePhotoPreview();
	}
	
	private void updatePhotoPreview() {
		Bitmap bitmap = BitmapFactory.decodeFile(mReport.mPhotos[0]);
    	//mPhotoImageView.setImageURI(Uri.fromFile(new File(mReport.mPhotos[0])));
		mPhotoImageView.setImageBitmap(bitmap);
        mPhotoImageView.setVisibility(View.VISIBLE);
        mDisplayLayout.removeAllViews();
        mDisplayLayout.setBackgroundResource(R.color.report_btn_bg);
        mDisplayLayout.addView(mPhotoImageView);
	}
	
	private void updateGalleryThumbnail() {
		int photo = 0;
		while (photo<Report.MAX_PHOTOS && !TextUtils.isEmpty(mReport.mPhotos[photo]))
			photo++;
		if (photo<Report.MAX_PHOTOS) {
			if (mCurrentPhotoPath!=null && mCurrentPhotoPath.startsWith("content://")) {
				new downloadPhotoFromPicasaTask().execute(mCurrentPhotoPath, String.valueOf(photo));
			} else if (mCurrentPhotoPath!=null && mCurrentPhotoPath.startsWith("http")) {
				new downloadPhotoTask().execute(mCurrentPhotoPath, String.valueOf(photo));
			} else {
				saveGalleryFileToReport(photo);
				updateThumbnail(photo);
			}
		}
	}	
	
	private void updateCameraThumbnail(Bitmap bitmap) {
		int photo = 0;
		while (photo<Report.MAX_PHOTOS && !TextUtils.isEmpty(mReport.mPhotos[photo]))
			photo++;
		if (photo<Report.MAX_PHOTOS)
			updateCameraThumbnail(bitmap, photo);
	}

	private void updateCameraThumbnail(Bitmap bitmap, int photo) {
		if (photo<0) {
			photo = 0;
			while (photo<Report.MAX_PHOTOS && !TextUtils.isEmpty(mReport.mPhotos[photo]))
				photo++;
		}
		if (photo<0 || photo>=Report.MAX_PHOTOS)
			return;
		
		mStarted = true;
		saveCameraFileToReport(bitmap, photo);
		updateThumbnail(photo);
	}

	private void updateThumbnail(int photo) {
	    final ImageView iv = (ImageView) findViewById(mPhotoThumbIds[photo]);
	    final ImageButton ib = (ImageButton) findViewById(mDeletePhotoBtnIds[photo]);
	    
		if (mReport.mPhotos[photo]==null) {
			iv.setImageResource(R.drawable.empty_photo);
			ib.setVisibility(View.GONE);
			return;
		}
		
		iv.setImageURI(Uri.fromFile(new File(mReport.mPhotos[photo])));
	    iv.setScaleType(ScaleType.CENTER_CROP);
	    //iv.setAdjustViewBounds(true);
	    
	    ib.setTag(photo);
	    ib.setVisibility(View.VISIBLE);
	    ib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int i = (Integer)v.getTag();
				if (mReport.mPhotos[i]!=null)
					new File(mReport.mPhotos[i]).delete();
				mReport.mPhotos[i] = null;
				((ImageView)findViewById(mPhotoThumbIds[i])).setImageResource(R.drawable.empty_photo);
				v.setVisibility(View.GONE);
			}
		});
	}
	
	private void updateAddress(boolean isStart, TextView v, boolean full, double lat, double lon) {		
	    String address = getResources().getString(R.string.report_wizard_location_loading);
	    v.setHint(address);
	    if (Geocoder.isPresent()) {
	    	LatLng loc = new LatLng(lat, lon);
	    	new GetAddressTask(this, v, full).execute(loc);
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode!=RESULT_OK) {
			return;
		}
		
    	Bitmap imageBitmap = null;

    	switch(requestCode) {
    	case REQUEST_IMAGE_CAPTURE:
        	if (data != null) {
		        Bundle extras = data.getExtras();
		        imageBitmap = (Bitmap) extras.get("data");
	    	}
        	mIsTmpFile = true;
        	if (mCurrentState==STATE_AQUIRE_PHOTO) {
	        	updateCameraPhotoPreview(imageBitmap);
        	}
        	else {
        		updateCameraThumbnail(imageBitmap);        		
	        	//Toast.makeText(this, "Extra captured image added to report", Toast.LENGTH_SHORT).show();
    			//mEditAddEmail.setOnFocusChangeListener(mOnFocus);
        	}
        	break;
    	case REQUEST_IMAGE_BROWSE:
		    if (data != null && data.getData() != null) {
		        Uri _uri = data.getData();
		        mCurrentPhotoPath = Util.getSelectedImagePath(this, _uri);
	        	mIsTmpFile = false;
	
	        	if (mCurrentPhotoPath==null) {
	        		mCurrentPhotoPath = _uri.toString();
	        		mIsTmpFile = true;
	        	}
	        	if (mCurrentState==STATE_AQUIRE_PHOTO) {
		        	updateGalleryPhotoPreview();
		        }
		        else {
	        		updateGalleryThumbnail();
		        	Toast.makeText(this, R.string.msg_report_gallery_photo_added, Toast.LENGTH_SHORT).show();
		        }
		    }
		    break;
    	case REQUEST_PICK_LOCATION:
        	if (data != null) {
				mReport.mIncidentLat = data.getDoubleExtra(LocationPickerActivity.KEY_TARGET_LAT, Double.NaN);
				mReport.mIncidentLon = data.getDoubleExtra(LocationPickerActivity.KEY_TARGET_LON, Double.NaN);
				mHasTargetLocation = !(Double.isNaN(mReport.mIncidentLat) || Double.isNaN(mReport.mIncidentLon));
				updateAddress(false, mAddress, true, mReport.mIncidentLat, mReport.mIncidentLon);
        	}
        	break;
    	case REQUEST_PICK_ORIGIN:
        	if (data != null) {
				mReport.mOriginLat = data.getDoubleExtra(LocationPickerActivity.KEY_TARGET_LAT, Double.NaN);
				mReport.mOriginLon = data.getDoubleExtra(LocationPickerActivity.KEY_TARGET_LON, Double.NaN);
				mHasOrigin = !(Double.isNaN(mReport.mOriginLat) || Double.isNaN(mReport.mOriginLon));
				updateAddress(false, mEditOrigin, false, mReport.mOriginLat, mReport.mOriginLon);
        	}
        	break;
    	case REQUEST_PICK_DESTINATION:
        	if (data != null) {
				mReport.mDestinationLat = data.getDoubleExtra(LocationPickerActivity.KEY_TARGET_LAT, Double.NaN);
				mReport.mDestinationLon = data.getDoubleExtra(LocationPickerActivity.KEY_TARGET_LON, Double.NaN);
				mHasDestination = !(Double.isNaN(mReport.mDestinationLat) || Double.isNaN(mReport.mDestinationLon));
				updateAddress(false, mEditDestination, false, mReport.mDestinationLat, mReport.mDestinationLon);
        	}
    		break;
    	case REQUEST_SELECT_CONTACT:
        	if (data != null) {
				Bundle extras = data.getExtras();
				long id = extras.getLong(ContactListActivity.KEY_RESULT_CONTACT_ID);
				//String name = extras.getString(ContactListActivity.KEY_RESULT_CONTACT_NAME);
				String email = extras.getString(ContactListActivity.KEY_RESULT_CONTACT_EMAIL);
				RecipientInfo ri = new RecipientInfo(id, email);
				mReport.mRecipients.add(ri);
				mContactsAdapter.notifyDataSetChanged();
        	}
    		break;
    	case REQUEST_SELECT_SPECIES:
        	if (data != null) {
				long sid = data.getLongExtra(SpeciesIdWizardActivity.KEY_RESULT_SPECIES_ID, -1L);
	    		//String sname = data.getStringExtra(SpeciesIdWizardActivity.KEY_RESULT_SPECIES_NAME);
	    		if (sid!=-1L) {
	    			mReport.mSpeciesId = sid;
	    			mReport.mSpeciesName = WildscanDataManager.getSpeciesName(sid);
	    			mEditSpecies.setText(mReport.mSpeciesName);
	    			int i = (int)sid;
	    			mEditSpecies.setTag(Integer.valueOf(i));
	    		}
        	}
    		break;
    	}
    	mStarted = true;
	}

	public void onClickBtnCapture(View v) {
		dispatchTakePictureIntent();
	}
	public void onClickBtnBrowse(View v) {
		dispatchBrowseIntent();
	}
	public void onClickBtnCancel(View v) {
		if (!mStarted)
			finish();
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.report_wizard_confirm_discard_title)
				.setMessage(R.string.report_wizard_confirm_discard_message)
				.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cleanupFiles();
						finish();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
			builder.show();
		}
	}
	
	private void cleanupFiles() {
		File dir = new File(mReportsFolderPath);
		if (dir.exists()) {
			File[] reportFiles;
			File tmpDir = new File(dir,"tmp");
			if (tmpDir.exists()) {
				reportFiles = tmpDir.listFiles();
				for (File f : reportFiles) {
					f.delete();
				}
				tmpDir.delete();
			}
			reportFiles = dir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					return filename.startsWith(mReport.mReportId);
				}
			});
			for (File f : reportFiles) {
				f.delete();
			}
		}
	}
	
	private void submitReport(boolean emailReport) {
		// TODO: review report details and confirm submission
		new SubmitReportTask().execute(emailReport);
	}
	
	private class downloadPhotoTask extends AsyncTask<String, Void, Void> {
		int mPhotoNum = -1;
		String mOutputFileName = null;
		String mTmpFile = null;
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mCurrentState==STATE_AQUIRE_PHOTO) {
				updatePhotoPreview();
			}
			else {
				updateThumbnail(mPhotoNum);
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			Uri uri = Uri.parse(params[0]);
			mPhotoNum = Integer.valueOf(params[1]);
			
		    File tmpDir = new File(mReportsFolderPath + File.separator + "tmp");
		    if (!tmpDir.exists())
		    	tmpDir.mkdirs();
		    mTmpFile = tmpDir.getAbsolutePath() + File.separator + uri.getLastPathSegment();
			mOutputFileName = mReportsFolderPath + File.separator + mReport.mReportId + "_" + params[1] + ".jpg";
			
			Util.httpDownloadFile(uri.toString(), mTmpFile);
			try {
				Util.compressPhoto(mTmpFile, mOutputFileName);
				new File(mTmpFile).delete();
				mReport.mPhotos[mPhotoNum] = mOutputFileName;
				mCurrentPhotoPath = null;
			} catch (IOException e) {
				e.printStackTrace();
			}			
			return null;
		}
	}
	
	private class downloadPhotoFromPicasaTask extends AsyncTask<String, Void, Void> {

		static final String TMP_PICASA_FILE = "report_picasa_tmp";
		int mPhotoNum = -1;
		String mOutputFileName = null;
		String mTmpFile = null;
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (mCurrentState==STATE_AQUIRE_PHOTO) {
				updatePhotoPreview();
			}
			else {
				updateThumbnail(mPhotoNum);
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			Uri uri = Uri.parse(params[0]);
			mPhotoNum = Integer.valueOf(params[1]);
			final String[] proj = { "mime_type" };
			String ext = null;
			
		    File tmpDir = new File(mReportsFolderPath + File.separator + "tmp");
		    if (!tmpDir.exists())
		    	tmpDir.mkdirs();
		    mTmpFile = tmpDir.getAbsolutePath() + File.separator + TMP_PICASA_FILE;

		    Cursor c = getContentResolver().query(uri, proj, null, null, null);
			if (c!=null) {
				c.moveToFirst();
				ContentValues cv = new ContentValues(c.getColumnCount());
				DatabaseUtils.cursorRowToContentValues(c, cv);
				String mime = cv.getAsString(proj[0]);
				ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
				if (ext != null) {
					mTmpFile += "." + ext;
				}
			}
			c.close();
			mOutputFileName = mReportsFolderPath + File.separator + mReport.mReportId + "_" + params[1] + ".jpg";

			OutputStream out = null;
			InputStream in = null;
			try {
				out = new FileOutputStream(mTmpFile);
				in = getContentResolver().openInputStream(uri);
				byte buffer[] = new byte[2048];
				while (in.read(buffer)!=-1)
					out.write(buffer);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (out!=null) out.close();
					if (in!=null) in.close();
				} catch (IOException e) {}
			}
			try {
				Util.compressPhoto(mTmpFile, mOutputFileName);
				new File(mTmpFile).delete();
				mReport.mPhotos[mPhotoNum] = mOutputFileName;
				mCurrentPhotoPath = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
		
	private void deleteTempImageFile() {
		if (mCurrentPhotoPath != null && mIsTmpFile) {
			new File(mCurrentPhotoPath).delete();
		}
		mCurrentPhotoPath = null;
	}

	private void skipToSumbitReport() {
		if (mCurrentState == STATE_SUBMIT_REPORT)
			return;
		
		cleanupState();
		mCurrentState = STATE_SUBMIT_REPORT;
		showDisplay();
	}
	
	private void nextScreen() {
		cleanupState();
		if (mCurrentState < STATE_SUBMIT_REPORT) {
			mCurrentState++;
		
			showDisplay();
		}
	}

	private void previousScreen() {
		cleanupState();
		if (mCurrentState > 0) {
			mCurrentState--;
		
			showDisplay();
		}
	}

	private void dispatchDateTimePicker() {
		DateTimePickerFragment f = new DateTimePickerFragment();
		f.setListeners(this, this);
		f.show(getFragmentManager(), "dateTimePicker");
	}

	public void onTimeSet(TimePicker view, int hour, int minute) {
		mReport.mHour = hour;
		mReport.mMinute = minute;
		updateDateTime();
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		mReport.mYear = year;
		mReport.mMonth = month;
		mReport.mDay = day;
		
		updateDateTime();
	}
	
	private void updateDateTime() {
		Calendar c = Calendar.getInstance();
		c.set(mReport.mYear, mReport.mMonth, mReport.mDay, mReport.mHour, mReport.mMinute);
		DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
		
		mDateTime.setText(sdf.format(c.getTime()));
	}
	
//	private static final boolean ALWAYS_SAVE_JSON_TO_FILE = true;
	private class SubmitReportTask extends AsyncTask<Boolean, Void, Void> {
		
		String jsonFile = null;
		boolean uploadOk = false,
				emailReport = false,
				saveReport = getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getBoolean(PrefsFragment.PREFS_KEY_SAVE_REPORT_BAK, false);
		
		@Override
		protected Void doInBackground(Boolean... params) {
			emailReport = params[0];
			boolean jsonOk = false;
			StringWriter sw = new StringWriter();
			try {

				mReport.writeJson(getApplicationContext(), sw);
				jsonOk = true;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (jsonOk) {

				if (Util.isNetworkAvailable(ReportWizardActivity.this))
					uploadOk = Util.uploadReportJson(ReportWizardActivity.this, sw.toString());
				
				String dir = null;
				if (uploadOk && saveReport) {
					dir = Environment.getExternalStorageDirectory() 
							+ File.separator + "Wildscan" 
							+ File.separator + REPORTS_FOLDER
							+ File.separator + REPORTS_BACKUP_FOLDER;
				}
				else if (!uploadOk) {
					dir = Environment.getExternalStorageDirectory() 
							+ File.separator + "Wildscan" 
							+ File.separator + REPORTS_FOLDER
							+ File.separator + REPORTS_PENDING_FOLDER;
				}
				if (dir!=null) {
					try {
						new File(dir).mkdirs();
						jsonFile = dir + File.separator + mReport.mReportId + ".json";
						FileWriter fw = new FileWriter(jsonFile);
						fw.write(sw.toString());
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					sw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		private Button dismissBtn;
		private TextView messageView;
		@SuppressLint("InflateParams")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			LayoutInflater inf = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View v = inf.inflate(R.layout.dialog_submit_progress, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(ReportWizardActivity.this);
			builder
				.setView(v)
				.setTitle(R.string.report_submit_dialog_title)
				.setCancelable(false)
				.setNeutralButton(getString(R.string.dismiss), (DialogInterface.OnClickListener)null);
			mSubmitProgress = builder.show();
			dismissBtn = mSubmitProgress.getButton(Dialog.BUTTON_NEUTRAL);
			dismissBtn.setEnabled(false);
			messageView = (TextView) mSubmitProgress.findViewById(R.id.message);
			messageView.setText(R.string.report_submit_message);
		}

		@Override
		protected void onPostExecute(Void res) {
			int msgId = uploadOk ? R.string.report_submit_success : R.string.report_submit_fail;
			int resId = uploadOk ? R.drawable.submit_ok : R.drawable.submit_fail;
			
			((ImageView)mSubmitProgress.findViewById(R.id.result)).setImageResource(resId);
			mSubmitProgress.findViewById(R.id.progress).setVisibility(View.GONE);
			mSubmitProgress.findViewById(R.id.result).setVisibility(View.VISIBLE);
			messageView.setText(msgId);
			dismissBtn.setEnabled(true);
			dismissBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mSubmitProgress.dismiss();
					if (emailReport)
						mReport.emailReport(getApplicationContext());
					ReportWizardActivity.this.finish();
				}
			});
		}
		
	}
	
	private class GetAddressTask extends AsyncTask<LatLng, Void, String> {
		Context mContext;
		TextView mView;
		LatLng mLatLng;
		boolean mFullAddress;
		public GetAddressTask(Context context, TextView v, boolean full) {
			super();
			mContext = context;
			mView = v;
			mFullAddress = full;
		}
		/**
		 * Get a Geocoder instance, get the latitude and longitude
		 * look up the address, and return it
		 *
		 * @params params One or more Location objects
		 * @return A string containing the address of the current
		 * location, or an empty string if no address can be found,
		 * or an error message
		 */
		@Override
		protected String doInBackground(LatLng... params) {
			Locale locale= StaticContent.LANG_CODE_TO_LOCALE.get(WildscanDataManager.getInstance(getApplicationContext()).getLanguage());
			Geocoder geocoder =
				new Geocoder(mContext, locale);
//					new Geocoder(mContext, Locale.ENGLISH);
//					new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			mLatLng = params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			try {
				/*
				 * Return 1 address.
				 */
				addresses = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
			} catch (IOException e1) {
				Log.e("GetAddressTask", "IO Exception in getFromLocation()");
				e1.printStackTrace();
				return ("IO Exception trying to get address");
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				String errorString = "Illegal arguments " + Double.toString(mLatLng.latitude) + " , " + Double.toString(mLatLng.longitude) +
						" passed to address service";
				Log.e("GetAddressTask", errorString);
				e2.printStackTrace();
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				Address address = addresses.get(0);
				/*
				 * Format the first line of address (if available),
				 * city, and country name.
				 */
				String addressText;
				if (mFullAddress)
					addressText = String.format(
						"@@%s, %s, %s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
						// Locality is usually a city
						address.getLocality(),
						// The country of the address
						address.getCountryName());
				else
					addressText = String.format(
							"@@%s, %s",
							// Locality is usually a city
							address.getLocality(),
							// The country of the address
							address.getCountryName());
				// Return the text
				return addressText;
			} else {
				return getString(R.string.msg_no_adress_found);
			}
		}
		
		@SuppressLint("DefaultLocale")
		@Override
        protected void onPostExecute(String address) {
            // Set activity indicator visibility to "gone"
//            mActivityIndicator.setVisibility(View.GONE);
            // Display the results of the lookup.
            if (address.startsWith("@@")) {
            	mView.setText(address.substring(2));
            } else if (mLatLng!=null && !Double.isNaN(mLatLng.latitude) && !Double.isNaN(mLatLng.longitude)) {
            	String text = String.format(Locale.ENGLISH, "[%5.3f,%5.3f]", mLatLng.latitude, mLatLng.longitude);
//            	String text = String.format(Locale.getDefault(), "[%5.3f,%5.3f]", mLatLng.latitude, mLatLng.longitude);
            	mView.setText(text);
            } else {
            	mView.setHint(R.string.report_wizard_location_hint);
            	Toast.makeText(mContext, getString(R.string.msg_no_adress_for_location, mLatLng.latitude, mLatLng.longitude), Toast.LENGTH_SHORT).show();
            }
		}
	}

 	public static class TimePickerFragment extends DialogFragment {
 		DateTimePickerDialog.OnTimeSetListener mListener = null;
 		
 		public void setListener(DateTimePickerDialog.OnTimeSetListener l) {
 			mListener = l;
 		}
 		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			int hour, minute;
			final Calendar c = Calendar.getInstance();
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), TimePickerDialog.THEME_HOLO_DARK, (OnTimeSetListener) mListener, hour, minute, true);
		}		
	}
 	
 	public static class DatePickerFragment extends DialogFragment {
 		DateTimePickerDialog.OnDateSetListener mListener = null;
 		
 		public void setListener(DateTimePickerDialog.OnDateSetListener l) {
 			mListener = l;
 		}
 		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			int year, month, day;
			final Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of TimePickerDialog and return it
			return new DatePickerDialog (getActivity(), DatePickerDialog.THEME_HOLO_DARK, (OnDateSetListener) mListener, year, month, day);
		}		
	}

 	public static class DateTimePickerFragment extends DialogFragment {
 		DateTimePickerDialog.OnDateSetListener mDateListener = null;
 		DateTimePickerDialog.OnTimeSetListener mTimeListener = null;
 		
 		public void setListeners(DateTimePickerDialog.OnDateSetListener ld, DateTimePickerDialog.OnTimeSetListener lt) {
 			mDateListener = ld;
 			mTimeListener = lt;
 		}
 		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			int year, month, day;
			int hour, minute;
			final Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new DateTimePickerDialog (getActivity(), DatePickerDialog.THEME_HOLO_DARK, mDateListener, mTimeListener, year, month, day, hour, minute, true);
		}		
	}
 }
