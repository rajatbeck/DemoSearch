package ngvl.android.demosearch;

import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ngvl.android.demosearch.Adapter.SearchResultAdapter;
import ngvl.android.demosearch.Model.RecentSuggestionDatabase;

public class SearchableActivity extends AppCompatActivity {

    private static boolean SEARCH_IS_CLICKED = false;
    private static boolean SEARCH_SUGGESTION_IS_CLICKED = false;
    private static final String TAG = SearchableActivity.class.getSimpleName();
    private MyHandler mHandler;
    private RecyclerView mRecyclerView;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(SearchableActivity.this));

        intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d(TAG, "Search button is clicked");
            String query = intent.getStringExtra(SearchManager.QUERY);
            SEARCH_IS_CLICKED = true;
            SEARCH_SUGGESTION_IS_CLICKED = false;
            Uri uri = CitySuggestionProvider.CONTENT_URI;
            ContentValues contentValues = new ContentValues();
            contentValues.put(RecentSuggestionDatabase.KEY_NAME, intent.getStringExtra(SearchManager.QUERY));
            contentValues.put(RecentSuggestionDatabase.KEY_ICON, R.drawable.ic_restore_white_24dp);
            getContentResolver().insert(uri, contentValues);
            mHandler = new MyHandler(this);
            mHandler.startQuery(0, null, uri, null, null, new String[]{query}, null);

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.d(TAG, "Suggestion is clicked");
            SEARCH_SUGGESTION_IS_CLICKED = true;
            SEARCH_IS_CLICKED = false;
            Log.d(TAG, String.valueOf(intent.getData()));
            mHandler = new MyHandler(this);
            mHandler.startQuery(0, null, intent.getData(), null, null, null, null);
        }
    }


    class MyHandler extends AsyncQueryHandler {
        private final String TAG = MyHandler.class.getSimpleName();
        // avoid memory leak
        WeakReference<SearchableActivity> activity;
        List<String> scriptList = new ArrayList<>();

        public MyHandler(SearchableActivity searchableActivity) {
            super(searchableActivity.getContentResolver());
            activity = new WeakReference<>(searchableActivity);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            if (cursor == null || cursor.getCount() == 0) return;

            cursor.moveToFirst();
            if (SEARCH_SUGGESTION_IS_CLICKED) {
                if (cursor.getCount() == 1) {
                    Log.d(TAG, cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(RecentSuggestionDatabase.KEY_NAME, cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)));
                    contentValues.put(RecentSuggestionDatabase.KEY_ICON, R.drawable.ic_restore_white_24dp);
                    getContentResolver().insert(CitySuggestionProvider.CONTENT_URI, contentValues);
                }
            }
            long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            String text = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            long dataId = cursor.getLong(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID));
            Log.d(TAG, String.valueOf(cursor.getCount()));
            while (!cursor.isAfterLast()) {
                scriptList.add(cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)));
                cursor.moveToNext();
            }
            cursor.close();

            if (activity.get() != null) {
                SearchResultAdapter searchResultAdapter = new SearchResultAdapter(scriptList);
                mRecyclerView.setAdapter(searchResultAdapter);
            }
        }
    }

    ;
}
