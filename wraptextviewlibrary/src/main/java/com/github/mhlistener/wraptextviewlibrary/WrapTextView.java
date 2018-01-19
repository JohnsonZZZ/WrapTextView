package com.github.mhlistener.wraptextviewlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * Created by JohnsonFan on 2017/12/5.
 */

public class WrapTextView extends View {
	public final String DEFALUT_LANGUAGE = "中";
	private int mTextColor;//文本颜色
	private int mTextSize;//文本大小
	private String mText;//均匀分布文本内容
	private String mTextExtra;//文本末尾附属内容
	private String[] mTextContent;//均匀分布文本内容
	private Paint mPaint;//画笔
	private float mCellPadding;//均匀字体分布间距

	public WrapTextView(Context context) {
		this(context, null);
	}

	public WrapTextView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WrapTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttributeSet(attrs);
	}

	private void initAttributeSet(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.WrapTextView);
			mTextColor = array.getColor(R.styleable.WrapTextView_textColor, Color.BLACK);
			mTextSize = array.getDimensionPixelSize(R.styleable.WrapTextView_textSize, sp2px(14));
			mText = array.getString(R.styleable.WrapTextView_text);
			mTextExtra = array.getString(R.styleable.WrapTextView_textExtra);
			array.recycle();

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(mTextColor);
			mPaint.setTextSize(mTextSize);
			initTextContent();
			mTextExtra = mTextExtra == null ? "" : mTextExtra;
		}
	}

	private void initTextContent() {
		mText = mText == null ? "" : mText;
		mTextContent = new String[mText.length()];
		for (int i = 0; i < mText.length(); i ++) {
			mTextContent[i] = mText.substring(i, (i + 1));
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int drawY = (int) ((canvas.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2)) ;//字体垂直居中
		for (int i = 0; i < mTextContent.length; i ++) {
			float fontChangeSize = Math.abs(measureText(mTextContent[i]) - measureText(1));
			log("onDraw fontChangeSize:" + fontChangeSize);
			log(String.format("onDraw 第%d个字位置:%s", i+1, getPaddingLeft() + i * mCellPadding + measureText(i) + ""));
			canvas.drawText(mTextContent[i], getPaddingLeft() + i * mCellPadding + measureText(i) +
					fontChangeSize / 2, drawY, mPaint);
			if (i == mTextContent.length - 1) {
				log("onDraw extra位置:" + (getPaddingLeft() + i * mCellPadding + measureText(i + 1)));
				if (i == 0) {
					canvas.drawText(mTextExtra, getPaddingLeft() + mCellPadding + measureText(mTextContent.length),
							drawY, mPaint);
				} else {
					canvas.drawText(mTextExtra, getPaddingLeft() + i * mCellPadding + measureText(mTextContent.length),
							drawY, mPaint);
				}
			}
		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		calculateCellPadding(w);
		log("onSizeChanged, mCellPadding:" + mCellPadding);
	}

	private void calculateCellPadding(int viewWidth) {
		if (mTextContent.length == 2) {
			mCellPadding = viewWidth - getPaddingLeft() - getPaddingRight() - measureText(2) - measureText
					(mTextExtra);
		} else if (mTextContent.length > 2) {
			mCellPadding = (viewWidth - getPaddingLeft() - getPaddingRight() - measureText(mTextContent.length)
					- mPaint.measureText(mTextExtra))/(mTextContent.length - 1);
		} else {
			mCellPadding = viewWidth - getPaddingLeft() - getPaddingRight() - measureText(1) - measureText
					(mTextExtra) ;
		}
	}

	/**
	 * 适配Wrap_Content
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//适配wrap_content
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int wrapWidth = (int) (measureText(mText.length()) + measureText(mTextExtra) +
				getPaddingLeft() + getPaddingRight());
		int wrapHeight = (int) (measureText(1) + getPaddingTop() + getPaddingBottom());

		if (MeasureSpec.AT_MOST == widthMode && MeasureSpec.AT_MOST == heightMode) {
			setMeasuredDimension(wrapWidth, wrapHeight);
		} else if (MeasureSpec.AT_MOST == widthMode) {
			setMeasuredDimension(wrapWidth, heightSize);
		} else if (MeasureSpec.AT_MOST == heightMode) {
			setMeasuredDimension(widthSize, wrapHeight);
		} else {
			setMeasuredDimension(widthSize, heightSize);
		}

	}

	/**
	 * 计算字符长度
	 * @param text
	 * @return
	 */
	private float measureText(String text) {
		return mPaint.measureText(text);
	}

	/**
	 * 获取maxLength个中文字符的长度
	 * @param maxLength
	 * @return
	 */
	private float measureText(int maxLength) {
		return measureText(DEFALUT_LANGUAGE) * maxLength;
	}

	public void setText(String text) {
		mText = text;
		initTextContent();
		calculateCellPadding(getMeasuredWidth());
		invalidate();
	}

	public void setTextExtra(String text) {
		mTextExtra = text == null ? "" : text;
		calculateCellPadding(getMeasuredWidth());
		invalidate();
	}

	private int sp2px(float spValue){
		float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	private void log(String logText) {
		Log.e("WrapTextView", logText);
	}

}
