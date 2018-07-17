package com.wearock.pmppractice.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ViewHelper {

    public static Bitmap readBitmap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeStream(context.getResources().openRawResource(resId), null, opt);
    }

}
