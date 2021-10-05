package com.benmassarano.gardeninghelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    protected static String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                               byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    protected static Bitmap getBitmapFromString(String stringPicture) {
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    protected static int differenceBetweenDays(String startDate, String endDate) {
        Date start = null;
        Date end = null;
        try {
            start = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(startDate);
            end = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(endDate);
        } catch (ParseException e) {
            Log.i("debug", "Date casting failed");
            e.printStackTrace();
        }

        long diffInMs = end.getTime() - start.getTime();
        long msInDay = 24 * 60 * 60 * 1000;
        return (int) ((double) diffInMs / msInDay);
    }

    protected static String getCurrentDay() {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }
}
