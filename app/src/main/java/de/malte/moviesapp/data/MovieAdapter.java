package de.malte.moviesapp.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import de.malte.moviesapp.R;
import de.malte.moviesapp.Utility;
import de.malte.moviesapp.entity.Movie;

/**
 * Movie Adapter
 * Fill the grid with movie-posters
 */
public class MovieAdapter extends CursorAdapter {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.grid_cell_imageview);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MovieAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = R.layout.grid_cell;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String part = cursor.getString(Movie.COLUMN_POSTER_PATH);
        String url = Utility.buildMoviePosterURI(part);
        ImageView iv = viewHolder.imageView;

        Picasso.with(context).load(url).into(iv);
    }
}
