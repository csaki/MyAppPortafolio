package com.devsaki.myappportafolio.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by DevSaki on 31/01/2016.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.devsaki.myappportafolio";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_VIDEO = "video";

    public static final class MovieEntry{

        public static final Uri CONTENT_URI_DB =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final Uri CONTENT_URI_DB_FAVORITE =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).appendPath("favorites").build();
        public static final Uri CONTENT_URI_REST =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).appendPath("rest").build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_DATE_UPDATE = "date_update";

        public static final String[] PROJECTION = new String[]{
                COLUMN_MOVIE_ID, COLUMN_POSTER_PATH,
                COLUMN_OVERVIEW, COLUMN_RELEASE_DATE, COLUMN_ORIGINAL_TITLE,
                COLUMN_VOTE_AVERAGE, COLUMN_RUNTIME, COLUMN_FAVORITE, COLUMN_DATE_UPDATE
        };

        public static final int INDEX_MOVIE_ID = 0;
        public static final int INDEX_POSTER_PATH = 1;
        public static final int INDEX_OVERVIEW = 2;
        public static final int INDEX_RELEASE_DATE = 3;
        public static final int INDEX_ORIGINAL_TITLE = 4;
        public static final int INDEX_VOTE_AVERAGE = 5;
        public static final int INDEX_RUNTIME = 6;
        public static final int INDEX_FAVORITE = 7;
        public static final int INDEX_COLUMN_DATE_UPDATE = 8;


        public static Uri buildMovieUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI_DB, id);
        }
    }

    public static final class VideoEntry implements BaseColumns{

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;


        // Table name
        public static final String TABLE_NAME = "video";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_TYPE = "type";

        public static final String[] PROJECTION = new String[]{
                _ID, COLUMN_MOVIE_ID, COLUMN_NAME,
                COLUMN_SITE, COLUMN_KEY, COLUMN_TYPE
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_MOVIE_ID = 1;
        public static final int INDEX_NAME = 2;
        public static final int INDEX_SITE = 3;
        public static final int INDEX_KEY = 4;
        public static final int INDEX_TYPE = 5;

        public static Uri buildVideoByMovieIdUri(long id) {
            return MovieContract.MovieEntry.buildMovieUriWithId(id).buildUpon().appendPath(PATH_VIDEO).build();
        }
    }

    public static final class ReviewEntry implements BaseColumns {

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        // Table name
        public static final String TABLE_NAME = "review";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_AUTHOR = "review_author";
        public static final String COLUMN_CONTENT = "review_content";

        public static final String[] PROJECTION = new String[]{
                _ID, COLUMN_MOVIE_ID, COLUMN_AUTHOR, COLUMN_CONTENT
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_MOVIE_ID = 1;
        public static final int INDEX_AUTHOR = 2;
        public static final int INDEX_CONTENT = 3;

        public static Uri buildReviewByMovieIdUri(long id) {
            return MovieContract.MovieEntry.buildMovieUriWithId(id).buildUpon().appendPath(PATH_REVIEW).build();
        }
    }


}
