package com.devsaki.myappportafolio;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devsaki.myappportafolio.domain.Movie;
import com.devsaki.myappportafolio.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    public final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM = "item";
    private Movie movieSelected;
    private int id;

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
        if(container==null) {
            return rootView;
        }

        id = getArguments().getInt(ARG_ITEM_ID, -1);
        movieSelected = getArguments().getParcelable(ARG_ITEM);
        if(movieSelected==null){
            new MovieAsyncTask(rootView).execute();
        }else{
            populate(movieSelected, rootView);
        }

        return rootView;
    }

    public void refresh(int id, Movie movie){
        this.id = id;
        this.movieSelected = movie;
        if(movieSelected==null){
            new MovieAsyncTask(getView()).execute();
        }else{
            populate(movieSelected, getView());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_ITEM, movieSelected);
        outState.putInt(ARG_ITEM_ID, id);
    }

    public void markAsFavorite(View view){

    }

    private void populate(Movie movie, View rootView){
        TextView tvMovieTitle = (TextView) rootView.findViewById(R.id.tvMovieTitle);
        TextView tvMovieOverview = (TextView) rootView.findViewById(R.id.tvMovieOverview);
        TextView tvMovieReleaseDate = (TextView) rootView.findViewById(R.id.tvMovieReleaseDate);
        TextView tvMovieRuntime = (TextView) rootView.findViewById(R.id.tvMovieRuntime);
        TextView tvMovieVoteAverage = (TextView) rootView.findViewById(R.id.tvMovieVoteAverage);
        ImageView ivMovie = (ImageView) rootView.findViewById(R.id.ivMovie);

        tvMovieTitle.setText(movie.getOriginalTitle());
        tvMovieOverview.setText(movie.getOverview());

        tvMovieReleaseDate.setText(movie.getReleaseDate().substring(0, 4));
        tvMovieRuntime.setText(String.valueOf(movie.getRuntime()));
        tvMovieVoteAverage.setText(movie.getVoteAverage() + "/10");
        Glide.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + movie.getPosterPath()).fitCenter().into(ivMovie);

        rootView.findViewById(R.id.rlMovieDetail).setVisibility(View.VISIBLE);
    }

    public void refresh(Intent data) {
        refresh(data.getIntExtra(ARG_ITEM_ID, -1), (Movie) data.getParcelableExtra(ARG_ITEM));
    }


    public class MovieAsyncTask extends AsyncTask<Integer, Integer, Movie> {

        private View rootView;

        public MovieAsyncTask(View rootView) {
            this.rootView = rootView;
        }

        @Override
        protected void onPreExecute() {
            ProgressBar pb = (ProgressBar) rootView.findViewById(R.id.pbLoadingDetail);
            pb.setVisibility(View.VISIBLE);

            rootView.findViewById(R.id.rlMovieDetail).setVisibility(View.GONE);
        }

        @Override
        protected Movie doInBackground(Integer... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                String themoviedbapiUrl = "http://api.themoviedb.org/3/movie/" + id;

                Uri builtUri = Uri.parse(themoviedbapiUrl).buildUpon().appendQueryParameter("api_key", getResources().getString(R.string.themoviedb_api_key)).build();

                URL url = new URL(builtUri.toString());
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                String json = buffer.toString();
                JSONObject jsonObject = new JSONObject(json);
                Movie result = new Movie();
                result.setId(id);
                result.setPosterPath(jsonObject.getString("poster_path"));
                result.setOriginalTitle(jsonObject.getString("original_title"));
                result.setVoteAverage(jsonObject.getDouble("vote_average"));
                result.setOverview(jsonObject.getString("overview"));
                result.setRuntime(jsonObject.getInt("runtime"));

                result.setReleaseDate(jsonObject.getString("release_date"));

                return result;
            } catch (Exception e){
                Log.e(LOG_TAG, "Error ", e);
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


            return null;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            ProgressBar pb = (ProgressBar) rootView.findViewById(R.id.pbLoadingDetail);
            pb.setVisibility(View.GONE);

            movieSelected = movie;
            populate(movie, rootView);
        }
    }
}
