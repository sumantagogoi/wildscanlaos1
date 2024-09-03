package org.freeland.wildscan;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.freeland.wildscan.data.WildscanDataManager;
import org.freeland.wildscan.util.PRNGFixes;
import org.freeland.wildscan.util.Util;
import org.freeland.wildscan.util.Util.InvalidCrcException;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

public class SignInActivity extends Activity implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {
	private static final String TAG = "SignInActivity";

	private static final int STATE_DEFAULT = 0;
	private static final int STATE_SIGN_IN = 1;
	private static final int STATE_IN_PROGRESS = 2;
	
	private static final int STATE_CONNECTED = 1;
	private static final int STATE_REQUEST_CODE = 2;
	private static final int STATE_HAS_CODE = 3;
	private static final int STATE_DID_SIGNUP = 4;
	private static final int STATE_DID_LOGIN = 5;

	private static final int RC_SIGN_IN = 0;
	private static final int RC_RECOVER_FROM_PLAY_SERVICES_ERROR = 1;
	private static final int RC_RECOVER_FROM_AUTH_ERROR = 2;

	private static final int DIALOG_PLAY_SERVICES_ERROR = 0;

	private static final String SAVED_PROGRESS_1 = "sign_in_progress";
	private static final String SAVED_PROGRESS_2 = "login_progress";

	public static final String KEY_EXTRA_TOKEN = "org.freeland.wildscan.SignInActivity.Token";

	private static final String SERVER_CLIENT_ID = "762033562570-lfj0ogvr3d2ikar4ip1vetqo0gm6usnl.apps.googleusercontent.com";
	private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
	private static final String OAUTH_SCOPE = "oauth2:" + "server:client_id:" + SERVER_CLIENT_ID + ":api_scope:" + EMAIL_SCOPE;
	
	private GoogleApiClient mGoogleApiClient = null;
	private SignUpTask mSignUpTask = null;
	private int mSignInProgress;
	private boolean mStarted = true;
	ConnectionResult mSignInResult = null;

	private SignInButton mSignInButton;
	private TextView mMessage;//, mStatus;

	private String mAccount;
	private String mServerCookie = null;
	private long mServerUserId = -1L;
//	private String mIdToken;
	private WildscanDataManager mDataManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		setContentView(R.layout.activity_sign_in);
		
		mDataManager = WildscanDataManager.getInstance(this);
		// allow if already signed up and no netkork is available..
		if (!Util.isNetworkAvailable(this) && mDataManager.getUserId()!=-1L) {
			setResult(RESULT_OK);
			finish();
		}

		mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
//		mStatus = (TextView) findViewById(R.id.sign_in_status);
		mMessage = (TextView) findViewById(R.id.message);
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/JuliusSansOne.ttf");
		mMessage.setTypeface(tf);
		mMessage.setText(null);
		((TextView) findViewById(R.id.request)).setTypeface(tf);


