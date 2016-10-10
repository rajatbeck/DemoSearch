package ngvl.android.demosearch.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by rajatbeck on 10/9/2016.
 */

public class RecentSuggestionDatabase extends SQLiteOpenHelper {

    public static final String KEY_ROW_ID = "_id";
    public static final String KEY_NAME = "comp_name";
    public static final String SQLITE_TABLE = "recent_scripts";
    public static final String DATABASE_NAME = "RecentSearch.db";

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + "(" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_NAME + " TEXT UNIQUE );";
    private static final String SQL_DROP = "DROP TABLE IF EXISTS " + SQLITE_TABLE;


    public RecentSuggestionDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);

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

        /*String test = "1";
        if (id != null) {
            sqLiteQueryBuilder.appendWhere(KEY_ROW_ID + "=" + test);
        }*/
        if (sortOrder == null || sortOrder == "") {
            sortOrder = KEY_NAME;
        }
        Cursor cursor = sqLiteQueryBuilder.query(getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        cursor.moveToFirst();
//        Log.d("cursor", cursor.getString(cursor.getColumnIndex("comp_name")));
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
