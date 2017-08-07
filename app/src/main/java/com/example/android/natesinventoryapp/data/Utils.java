package com.example.android.natesinventoryapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.example.android.natesinventoryapp.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Hardkornate on 8/3/17.
 */

public class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();

    public static Bitmap getBitmapFromUri(Uri uri, Context mContext, ImageView imageView) {

        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }
        // Get the dimensions of the View
        int targetW = 120;
        int targetH = 120;

        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            if (input != null) {
                input.close();
            }

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = mContext.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            if (input != null) {
                input.close();
            }
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, mContext.getString(R.string.exception_image_load_failed), fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, mContext.getString(R.string.exception_image_load_failed), e);
            return null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                Log.e(LOG_TAG, mContext.getString(R.string.exception_input_output_error));
            }
        }
    }

    private void Utils() {
    }
}

