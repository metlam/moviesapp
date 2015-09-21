package de.malte.moviesapp.fragment;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.malte.moviesapp.R;
import de.malte.moviesapp.Utility;
import de.malte.moviesapp.data.MovieContract;
import de.malte.moviesapp.entity.Movie;

/**
 * Detail Fragment
 * Display the movie detail information.
 * Contains the trailer and review fragment
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    // displayed movie id
    private int mMovie_id;

    // poster
    private ImageView m_iv_poster;

    // details
    private String mTitle;
    private String mReleaseDate;
    private double mVoteAverage;
    private String mOverview;
    private String mPosterPath;


    public DetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle args = getArguments();

        TextView tv_title = (TextView)rootView.findViewById(R.id.fragment_detail_title);
        m_iv_poster = (ImageView)rootView.findViewById(R.id.fragment_detail_poster);
        TextView tv_release_date = (TextView)rootView.findViewById(R.id.fragment_detail_release_date);
        TextView tv_vote_average = (TextView)rootView.findViewById(R.id.fragment_detail_vote_agerage);
        TextView tv_overview = (TextView)rootView.findViewById(R.id.fragment_detail_overview);
        Button saveButton = (Button)rootView.findViewById(R.id.fragment_detail_button_save_favorite);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFavoriteToDatabase();
            }
        });

        if (args != null) {
            mMovie_id = args.getInt(Movie.COLUMN_NAME_MOVIE_ID);

            mTitle = args.getString(Movie.COLUMN_NAME_TITLE);
            mReleaseDate = args.getString(Movie.COLUMN_NAME_RELEASE_DATE);
            mVoteAverage = args.getDouble(Movie.COLUMN_NAME_VOTE_AVERAGE);
            mOverview = args.getString(Movie.COLUMN_NAME_OVERVIEW);

            mPosterPath = args.getString(Movie.COLUMN_NAME_POSTER_PATH);
            String url = Utility.buildMoviePosterURI(mPosterPath);
            Picasso.with(getActivity()).load(url).into(m_iv_poster);

            tv_title.setText(mTitle);
            tv_release_date.setText(mReleaseDate);
            tv_vote_average.setText(Double.toString(mVoteAverage));
            tv_overview.setText(mOverview);
        }


        Bundle bundle = new Bundle();
        bundle.putInt(Movie.COLUMN_NAME_MOVIE_ID, mMovie_id);

        TrailersFragment tf = new TrailersFragment();
        tf.setArguments(bundle);

        ReviewsFragment rf = new ReviewsFragment();
        rf.setArguments(bundle);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail_trailer_container, tf)
                .replace(R.id.fragment_detail_review_container, rf)
                .commit();

        return rootView;
    }

    /**
     * Save detail information of displayed movie into database
     * @return saved movie id
     */
    public long saveFavoriteToDatabase() {
        long movieId;

        Cursor favoriteCursor = getActivity().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{Integer.toString(mMovie_id)},
                null
        );

        if (favoriteCursor.moveToFirst()) {
            Log.d(LOG_TAG, "movie already in db");

            int movieIdIndex= favoriteCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            movieId = favoriteCursor.getLong(movieIdIndex);
        }
        else {
            Log.d(LOG_TAG, "movie not in db. creating cv");

            ContentValues cvMovie = new ContentValues();

            cvMovie.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie_id);
            cvMovie.put(MovieContract.MovieEntry.COLUMN_TITLE, mTitle);
            cvMovie.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mReleaseDate);
            cvMovie.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mOverview);
            cvMovie.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, mVoteAverage);
            cvMovie.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, mPosterPath);

            // convert image to bytearray and store as blob in database
            BitmapDrawable image = (BitmapDrawable) m_iv_poster.getDrawable();
            Bitmap bitmap = image.getBitmap();
            byte[] array = Utility.getBitmapAsByteArray(bitmap);

            cvMovie.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, array);

            Uri insertUri = getActivity().getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI, cvMovie);

            movieId = ContentUris.parseId(insertUri);
        }

        favoriteCursor.close();

        return movieId;
    }

}