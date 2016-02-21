package com.devsaki.myappportafolio.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by DevSaki on 31/01/2016.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;
    public static final int DB_FAVORITE_MOVIES = 100;
    public static final int DB_MOVIE_WITH_ID = 101;
    public static final int DB_MOVIE_VIDEOS = 102;
    public static final int DB_MOVIE_REVIEWS = 103;
    public static final int REST_MOVIES = 104;

    private static final String sMovieIdSelection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sMovieSortOrder =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " ASC ";
    private static final String sMovieFavoriteSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_FAVORITE + " = 1 ";

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case DB_MOVIE_WITH_ID: {
                retCursor = cursorFromUriMovieByID(uri, projection);
                break;
            }
            case DB_FAVORITE_MOVIES: {
                retCursor = cursorFavoriteMovies(projection);
                break;
            }
            case DB_MOVIE_VIDEOS: {
                retCursor = cursorVideos(uri, projection);
                break;

            }
            case DB_MOVIE_REVIEWS: {
                retCursor = cursorReviews(uri, projection);
                break;
            }
            case REST_MOVIES: {
                retCursor = MovieRestHelper.getMovies();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor cursorReviews(Uri uri, String[] projection) {
        String selection = sMovieIdSelection;
        String[] selectionArgs = new String[]{uri.getPathSegments().get(1)};
        return mOpenHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                ""
        );
    }

    private Cursor cursorVideos(Uri uri, String[] projection) {
        String selection = sMovieIdSelection;
        String[] selectionArgs = new String[]{uri.getPathSegments().get(1)};
        return mOpenHelper.getReadableDatabase().query(
                MovieContract.VideoEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                ""
        );
    }

    private Cursor cursorFromUriMovieByID(Uri uri, String[] projection) {
        String selection = sMovieIdSelection;
        String[] selectionArgs = new String[]{uri.getPathSegments().get(1)};

        return mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sMovieSortOrder
        );
    }

    private Cursor cursorFavoriteMovies(String[] projection) {
        String selection = sMovieFavoriteSelection;
        String[] selectionArgs = new String[]{};

        return mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sMovieSortOrder
        );
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case DB_FAVORITE_MOVIES:
            case REST_MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case DB_MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case DB_MOVIE_VIDEOS:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case DB_MOVIE_REVIEWS:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case DB_MOVIE_WITH_ID: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.MovieEntry.buildMovieUriWithId(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case REST_MOVIES: {
                MovieRestHelper.setMovies(values);
                break;
            }
            case DB_MOVIE_VIDEOS: {
                db.beginTransaction();
                db.setTransactionSuccessful();
                for (ContentValues item : values) {
                    db.insert(MovieContract.VideoEntry.TABLE_NAME, null, item);
                }
                db.endTransaction();
                break;
            }
            case DB_MOVIE_REVIEWS: {
                db.beginTransaction();
                db.setTransactionSuccessful();
                for (ContentValues item : values) {
                    db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, item);
                }
                db.endTransaction();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        selection = sMovieIdSelection;
        selectionArgs = new String[]{uri.getPathSegments().get(1)};
        switch (match) {
            case DB_MOVIE_WITH_ID:
                rowsDeleted = db.delete(
                        MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = db.delete(
                        MovieContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        selection = sMovieIdSelection;
        selectionArgs = new String[]{uri.getPathSegments().get(1)};

        switch (match) {
            case DB_MOVIE_WITH_ID:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(MovieContract.MovieEntry.CONTENT_URI_DB_FAVORITE, null);
        }
        return rowsUpdated;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/favorites", DB_FAVORITE_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/rest", REST_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", DB_MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/" + MovieContract.PATH_VIDEO, DB_MOVIE_VIDEOS);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/" + MovieContract.PATH_REVIEW, DB_MOVIE_REVIEWS);
        return matcher;
    }

}
