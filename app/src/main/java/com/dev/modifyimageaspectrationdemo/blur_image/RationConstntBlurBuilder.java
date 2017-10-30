package com.dev.modifyimageaspectrationdemo.blur_image;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

/* https://www.quora.com/How-do-I-add-a-blur-effect-to-an-image-in-Android-development */
public class RationConstntBlurBuilder {

    /*private static final float BLUR_RADIUS = 15f;*/
    private static final float BLUR_RADIUS = 24f;

    public static Bitmap blur(Context context, Bitmap image) {
        Bitmap outputBitmap = Bitmap.createBitmap(image);
 
        RenderScript rs = RenderScript.create(context);
 
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, image);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
 
        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
 
        return outputBitmap;
    }
 
}