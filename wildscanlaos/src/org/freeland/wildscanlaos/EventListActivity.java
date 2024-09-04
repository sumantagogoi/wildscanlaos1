/**
 * 
 */
package org.freeland.wildscanlaos;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * @author Noam
 *
 */
public class EventListActivity extends Activity {
	EventListFragment mEventsFramgent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		
        setContentView(R.layout.activity_events_list);

		String tag = getResources().getString(R.string.events_list_fragment_tag);
		mEventsFramgent = (EventListFragment) getFragmentManager().findFragmentByTag(tag);
		if (mEventsFramgent==null) {
			mEventsFramgent = new EventListFragment();
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.eventsListListFragment, mEventsFramgent);
            ft.commit();			
		}
    }
    
	public void onClickHeaderBtnShowOnMap(View view) {
		if (mEventsFramgent.getEventList().length > 0) {
			Intent intent = new Intent(this, EventMapActivity.class);
			intent.putExtra(EventMapActivity.KEY_EVENTS_LIST, mEventsFramgent.getEventList());
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			finish();
		}
		else {
			Toast.makeText(this, R.string.msg_map_no_events, Toast.LENGTH_SHORT).show();
		}
	}


}
