package org.freeland.wildscan.view;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.freeland.wildscan.R;

/**
 * Created by Noman on 1/26/2016.
 */
public class RegionDialog extends DialogFragment implements View.OnClickListener{
    private LinearLayout southEastAsia, africa, southAmerica;
    private CheckBox cbAsia,cbAfrica,cbAmerica;
    private int region = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_region, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

    }
    private void init(View view){
        southEastAsia = (LinearLayout) view.findViewById(R.id.south_east_asia);
        africa = (LinearLayout) view.findViewById(R.id.africaLayout);
        southAmerica = (LinearLayout) view.findViewById(R.id.southAmericaLayout);

        cbAfrica = (CheckBox) view.findViewById(R.id.africaCheck);
        cbAmerica = (CheckBox) view.findViewById(R.id.southAmericaCheck);
        cbAsia = (CheckBox) view.findViewById(R.id.southAsiaCheck);

        southEastAsia.setOnClickListener(this);
        africa.setOnClickListener(this);
        southAmerica.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

    }
}
