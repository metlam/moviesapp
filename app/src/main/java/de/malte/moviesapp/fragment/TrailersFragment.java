package de.malte.moviesapp.fragment;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.malte.moviesapp.R;
import de.malte.moviesapp.Utility;
import de.malte.moviesapp.entity.Movie;
import de.malte.moviesapp.entity.Trailer;

/**
 * Trailers Fragment
 * Display the list of trailers, launch youtube/browser to view trailers
 * Share first Trailer of list via ShareActionProvider
 */
public class TrailersFragment extends Fragment {

    private static final String LOG_TAG = OverviewFragment.class.getSimpleName();

    private int mMovie_id;

    private boolean mTrailersLoaded = false;

    // the main layout for trailers (whole fragment)
    private LinearLayout mMasterLinearLayout;

    // the list of trailers
    private LinearLayout mTrailerLinearLayout;

    // share the first youtube url
    private ShareActionProvider mShareActionProvider;
    private String mShareTrailer = "";


    public TrailersFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trailers, container, false);

        Bundle args = getArguments();

        if (args != null) {
            mMovie_id = args.getInt(Movie.COLUMN_NAME_MOVIE_ID);
        }

        mMasterLinearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_trailers);
        mTrailerLinearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_trailers_list);

        return rootView;
    }

    /**
     * Launch trailer in youtube app. Fallback to display in browser if youtube app in unavailable
     *
     * source: http://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
     */
    private void watchYoutubeVideo(String youtube_id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtube_id));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Utility.YOUTUBE_URL + youtube_id));
            startActivity(intent);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_trailer, menu);

        MenuItem item = menu.findItem(R.id.menu_detail_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareTrailer != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }


    /**
     * Create the Intent for the ShareActionProvider
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareTrailer);

        return shareIntent;
    }


    @Override
    public void onResume() {
        super.onResume();

        boolean movie_id_is_valid = (mMovie_id != 0);
        boolean has_internet_connection = Utility.hasNetworkConnection(getActivity());

        if (!mTrailersLoaded && movie_id_is_valid && has_internet_connection) {
            String strMovie_id = Integer.toString(mMovie_id);

            AsyncTask<String, Void, List<Trailer>> trailerTask = new TrailerLoader();
            trailerTask.execute(strMovie_id);
        }
    }


    /**
     * Asyntask to load the trailer list from the web
     */
    protected class TrailerLoader extends AsyncTask<String, Void, List<Trailer>> {

        @Override
        protected List<Trailer> doInBackground(String... params) {
            return loadTrailerList(params[0]);
        }

        private List<Trailer> loadTrailerList(String idMovie) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<Trailer> trailerList = null;

            String movieJsonStr = null;

            String api_key = Utility.MOVIE_API_KEY;
            String sort_by = Utility.getPreferredSortOrder(getActivity());

            try {
                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";

                final String TRAILER_PATH = "videos";

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(idMovie)
                        .appendPath(TRAILER_PATH)
                        .appendQueryParameter(APIKEY_PARAM, api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.d(LOG_TAG, url.toString());

                // Create the request
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                trailerList = getTrailerDataFromJson(movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return trailerList;
        }

        private List<Trailer> getTrailerDataFromJson(String trailerJsonStr) throws JSONException {

            final String MDB_LIST = "results";
            final String MDB_ID = "id";
            final String MDB_KEY = "key";
            final String MDB_NAME = "name";
            final String MDB_SITE = "site";
            final String MDB_TYPE = "type";

            try {
                JSONObject trailerJson = new JSONObject(trailerJsonStr);
                JSONArray trailerArray = trailerJson.getJSONArray(MDB_LIST);

                List<Trailer> trailerList = new ArrayList<>();

                for(int i = 0; i < trailerArray.length(); i++) {
                    JSONObject trailer = trailerArray.getJSONObject(i);

                    String id = trailer.getString(MDB_ID);
                    String key = trailer.getString(MDB_KEY);
                    String name = trailer.getString(MDB_NAME);
                    String site = trailer.getString(MDB_SITE);
                    String type = trailer.getString(MDB_TYPE);

                    Trailer t = new Trailer(id, key, name, site, type);

                    trailerList.add(t);
                }

                return trailerList;

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

                return null;
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(List<Trailer> trailerList) {
            super.onPostExecute(trailerList);

            if (trailerList == null)
                return;

            // display the trailers in linear layout
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            for (Trailer t: trailerList) {
                View trailerView = inflater.inflate(R.layout.trailer_row, mTrailerLinearLayout, false);
                TextView tvTrailer = (TextView)trailerView.findViewById(R.id.trailer_row_textview);

                trailerView.setTag(t.getKey());
                trailerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        watchYoutubeVideo(v.getTag().toString());
                    }
                });
                tvTrailer.setText(t.getName());

                mTrailerLinearLayout.addView(trailerView);
            }

            // display fragment and set share content to the first trailer
            if (!trailerList.isEmpty()) {
                Trailer first = trailerList.get(0);
                mShareTrailer = first.getName() + " "+ Utility.YOUTUBE_URL + first.getKey();

                mMasterLinearLayout.setVisibility(View.VISIBLE);

                // rerender menu with share information
                getActivity().invalidateOptionsMenu();
            }

            mTrailersLoaded = true;
        }
    }
}
