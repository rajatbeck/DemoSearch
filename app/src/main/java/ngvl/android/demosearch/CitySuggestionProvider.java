package ngvl.android.demosearch;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ngvl.android.demosearch.Model.RecentSuggestionDatabase;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//Test Push
public class CitySuggestionProvider extends ContentProvider {

    public static final String AUTHORITY = "ngvl.android.demosearch.citysuggestion";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search_suggest_query");


    private static final int TYPE_ALL_SUGGESTIONS = 1;
    private static final int TYPE_SINGLE_SUGGESTION = 2;
    private static final int ACTION_ALL_SUGGESTION = 3;
    private static final String TAG = "CITY_SUGGESTION";
    private RecentSuggestionDatabase recentSuggestionDatabase;
//    public final static int MODE = DATABASE_MODE_QUERIES;


    private UriMatcher mUriMatcher;
    private List<String> cities;

//    public CitySuggestionProvider() {
//        setupSuggestions(AUTHORITY, MODE);
//    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        recentSuggestionDatabase = new RecentSuggestionDatabase(context);
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, "/#", TYPE_SINGLE_SUGGESTION);
        mUriMatcher.addURI(AUTHORITY, "search_suggest_query/*", TYPE_ALL_SUGGESTIONS);
        mUriMatcher.addURI(AUTHORITY, "search_suggest_query", ACTION_ALL_SUGGESTION);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                        SearchManager.SUGGEST_COLUMN_ICON_1
                }
        );


        if (mUriMatcher.match(uri) == TYPE_ALL_SUGGESTIONS) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
//                    .url("https://dl.dropboxusercontent.com/u/6802536/cidades.json")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonString = response.body().string();
//                JSONArray jsonArray = new JSONArray(jsonString);
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.getString("status").equals("SUCCESS")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    cities = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        cities.add(jsonObject1.getString("comp_name"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cities != null) {
                String query = uri.getLastPathSegment().toUpperCase();
                int limit = Integer.parseInt(uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));
                int lenght = cities.size();
                String id = uri.getPathSegments().get(1);
                Cursor recentCursor = recentSuggestionDatabase.getRecentSearches(id, projection, selection, selectionArgs, sortOrder);


                for (int i = 0; i < lenght && cursor.getCount() < limit; i++) {
                    String city = cities.get(i);
                    if (city.toUpperCase().contains(query)) {
                        cursor.addRow(new Object[]{i, city, i, R.drawable.ic_search_white_24dp});
                    }
                }
                Cursor[] mergeCursor = new Cursor[]{recentCursor, cursor};
                Cursor cursor1 = new MergeCursor(mergeCursor);
                cursor1.moveToFirst();
                cities.clear();
                while (cursor1.isAfterLast() == false) {
                    cities.add(cursor1.getString(cursor1.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)));
                    cursor1.moveToNext();
                }
                Log.d(TAG, String.valueOf(cities));
                return new MergeCursor(mergeCursor);
            }
        } else if (mUriMatcher.match(uri) == TYPE_SINGLE_SUGGESTION) {
            int position = Integer.parseInt(uri.getLastPathSegment());
            Log.d("Single Suggestion", cities.toString());
            String city = cities.get(position);
            cursor.addRow(new Object[]{position, city, position, R.drawable.ic_search_white_24dp});
//TODO:Reslove conflic in click of history and suggestions from server
        } else if (mUriMatcher.match(uri) == ACTION_ALL_SUGGESTION) {
            if (cities != null) {

                //Called when the search action button is clicked
                if (selectionArgs == null) {
//                    String id = uri.getPathSegments().get(0);
                    Cursor recentCursor = recentSuggestionDatabase.getRecentSearches(null, projection, selection, selectionArgs, sortOrder);
                    return recentCursor;
                } else {
                    String query = selectionArgs[0].toUpperCase();
                    int lenght = cities.size();
                    for (int i = 0; i < lenght && cursor.getCount() < 50; i++) {
                        String city = cities.get(i);
                        if (city.toUpperCase().contains(query)) {
                            cursor.addRow(new Object[]{i, city, i, R.drawable.ic_search_white_24dp});
                            Log.d("Action Suggestion", query + String.valueOf(cursor.getCount()) + cities.toString());

                        }
                    }
                }
            }
        }
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert is called" + String.valueOf(values));
        try {
            long id = recentSuggestionDatabase.addNewScript(values);
            Uri returnUri = CONTENT_URI.withAppendedPath(CONTENT_URI, String.valueOf(id));
            return returnUri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//        throw new UnsupportedOperationException("Not yet");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
