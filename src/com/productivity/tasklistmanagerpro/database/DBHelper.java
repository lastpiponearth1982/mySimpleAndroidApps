package com.productivity.tasklistmanagerpro.database;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//helper class for creating database
public class DBHelper extends SQLiteOpenHelper {
	DBHelper helper;
	SQLiteDatabase db;
	ArrayList<String> itemsArrayList = new ArrayList<>();
	Context context;

	// database and table attributes
	public static final String DBNAME = "TaskListManagerProDB";// database name
	public static final int DBVERSION = 1;// database version
	public static final String TABLENAME = "TaskTable";// table name
	public static final String KEY_ID = "_id";// column 0
	public static final String KEY_TASKNAME = "TaskName";// column 1
	public static final String KEY_TASKFLAG = "TaskFlag";// column 2
	public static final String CREATESQL = "create table TaskTable(_id integer primary key "
			+ "autoincrement not null, TaskName text not null, TaskFlag integer not null);";
	public static final String[] ALLROWS = { KEY_ID, KEY_TASKNAME, KEY_TASKFLAG };// string
																					// array
																					// used
																					// for
																					// retrieving
																					// all
																					// data
	public static final String[] FLAGS = { KEY_TASKNAME };// string array for
															// querying TaskFlag
															// column

	public DBHelper(Context context) {
		super(context, DBNAME, null, DBVERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATESQL);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLENAME + ";");

	}

	// method for transferring cursor data to arraylist
	public void transferCursorDataToArrayList(Cursor c) {
		int cursorLength = c.getCount();
		c.moveToFirst();

		for (int i = 0; i < cursorLength; i++) {
			String fromCursor = c.getString(c.getColumnIndex(KEY_TASKNAME));
			itemsArrayList.add(fromCursor);
			c.moveToNext();
		}
	}

}
