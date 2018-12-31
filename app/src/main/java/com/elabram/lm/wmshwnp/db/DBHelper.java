package com.elabram.lm.wmshwnp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "wms_db";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // create wms table
        db.execSQL(AttendanceModel.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + AttendanceModel.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // Insert Attendance
    public long insertAttendance(String time_start, String time_end, String place_start, String place_end,
                                 String gmt, String timezone_id) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(AttendanceModel.COLUMN_TIMESTART, time_start);
        values.put(AttendanceModel.COLUMN_TIMEEND, time_end);
        values.put(AttendanceModel.COLUMN_PLACESTART, place_start);
        values.put(AttendanceModel.COLUMN_PLACESEND, place_end);
        values.put(AttendanceModel.COLUMN_GMT, gmt);
        values.put(AttendanceModel.COLUMN_TIMEZONE_ID, timezone_id);

        // insert row
        long id = db.insert(AttendanceModel.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public List<AttendanceModel> getAllAttendance() {
        List<AttendanceModel> attendanceModels = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + AttendanceModel.TABLE_NAME + " ORDER BY " +
                AttendanceModel.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AttendanceModel attendanceModel = new AttendanceModel();
                attendanceModel.setId(cursor.getInt(cursor.getColumnIndex(AttendanceModel.COLUMN_ID)));
                attendanceModel.setTime_start(cursor.getString(cursor.getColumnIndex(AttendanceModel.COLUMN_TIMESTART)));
                attendanceModel.setTime_end(cursor.getString(cursor.getColumnIndex(AttendanceModel.COLUMN_TIMEEND)));
                attendanceModel.setPlace_start(cursor.getString(cursor.getColumnIndex(AttendanceModel.COLUMN_PLACESTART)));
                attendanceModel.setPlace_end(cursor.getString(cursor.getColumnIndex(AttendanceModel.COLUMN_PLACESEND)));
                attendanceModel.setTimestamp(cursor.getString(cursor.getColumnIndex(AttendanceModel.COLUMN_TIMESTAMP)));

                attendanceModels.add(attendanceModel);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return attendanceModels list
        return attendanceModels;
    }
}
