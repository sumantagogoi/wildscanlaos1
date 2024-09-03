package org.freeland.wildscan;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.freeland.wildscan.communication.ServiceGenerator;
import org.freeland.wildscan.data.WildscanDataManager;
import org.freeland.wildscan.data.WildscanDataManager.SyncProgressListener;
import org.freeland.wildscan.data.WildscanDownloaderService;
import org.freeland.wildscan.models.RegionStats;
import org.freeland.wildscan.models.Regions;
import org.freeland.wildscan.models.response.RegionResponse;
import org.freeland.wildscan.models.response.StatsResponse;
import org.freeland.wildscan.util.AppConstants;
import org.freeland.wildscan.util.AppPreferences;
import org.freeland.wildscan.util.Util;
import org.freeland.wildscan.util.Util.InvalidCrcException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class WildscanLaunchActivity extends Activity implements IDownloaderClient, SyncProgressListener {

    private final int RC_SIGN_IN = 1000;
    private final int STARTUP_SCREEN_2_DELAY = 1000;
    WildscanDataManager mDataManager;
    private String LOG_TAG = "WildscanLaunchActivity";
    private IStub mDownloaderClientStub;
    private IDownloaderService mRemoteService;
    private TextView mProgressMessageView, mProgressStatusView;
    private FrameLayout mFrame;
    private boolean mDownloadComplete = false;
    private boolean mSignInComplete = false;
    private boolean isFirstTime;


    private void startMainActivity() {

        boolean isFirstTime = getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0) == 0;
        String localFolder = getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getString(PrefsFragment.PREFS_KEY_DATA_FOLDER, null);
        if (localFolder == null) {
            localFolder = getFilesDir().getAbsolutePath();
            isFirstTime = true;

            getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).edit().putString(PrefsFragment.PREFS_KEY_DATA_FOLDER, localFolder).commit();
        }
        File d = new File(localFolder);
        if (!d.exists()) {
            d.mkdirs();
        }
        File f = new File(d, ".nomedia");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isFirstTime) {
            setView(R.layout.dialog_initial_sync);
            mProgressMessageView.setText(R.string.sync_initial_starting_message);
        } else {
            setView(R.layout.dialog_startup_1);
            mProgressMessageView.setText(R.string.sync_starting_message);
        }
        if (Util.isNetworkAvailable(this))
            mDataManager.requestSync(this, !isFirstTime, isFirstTime);
        else {
            onSyncComplete(false);
        }
    }

    private void getRegionStats() {
        Call<StatsResponse> regionStatsCall = ServiceGenerator.createService().getRegionStats(AppConstants.API_SECRET_KEY,
               "");
        regionStatsCall.enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(Response<StatsResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    RegionStats global = response.body().getData().get(1);
                    RegionStats africa = response.body().getData().get(0);
                    RegionStats america = response.body().getData().get(2);
                    RegionStats asia = response.body().getData().get(3);
                    AppPreferences.setRegionStats(WildscanLaunchActivity.this,
                            global.getTotal_contacts(), global.getTotal_species(), asia.getTotal_contacts(), asia.getTotal_species(),
                            america.getTotal_contacts(), america.getTotal_species(), africa.getTotal_contacts(), africa.getTotal_species());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void getRegions() {
        Call<RegionResponse> regionStatsCall = ServiceGenerator.createService()
                .getRegions(AppConstants.API_SECRET_KEY);
        regionStatsCall.enqueue(new Callback<RegionResponse>() {
            @Override
            public void onResponse(Response<RegionResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    for (Regions regions : response.body().getData()) {
                        AppPreferences.putString(WildscanLaunchActivity.this, regions.getCode()
                                +"-email",regions.getReportEmail());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });

    }

    @Override
    public void onSyncComplete(boolean canceled) {
        //setView(R.layout.dialog_startup_2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //mStartupProgressDialog.dismiss();
                if (isFirstTime) {
                    Intent main = new Intent(WildscanLaunchActivity.this, WildscanMainActivity.class);
                    main.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(main);
                    finish();
                    AppPreferences.setIsCallFromActivity(WildscanLaunchActivity.this, false);
                }

            }
        }, STARTUP_SCREEN_2_DELAY);
//		}, 100);
    }

    @Override
    public void onSyncProgress(String msg) {
        if (mProgressStatusView != null)
            mProgressStatusView.setText(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRegionStats();
        getRegions();
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        isFirstTime = getSharedPreferences(PrefsFragment.PREFS_FILENAME, 0).getLong(PrefsFragment.PREFS_KEY_LAST_SUCCESSFUL_SYNC, 0) == 0;

        setContentView(R.layout.activity_launch);
        mFrame = (FrameLayout) findViewById(R.id.frame);
        setView(R.layout.dialog_startup_1);

        mDataManager = WildscanDataManager.getInstance(this);

        boolean startMain = true;
        mDownloadComplete = mDataManager.expansionFilesDelivered();
        mSignInComplete = true;//WildscanDataManager.getUserId()!=-1L;
/*        if (!mSignInComplete) {
            mProgressStatusView.setText(R.string.status_verify);
            startMain = false;
//			Intent signin = new Intent(WildscanLaunchActivity.this, SignInActivity.class);
//			startActivityForResult(signin, RC_SIGN_IN);
            new SignInTask().execute();
        }*/
        if (!mDownloadComplete) {
            try {
                Intent launchIntent = this.getIntent();

                // Build an Intent to start this activity from the Notification
                Intent notifierIntent = new Intent(this, getClass());
                notifierIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                notifierIntent.setAction(launchIntent.getAction());

                if (launchIntent.getCategories() != null) {
                    for (String category : launchIntent.getCategories()) {
                        notifierIntent.addCategory(category);
                    }
                }

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifierIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Start the download service (if required)
                //Log.v(LOG_TAG , "Start the download service");
                int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this, pendingIntent, WildscanDownloaderService.class);

                // If download has started, initialize activity to show progress
                if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
                    //Log.v(LOG_TAG, "initialize activity to show progress");
                    // Instantiate a member instance of IStub
                    mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this, WildscanDownloaderService.class);
                    setView(R.layout.dialog_initial_sync);
                    startMain = false;
                } else {
                    // If the download wasn't necessary, fall through to start the app
                    //Log.v(LOG_TAG, "No download required");
                }
            } catch (NameNotFoundException e) {
                Log.e(LOG_TAG, "Packege name not found");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        if (startMain)
            startMainActivity();
        if (!isFirstTime) {
            Intent main = new Intent(WildscanLaunchActivity.this, WildscanMainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
            finish();
        }

    }

    @Override
    protected void onStart() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.connect(this);
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.connect(this);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.disconnect(this);
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                synchronized (this) {
                    mSignInComplete = true;
                    if (mDownloadComplete)
                        startMainActivity();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error)
                        .setMessage(R.string.status_signin_failed_abort)
                        .setNegativeButton(R.string.exit, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setPositiveButton(R.string.retry, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent retry = new Intent(getApplicationContext(), WildscanLaunchActivity.class);
                                retry.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(retry);
                                finish();
                            }
                        }).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setView(int layout) {
        mFrame.removeAllViews();
        LayoutInflater i = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = i.inflate(layout, mFrame, true);
        mProgressMessageView = (TextView) v.findViewById(R.id.message);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/JuliusSansOne.ttf");
        if (mProgressMessageView != null) {
            mProgressMessageView.setTypeface(tf);
            mProgressMessageView.setText(null);
        }
        mProgressStatusView = (TextView) v.findViewById(R.id.status);
        if (mProgressStatusView != null) {
            mProgressStatusView.setTypeface(tf);
            mProgressStatusView.setText(null);
        }
    }

    @Override
    public void onServiceConnected(Messenger m) {
        mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
    }

    @Override
    public void onDownloadStateChanged(int newState) {
        //Log.v(LOG_TAG, "DownloadStateChanged : " + getString(Helpers.getDownloaderStringResourceIDFromState(newState)));

        switch (newState) {
            case STATE_DOWNLOADING:
                Log.v(LOG_TAG, "Downloading...");
                break;
            case STATE_COMPLETED: // The download was finished
                synchronized (this) {
                    mDownloadComplete = true;
                    if (mSignInComplete)
                        startMainActivity();
                }
                break;
            case STATE_FAILED_UNLICENSED:
            case STATE_FAILED_FETCHING_URL:
            case STATE_FAILED_SDCARD_FULL:
            case STATE_FAILED_CANCELED:
            case STATE_FAILED:
                // TODO: mDataManager.close();
                Toast.makeText(this, R.string.status_apk_exp_dl_fail_abort, Toast.LENGTH_LONG).show();
                finish();
                break;
        }
    }

    @Override
    public void onDownloadProgress(DownloadProgressInfo progress) {
        long percents = progress.mOverallProgress * 100 / progress.mOverallTotal;
        //Log.v(LOG_TAG, "DownloadProgress:"+Long.toString(percents) + "%");
        if (mProgressStatusView != null)
            mProgressStatusView.setText(getString(R.string.status_apk_exp_download, percents));
    }

    private class SignInTask extends AsyncTask<Void, Void, Void> {

        boolean success = false;

        @Override
        protected Void doInBackground(Void... params) {
            if (mDataManager.getUserId() == -1L)
                return null;
            if (!Util.isNetworkAvailable(WildscanLaunchActivity.this)) {
                success = true;
                return null;
            }

            String auth = mDataManager.generateAuthString();
            int ver = -1;
            try {
                ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (NameNotFoundException e1) {
                e1.printStackTrace();
            }
            if (auth == null || ver == -1)
                return null;
            try {
                List<NameValuePair> requestParams = new ArrayList<NameValuePair>(1);
                requestParams.add(new BasicNameValuePair("authorization", auth));
                // fix version code to 23 (no access to backend code for now..)
                ver = 23;
                requestParams.add(new BasicNameValuePair("ver", String.valueOf(ver)));

                HttpPost httpRequest = new HttpPost(mDataManager.getRemoteBaseUrl() + WildscanDataManager.REMOTE_PHP_LOGIN);
                httpRequest.setEntity(new UrlEncodedFormEntity(requestParams));
                httpRequest.addHeader("Cache-Control", "no-cache");
                HttpResponse httpResponse = Util.getHttpClient(getApplicationContext()).execute(httpRequest);
                int sc = httpResponse.getStatusLine().getStatusCode();
                InputStream is = httpResponse.getEntity().getContent();
                String response = Util.readResponse(is);
                is.close();
                if (sc == 200 && Util.checkLoginResponseJson(response, WildscanLaunchActivity.this)) {
                    success = true;
                }
            } catch (ConnectTimeoutException e) {
                success = true;
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidCrcException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WildscanLaunchActivity.this, R.string.msg_crc_check_fail, Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (success) {
                synchronized (this) {
                    mSignInComplete = true;
                    if (mDownloadComplete)
                        startMainActivity();
                }
            } else if (mDataManager.getUserId() == -1) {
                // do sign-in
                startMainActivity();

                //	Intent signin = new Intent(WildscanLaunchActivity.this, SignInActivity.class);
                //	startActivityForResult(signin, RC_SIGN_IN);
            } else {
                Toast.makeText(WildscanLaunchActivity.this, R.string.status_verification_failed, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
