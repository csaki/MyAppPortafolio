package com.devsaki.myappportafolio.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.devsaki.myappportafolio.domain.Movie;
import com.devsaki.myappportafolio.domain.Review;
import com.devsaki.myappportafolio.domain.Video;

import java.util.ArrayList;

/**
 * Created by DevSaki on 08/02/2016.
 */
public class MovieRestHelper {

    private static ArrayList<Movie> movies;
    public static final String LOG_TAG = MovieRestHelper.class.getSimpleName();

    private MovieRestHelper(){}

    public static void setMovies(ContentValues[] contentValues) {
        if(contentValues==null){
            movies = null;
            return;
        }
        movies = new ArrayList<>(contentValues.length);
        for (ContentValues contentValue : contentValues) {
            Movie movie = new Movie();
            movie.setId(contentValue.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
            movie.setRuntime(ifNull(contentValue.getAsInteger(MovieContract.MovieEntry.COLUMN_RUNTIME), 0));
            movie.setVoteAverage(ifNull(contentValue.getAsDouble(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE), 0));
            movie.setOverview(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_OVERVIEW));
            movie.setOriginalTitle(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE));
            movie.setPosterPath(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
            movie.setReleaseDate(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
            movies.add(movie);
        }
    }

    public static ContentValues[] fromMovies(ArrayList<Movie> movies){
        ContentValues[] contentValues = new ContentValues[movies.size()];
        for (int i = 0; i < movies.size(); i++) {
            contentValues[i] = fromMovie(movies.get(i));
        }
        return contentValues;
    }

    public static ContentValues[] fromVideos(Movie movie){
        ContentValues[] contentValues = new ContentValues[movie.getVideos().size()];
        for (int i = 0; i < movie.getVideos().size(); i++) {
            Video video = movie.getVideos().get(i);
            contentValues[i] = new ContentValues();
            contentValues[i].put(MovieContract.VideoEntry.COLUMN_TYPE, video.getType());
            contentValues[i].put(MovieContract.VideoEntry.COLUMN_KEY, video.getKey());
            contentValues[i].put(MovieContract.VideoEntry.COLUMN_NAME, video.getName());
            contentValues[i].put(MovieContract.VideoEntry.COLUMN_SITE, video.getSite());
            contentValues[i].put(MovieContract.VideoEntry.COLUMN_MOVIE_ID, movie.getId());
        }
        return contentValues;
    }

    public static ContentValues[] fromReviews(Movie movie){
        ContentValues[] contentValues = new ContentValues[movie.getReviews().size()];
        for (int i = 0; i < movie.getReviews().size(); i++) {
            Review review = movie.getReviews().get(i);
            contentValues[i] = new ContentValues();
            contentValues[i].put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
            contentValues[i].put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());
            contentValues[i].put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movie.getId());
        }
        return contentValues;
    }

    public static ContentValues fromMovie(Movie movie){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
        contentValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        contentValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, movie.getRuntime());
        contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        contentValues.put(MovieContract.MovieEntry.COLUMN_DATE_UPDATE, movie.getDateUpdate());
        contentValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, movie.isFavorite()?1:0);
        return contentValues;
    }

    public static int ifNull(Integer i, int value){
        if(i == null)
            return value;

        return i;
    }

    public static double ifNull(Double i, double value){
        if(i == null)
            return value;

        return i;
    }

    public static Cursor getMovies() {
        MatrixCursor mc = getMatrixCursorMovie();
        if (movies != null) {
            for (Movie movie : movies) {
                addMovie(mc, movie);
            }
        }
        return mc;
    }

    @NonNull
    private static MatrixCursor getMatrixCursorMovie() {
        return new MatrixCursor(MovieContract.MovieEntry.PROJECTION);
    }


    private static void addMovie(MatrixCursor mc, Movie movie) {
        mc.addRow(new Object[]{
                movie.getId(),
                movie.getPosterPath(),
                movie.getOverview(),
                movie.getReleaseDate(),
                movie.getOriginalTitle(),
                movie.getVoteAverage(),
                movie.getRuntime(),
                movie.isFavorite()?1:0,
                movie.getDateUpdate()
        });
    }

    public static ArrayList<Movie> moviesFromCursor(Cursor cursor){
        ArrayList<Movie> movies = new ArrayList<>(cursor.getCount());
        if(cursor.moveToFirst()){
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(MovieContract.MovieEntry.INDEX_MOVIE_ID));
                movie.setRuntime(cursor.getInt(MovieContract.MovieEntry.INDEX_RUNTIME));
                movie.setVoteAverage(cursor.getDouble(MovieContract.MovieEntry.INDEX_VOTE_AVERAGE));
                movie.setOverview(cursor.getString(MovieContract.MovieEntry.INDEX_OVERVIEW));
                movie.setPosterPath(cursor.getString(MovieContract.MovieEntry.INDEX_POSTER_PATH));
                movie.setOriginalTitle(cursor.getString(MovieContract.MovieEntry.INDEX_ORIGINAL_TITLE));
                movie.setFavorite(cursor.getInt(MovieContract.MovieEntry.INDEX_FAVORITE)==1);
                movie.setReleaseDate(cursor.getString(MovieContract.MovieEntry.INDEX_RELEASE_DATE));

                movies.add(movie);
            } while (cursor.moveToNext());
        }
        MovieRestHelper.movies = null;
        return movies;
    }

    public static ArrayList<Video> videosFromCursor(Cursor cursor){
        ArrayList<Video> videos = new ArrayList<>(cursor.getCount());
        if(cursor.moveToFirst()){
            do {
                Video video = new Video();
                video.setKey(cursor.getString(MovieContract.VideoEntry.INDEX_KEY));
                video.setName(cursor.getString(MovieContract.VideoEntry.INDEX_NAME));
                video.setSite(cursor.getString(MovieContract.VideoEntry.INDEX_SITE));
                video.setType(cursor.getString(MovieContract.VideoEntry.INDEX_TYPE));
                videos.add(video);
            } while (cursor.moveToNext());
        }
        return videos;
    }


    public static ArrayList<Review> reviewsFromCursor(Cursor cursor){
        ArrayList<Review> reviews = new ArrayList<>(cursor.getCount());
        if(cursor.moveToFirst()){
            do {
                Review review = new Review();
                review.setAuthor(cursor.getString(MovieContract.ReviewEntry.INDEX_AUTHOR));
                review.setContent(cursor.getString(MovieContract.ReviewEntry.INDEX_CONTENT));
                reviews.add(review);
            } while (cursor.moveToNext());
        }
        return reviews;
    }


}
