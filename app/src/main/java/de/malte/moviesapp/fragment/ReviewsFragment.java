package de.malte.moviesapp.fragment;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import de.malte.moviesapp.entity.Review;

/**
 * Reviews Fragment
 * Display movie reviews
 */
public class ReviewsFragment extends Fragment {

    private static final String LOG_TAG = OverviewFragment.class.getSimpleName();

    private int mMovie_id;

    // have the reviews been loaded
    private boolean mReviewsLoaded;

    // the main layout for reviews (whole fragment)
    private LinearLayout mMasterLinearLayout;

    // the list of reviews
    private LinearLayout mReviewLinearLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        mReviewsLoaded = false;

        Bundle args = getArguments();

        if (args != null) {
            mMovie_id = args.getInt(Movie.COLUMN_NAME_MOVIE_ID);
        }

        mMasterLinearLayout = (LinearLayout)rootView.findViewById(R.id.fragment_reviews);
        mReviewLinearLayout = (LinearLayout)rootView.findViewById(R.id.fragment_reviews_list);

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();

        boolean movie_id_is_valid = (mMovie_id != 0);
        boolean has_internet_connection = Utility.hasNetworkConnection(getActivity());

        if (!mReviewsLoaded && movie_id_is_valid && has_internet_connection) {
            String strMovie_id = Integer.toString(mMovie_id);

            AsyncTask<String, Void, List<Review>> trailerTask = new ReviewLoader();
            trailerTask.execute(strMovie_id);
        }
    }


    /**
     * Asyntask to load the reviews from the web
     */
    protected class ReviewLoader extends AsyncTask<String, Void, List<Review>> {

        @Override
        protected List<Review> doInBackground(String... params) {
            return loadReviewList(params[0]);
        }

        private List<Review> loadReviewList(String idMovie) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<Review> reviewList = null;

            String reviewJsonStr = null;

            String api_key = Utility.MOVIE_API_KEY;
            String sort_by = Utility.getPreferredSortOrder(getActivity());

            try {
                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";

                final String REVIEW_PATH = "reviews";

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(idMovie)
                        .appendPath(REVIEW_PATH)
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
                reviewJsonStr = buffer.toString();
                reviewList = getReviewDataFromJson(reviewJsonStr);
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
            return reviewList;
        }

        private List<Review> getReviewDataFromJson(String reviewJsonStr) throws JSONException {

            final String MDB_LIST = "results";
            final String MDB_ID = "id";
            final String MDB_AUTHOR = "author";
            final String MDB_CONTENT = "content";
            final String MDB_URL = "url";

            try {
                JSONObject trailerJson = new JSONObject(reviewJsonStr);
                JSONArray trailerArray = trailerJson.getJSONArray(MDB_LIST);

                List<Review> reviewList = new ArrayList<>();

                for(int i = 0; i < trailerArray.length(); i++) {
                    JSONObject trailer = trailerArray.getJSONObject(i);

                    String id = trailer.getString(MDB_ID);
                    String author = trailer.getString(MDB_AUTHOR);
                    String content = trailer.getString(MDB_CONTENT);
                    String url = trailer.getString(MDB_URL);

                    Review r = new Review(id, author, content, url);

                    reviewList.add(r);
                }

                return reviewList;

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

                return null;
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(List<Review> reviewList) {
            super.onPostExecute(reviewList);

            if (reviewList == null)
                return;

            // add reviews to linear layout
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (Review r: reviewList) {
                View reviewView = inflater.inflate(R.layout.review_row, mReviewLinearLayout, false);
                TextView tvAuthor = (TextView)reviewView.findViewById(R.id.review_row_author);
                TextView tvContent = (TextView)reviewView.findViewById(R.id.review_row_content);

                tvAuthor.setText(r.getAuthor());
                tvContent.setText(r.getContent());

                mReviewLinearLayout.addView(reviewView);
            }

            // display fragment
            if (!reviewList.isEmpty()) {
                mMasterLinearLayout.setVisibility(View.VISIBLE);
            }

            mReviewsLoaded = true;
        }
    }
}