		mSignInButton.setOnClickListener(this);
//		findViewById(R.id.progress).setClickable(true);
//		if (BuildConfig.DEBUG) {
//			findViewById(R.id.progress).setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					new ResetUserTask().execute();
//				}
//			});
//		}
		if (savedInstanceState != null) {
			mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS_1, STATE_DEFAULT);
			mLoginState = savedInstanceState.getInt(SAVED_PROGRESS_2, STATE_DEFAULT);
			if (mSignInProgress!=STATE_DEFAULT || mLoginState!=STATE_DEFAULT)
				mStarted = true;
		}

		mGoogleApiClient = buildGoogleApiClient();
	}

	private GoogleApiClient buildGoogleApiClient() {
		return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.useDefaultAccount()
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, Plus.PlusOptions.builder().build())
				.addScope(new Scope(EMAIL_SCOPE)).build();
	}

	@Override
	protected void onStart() {
//		if (BuildConfig.DEBUG) Log.i(TAG, "onStart");
		super.onStart();
		if (mStarted)
			mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
//		if (BuildConfig.DEBUG) Log.i(TAG, "onStop");
		super.onStop();

		if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
//		if (BuildConfig.DEBUG) Log.i(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putInt(SAVED_PROGRESS_1, mSignInProgress);
		outState.putInt(SAVED_PROGRESS_2, mLoginState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS_1, STATE_DEFAULT);
		mLoginState = savedInstanceState.getInt(SAVED_PROGRESS_2, STATE_DEFAULT);
		if (mSignInProgress!=STATE_DEFAULT || mLoginState!=STATE_DEFAULT)
			mStarted = true;
	}

	@Override
	public void onClick(View v) {
		if (!mStarted) {
			mStarted = true;
			mGoogleApiClient.connect();
			return;
		}
//		if (BuildConfig.DEBUG) Log.i(TAG, "onClick " + v.getId());
		if (mLoginState==STATE_HAS_CODE) {
			Intent retry = new Intent(this, getClass());
			retry.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|Intent.FLAG_ACTIVITY_FORWARD_RESULT);
			startActivity(retry);
			finish();
		}
		if (mGoogleApiClient.isConnected()) {
			v.setEnabled(false);
			doSignIn();
		}
		else if (!mGoogleApiClient.isConnecting()) {
			switch (v.getId()) {
			case R.id.sign_in_button:
				mMessage.setText(R.string.status_signing_in);
				resolveSignInError();
				break;
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// Reaching onConnected means we consider the user signed in.
//		if (BuildConfig.DEBUG) Log.i(TAG, "onConnected");

		// Update the user interface to reflect that the user is signed in.
		mSignInButton.setEnabled(false);

		// Indicate that the sign in process is complete.
		mSignInProgress = STATE_DEFAULT;
		mLoginState = STATE_CONNECTED;

		doSignIn();		
	}
	
	private int mLoginState = STATE_DEFAULT;
	private class SignUpTask extends AsyncTask<Void, Void, Void> {
		private static final String TAG = "SignUpTask";
//		private static final String NAME_KEY = "given_name";
//		private static final String EMAIL_KEY = "email";
				
		@Override
		protected Void doInBackground(Void... params) {
//			if (BuildConfig.DEBUG) Log.i(TAG, "doInBackground");
			if (mLoginState<STATE_CONNECTED)
				return null;
			if (mDataManager.getUserId()!=-1L)
				signIn();
			else {
				try {
//					showStatus("Checking account with WildScan");
					if (mLoginState < STATE_HAS_CODE) {
						showMessage(R.string.status_retrieve_id);
						Bundle extras = new Bundle();
						extras.putBoolean(GoogleAuthUtil.KEY_SUPPRESS_PROGRESS_SCREEN, true);
						final String token = GoogleAuthUtil.getToken(getApplicationContext(), mAccount, OAUTH_SCOPE, extras);
						if (token!=null) {
							mLoginState = STATE_HAS_CODE;
							//showMessage("Token recieved:\n" + token);
							newUserSignUp(token);
							GoogleAuthUtil.invalidateToken(getApplicationContext(), token);
						}
					}
					if (mLoginState==STATE_DID_SIGNUP)
						signIn();
				} catch (final GooglePlayServicesAvailabilityException playEx) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							int statusCode = playEx.getConnectionStatusCode();
							Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
									SignInActivity.this, RC_RECOVER_FROM_PLAY_SERVICES_ERROR);
							dialog.show();
						}
					});
				} catch (UserRecoverableAuthException userAuthEx) {
					mLoginState = STATE_REQUEST_CODE;
					// This should not occur for ID tokens.
					//showMessage("Error: " + userAuthEx.getMessage());
					Log.e(TAG, userAuthEx.getMessage());
					userAuthEx.printStackTrace();
					startActivityForResult(userAuthEx.getIntent(), RC_RECOVER_FROM_AUTH_ERROR);
				} catch (GoogleAuthException authEx) {
					// General auth error.
					//showMessage("Error: " + authEx.getMessage());
					showMessage(R.string.status_registration_fail);
					Log.e(TAG, authEx.getMessage());
					authEx.printStackTrace();
				} catch (MalformedURLException e) {
					showMessage(R.string.status_registration_fail);
					e.printStackTrace();
				} catch (IOException e) {
					showMessage(R.string.status_registration_fail);
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
//			if (BuildConfig.DEBUG) Log.i(TAG, "onPostExecute");
			mSignUpTask = null;
			switch (mLoginState) {
			case (STATE_CONNECTED):
			case (STATE_REQUEST_CODE):
				// waiting for google+ authorization
				return;
			case (STATE_DID_LOGIN):
				SignInActivity.this.setResult(RESULT_OK);
				SignInActivity.this.finish();
				break;
			case (STATE_DID_SIGNUP):
				showMessage(R.string.status_new_user_signin_fail);
			case (STATE_HAS_CODE):
			default:
				//SignInActivity.this.setResult(RESULT_CANCELED);
			}
			findViewById(R.id.progress).setVisibility(View.GONE);
			onSignedOut();
		}
	}

	
	private void signIn() {
		String auth = mDataManager.generateAuthString();
		int ver = -1;
		try {
			ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		if (auth==null || ver==-1)
			return;
		try {
			List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
			requestParams.add(new BasicNameValuePair("authorization", auth));
			requestParams.add(new BasicNameValuePair("ver", String.valueOf(ver)));

			HttpPost httpRequest = new HttpPost(mDataManager.getRemoteBaseUrl() + WildscanDataManager.REMOTE_PHP_LOGIN);
			httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
		    httpRequest.addHeader("Cache-Control", "no-cache");
			HttpResponse httpResponse = Util.getHttpClient(getApplicationContext()).execute(httpRequest);
			int sc = httpResponse.getStatusLine().getStatusCode();
			InputStream is = httpResponse.getEntity().getContent();
			String response = Util.readResponse(is);
			is.close();
			if (sc == 200) {
				if (Util.checkLoginResponseJson(response, this)) {
					mLoginState = STATE_DID_LOGIN;
				} else {
					showMessage(R.string.status_login_failed);
				}
//			} else if (sc == 400 || sc == 401) {
//				showMessage("Server auth error, please try again. ");// + response);
//				if (BuildConfig.DEBUG) Log.i(TAG, "Server auth error: " + response);
			} else {
				showMessage(R.string.status_server_error);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidCrcException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(SignInActivity.this, R.string.msg_crc_check_fail, Toast.LENGTH_LONG).show();
				}
			});
			e.printStackTrace();
		}
		if (mLoginState!=STATE_DID_LOGIN) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onSignedOut();
				}
			});			
		}
	}
	
	@SuppressLint("TrulyRandom")
	private void newUserSignUp(String token) throws IOException {
		
		if (mLoginState < STATE_HAS_CODE)
			return;
		
		showMessage(R.string.status_registering);
		
		byte[] secretBytes = new byte[20];
		PRNGFixes.apply();
		new SecureRandom().nextBytes(secretBytes);

		String secret = Base64.encodeToString(secretBytes, Base64.NO_WRAP).substring(0, 15);

		List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
		requestParams.add(new BasicNameValuePair("code", token));
		requestParams.add(new BasicNameValuePair("android_id", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));
		requestParams.add(new BasicNameValuePair("client_user_secret", secret));

		HttpPost httpRequest = new HttpPost(mDataManager.getRemoteBaseUrl() + WildscanDataManager.REMOTE_PHP_SIGNUP);
	    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(requestParams);
	    httpRequest.setEntity(entity);
	    httpRequest.addHeader("Cache-Control", "no-cache");
		HttpClient cl = Util.getHttpClient(getApplicationContext());
		HttpResponse httpResponse = cl.execute(httpRequest);
		int sc = httpResponse.getStatusLine().getStatusCode();
		InputStream is = httpResponse.getEntity().getContent();
		String response = Util.readResponse(is);
		is.close();
		if (sc == 200) {
			if (readSignupResponseJson(response)) {
				mDataManager.setUserCredentials(mServerUserId, mServerCookie, token, secret);
				mLoginState = STATE_DID_SIGNUP;
			} else {
				showMessage(R.string.status_signup_failed);// response:\n" + response);
			}
//		} else if (sc == 400 || sc == 401) {
//			showMessage("Server authorization error, please try again");// + response);
////			if (BuildConfig.DEBUG) Log.i(TAG, "Server auth error: " + response);
		} else {
//			if (BuildConfig.DEBUG) Log.i(TAG, "Server returned the following error code: " + sc + "\nmessage: " + response + "\n");
			showMessage(getString(R.string.status_server_error_code, sc));
		}
	}

	private boolean readSignupResponseJson(String jsonResponse) {
		try {
			JSONObject response = new JSONObject(jsonResponse);
			mServerCookie = response.getString("cookie");
			mServerUserId = response.getLong("user-id");
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			//showMessage("Error: " + e.getMessage() + "\n" + jsonResponse);
		}
		return false;
	}

	private void showMessage(final int resId) {
		showMessage(getString(resId));
	}
	private void showMessage(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() { mMessage.setText(msg); }
		});
	}

	private void setDefaultAccount() {
		//mAccount  = Plus.AccountApi.getAccountName(mGoogleApiClient);
		Account[] accounts = AccountManager.get(this).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		if (accounts.length>0) {
			mAccount = accounts[0].name;
		}		
	}

	private void doSignIn() {
		if (mAccount == null) {
			setDefaultAccount();
		}
		if (mSignUpTask==null) {
			findViewById(R.id.progress).setVisibility(View.VISIBLE);
			mSignUpTask = new SignUpTask();
			mSignUpTask.execute();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
//		if (BuildConfig.DEBUG) Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "	+ result.getErrorCode());

		if (mSignInProgress != STATE_IN_PROGRESS) {
			mSignInResult = result;

			if (mSignInProgress == STATE_SIGN_IN) {
				resolveSignInError();
			}
		}
		onSignedOut();
	}

	private void resolveSignInError() {
		if (mSignInResult != null) {
			try {
				mSignInProgress = STATE_IN_PROGRESS;
				//startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
				mSignInResult.startResolutionForResult(this, RC_SIGN_IN);
			} catch (SendIntentException e) {
//				if (BuildConfig.DEBUG) Log.i(TAG, "Sign in intent could not be sent: "	+ e.getLocalizedMessage());
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		} else {
			showDialog(DIALOG_PLAY_SERVICES_ERROR);
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RC_RECOVER_FROM_PLAY_SERVICES_ERROR:
		case RC_RECOVER_FROM_AUTH_ERROR:
			if (resultCode == RESULT_OK) {
				doSignIn();
			}
			break;
		case RC_SIGN_IN:
			mSignInProgress = resultCode==RESULT_OK ? STATE_SIGN_IN : STATE_DEFAULT;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
			break;
		}
	}

	private void onSignedOut() {
		// Update the UI to reflect that the user is signed out.
		mSignInButton.setEnabled(true);
		//mStatus.setText("signed out");
	}

	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PLAY_SERVICES_ERROR:
			if (mSignInResult!=null
					&& GooglePlayServicesUtil.isUserRecoverableError(mSignInResult.getErrorCode())) {
				return GooglePlayServicesUtil.getErrorDialog(mSignInResult.getErrorCode(),
						this, RC_SIGN_IN,
						new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						Log.e(TAG, "Google Play services resolution cancelled");
						mSignInProgress = STATE_DEFAULT;
						mMessage.setText(R.string.status_signed_out);
					}
				});
			} else {
				return new AlertDialog.Builder(this)
				.setMessage(R.string.status_play_services_error)
				.setPositiveButton(R.string.close,
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.e(TAG, "Google Play services error could not be resolved: "	+ String.valueOf(mSignInResult!=null ? mSignInResult.getErrorCode() : -1));
						mSignInProgress = STATE_DEFAULT;
						mMessage.setText(R.string.status_signed_out);
					}
				}).create();
			}
		default:
			return super.onCreateDialog(id);
		}
	}
	
