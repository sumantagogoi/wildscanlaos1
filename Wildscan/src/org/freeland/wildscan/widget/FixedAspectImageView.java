package org.freeland.wildscan.widget;

import org.freeland.wildscan.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FixedAspectImageView extends ImageView {
	protected float mAspectRatio = 1.0f;
	protected boolean mWidthFixed = true;

    public FixedAspectImageView(Context context) {
        super(context);
    }

    public FixedAspectImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

	public FixedAspectImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
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
