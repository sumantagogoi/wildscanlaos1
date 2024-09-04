package org.freeland.wildscanlaos.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.freeland.wildscanlaos.R;
import org.freeland.wildscanlaos.WildscanMainActivity;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/5/2016.
 */
public class Dialogs {


    private Dialog myDialog;

   /* public static void showTutotialDialog(final Context context) {
        final Dialog tutorial = new Dialog(context,
                android.R.style.Theme_Translucent_NoTitleBar);
        tutorial.setContentView(R.layout.dialog_tutorial);


        ImageView regionGuide = (ImageView) tutorial.findViewById(R.id.region_guide);
        if (Util.hasSoftKeys((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))) {
            regionGuide.setImageResource(R.drawable.guide_no_soft_keys);
        } else {
            regionGuide.setImageResource(R.drawable.guide_has_soft_key);
        }
        regionGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppPreferences.setShowTutotial(context, false);
                tutorial.dismiss();
            }
        });

        tutorial.show();
    }*/

    /****************************
     * REGION DIALOG TO SELECT REGION FOR SYNC DATA**********************
     */
    /*
    public void showRegionDialog(final Context context) {
        try {
            final boolean globalCheck = AppPreferences.isGlobalRegion(context),
                          asiaCheck = AppPreferences.isAsiaRegion(context),
                          africaCheck = AppPreferences.isAfricaRegion(context);
            if (null == myDialog) myDialog = new Dialog(context, R.style.MyDialogTheme);
            myDialog.setContentView(R.layout.dialog_region);


            ((TextView) myDialog.findViewById(R.id.globalContactsTV))
                    .setText("Contacts: " + AppPreferences.getGlobalContacts(context));
            ((TextView) myDialog.findViewById(R.id.globalSpeciesTV))
                    .setText("Species: " + AppPreferences.getGlobalSpecies(context));
            ((TextView) myDialog.findViewById(R.id.asiaContactsTV))
                    .setText("Contacts: " + AppPreferences.getAsiaContacts(context));
            ((TextView) myDialog.findViewById(R.id.asiaSpeciesTV))
                    .setText("Species: " + AppPreferences.getAsiaSpecies(context));
            ((TextView) myDialog.findViewById(R.id.africaContactTV))
                    .setText("Contacts: " + AppPreferences.getAfricaContacts(context));
            ((TextView) myDialog.findViewById(R.id.africaSpeciesTV))
                    .setText("Species: " + AppPreferences.getAfricaSpecies(context));

            LinearLayout africa = (LinearLayout) myDialog.findViewById(R.id.africaLayout);

            CheckBox cbGlobal = (CheckBox) myDialog.findViewById(R.id.globalCheck);
            CheckBox cbAfrica = (CheckBox) myDialog.findViewById(R.id.africaCheck);
            CheckBox cbAsia = (CheckBox) myDialog.findViewById(R.id.southAsiaCheck);

            //cbGlobal.setChecked(AppPreferences.isGlobalRegion(context));
            cbAfrica.setChecked(AppPreferences.isAfricaRegion(context));
            cbAsia.setChecked(AppPreferences.isAsiaRegion(context));

            Spinner region = (Spinner) myDialog.findViewById(R.id.regionSpinner);

            final ArrayList<String> regionList = new ArrayList<>();
            regionList.add(context.getResources().getString(R.string.region_dialog_global));
            regionList.add(context.getResources().getString(R.string.region_dialog_south_asia));
            regionList.add(context.getResources().getString(R.string.region_dialog_africa));
            /*
            final ArrayList<String> regionCodes = new ArrayList<>();
            regionCodes.add(AppConstants.REGION_GLOBAL_CODE);
            regionCodes.add(AppConstants.REGION_SOUTH_EAST_ASIA_CODE);
            regionCodes.add(AppConstants.REGION_AFRICA_CODE);

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_dropdown_item_1line, regionList);
            region.setAdapter(arrayAdapter);
            if (AppPreferences.getReportingRegion(context)
                    .equals(AppConstants.REGION_GLOBAL_CODE)) {
                region.setSelection(arrayAdapter.getPosition(context.getResources()
                        .getString(R.string.region_dialog_global)));
            } else if (AppPreferences.getReportingRegion(context)
                    .equals(AppConstants.REGION_SOUTH_EAST_ASIA_CODE)) {
                region.setSelection(arrayAdapter.getPosition(context.getResources()
                        .getString(R.string.region_dialog_south_asia)));
            } else if (AppPreferences.getReportingRegion(context)
                    .equals(AppConstants.REGION_AFRICA_CODE)) {
                region.setSelection(arrayAdapter.getPosition(context.getResources()
                        .getString(R.string.region_dialog_africa)));
            }
            */
            /*
            region.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    AppPreferences.setReportingRegion(context, regionCodes.get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            cbGlobal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    AppPreferences.setRegions(context,
                            b,
                            AppPreferences.isAmericanRegion(context),
                            AppPreferences.isAsiaRegion(context),
                            AppPreferences.isAfricaRegion(context));
                    if (b) {
                        AppPreferences.setIsCallFromActivity(context, true);
                        if (Util.isNetworkAvailable(context))
                            ((WildscanMainActivity) context).showSyncProgress();
                        else
                            Toast.makeText(context, context.getResources()
                                            .getString(R.string.no_network_connection_toast),
                                    Toast.LENGTH_LONG).show();
                    }

                }
            });
            cbAsia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    AppPreferences.setRegions(context,
                            AppPreferences.isGlobalRegion(context),
                            AppPreferences.isAmericanRegion(context),
                            b,
                            AppPreferences.isAfricaRegion(context));
                    if (b) {
                        AppPreferences.setIsCallFromActivity(context, true);
                        if (Util.isNetworkAvailable(context))
                            ((WildscanMainActivity) context).showSyncProgress();
                        else
                            Toast.makeText(context, context.getResources()
                                            .getString(R.string.no_network_connection_toast),
                                    Toast.LENGTH_LONG).show();
                    }

                }
            });
            cbAfrica.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    AppPreferences.setRegions(context,
                            AppPreferences.isGlobalRegion(context),
                            AppPreferences.isAmericanRegion(context),
                            AppPreferences.isAsiaRegion(context),
                            b);
                    if (b) {
                        AppPreferences.setIsCallFromActivity(context, true);
                        if (Util.isNetworkAvailable(context))
                            ((WildscanMainActivity) context).showSyncProgress();
                        else
                            Toast.makeText(context, context.getResources()
                                            .getString(R.string.no_network_connection_toast),
                                    Toast.LENGTH_LONG).show();
                    }                }
            });

            myDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}