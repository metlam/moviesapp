package de.malte.moviesapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;

/**
 * Useful functions and constants
 *
 * Movie API Key must be replaced with own key!
 */
public class Utility {

    public static final String MOVIE_API_KEY = "___ENTER_YOUR_API_KEY_HERE___";

    private static final String MOVIE_IMG_URL = "http://image.tmdb.org/t/p";

    public static final String YOUTUBE_URL = "http://www.youtube.com/watch?v=";


    /**
     * Build movie poster url from json's relative path
     */
    public static String buildMoviePosterURI(String relativePath) {
        final String IMAGE_SIZE = "w185";
        final String file = relativePath.substring(1);

        Uri builtUri = Uri.parse(MOVIE_IMG_URL).buildUpon()
                .appendPath(IMAGE_SIZE)
                .appendPath(file)
                .build();

        return builtUri.toString();
    }

    /**
     * Get preferred sort-order
     */
    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_movie),
                context.getString(R.string.pref_sort_movie_default));
    }

    /**
     * Transform Bitmap to byte-Array
     *
     * source: http://stackoverflow.com/questions/9357668/how-to-store-image-in-sqlite-database
     */
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Check if device is connected to the internet
     *
     * source: http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
     */
    public static boolean hasNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
