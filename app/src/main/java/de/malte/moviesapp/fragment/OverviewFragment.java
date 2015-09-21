package de.malte.moviesapp.fragment;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
import de.malte.moviesapp.data.FavoriteMovieAdapter;
import de.malte.moviesapp.data.MovieAdapter;
import de.malte.moviesapp.data.MovieContract;
import de.malte.moviesapp.entity.Movie;


/**
 * Overview Fragment
 * Display a grid of movie posters
 */
public class OverviewFragment extends Fragment {
    private static final String LOG_TAG = OverviewFragment.class.getSimpleName();

    private GridView mGridView;
    private CursorAdapter mAdapter;

    // display details of first item automatically in tablet mode
    private boolean autoClickFirst = false;

    // is this the first start of this fragment?
    private boolean mFirstStart = true;

    public interface Callback {
        // DetailFragmentCallback for when an item has been selected.
        void onItemSelected(Bundle extras);
    }

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    public OverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        mGridView = (GridView)rootView.findViewById(R.id.grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor c = (Cursor) parent.getItemAtPosition(position);
                if (c != null) {
                    Bundle extras = new Bundle();
                    extras.putInt(Movie.COLUMN_NAME_MOVIE_ID, c.getInt(Movie.COLUMN_MOVIE_ID));
                    extras.putString(Movie.COLUMN_NAME_TITLE, c.getString(Movie.COLUMN_TITLE));
                    extras.putString(Movie.COLUMN_NAME_RELEASE_DATE, c.getString(Movie.COLUMN_RELEASE_DATE));
                    extras.putString(Movie.COLUMN_NAME_POSTER_PATH, c.getString(Movie.COLUMN_POSTER_PATH));
                    extras.putDouble(Movie.COLUMN_NAME_VOTE_AVERAGE, c.getDouble(Movie.COLUMN_VOTE_AVERAGE));
                    extras.putString(Movie.COLUMN_NAME_OVERVIEW, c.getString(Movie.COLUMN_OVERVIEW));

                    // let mainactivity handle this
                    ((Callback) getActivity()).onItemSelected(extras);
                }
            }
        });

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (mFirstStart) {
            refreshMovies(); // otherwise bug: refresh gets calles twice
            mFirstStart = false;
        }
    }

    /**
     * Load movies list (again)
     */
    private void refreshMovies() {
        String sort_by = Utility.getPreferredSortOrder(getActivity());


        if (sort_by.equals("favorite")) {
            AsyncTask<Void, Void, Cursor> task = new FavoriteMovieLoader();
            task.execute();
        }
        else {
            // do not try to refresh when not connected to the internet
            if (!Utility.hasNetworkConnection(getActivity()))
                return;

            AsyncTask<Void, Void, List<Movie>> task = new MovieLoader();
            task.execute();
        }
    }

    /**
     * Refresh movie list if sort order has changed
     */
    public void onSortOrderChanged() {
        if (mAdapter != null)
            mAdapter.swapCursor(null);

        refreshMovies();
    }

    /**
     * Select First Element in Gridview automatically. Useful in Tablet-Mode
     */
    public void setAutoClickFirst(boolean auto) {
        autoClickFirst = auto;
    }

    /**
     * AsynTask to load the movies list from the web api
     */
    protected class MovieLoader extends AsyncTask<Void, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(Void... params) {
            return loadMovieList();
        }

        private List<Movie> loadMovieList() {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<Movie> movieList = null;

            String movieJsonStr = null;

            String api_key = Utility.MOVIE_API_KEY;
            String sort_by = Utility.getPreferredSortOrder(getActivity());

            try {
                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String APIKEY_PARAM = "api_key";

                // get some nice voted movies
                final String VOTE_COUNT_PARAM = "vote_count.gte";
                final String MINIMUM_VOTE_COUNT = "100";

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sort_by)
                        .appendQueryParameter(APIKEY_PARAM, api_key)
                        .appendQueryParameter(VOTE_COUNT_PARAM, MINIMUM_VOTE_COUNT)
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
                movieList = getMovieDataFromJson(movieJsonStr);
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

            return movieList;
        }

        private List<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException {

            // fields in json
            final String MDB_LIST = "results";
            final String MDB_ADULT = "adult";
            final String MDB_BACKDROP_PATH = "backdrop_path";
            final String MDB_ID = "id";
            final String MDB_ORIGINAL_LANGUAGEE = "original_language";
            final String MDB_ORIGINAL_TITLE = "original_title";
            final String MDB_OVERVIEW = "overview";
            final String MDB_RELEASE_DATE = "release_date";
            final String MDB_POSTER_PATH = "poster_path";
            final String MDB_POPULARITY = "popularity";
            final String MDB_TITLE = "title";
            final String MDB_VIDEO = "video";
            final String MDB_VOTE_AVERAGE = "vote_average";
            final String MDB_VOTE_COUNT = "vote_count";

            try {
                JSONObject movieJson = new JSONObject(movieJsonStr);
                JSONArray movieArray = movieJson.getJSONArray(MDB_LIST);

                List<Movie> movieList = new ArrayList<>();

                for(int i = 0; i < movieArray.length(); i++) {
                    JSONObject movie = movieArray.getJSONObject(i);

                    boolean adult = adult = movie.getBoolean(MDB_ADULT);
                    String backdrop_path = movie.getString(MDB_BACKDROP_PATH);
                    int id = movie.getInt(MDB_ID);
                    String original_language = movie.getString(MDB_ORIGINAL_LANGUAGEE);
                    String original_title = movie.getString(MDB_ORIGINAL_TITLE);
                    String overview = movie.getString(MDB_OVERVIEW);
                    String release_date = movie.getString(MDB_RELEASE_DATE);
                    String poster_path = movie.getString(MDB_POSTER_PATH);
                    double popularity = movie.getDouble(MDB_POPULARITY);
                    String title = movie.getString(MDB_TITLE);
                    boolean video = movie.getBoolean(MDB_VIDEO);
                    double vote_average = movie.getDouble(MDB_VOTE_AVERAGE);
                    int vote_count = movie.getInt(MDB_VOTE_COUNT);

                    Movie m = new Movie(id, adult, backdrop_path, original_language,
                            original_title, overview, release_date, poster_path, popularity,
                            title, video, vote_average, vote_count);

                    movieList.add(m);
                }

                return movieList;

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

                return null;
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(List<Movie> movieList) {
            super.onPostExecute(movieList);

            if (movieList == null)
                return;

            // convert list of Movies to Cursor
            String columns[] = MovieContract.MovieEntry.MOVIE_COLUMS;

            MatrixCursor matrixCursor= new MatrixCursor(columns);
            getActivity().startManagingCursor(matrixCursor);

            int i = 0;
            for (Movie m: movieList) {
                i++;
                matrixCursor.addRow(new Object[]{i, m.getId(), m.getTitle(), m.getRelease_date(), m.getPoster_path(), "", m.getVote_average(), m.getOverview()});
            }

            // set the adapter to display the movies
            mAdapter = new MovieAdapter(getActivity(), null, 0);
            mGridView.setAdapter(mAdapter);
            mAdapter.swapCursor(matrixCursor);
            mAdapter.notifyDataSetChanged();

            if (mGridView.getCount() > 0 && autoClickFirst) {
                final int first = 0;
                mGridView.performItemClick(mAdapter.getView(first, null, null), first, mAdapter.getItemId(first));
            }
        }
    }


    /**
     * AsynTask to load the favorite movies list from the internal database
     */
    protected class FavoriteMovieLoader extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            return getActivity().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.MOVIE_COLUMS,
                    null,
                    null,
                    null
            );
        }


        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(Cursor movies) {
            super.onPostExecute(movies);

            mAdapter = new FavoriteMovieAdapter(getActivity(), null, 0);
            mGridView.setAdapter(mAdapter);
            mAdapter.swapCursor(movies);

            if (mGridView.getCount() > 0 && autoClickFirst) {
                final int first = 0;
                mGridView.performItemClick(mAdapter.getView(first, null, null), first, mAdapter.getItemId(first));
            }
        }
    }

}
