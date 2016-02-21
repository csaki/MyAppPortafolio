package com.devsaki.myappportafolio.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.devsaki.myappportafolio.R;
import com.devsaki.myappportafolio.data.MovieContract;
import com.devsaki.myappportafolio.data.MovieRestHelper;
import com.devsaki.myappportafolio.domain.Movie;
import com.devsaki.myappportafolio.domain.Review;
import com.devsaki.myappportafolio.domain.Video;
import com.devsaki.myappportafolio.util.AndroidUtil;
import com.devsaki.myappportafolio.util.ConstantPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String CURRENT_PAGE = "currentPage";
    public static final String QTY_PAGES = "qtyPages";
    public static final String SORT = "sort";
    public static final String ID = "id";
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();


    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        int currentSort = extras.getInt(SORT);
        Log.d(LOG_TAG, "Sort : " + currentSort);
        int currentPage = extras.getInt(CURRENT_PAGE);
        int qtyPages = extras.getInt(QTY_PAGES);
        int id = extras.getInt(ID);

        if(currentSort==ConstantPreferences.MOVIE_SORT_MY_FAVORITES){
            return;
        }

        try {

            if(id != 0){
                String themoviedbapiUrl = "http://api.themoviedb.org/3/movie/" + id;

                Uri builtUri = Uri.parse(themoviedbapiUrl).buildUpon().appendQueryParameter("api_key", getContext().getResources().getString(R.string.themoviedb_api_key))
                        .appendQueryParameter("append_to_response", "videos,reviews").build();

                String json = AndroidUtil.callGet(builtUri.toString());

                Cursor currentCursorMovie = getContext().getContentResolver().query(MovieContract.MovieEntry.buildMovieUriWithId(id), MovieContract.MovieEntry.PROJECTION, null, null, null);
                Movie currentMovie = null;
                boolean favorite = false;

                if(currentCursorMovie.moveToFirst()){
                    currentMovie = MovieRestHelper.moviesFromCursor(currentCursorMovie).get(0);
                    favorite = currentMovie.isFavorite();
                }

                if(currentMovie==null||!AndroidUtil.beforeOneDay(currentMovie.getDateUpdate())){
                    JSONObject jsonObject = new JSONObject(json);
                    Movie result = new Movie();
                    result.setId(id);
                    result.setPosterPath(jsonObject.getString("poster_path"));
                    result.setOriginalTitle(jsonObject.getString("original_title"));
                    result.setVoteAverage(jsonObject.getDouble("vote_average"));
                    result.setOverview(jsonObject.getString("overview"));
                    result.setRuntime(jsonObject.getInt("runtime"));

                    result.setReleaseDate(jsonObject.getString("release_date"));
                    result.setDateUpdate(new Date().getTime());
                    result.setFavorite(favorite);

                    //read videos
                    JSONArray jsonArray = jsonObject.getJSONObject("videos").getJSONArray("results");
                    result.setVideos(new ArrayList<Video>(jsonArray.length()));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonVideo = jsonArray.getJSONObject(i);
                        result.getVideos().add(new Video(jsonVideo.getString("name"), jsonVideo.getString("site"), jsonVideo.getString("key"), jsonVideo.getString("type")));
                    }

                    //read reviews
                    jsonArray = jsonObject.getJSONObject("reviews").getJSONArray("results");
                    result.setReviews(new ArrayList<Review>(jsonArray.length()));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonVideo = jsonArray.getJSONObject(i);
                        result.getReviews().add(new Review(jsonVideo.getString("author"), jsonVideo.getString("content")));
                    }

                    Uri uri = MovieContract.MovieEntry.buildMovieUriWithId(result.getId());
                    getContext().getContentResolver().delete(uri, null, null);
                    getContext().getContentResolver().insert(uri, MovieRestHelper.fromMovie(result));
                    getContext().getContentResolver().bulkInsert(MovieContract.ReviewEntry.buildReviewByMovieIdUri(result.getId()), MovieRestHelper.fromReviews(result));
                    getContext().getContentResolver().bulkInsert(MovieContract.VideoEntry.buildVideoByMovieIdUri(result.getId()), MovieRestHelper.fromVideos(result));
                }
            } else {
                Log.d(LOG_TAG, "Starting to load movie list");
                ArrayList<Movie> result = new ArrayList<>();
                String themoviedbapiUrl = "http://api.themoviedb.org/3/movie/" + (currentSort==ConstantPreferences.MOVIE_SORT_RATING? "top_rated" : "popular") + "?";

                for(int i = 0; i < qtyPages; i++){
                    Uri builtUri = Uri.parse(themoviedbapiUrl).buildUpon()
                            .appendQueryParameter("api_key", getContext().getResources().getString(R.string.themoviedb_api_key))
                            .appendQueryParameter("page", String.valueOf(currentPage + i)).build();

                    String json = AndroidUtil.callGet(builtUri.toString());
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray movies = jsonObject.getJSONArray("results");
                    for(int j = 0; j < movies.length(); j++){
                        JSONObject jsonMovie = movies.getJSONObject(j);
                        Movie movie = new Movie();
                        movie.setId(jsonMovie.getInt("id"));
                        movie.setOriginalTitle(jsonMovie.getString("original_title"));
                        movie.setPosterPath(jsonMovie.getString("poster_path"));
                        result.add(movie);
                    }
                }
                Log.d(LOG_TAG, "Qty loaded : " + result.size());
                Log.d(LOG_TAG, "Qty loaded : " + result.size());
                getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI_REST, MovieRestHelper.fromMovies(result));
            }
            Log.d(LOG_TAG, "Starting sync finish");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, int sort, int qtyPages, int currentPage, int id) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putInt(QTY_PAGES, qtyPages);
        bundle.putInt(ID, id);
        bundle.putInt(CURRENT_PAGE, currentPage);
        bundle.putInt(SORT, sort);
        ContentResolver.requestSync(getSyncAccount(context, sort, qtyPages, currentPage, id),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context, int sort, int qtyPages, int currentPage, int id) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(context, sort, qtyPages, currentPage, id);
        }
        return newAccount;
    }

    private static void onAccountCreated(Context context, int sort, int qtyPages, int currentPage, int id) {
        syncImmediately(context, sort, qtyPages, currentPage, id);
    }

    public static void initializeSyncAdapter(Context context, int sort, int qtyPages, int currentPage, int id) {
        getSyncAccount(context, sort, qtyPages, currentPage, id);
    }
}