//	private class ResetUserTask extends AsyncTask<Void, Void, Void> {
//
//		@Override
//		protected Void doInBackground(Void... params) {
//			showMessage("resetting user...");
//			List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
//			requestParams.add(new BasicNameValuePair("email", mAccount));
//			requestParams.add(new BasicNameValuePair("android_id", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));
//			
//			try {
//				HttpPost httpRequest = new HttpPost(RESET_URL);
//			    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(requestParams);
//			    httpRequest.setEntity(entity);
//				{
//					String msg = httpRequest.getRequestLine() + "\n";
//					Header[] headers = httpRequest.getAllHeaders();
//					String content = EntityUtils.toString(httpRequest.getEntity());
//	
//					for (Header header : headers) {
//					    msg += (header.getName() + ": " + header.getValue()) + "\n";
//					}
//					msg += "\n" + content;
//					showMessage(msg);
//				}
//				HttpClient cl = Util.getHttpClient(getApplicationContext());
//				HttpResponse httpResponse = cl.execute(httpRequest);
//				int sc = httpResponse.getStatusLine().getStatusCode();
//				InputStream is = httpResponse.getEntity().getContent();
//				String response = Util.readResponse(is);
//				if (sc == 200) {
//					showMessage("user reset");
//				} else {
//					showMessage("Server returned the following error message: " + response + "\n");
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			mDataManager.resetUserCredentials();
//			return null;
//		}
//		
//	}
}
