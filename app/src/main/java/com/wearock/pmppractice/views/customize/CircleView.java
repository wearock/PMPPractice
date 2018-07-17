package com.wearock.pmppractice.views.customize;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class CircleView extends View {

    private Paint mTextPain;
    private int bgColor = Color.WHITE;
    private String mText = "";
    private int radius;

    public CircleView(Context context) {
        this(context,null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextPain = new Paint();
        mTextPain.setColor(Color.WHITE);
        mTextPain.setAntiAlias(true);
        mTextPain.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() / 2;
        int height = getHeight() / 2;
        radius = Math.min(width, height);
        //paint bg
        mTextPain.setColor(bgColor);
        canvas.drawCircle(width, height, radius, mTextPain);

        //paint font
        mTextPain.setColor(Color.WHITE);
        mTextPain.setTextSize(dp2px(16));
        Paint.FontMetrics fontMetrics = mTextPain.getFontMetrics();
        canvas.drawText(mText, 0, mText.length(), radius
                , radius + Math.abs(fontMetrics.top + fontMetrics.bottom) / 2, mTextPain);
    }

    public void setText(String str) {
        if(!TextUtils.isEmpty(str)){
            mText = str;
        }else {
            mText =  "";
        }
        invalidate();
    }

    public String getText() {
        return mText;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        invalidate();
    }

    private int dp2px(int dp) {
        // px = dp * (dpi / 160)
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int dpi = metrics.densityDpi;
        return (int) (dp * (dpi / 160f) + 0.5f);
    }

}
