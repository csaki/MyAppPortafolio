package com.devsaki.myappportafolio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.devsaki.myappportafolio.data.MovieContract;
import com.devsaki.myappportafolio.data.MovieProvider;
import com.devsaki.myappportafolio.data.MovieRestHelper;
import com.devsaki.myappportafolio.domain.Movie;
import com.devsaki.myappportafolio.domain.Review;
import com.devsaki.myappportafolio.domain.Video;
import com.devsaki.myappportafolio.sync.MovieSyncAdapter;
import com.devsaki.myappportafolio.util.AndroidUtil;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single MovieSyncService detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM = "item";
    private Movie movieSelected;
    private int id;
    private TextView tvMovieTitle;
    private TextView tvMovieOverview;
    private TextView tvMovieReleaseDate;
    private TextView tvMovieRuntime;
    private TextView tvMovieVoteAverage;
    private Button btnMarkAsFavorite;
    private ImageView ivMovie;
    private RecyclerView reviewList;
    private RecyclerView videosList;
    private View rlMovieDetail;
    private ProgressBar pb;
    private SharedPreferences preferences;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        if (container == null) {
            return rootView;
        }

        tvMovieTitle = (TextView) rootView.findViewById(R.id.tvMovieTitle);
        tvMovieOverview = (TextView) rootView.findViewById(R.id.tvMovieOverview);
        tvMovieReleaseDate = (TextView) rootView.findViewById(R.id.tvMovieReleaseDate);
        tvMovieRuntime = (TextView) rootView.findViewById(R.id.tvMovieRuntime);
        tvMovieVoteAverage = (TextView) rootView.findViewById(R.id.tvMovieVoteAverage);
        btnMarkAsFavorite = (Button) rootView.findViewById(R.id.btnMarkAsFavorite);
        btnMarkAsFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieSelected.setFavorite(!movieSelected.isFavorite());
                getContext().getContentResolver().update(MovieContract.MovieEntry.buildMovieUriWithId(movieSelected.getId()), MovieRestHelper.fromMovie(movieSelected), null, null);
            }
        });

        ivMovie = (ImageView) rootView.findViewById(R.id.ivMovie);

        reviewList = (RecyclerView) rootView.findViewById(R.id.reviews_list);
        reviewList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        reviewList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        videosList = (RecyclerView) rootView.findViewById(R.id.videos_list);
        videosList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        videosList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        rlMovieDetail = rootView.findViewById(R.id.rlMovieDetail);
        pb = (ProgressBar) rootView.findViewById(R.id.pbLoadingDetail);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        id = getArguments().getInt(ARG_ITEM_ID, -1);
        movieSelected = getArguments().getParcelable(ARG_ITEM);
        if (movieSelected == null) {
            MovieSyncAdapter.syncImmediately(getActivity(), 0, 0, 0, id);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_ITEM, movieSelected);
        outState.putInt(ARG_ITEM_ID, id);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (tvMovieTitle == null) {
            return;
        }
        if (movieSelected == null) {
            getLoaderManager().initLoader(MovieProvider.DB_MOVIE_WITH_ID, null, this);
        } else {
            populate(movieSelected);
            populateVideos(movieSelected.getVideos());
            populateReviews(movieSelected.getReviews());
        }
    }

    private void populate(final Movie movie) {
        movieSelected = movie;
        tvMovieTitle.setText(movie.getOriginalTitle());
        tvMovieOverview.setText(movie.getOverview());

        tvMovieReleaseDate.setText(movie.getReleaseDate().substring(0, 4));
        tvMovieRuntime.setText(String.valueOf(movie.getRuntime()));
        tvMovieVoteAverage.setText(movie.getVoteAverage() + "/10");
        if(movie.isFavorite()){
            btnMarkAsFavorite.setText(R.string.unmark_as_favorite);
        }else{
            btnMarkAsFavorite.setText(R.string.mark_as_favorite);
        }

        Glide.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + movie.getPosterPath()).fitCenter().into(ivMovie);

        rlMovieDetail.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String[] projection = null;
        switch (id) {
            case MovieProvider.DB_MOVIE_WITH_ID:
                uri = MovieContract.MovieEntry.buildMovieUriWithId(this.id);
                projection = MovieContract.MovieEntry.PROJECTION;
                pb.setVisibility(View.VISIBLE);
                break;
            case MovieProvider.DB_MOVIE_REVIEWS:
                uri = MovieContract.ReviewEntry.buildReviewByMovieIdUri(this.id);
                projection = MovieContract.ReviewEntry.PROJECTION;
                break;
            case MovieProvider.DB_MOVIE_VIDEOS:
                uri = MovieContract.VideoEntry.buildVideoByMovieIdUri(this.id);
                projection = MovieContract.VideoEntry.PROJECTION;
                break;

        }
        return new CursorLoader(getActivity(), uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (data.getCount() == 0) {
            return;
        }
        switch (id) {
            case MovieProvider.DB_MOVIE_WITH_ID:
                ArrayList<Movie> movies = MovieRestHelper.moviesFromCursor(data);

                if (movies.size() != 0 && AndroidUtil.beforeOneDay(movies.get(0).getDateUpdate())) {
                    populate(movies.get(0));
                    pb.setVisibility(View.GONE);
                    getLoaderManager().initLoader(MovieProvider.DB_MOVIE_REVIEWS, null, this);
                    getLoaderManager().initLoader(MovieProvider.DB_MOVIE_VIDEOS, null, this);
                }
                break;
            case MovieProvider.DB_MOVIE_REVIEWS:
                populateReviews(MovieRestHelper.reviewsFromCursor(data));
                break;
            case MovieProvider.DB_MOVIE_VIDEOS:
                populateVideos(MovieRestHelper.videosFromCursor(data));
                break;
        }
    }

    private void populateVideos(final ArrayList<Video> videos) {
        movieSelected.setVideos(videos);
        videosList.setAdapter(new MovieListAdapter(videos));
    }

    private void populateReviews(ArrayList<Review> reviews) {
        movieSelected.setReviews(reviews);
        reviewList.setAdapter(new ReviewListAdapter(reviews));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void refresh(int id) {
        this.id = id;
        MovieSyncAdapter.syncImmediately(getActivity(), 0, 0, 0, id);
        getLoaderManager().restartLoader(MovieProvider.DB_MOVIE_WITH_ID, null, this);
        getLoaderManager().restartLoader(MovieProvider.DB_MOVIE_REVIEWS, null, this);
        getLoaderManager().restartLoader(MovieProvider.DB_MOVIE_VIDEOS, null, this);
    }

    public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {

        private final List<Video> videos;

        public MovieListAdapter(List<Video> objects) {
            videos = objects;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.video_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Video video = videos.get(position);
            holder.textView.setText(video.getName());
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + video.getKey())));
                }
            });

        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView textView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                textView = (TextView) mView.findViewById(R.id.tvVideoName);
            }
        }
    }

    public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ViewHolder> {

        private final List<Review> reviews;

        public ReviewListAdapter(List<Review> objects) {
            reviews = objects;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Review review = reviews.get(position);
            holder.tvAuthor.setText(review.getAuthor());
            holder.tvReview.setText(review.getContent());
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView tvAuthor;
            public final TextView tvReview;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                tvAuthor = (TextView) mView.findViewById(R.id.tvAuthor);
                tvReview = (TextView) mView.findViewById(R.id.tvReview);
            }
        }
    }
}
