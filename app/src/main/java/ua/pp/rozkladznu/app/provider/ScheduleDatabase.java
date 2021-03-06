package ua.pp.rozkladznu.app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import ua.pp.rozkladznu.app.provider.ScheduleContract.AcademicHourColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.AudienceColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.CampusColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.DepartmentColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.GroupColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.LecturerColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.ScheduleColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.SubjectColumns;
import ua.pp.rozkladznu.app.provider.ScheduleContract.SyncColumns;

/**
 * @author Vojko Vladimir
 */
public class ScheduleDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schedule.db";
    private static final int VERSION_1_APP_VERSION_CODE_1 = 1;
    private static final int CURRENT_DATABASE_VERSION = VERSION_1_APP_VERSION_CODE_1;

    interface Tables {
        String DEPARTMENT = "department";
        String GROUP = "`group`";
        String LECTURER = "lecturer";
        String SUBJECT = "subject";
        String ACADEMIC_HOUR = "academic_hour";
        String CAMPUS = "campus";
        String AUDIENCE = "audience";
        String SCHEDULE = "schedule";
    }

    public ScheduleDatabase(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.DEPARTMENT + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DepartmentColumns.DEPARTMENT_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.GROUP + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GroupColumns.DEPARTMENT_ID + " INTEGER NOT NULL, "
                + GroupColumns.COURSE + " INTEGER NOT NULL, "
                + GroupColumns.SUBGROUP_COUNT + " INTEGER NOT NULL, "
                + GroupColumns.GROUP_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.LECTURER + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LecturerColumns.LECTURER_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.SUBJECT + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SubjectColumns.SUBJECT_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.ACADEMIC_HOUR + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AcademicHourColumns.NUM + " INTEGER NOT NULL, "
                + AcademicHourColumns.START_TIME + " INTEGER NOT NULL, "
                + AcademicHourColumns.END_TIME + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.CAMPUS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CampusColumns.CAMPUS_NAME + " TEXT NOT NULL, "
                + CampusColumns.LATITUDE + " REAL NOT NULL, "
                + CampusColumns.LONGITUDE + " REAL NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.AUDIENCE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AudienceColumns.CAMPUS_ID + " INTEGER NOT NULL, "
                + AudienceColumns.AUDIENCE_NUMBER + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.SCHEDULE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ScheduleColumns.SCHEDULE_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.SUBGROUP + " INTEGER NOT NULL, "
                + ScheduleColumns.GROUP_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.SUBJECT_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.DAY_OF_WEEK + " INTEGER NOT NULL, "
                + ScheduleColumns.ACADEMIC_HOUR_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.LECTURER_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.AUDIENCE_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.PERIODICITY + " INTEGER NOT NULL, "
                + ScheduleColumns.START_DATE + " INTEGER NOT NULL, "
                + ScheduleColumns.END_DATE + " INTEGER NOT NULL, "
                + ScheduleColumns.CLASS_TYPE + " INTEGER NOT NULL, "
                + ScheduleColumns.FREE_TRAJECTORY + " INTEGER NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
