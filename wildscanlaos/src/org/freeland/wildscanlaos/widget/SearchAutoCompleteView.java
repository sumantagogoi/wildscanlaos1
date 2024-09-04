package org.freeland.wildscanlaos.widget;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;

import org.freeland.wildscanlaos.R;

import java.util.ArrayList;
import java.util.List;

public class SearchAutoCompleteView extends AutoCompleteTextView {

    static final int MAX_LINES = 5;
    private Context mContext;
    private boolean mReplaceText = true;

    public SearchAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setThreshold(1);
    }

    public SearchAutoCompleteView(Context context) {
        super(context);
        mContext = context;
        setThreshold(1);
    }

    public SearchAutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setThreshold(1);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        return ((Pair<String, Integer>) selectedItem).first;
    }

    @Override
    public void onFilterComplete(int count) {
        setDropDownWidth(LayoutParams.WRAP_CONTENT);
        if (count > MAX_LINES)
            setDropDownHeight(mContext.getResources().getDimensionPixelSize(R.dimen.search_dropdown_height));
        else
            setDropDownHeight(LayoutParams.WRAP_CONTENT);
        super.onFilterComplete(count);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        // TODO Auto-generated method stub
        super.performFiltering(text, keyCode);
    }

    @Override protected void replaceText(CharSequence text) {
        if(mReplaceText)
        super.replaceText(text);
    }

    public void shouldReplaceText(boolean replace){
        this.mReplaceText = replace;
    }

    public static class Adapter extends ArrayAdapter<Pair<String, Integer>> {

        private List<Pair<String, Integer>> mFullList = null;
        private List<Pair<String, Integer>> mFilteredList;
        private Object mLock = new Object();
        private AutocompleteFilter mFilter = null;
        private List<Pair<String, Integer>> mKnownAsList;

        public Adapter(Context context, int resource, List<Pair<String, Integer>> objects, List<Pair<String, Integer>> knownAsList) {
            super(context, resource, objects);
            mFilteredList = objects;
            this.mKnownAsList = knownAsList;



        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            if (TextView.class.isInstance(v)) {
                String text = (String) getItem(position).first;
                v.setTag(getItem(position).second);
                ((TextView) v).setText(Html.fromHtml(text));
            }
            return v;
        }

        @Override
        public int getCount() {
            return mFilteredList.size();
        }

        @Override
        public Pair<String, Integer> getItem(int position) {
            return mFilteredList.get(position);
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null)
                mFilter = new AutocompleteFilter();
            return mFilter;
        }

        private class AutocompleteFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (mFullList == null) {
                    synchronized (mLock) {
                        mFullList = new ArrayList<Pair<String, Integer>>(mFilteredList);
                    }
                }

                if (constraint == null || constraint.length() == 0) {
                    ArrayList<Pair<String, Integer>> list;
                    synchronized (mLock) {
                        list = new ArrayList<Pair<String, Integer>>(mFullList);
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    String prefixString = constraint.toString().toLowerCase();

                    final ArrayList<Pair<String, Integer>> values;
                    ArrayList<Pair<String, Integer>> knownAsList = null;

                    synchronized (mLock) {
                        values = new ArrayList<Pair<String, Integer>>(mFullList);
                        if (mKnownAsList != null)
                            knownAsList = new ArrayList<>(mKnownAsList);
                    }

                    final int count = values.size();
                    final ArrayList<Pair<String, Integer>> newValues = new ArrayList<Pair<String, Integer>>();

                    for (int i = 0; i < count; i++) {
                        final Pair<String, Integer> value = values.get(i);
                        Pair<String, Integer> knownAs = null;
                        if (knownAsList != null)
                            knownAs = knownAsList.get(i);

                        final String valueText = Html.fromHtml((String) value.first).toString().toLowerCase();
                        String strKnownAs = null;
                        if (knownAs != null)
                            strKnownAs = Html.fromHtml((String) knownAs.first).toString().toLowerCase();
                        // First match against the whole, non-splitted value
                        if (strKnownAs != null) {
                            if (valueText.startsWith(prefixString) || strKnownAs.contains(prefixString)) {
                                newValues.add(value);
                            } else {
                                final String[] words = valueText.split(" ");
                                final int wordCount = words.length;

                                // Start at index 0, in case valueText starts with space(s)
                                for (int k = 0; k < wordCount; k++) {
                                    if (words[k].startsWith(prefixString)) {
                                        newValues.add(value);
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (valueText.startsWith(prefixString)) {
                                newValues.add(value);
                            } else {
                                final String[] words = valueText.split(" ");
                                final int wordCount = words.length;

                                // Start at index 0, in case valueText starts with space(s)
                                for (int k = 0; k < wordCount; k++) {
                                    if (words[k].startsWith(prefixString)) {
                                        newValues.add(value);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    newValues.add(new Pair<String, Integer>(getContext()
                            .getResources().getString(R.string.species_lib_search_show_all),-2));
                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredList = (List<Pair<String, Integer>>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

        }

    }
}
