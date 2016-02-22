package com.devsaki.myappportafolio;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.devsaki.myappportafolio.domain.Movie;

/**
 * An activity representing a single MovieSyncService detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieListActivity}.
 */
public class MovieDetailActivity extends AppCompatActivity {

    MovieDetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (findViewById(R.id.movie_detail_container) == null) {
            Intent i = new Intent();
            Movie movie = savedInstanceState.getParcelable(MovieDetailFragment.ARG_ITEM);
            int id = savedInstanceState.getInt(MovieDetailFragment.ARG_ITEM_ID);
            i.putExtra(MovieDetailFragment.ARG_ITEM, movie);
            i.putExtra(MovieDetailFragment.ARG_ITEM_ID, id);
            setResult(0, i);
            finish();
            return;
        }

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        Bundle arguments = new Bundle();
        arguments.putInt(MovieDetailFragment.ARG_ITEM_ID,
                getIntent().getIntExtra(MovieDetailFragment.ARG_ITEM_ID, -1));
        fragment = new MovieDetailFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(false);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        fragment.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else {
            if (fragment != null)
                fragment.onOptionsItemSelected(item);
            return true;
        }
    }
}
