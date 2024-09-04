package org.freeland.wildscanlaos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

public class SyncProgressDialog extends Dialog {
	
	private final int mLayoutResId;

	public SyncProgressDialog(Context context, int layoutResId) {
		super(context);
		mLayoutResId = layoutResId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (mLayoutResId>0) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(mLayoutResId);
		}
	}
}
