package org.freeland.wildscan;

import java.lang.ref.WeakReference;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpeciesIdWizardActivity extends Activity implements OnItemClickListener {
	
	public static final int MAX_OPTIONS = 11;
	public static final int Q_PROD_ANI = 0;
	public static final int Q_COVERING = 1;
	public static final int Q_NUMLEGS = 2;
	public static final int Q_COLOR = 3;
	public static final int Q_SIZE = 4;
	public static final int Q_FEATURES = 5;
	public static final int Q_PURPOSE = 6;
	public static final int Q_MATERIAL = 7;
	public static final int Q_PART = 8;
	public static final int Q_DONE = 9;
	
	public static final String KEY_NEED_RESULT = "org.freeland.wildscan.SpeciesIdWizard.NeedResult";
	
	public static final String KEY_RESULT_SPECIES_ID = "org.freeland.wildscan.SpeciesIdWizard.SpeciesId";
//	public static final String KEY_RESULT_SPECIES_NAME = "org.freeland.wildscan.SpeciesIdWizard.SpeciesName";
	
	public static final int REQUEST_SELECT_SPECIES = 0;
	
//	static class AnswerDetails {
//		String mLabel;
//		String mFilter;
//		int mDrawable;
//	}
	
	private static final int[] Q_PROD_ANI_DRAWABLES = {
		R.drawable.id_q_prod_ani_animal, R.drawable.id_q_prod_ani_product
	};
	private static final int[] Q_COVER_DRAWABLES = { 
		R.drawable.id_q_cover_scales, R.drawable.id_q_cover_fur, R.drawable.id_q_cover_feathers, R.drawable.id_q_cover_skin, R.drawable.id_unk
	};
	private static final int[] Q_NUMLEGS_DRAWABLES = null;
	private static final int[] Q_COLOR_DRAWABLES = { 
		R.drawable.id_q_color_red, R.drawable.id_q_color_orange, R.drawable.id_q_color_yellow, R.drawable.id_q_color_green, 
		R.drawable.id_q_color_blue, R.drawable.id_q_color_purple, R.drawable.id_q_color_gray, R.drawable.id_q_color_brown,
		R.drawable.id_q_color_white, R.drawable.id_q_color_black, R.drawable.id_unk
	};
	private static final int[] Q_SIZE_DRAWABLES = {
		R.drawable.id_q_size_tiny, R.drawable.id_q_size_small, R.drawable.id_q_size_medium, R.drawable.id_q_size_large, R.drawable.id_q_size_unk
	};
	private static final int[] Q_FEATURES_DRAWABLES = null;
	private static final int[] Q_PURPOSE_DRAWABLES = {
		R.drawable.id_q_purpose_food, R.drawable.id_q_purpose_clothing, R.drawable.id_q_purpose_jewelery, R.drawable.id_q_purpose_ornament, R.drawable.id_q_purpose_medicine, R.drawable.id_unk
	};
	private static final int[] Q_MATERIAL_DRAWABLES = {
		R.drawable.id_q_material_bone, R.drawable.id_q_material_leather, R.drawable.id_q_material_shell, R.drawable.id_q_material_powder, R.drawable.id_q_material_meat, R.drawable.id_unk 
	};
	private static final int[] Q_PART_DRAWABLES = {
		R.drawable.id_q_part_horn, R.drawable.id_q_part_tusk, R.drawable.id_q_part_skin, R.drawable.id_q_part_paw, R.drawable.id_q_part_tooth, R.drawable.id_unk 
	};


	private static final int[][] Q_DRAWABLES = { 
		Q_PROD_ANI_DRAWABLES,
		Q_COVER_DRAWABLES, Q_NUMLEGS_DRAWABLES, Q_COLOR_DRAWABLES, Q_SIZE_DRAWABLES, Q_FEATURES_DRAWABLES, 
		Q_PURPOSE_DRAWABLES, Q_MATERIAL_DRAWABLES, Q_PART_DRAWABLES
	};
	
	private Bundle mFilters = new Bundle();
	private int mSelection = -1;
	private WeakReference<View> mSelectedView = null;
	private boolean[] mMulti = new boolean[MAX_OPTIONS];
	private String[] mCurrQuestionFilters;
	private int[] mCurrQuestionDrawableIds;
	private int mCurrQuestion = 0;
	private String[] mCurrLabels;
	private GridView mAnswersView = null;
	private boolean mNeedResult = false;
//	private Button mNextBtn = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		mNeedResult = getIntent().getBooleanExtra(KEY_NEED_RESULT, false);
		setContentView(R.layout.activity_species_id_wizard);
		
//		mNextBtn = (Button) findViewById(R.id.speciesIdWizardBtnNext);
		showQuestion();
	}
	
	private void showQuestion() {
		mCurrLabels = null;
		mCurrQuestionDrawableIds = Q_DRAWABLES[mCurrQuestion];
		mSelection = -1;
		mSelectedView = null;
		for (int i=0; i<MAX_OPTIONS; i++)
			mMulti[i] = false;
		
		switch (mCurrQuestion) {
		case Q_PROD_ANI:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_prod_ani_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_prod_ani);
			//mCurrQuestionDrawableIds = getResources().getIntArray(R.array.id_wizard_prod_ani_drawables);
			break;
		case Q_COVERING:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_covering_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_covering);
			//mCurrQuestionDrawableIds = getResources().getIntArray(R.array.id_wizard_covering_drawables);
			break;
		case Q_NUMLEGS:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_num_legs_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_num_legs);
			break;
		case Q_COLOR:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_color_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_color);
			//mCurrQuestionDrawableIds = getResources().getIntArray(R.array.id_wizard_color_drawables);
			break;
		case Q_SIZE:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_size_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_size);
			//mCurrQuestionDrawableIds = getResources().getIntArray(R.array.id_wizard_size_drawables);
			break;
		case Q_FEATURES:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_features_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_features);
			break;
		case Q_PURPOSE:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_purpose_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_purpose);
			break;
		case Q_MATERIAL:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_material_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_material);
			break;
		case Q_PART:
			mCurrQuestionFilters = getResources().getStringArray(R.array.id_wizard_part_filters);
			mCurrLabels = getResources().getStringArray(R.array.id_wizard_part);
			break;
		}
		
		TextView question = (TextView) findViewById(R.id.speciesIdWizardQuestion);
		question.setText(getResources().getStringArray(R.array.id_wizard_questions)[mCurrQuestion]);
		
		mAnswersView = (GridView) findViewById(R.id.speciesIdWizardAnswersGrid);//new GridView(this);
		
		mAnswersView.setOnItemClickListener(this);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, R.id.speciesIdWizardQuestion);
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
		params.topMargin = (int)px;
		
		if (mCurrQuestion != Q_FEATURES) {
			mAnswersView.setNumColumns(2);
//			mAnswersView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			mAnswersView.setAdapter(new AnswersSingleChoiceViewAdapter());

			px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
			params.leftMargin = params.rightMargin = (int)px;
			mAnswersView.setLayoutParams(params);

//			mAnswersView.setHorizontalSpacing((int)px);
			px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
			mAnswersView.setVerticalSpacing((int)px);
			//mNextBtn.setEnabled(false);
		} else {
			mAnswersView.setNumColumns(1);
//			mAnswersView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			mAnswersView.setAdapter(new AnswersMultiChoiceViewAdapter());
			
			px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
			params.leftMargin = params.rightMargin = (int)px;
			mAnswersView.setLayoutParams(params);
			
			px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
			mAnswersView.setVerticalSpacing((int)px);
		}
		
		//mAnswersView.invalidate();		
	}
	
	public void onClickBtnCancel(View view) {
		finish();
	}
	public void onClickBtnIdentify(View view) {
		nextQuestion(true);
	}
	
	@Override
	public void onBackPressed() {
		previousQuestion();
	}

	private void nextQuestion(boolean skip) {
		int next_q = 0;
		String key=null, value=null;
		if (mCurrQuestion!=Q_FEATURES && mSelection>=0 && mSelection<mCurrQuestionFilters.length)
			value = mCurrQuestionFilters[mSelection];
		switch (mCurrQuestion) {
		case Q_PROD_ANI:
			next_q = mSelection!=1?Q_COVERING:Q_PURPOSE;
			key = SpeciesLibActivity.KEY_PROD_ANI_FILTER;
			break;
		case Q_COVERING:
			//next_q = Q_NUMLEGS;
			next_q = Q_COLOR;
			key = SpeciesLibActivity.KEY_COVER_FILTER;
			break;
		case Q_NUMLEGS:
			// unused
			break;
		case Q_COLOR:
			next_q = Q_SIZE;
			key = SpeciesLibActivity.KEY_COLOR_FILTER;
			break;
		case Q_SIZE:
			next_q = Q_FEATURES;
			key = SpeciesLibActivity.KEY_SIZE_FILTER;
			break;
		case Q_FEATURES:
			next_q = Q_DONE;
			key = SpeciesLibActivity.KEY_SPEC_FILTER;
			for (int i=0; i<mCurrQuestionFilters.length; i++) {
				if (mMulti[i]) {
					if (value==null) {
						value = "";
					} else {
						value += "/";
					}
					value += mCurrQuestionFilters[i];
				}
			}
			break;
		case Q_PURPOSE:
			next_q = Q_MATERIAL;
			key = SpeciesLibActivity.KEY_PURPOSE_FILTER;
			break;
		case Q_MATERIAL:
			next_q = Q_PART;
			key = SpeciesLibActivity.KEY_MATERIAL_FILTER;
			break;
		case Q_PART:
			next_q = Q_DONE;
			key = SpeciesLibActivity.KEY_PART_FILTER;
			break;
		}
		if (value != null) {
			mFilters.putString(key, value);
		}
		if (skip)
			next_q = Q_DONE;
		if (next_q != Q_DONE) {
			mCurrQuestion = next_q;
			showQuestion();
		} else {
			endWizard();
		}
	}
	
	private void previousQuestion() {
		String key=null;
		switch (mCurrQuestion) {
		case Q_PROD_ANI:
			key = SpeciesLibActivity.KEY_PROD_ANI_FILTER;
			onClickBtnCancel(null);
			break;
		case Q_COVERING:
			mCurrQuestion = Q_PROD_ANI;
			key = SpeciesLibActivity.KEY_COVER_FILTER;
			break;
		case Q_PURPOSE:
			key = SpeciesLibActivity.KEY_PURPOSE_FILTER;
			mCurrQuestion = Q_PROD_ANI;
			break;
		case Q_NUMLEGS:
			// unused
			break;
		case Q_COLOR:
			key = SpeciesLibActivity.KEY_COLOR_FILTER;
			mCurrQuestion = Q_COVERING;
			break;
		case Q_SIZE:
			key = SpeciesLibActivity.KEY_SIZE_FILTER;
			mCurrQuestion = Q_COLOR;
			break;
		case Q_FEATURES:
			key = SpeciesLibActivity.KEY_SPEC_FILTER;
			mCurrQuestion = Q_SIZE;
			break;
		case Q_MATERIAL:
			key = SpeciesLibActivity.KEY_MATERIAL_FILTER;
			mCurrQuestion = Q_PURPOSE;
			break;
		case Q_PART:
			key = SpeciesLibActivity.KEY_PART_FILTER;
			mCurrQuestion = Q_MATERIAL;
			break;
		}
		mFilters.remove(key);
		showQuestion();
	}
	
	private void endWizard() {
		Intent intent = new Intent(getBaseContext(), SpeciesLibActivity.class);
		intent.putExtras(mFilters);
		if (mNeedResult) {
			intent.putExtra(SpeciesLibActivity.KEY_NEED_RESULT, true);
			startActivityForResult(intent, REQUEST_SELECT_SPECIES);
		}
		else {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK || requestCode != REQUEST_SELECT_SPECIES)
			return;
		Intent res = new Intent();
		long id = data.getLongExtra(SpeciesLibActivity.KEY_RESULT_SPECIES_ID, -1L);
		res.putExtra(KEY_RESULT_SPECIES_ID, id);
		setResult(Activity.RESULT_OK, res);
		finish();
	}

	public static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
	public static final int[] EMPTY_STATE_SET = { android.R.attr.state_empty };
	private void setItemChecked(View view, boolean check, boolean multi) {
		view.getBackground().setState(check ? CHECKED_STATE_SET : EMPTY_STATE_SET);

		int color = getResources().getColorStateList(R.color.selector_id_item_text).getColorForState(check ? CHECKED_STATE_SET : EMPTY_STATE_SET, getResources().getColor(R.color.list_item_text_fg));
		((TextView)view.findViewById(R.id.text1)).setTextColor(color);

		if (multi)
			((ImageView)view.findViewById(R.id.image1)).getDrawable().setState(check ? CHECKED_STATE_SET : EMPTY_STATE_SET);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
		if (mCurrQuestion != Q_FEATURES) {
			// single choice selection...
			if (mSelectedView!=null && mSelectedView.get().equals(view))
				return;
			if (mSelectedView!=null) {
				View old = mSelectedView.get();
				if (old != null)
					setItemChecked(old, false, false);
			}
			
			setItemChecked(view, true, false);
			
			mSelection = position;
			mSelectedView = new WeakReference<View>(view);
			
			nextQuestion(false);
		}
		else {
			// multiple choice..
			mMulti[position] = !mMulti[position];
			setItemChecked(view, mMulti[position], true);
		}
	}

	private class AnswersSingleChoiceViewAdapter extends BaseAdapter {

		private final int mLayoutResource = R.layout.item_id_q_single;

		@Override
		public int getCount() {
			return mCurrLabels.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			CharSequence text = mCurrLabels[position];
			int image = -1;
			if (mCurrQuestionDrawableIds!=null) {
				image = mCurrQuestionDrawableIds[position];
			}
			
			if (convertView==null) {
				LayoutInflater inf = (LayoutInflater) SpeciesIdWizardActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
				v = inf.inflate(mLayoutResource, parent, false);
			}
			else {
				v = convertView;
			}
			TextView tv = (TextView)v.findViewById(R.id.text1);
			tv.setText(text);
			
			if (image!=-1) {
				ImageView iv = (ImageView)v.findViewById(R.id.image1); 
				iv.setImageResource(image);
			}
			
			return v;
		}
		
	}
	private class AnswersMultiChoiceViewAdapter extends BaseAdapter {
		
		private final int mLayoutResource = R.layout.item_id_q_multi;

		@Override
		public int getCount() {
			return mCurrLabels.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			CharSequence text = mCurrLabels[position];
			if (convertView==null) {
				LayoutInflater inf = (LayoutInflater) SpeciesIdWizardActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
				v = inf.inflate(mLayoutResource, parent, false);
			}
			else {
				v = convertView;
			}
			TextView tv = (TextView)v.findViewById(R.id.text1);
			tv.setText(text);
			
			return v;		
		}
	}
}
