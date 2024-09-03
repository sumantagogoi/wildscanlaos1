package org.freeland.wildscan;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class PrefsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(android.R.style.Theme_Holo);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new PrefsFragment())
        .commit();
		
	}

}
