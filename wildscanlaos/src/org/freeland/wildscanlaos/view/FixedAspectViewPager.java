package org.freeland.wildscanlaos.view;

import org.freeland.wildscanlaos.R;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.core.view.ViewPager;
import android.util.AttributeSet;

public class FixedAspectViewPager extends ViewPager {
	protected float mAspectRatio = 1.0f;
	protected boolean mWidthFixed = true;

	public FixedAspectViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedAspect);
		final int nAttrs = a.getIndexCount();
		for (int i=0; i<nAttrs; i++) {
			int index = a.getIndex(i);
			switch(index) {
			case (R.styleable.FixedAspect_ratio):
				mAspectRatio = a.getFloat(index, 1.0f);
				break;
			case (R.styleable.FixedAspect_mode):
				mWidthFixed = a.getInt(i, 0)==0;
			}
		}
		a.recycle();
	}

	public void setRatio(float ratio) {
		mAspectRatio = ratio;
	}
	
	public void fixWidth() {
		mWidthFixed = true;
	}
	
	public void fixHeight() {
		mWidthFixed = false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int w = getMeasuredWidth(), h = getMeasuredHeight();
		if (mAspectRatio>0) {
			if (mWidthFixed) {
				h = (int)(mAspectRatio * w);				
			} else {
				w = (int)(h / mAspectRatio);
			}
		}
		setMeasuredDimension(w, h);
	}

}
