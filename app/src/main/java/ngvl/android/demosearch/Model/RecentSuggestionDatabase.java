package ngvl.android.demosearch.Model;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by rajatbeck on 10/9/2016.
 */

public class RecentSuggestionDatabase extends SQLiteOpenHelper {

    public static final String KEY_ROW_ID = "_id";
    public static final String KEY_NAME = "comp_name";
    public static final String KEY_ICON = "icon";
    public static final String SQLITE_TABLE = "recent_scripts";
    public static final String DATABASE_NAME = "RecentSearch.db";
    private HashMap<String, String> mAliasMap;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + "(" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_ICON + " INT," +
                    KEY_NAME + " TEXT UNIQUE);";
    private static final String SQL_DROP = "DROP TABLE IF EXISTS " + SQLITE_TABLE;


    public RecentSuggestionDatabase(Context context) {
        super(context, DATABASE_NAME, null, 5);
        mAliasMap = new HashMap<>();
        mAliasMap.put(BaseColumns._ID, KEY_ROW_ID + " as " + BaseColumns._ID);
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, KEY_NAME + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, KEY_ROW_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, KEY_ICON + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DROP);
        onCreate(sqLiteDatabase);
    }

    public Cursor getRecentSearches(String id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(SQLITE_TABLE);

        sqLiteQueryBuilder.setProjectionMap(mAliasMap);

        if (id != null) {
            sqLiteQueryBuilder.appendWhere(KEY_NAME + " LIKE '%" + id + "%'");
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = KEY_ROW_ID;
        }
        Cursor cursor = sqLiteQueryBuilder.query(getReadableDatabase(),
                new String[]
                        {
                                BaseColumns._ID,
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                                SearchManager.SUGGEST_COLUMN_ICON_1
                        },
                selection,
                selectionArgs,
                null,
                null,
                sortOrder + " DESC", "2");
        cursor.moveToFirst();
        return cursor;
    }

    public long addNewScript(ContentValues values) throws SQLiteException {
        long id = getWritableDatabase().insert(SQLITE_TABLE, "", values);
        if (id <= 0) {
            throw new SQLiteException("Failed to add a Script");
        }
        return id;
    }
}
