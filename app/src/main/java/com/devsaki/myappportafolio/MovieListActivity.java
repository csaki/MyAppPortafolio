package com.devsaki.myappportafolio;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.bumptech.glide.Glide;
import com.devsaki.myappportafolio.data.MovieContract;
import com.devsaki.myappportafolio.data.MovieRestHelper;
import com.devsaki.myappportafolio.domain.Movie;
import com.devsaki.myappportafolio.sync.MovieSyncAdapter;
import com.devsaki.myappportafolio.util.ConstantPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MovieListActivity.class.getSimpleName();
    private static final int MOVIES = 0;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private int imageWidth, imageHeight;

    private RecyclerView movieList;

    private boolean loading;

    private int currentPage;

    private MovieItemRecyclerViewAdapter movieItemRecyclerViewAdapter;

    private ArrayList<Movie> movies;

    private int currentSort;

    private SharedPreferences preferences;

    private MovieDetailFragment fragment;

    private int qtyMoviesByPage;

    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        movieList = (RecyclerView) findViewById(R.id.movie_list);
        pb = (ProgressBar) findViewById(R.id.pbLoadingList);

        imageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 148,
                this.getResources().getDisplayMetrics());

        int spanCount = 1;

        int widthSize = getResources().getDisplayMetrics().widthPixels;
        int heightSize = getResources().getDisplayMetrics().heightPixels;

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            widthSize = widthSize / 2;
        }

        if (imageWidth >= widthSize) {
            imageWidth = widthSize;
        } else {
            spanCount = widthSize / imageWidth;
            int rest = widthSize % imageWidth;
            imageWidth = imageWidth + rest / spanCount;
        }

        imageHeight = imageWidth * 277 / 185;

        qtyMoviesByPage = heightSize / imageHeight * widthSize / imageWidth;

        final GridLayoutManager gridLayout = new GridLayoutManager(this, spanCount);
        movieList.setLayoutManager(gridLayout);


        movieList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (!loading) {
                        int visibleItemCount = gridLayout.getChildCount();
                        int totalItemCount = gridLayout.getItemCount();
                        int pastVisiblesItems = gridLayout.findFirstVisibleItemPosition();
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            Log.i(LOG_TAG, "list end");
                            syncImmediately(false);
                        }
                    }
                }
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState == null) {
            currentPage = 1;
            currentSort = preferences.getInt(ConstantPreferences.MOVIE_SORT, ConstantPreferences.MOVIE_SORT_POPULAR);
            movies = new ArrayList<>();
            movieItemRecyclerViewAdapter = new MovieItemRecyclerViewAdapter(movies);
            movieList.setAdapter(movieItemRecyclerViewAdapter);
            syncImmediately(true);
            getLoaderManager().initLoader(MOVIES, null, this);
        } else {
            currentPage = savedInstanceState.getInt("current_page");
            currentSort = savedInstanceState.getInt("current_sort");
            setTitle(getResources().getString(currentSort == ConstantPreferences.MOVIE_SORT_POPULAR ? R.string.most_popular : R.string.highest_rating));

            movies = savedInstanceState.getParcelableArrayList("movies");
            movieItemRecyclerViewAdapter = new MovieItemRecyclerViewAdapter(movies);
            movieList.setAdapter(movieItemRecyclerViewAdapter);

            if (mTwoPane && savedInstanceState.containsKey(MovieDetailFragment.ARG_ITEM_ID)) {
                fragment = new MovieDetailFragment();
                fragment.setArguments(savedInstanceState);
                fragment.setRetainInstance(false);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, fragment)
                        .commit();
            }

            if (movies.size() < qtyMoviesByPage) {
                syncImmediately(true);
                getLoaderManager().initLoader(MOVIES, null, this);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("current_page", currentPage);
        outState.putInt("current_sort", currentSort);
        outState.putParcelableArrayList("movies", movies);
        if (fragment != null) {
            fragment.onSaveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.most_popular) {
            if (currentSort == ConstantPreferences.MOVIE_SORT_POPULAR) {
                return true;
            }
            currentSort = ConstantPreferences.MOVIE_SORT_POPULAR;
        } else if (id == R.id.highest_rated) {
            if (currentSort == ConstantPreferences.MOVIE_SORT_RATING) {
                return true;
            }
            currentSort = ConstantPreferences.MOVIE_SORT_RATING;
        } else if (id == R.id.my_favorites) {
            if (currentSort == ConstantPreferences.MOVIE_SORT_MY_FAVORITES) {
                return true;
            }
            currentSort = ConstantPreferences.MOVIE_SORT_MY_FAVORITES;
        } else {
            if (fragment != null)
                fragment.onOptionsItemSelected(item);
            return true;
        }
        preferences.edit().putInt(ConstantPreferences.MOVIE_SORT, currentSort).apply();

        movies.clear();
        movieItemRecyclerViewAdapter.notifyDataSetChanged();

        currentPage = 1;

        syncImmediately(true);
        getLoaderManager().restartLoader(MOVIES, null, this);
        return true;
    }

    private void syncImmediately(boolean calculateQtyPages) {
        MovieRestHelper.setMovies(null);
        int calculatedPages = calculateQtyPages ? (int) Math.ceil((qtyMoviesByPage - movies.size()) / 20.0) : 1;
        MovieSyncAdapter.syncImmediately(this, currentSort, calculatedPages, currentPage, 0);
        loading = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == 0) {
            if (mTwoPane) {
                if (fragment == null) {
                    fragment = new MovieDetailFragment();
                    fragment.setRetainInstance(false);
                    fragment.setArguments(data.getExtras());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String[] projection = MovieContract.MovieEntry.PROJECTION;
        switch (id) {
            case MOVIES:
                pb.setVisibility(View.VISIBLE);
                if (currentSort == ConstantPreferences.MOVIE_SORT_MY_FAVORITES) {
                    uri = MovieContract.MovieEntry.CONTENT_URI_DB_FAVORITE;
                } else {
                    uri = MovieContract.MovieEntry.CONTENT_URI_REST;
                }

        }
        return new CursorLoader(this, uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        int id = loader.getId();
        pb.setVisibility(View.INVISIBLE);
        setTitle(getResources().getString(currentSort == ConstantPreferences.MOVIE_SORT_POPULAR ? R.string.most_popular : currentSort == ConstantPreferences.MOVIE_SORT_RATING ? R.string.highest_rating : R.string.my_favorites));
        if (data.getCount() == 0) {
            return;
        }
        Log.d(LOG_TAG, "page : " + currentPage);
        currentPage += (int) Math.ceil(data.getCount() / 20.0);
        loading = false;
        switch (id) {
            case MOVIES:
                ArrayList<Movie> movies = MovieRestHelper.moviesFromCursor(data);
                this.movies.addAll(movies);
                movieItemRecyclerViewAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class MovieItemRecyclerViewAdapter
            extends RecyclerView.Adapter<MovieItemRecyclerViewAdapter.ViewHolder> {

        private final List<Movie> mValues;

        public MovieItemRecyclerViewAdapter(List<Movie> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Movie movie = mValues.get(position);
            holder.ivMovie.getLayoutParams().width = imageWidth;
            holder.ivMovie.getLayoutParams().height = imageHeight;
            Glide.with(MovieListActivity.this).load("http://image.tmdb.org/t/p/w185" + movie.getPosterPath()).override(imageWidth, imageHeight).fitCenter().into(holder.ivMovie);
            //Picasso.with(MovieListActivity.this).load("http://image.tmdb.org/t/p/w185" + movie.getPosterPath()).resize(imageWidth, imageHeight).into(holder.ivMovie);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        if (fragment == null) {
                            Bundle arguments = new Bundle();
                            arguments.putInt(MovieDetailFragment.ARG_ITEM_ID, movie.getId());
                            arguments.putParcelable(MovieDetailFragment.ARG_ITEM, null);
                            fragment = new MovieDetailFragment();
                            fragment.setArguments(arguments);
                            fragment.setRetainInstance(false);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.movie_detail_container, fragment)
                                    .commit();
                        } else {
                            fragment.refresh(movie.getId());
                        }
                    } else {
                        Intent intent = new Intent(MovieListActivity.this, MovieDetailActivity.class);
                        intent.putExtra(MovieDetailFragment.ARG_ITEM_ID, movie.getId());
                        MovieListActivity.this.startActivityForResult(intent, 0);
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView ivMovie;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                ivMovie = (ImageView) view.findViewById(R.id.ivMovie);
            }
        }
    }
}
