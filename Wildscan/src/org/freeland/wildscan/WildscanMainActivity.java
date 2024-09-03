package org.freeland.wildscan;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.freeland.wildscan.data.WildscanDataManager;
import org.freeland.wildscan.data.WildscanDataManager.SyncProgressListener;
import org.freeland.wildscan.util.AppPreferences;
import org.freeland.wildscan.util.Dialogs;
import org.freeland.wildscan.util.Util;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class WildscanMainActivity extends Activity implements SyncProgressListener {

    final static String WEBVIEW_HEADER_FORMAT = "<html><head>"
            + "<style type=\"text/css\">body{color: %s; background-color: %s;}"
            + "</style></head>"
            + "<body>";
    final static String WEBVIEW_FOOTER = "</body></html>";
    static final ScrollView.LayoutParams params = new ScrollView.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    WildscanDataManager mDataManager;

    //	private String LOG_TAG = "WildscanMainActivity";
    EventListFragment mEventsFramgent;
    ProgressDialog mSyncProgress = null;
    Dialogs mRegionDialog;
    private Dialog mInfoDialog = null;
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        mDataManager = WildscanDataManager.getInstance(this);

        initDisplay();

        AppPreferences.setFirstTimeSync(this);
        if (AppPreferences.showTutorial(this)) {
            showTutorial(findViewById(R.id.action_region));
            AppPreferences.setShowTutotial(this, false);
        }
    }

    private void initDisplay() {
        setContentView(R.layout.activity_wildscan_main);

        mRegionDialog = new Dialogs();

        String tag = getResources().getString(R.string.events_list_fragment_tag);
        mEventsFramgent = (EventListFragment) getFragmentManager().findFragmentByTag(tag);
        if (mEventsFramgent == null) {
            mEventsFramgent = new EventListFragment();
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.homeEventsListFragment, mEventsFramgent, tag);
            ft.commit();
        }
    }

    private void showTutorial(View v) {

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                new MaterialShowcaseView.Builder(WildscanMainActivity.this)
                        .setTarget(WildscanMainActivity.this.findViewById(R.id.action_region))
                        .setDismissOnTouch(true)
                        .renderOverNavigationBar()
                        .setMaskColour(getResources().getColor(R.color.dark_gray))
                        .setContentText(getResources().getString(R.string.dialog_tutorial_message))
                        .show();
            }
        }, 500);
    }

        @Override
    protected void onDestroy() {
        super.onDestroy();
        WildscanDataManager.getInstance(this).close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wildscan_main, menu);
        this.menu = menu;
        return true;
    }


    public void onClickIdSpecies(View view) {
        Intent intent = new Intent(this, SpeciesIdWizardActivity.class);
        startActivity(intent);
    }

    public void onClickSubmitReport(View view) {
        startReportWizardActivity();
    }

    private void startReportWizardActivity() {
        Intent intent = new Intent(this, ReportWizardActivity.class);
        startActivity(intent);
    }

    public void onClickContacts(View view) {
        Intent intent = new Intent(this, ContactListActivity.class);
        startActivity(intent);
    }

    public void onClickViewMore(View view) {
        Intent intent = new Intent(this, EventListActivity.class);
        startActivity(intent);
    }

    public void onClickShowOnMap(View view) {
        EventInfo[] events = mEventsFramgent.getEventList();
        if (events.length > 0) {
            Intent intent = new Intent(this, EventMapActivity.class);
            intent.putExtra(EventMapActivity.KEY_EVENTS_LIST, events);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.msg_map_no_events, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSpeciesLibrary(View view) {
        Intent intent = new Intent(this, SpeciesLibActivity.class);
        startActivity(intent);
    }

    public void onClickMenuSync(MenuItem item) {
        showSyncProgress();
    }

    public void showSyncProgress() {
        mSyncProgress = new ProgressDialog(this);
        mSyncProgress.setTitle("Wildscan Sync");
        mSyncProgress.setCancelable(false);
        mSyncProgress.show();
        mDataManager.requestSync(this, false, false);


    }

    public void onClickMenuSettings(MenuItem item) {
        Intent intent = new Intent(this, PrefsActivity.class);
        startActivity(intent);
    }

    /*************************
     * SHOW REGION DIALOG TO SELECT REGION FOR SYNC DATA
     **********************************/
    public void onClickRegion(MenuItem item) {
        mRegionDialog.showRegionDialog(WildscanMainActivity.this);
    }

    @SuppressLint("InflateParams")
    public void onClickMenuInfo(MenuItem item) {
        mInfoDialog = new Dialog(this);
        mInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //mInfoDialog.setTitle(R.string.action_info);
        LayoutInflater inf = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View content = inf.inflate(R.layout.dialog_info, null);
        mInfoDialog.setContentView(content);
        try {
            String ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//			if (BuildConfig.DEBUG)
//				ver += "d";
            ((TextView) mInfoDialog.findViewById(R.id.info_version)).setText("ver " + ver);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mInfoDialog.show();
    }

    public void onClickInfoBtn(View v) {
        final String[] titles = mDataManager.getInfoTitles();
        final String[] sections = mDataManager.getInfoSections();
        //final TypedArray ids = getResources().obtainTypedArray(R.array.info_section_raw_ids);
        final int bgc = getResources().getColor(R.color.about_content_bg), fgc = getResources()
                .getColor(R.color.about_text);
        final String header = String.format(WEBVIEW_HEADER_FORMAT, Util.colorToHtmlString(fgc),
                Util.colorToHtmlString(bgc));
        final TextView title = (TextView) mInfoDialog.findViewById(R.id.dialogInfoSectionTitle);
        int position = Integer.parseInt(v.getTag().toString());
        if (position < titles.length)
            title.setText(titles[position]);
        int padding = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.0f, mDisplayMetrics);
        View section = null;
        if (position < sections.length) {
            WebView wv = new WebView(this);
            wv.setPadding(padding, padding, padding, padding);
            String content = sections != null && sections[position] != null ? sections[position]
                    : "";
            wv.loadData(header + content + WEBVIEW_FOOTER, "text/html; charset=UTF-8", null);
            wv.setBackgroundColor(0);
            section = wv;
        } else {
            /*ImageView iv = new ImageView(this);
            iv.setPadding(padding, padding, padding, padding);
			iv.setImageResource(R.drawable.contributor);
			iv.setScaleType(ScaleType.CENTER_INSIDE);
			section = iv;*/
        }
        if (section != null) {
            ScrollView sv = (ScrollView) mInfoDialog.findViewById(R.id.dialogInfoWebContainter);
            sv.removeAllViews();
            sv.addView(section, params);
            mInfoDialog.findViewById(R.id.dialogInfoContentView).setVisibility(View.VISIBLE);
            mInfoDialog.findViewById(R.id.dialogInfoSections).setVisibility(View.GONE);
        }
//		ids.recycle();

        mInfoDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event
                        .getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                    onClickInfoSectionBack(null);
                    return true;
                }
                return false;
            }
        });
    }

    public void onClickInfoSectionBack(View v) {
        mInfoDialog.findViewById(R.id.dialogInfoContentView).setVisibility(View.GONE);
        mInfoDialog.findViewById(R.id.dialogInfoSections).setVisibility(View.VISIBLE);
        mInfoDialog.setOnKeyListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppPreferences.setIsCallFromActivity(this, false);

    }

    @Override
    public void onSyncComplete(boolean canceled) {
        AppPreferences.setIsCallFromActivity(this, false);
        mSyncProgress.dismiss();
    }

    @Override
    public void onSyncProgress(String msg) {
        mSyncProgress.setMessage(msg);
    }
}
