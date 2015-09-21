package de.malte.moviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.malte.moviesapp.fragment.DetailFragment;
import de.malte.moviesapp.fragment.OverviewFragment;

/**
 * Main Activity
 * First Activity to get started
 */
public class MainActivity extends AppCompatActivity implements OverviewFragment.Callback {

    private static final String LOG_TAG = OverviewFragment.class.getSimpleName();

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String mSortOrder;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSortOrder = Utility.getPreferredSortOrder(this);
        setContentView(R.layout.activity_main);

        // if movie_detail_container is found, it must be displayed on a tablet
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
            else {
                mTwoPane = false;
                getSupportActionBar().setElevation(0f);
            }
        }

        OverviewFragment of = (OverviewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_overview);
        of.setAutoClickFirst(mTwoPane);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String sort = Utility.getPreferredSortOrder(this);

        if (sort != null && !sort.equals(mSortOrder)) {
            OverviewFragment of = (OverviewFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_overview);
            if (of != null)
                of.onSortOrderChanged();

            mSortOrder = sort;
        }


    }

    @Override
    public void onItemSelected(Bundle extras) {
        if (mTwoPane) {
            DetailFragment df = new DetailFragment();
            df.setArguments(extras);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }
}
