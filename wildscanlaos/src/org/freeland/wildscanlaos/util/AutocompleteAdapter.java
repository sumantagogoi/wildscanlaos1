package org.freeland.wildscanlaos.util;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AutocompleteAdapter extends ArrayAdapter<Pair<String,Integer>> {
	
//	private List<Pair<String,Integer>> mFullList = null;
//	private List<Pair<String,Integer>> mFilteredList;
//	private Object mLock = new Object();
//	private AutocompleteFilter mFilter = null;

	public AutocompleteAdapter(Context context, int resource, List<Pair<String,Integer>> objects) {
		super(context, resource, objects);
		
//		mFilteredList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		if (TextView.class.isInstance(v)) {
			String text = (String)getItem(position).first;
			v.setTag(getItem(position).second);
			((TextView)v).setText(Html.fromHtml(text));
		}
		return v;
	}

//	@Override
//	public Filter getFilter() {
//		if(mFilter==null)
//			mFilter = new AutocompleteFilter();
//		return mFilter;
//	}
//	
//	private class AutocompleteFilter extends Filter {
//		@Override
//		protected FilterResults performFiltering(CharSequence constraint) {
//            FilterResults results = new FilterResults();
//
//            if (mFullList == null) {
//                synchronized (mLock) {
//                    mFullList = new ArrayList<Pair<String,Integer>>(mFilteredList);
//                }
//            }
//
//            if (constraint == null || constraint.length() == 0) {
//                ArrayList<Pair<String,Integer>> list;
//                synchronized (mLock) {
//                    list = new ArrayList<Pair<String,Integer>>(mFullList);
//                }
//                results.values = list;
//                results.count = list.size();
//            } else {
//                String prefixString = constraint.toString().toLowerCase();
//
//                final ArrayList<Pair<String,Integer>> values;
//                synchronized (mLock) {
//                    values = new ArrayList<Pair<String,Integer>>(mFullList);
//                }
//
//                final int count = values.size();
//                final ArrayList<Pair<String,Integer>> newValues = new ArrayList<Pair<String,Integer>>();
//
//                for (int i = 0; i < count; i++) {
//                    final Pair<String,Integer> value = values.get(i);
//                    final String valueText = value.first.toString().toLowerCase();
//
//                    // First match against the whole, non-splitted value
//                    if (valueText.startsWith(prefixString)) {
//                        newValues.add(value);
//                    } else {
//                        final String[] words = valueText.split(" ");
//                        final int wordCount = words.length;
//
//                        // Start at index 0, in case valueText starts with space(s)
//                        for (int k = 0; k < wordCount; k++) {
//                            if (words[k].startsWith(prefixString)) {
//                                newValues.add(value);
//                                break;
//                            }
//                        }
//                    }
//                }
//
//                results.values = newValues;
//                results.count = newValues.size();
//            }
//
//            return results;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected void publishResults(CharSequence constraint, FilterResults results) {
//            mFilteredList = (List<Pair<String,Integer>>) results.values;
//            if (results.count > 0) {
//                notifyDataSetChanged();
//            } else {
//                notifyDataSetInvalidated();
//            }
//		}
//		
//	}

}
