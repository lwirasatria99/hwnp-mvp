package com.elabram.lm.wmshwnp.db;

public class AttendanceModel {

    public static final String TABLE_NAME = "attendance";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTART = "time_start";
    public static final String COLUMN_TIMEEND = "time_end";

    public static final String COLUMN_PLACESTART = "place_start";
    public static final String COLUMN_PLACESEND = "place_end";

    public static final String COLUMN_GMT = "gmt";
    public static final String COLUMN_TIMEZONE_ID = "timezone_id";


    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private String time_start;
    private String time_end;

    private String place_start;
    private String place_end;
    private String timestamp;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TIMESTART + " TEXT,"
                    + COLUMN_TIMEEND + " TEXT,"
                    + COLUMN_PLACESTART + " TEXT,"
                    + COLUMN_PLACESEND + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public AttendanceModel() {
    }

    public AttendanceModel(int id, String time_start, String time_end, String place_start, String place_end, String timestamp) {
        this.id = id;
        this.time_start = time_start;
        this.time_end = time_end;
        this.place_start = place_start;
        this.place_end = place_end;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

    public String getPlace_start() {
        return place_start;
    }

    public void setPlace_start(String place_start) {
        this.place_start = place_start;
    }

    public String getPlace_end() {
        return place_end;
    }

    public void setPlace_end(String place_end) {
        this.place_end = place_end;
    }
}
