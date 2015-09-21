package de.malte.moviesapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.malte.moviesapp.fragment.DetailFragment;

/**
 * Detail Activity
 * Start Detail Fragment
 */
public class DetailActivity extends AppCompatActivity  {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            DetailFragment df = new DetailFragment();
            df.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, df)
                    .commit();
        }
    }
}
