package com.productivity.tasklistmanagerpro.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.productivity.tasklistmanagerpro.R;
import com.productivity.tasklistmanagerpro.database.DBHelper;
import com.productivity.tasklistmanagerpro.preferences.OptionsMenu;
import com.productivity.tasklistmanagerpro.preferences.TaskListSharedPreferences;

public class MainActivity extends ListActivity {
	/*
	 * This simple tasklist app does not use the simplecursor adapter for
	 * updating its entries from the database. Instead, it uses a custom
	 * arrayadapter that gets its data from an arraylist whose data is supplied
	 * by a cursor.
	 */

	/* CHANGELOG VER. 1.1: */
	/*
	 * 1.Added inputfield filter for preventing sql statements being sent
	 * 2.Added done funcationality button to item detail dialog
	 */

	// these classes instantiate the backend for this data centric app
	DBHelper helper;// sqliteopenhelper class for creating and opening sqlite
					// database
	static SQLiteDatabase db;
	OptionsMenu optionsMenu = new OptionsMenu(this);// optionsmenu class
													// containing help for app
	TaskListSharedPreferences appPreferences = new TaskListSharedPreferences(
			this);// preferences class containing settings for autodelete

	// these data structures provide convenient temporary storage for effective
	// communication between objects and methods
	ArrayList<String> itemsArrayList;// arrayList that contains items to be
										// added to custom adapter
	String[] prioritiesString = { "High", "Medium", "Low" };// string array
															// settings for
															// spinnerobject
	int[] priorityColors = { Color.GREEN, Color.YELLOW, Color.RED };

	public String contentString;// convenience string for getting string of
								// click textview
	int itempositionInt;// convenience integer for storing position values of
						// list items
	int checkedItems = 0;// convenience integer for checking and updating number
							// of checked/marked items. toggles activation of
							// delete done button
							// SharedPreferences sharedPreferences;//
							// sharedpreferences for storing
	// // settings for enabling or disabling
	// // auto-delete
	String autodelete;// sharedpreference setting to toggle
						// autodelete after app exit.

	// these string values are convenient objects for displaying task number
	// according to priority setting
	private String totaliteminlistString = "Total Items: ";
	private String totalcheckedString = "Total Checked: ";
	private String totalhighString = "Total High: ";
	private String totalmediumString = "Total Medium: ";
	private String totallowString = "Total Low: ";

	String doneString = "<DONE>";// string tag used to flag tasks as done

	ListView list;// where elements from the arraylist is displayed
	EditText inputfield;// where user input is entered

	// textviews
	TextView appnameTextView, chosenpriorityTextView, totalitemsTextView,
			totalcheckeditemsTextView, totalHighTextView, totalMedTextView,
			totalLowTextView;
	// buttons
	Button addButton, overwriteButton, clearButton, doneButton, deleteButton;
	// spinners
	Spinner priorityspinner;

	// these objects are from the MyAdapter class
	CheckBox checkhere;
	TextView texthere;
	MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		appPreferences.retrieveSharedPrefsforAutoDelete();// retrieve autodelete
															// settings from
															// sharedpreferences

		appPreferences.retrieveMaxCharSettings();// retrieve maxchar settings
													// from sharedpreferences
		appPreferences.retrieveTextFileName();

		itemsArrayList = new ArrayList<>();

		try {
			helper.onCreate(db);// create db if not exists, if exists just log
								// it
		} catch (Exception e) {
			Log.d("exists", "table already exists");
		}

		// creates a DB table with name TaskTable
		createTable();// add table to existing db, if created, will ignore

		getDatabase();

		// creates a cursor to traverse all DB data
		Cursor c = getAllRows();

		transferCursorDataToArrayList(c); // copies cursor data to arraylist.
											// this is the method which makes
											// simplecursor adapter unneccesary.
											// this is good for arrays with
											// small number of dimensions

		c.close();// close the cursor
		// textview for application title
		appnameTextView = (TextView) findViewById(R.id.appname);

		// wiring views to java code
		list = (ListView) findViewById(android.R.id.list);

		// itemcount textviews
		totalitemsTextView = (TextView) findViewById(R.id.totalitems);
		totalcheckeditemsTextView = (TextView) findViewById(R.id.totalchecked);
		totalHighTextView = (TextView) findViewById(R.id.totalHigh);
		totalMedTextView = (TextView) findViewById(R.id.totalMed);
		totalLowTextView = (TextView) findViewById(R.id.totalLow);

		// spinner for selecting task priority
		priorityspinner = (Spinner) findViewById(R.id.priorityspinner);

		// edittext for entering task details
		inputfield = (EditText) findViewById(R.id.inputfield);

		// input buttons
		addButton = (Button) findViewById(R.id.add);
		overwriteButton = (Button) findViewById(R.id.overwrite);
		clearButton = (Button) findViewById(R.id.clear);
		doneButton = (Button) findViewById(R.id.done);
		deleteButton = (Button) findViewById(R.id.delete);

		// string for apptitle is styled by html
		String appTitle = "<h1>TaskListManagerPro<h1>";
		appnameTextView.setText(Html.fromHtml(appTitle));

		// add,overwrite,clear and done are disabled by default
		addButton.setEnabled(false);
		overwriteButton.setEnabled(false);
		clearButton.setEnabled(false);
		doneButton.setEnabled(false);

		// display adapter for spinner
		Adapter spinneradapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, prioritiesString);
		priorityspinner.setAdapter((SpinnerAdapter) spinneradapter);

		/* define listeners for UI elements */

		// listview listener for overwrite function
		OnItemClickListener listclick = new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, View view,
					int position, long id) {

				itempositionInt = position;
				contentString = (String) parent.getItemAtPosition(position);
				inputfield.setText(contentString);
				overwriteButton.setEnabled(true);
				addButton.setEnabled(false);

			}
		};
		// listview listener for delete function
		OnItemLongClickListener onlongitem = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				itempositionInt = position;
				int maxLength = appPreferences.retrieveMaxCharSettings();
				final String contentString = (String) parent
						.getItemAtPosition(position);
				if (contentString.length() > maxLength) {
					Toast.makeText(MainActivity.this, contentString,
							Toast.LENGTH_SHORT).show();
					AlertDialog.Builder itemDetails = new AlertDialog.Builder(
							MainActivity.this);
					itemDetails.setTitle("Item Details");

					itemDetails
							.setMessage(contentString)
							.setCancelable(false)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();

										}
									});
					itemDetails
							.setMessage(contentString)
							.setCancelable(false)
							.setNegativeButton("Mark as Done",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {

											String flagged = contentString
													+ doneString;

											getDatabase();

											db.execSQL("update TaskTable set TaskFlag="
													+ 1
													+ " where TaskName="
													+ "'"
													+ contentString
													+ "';");

											db.execSQL("update TaskTable set TaskName="
													+ "'"
													+ flagged
													+ "'"
													+ " where TaskName="
													+ "'"
													+ contentString + "';");
											itemsArrayList.clear();
											Cursor c = getAllRows();
											transferCursorDataToArrayList(c);
											refresh();
											c.close();

										}
									});
					itemDetails.show();
				}

				return true;
			}

		};
		// spinner listener for prioritysetting
		OnItemSelectedListener priorityselect = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String chosen = (String) parent.getItemAtPosition(position);
				switch (chosen) {
				case "Low":
					parent.setBackgroundColor(priorityColors[0]);
					String inputLow = "Low: ";
					inputfield.setText(inputLow);
					inputfield.setSelection(inputLow.length());
					break;
				case "Medium":
					parent.setBackgroundColor(priorityColors[1]);
					String inputMedium = "Medium: ";
					inputfield.setText(inputMedium);
					inputfield.setSelection(inputMedium.length());
					break;
				case "High":
					parent.setBackgroundColor(priorityColors[2]);
					String inputHigh = "High: ";
					inputfield.setText(inputHigh);
					inputfield.setSelection(inputHigh.length());
					break;

				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		};
		// inputfield listener for filtered input and activating buttons once
		// minimum criteria of characters are present:
		// 1. priority tag,
		// 2. no two different priority tags(ie, Low and Medium)
		// 3. characters must be beyond minimum number.

		TextWatcher inputwatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String lowString = "low:  ";
				String mediumString = "medium:  ";
				String highString = "high:  ";

				String priority = priorityspinner.getSelectedItem().toString();
				String in = inputfield.getText().toString();
				if (s.length() < lowString.length()
						|| s.length() < highString.length()
						|| s.length() < mediumString.length()) {
					addButton.setEnabled(false);
					clearButton.setEnabled(false);
				} else {
					addButton.setEnabled(true);
					clearButton.setEnabled(true);

					String select = "select *";
					String dropdatabase = "drop database";
					String dropTable = "drop table";
					String delete = "delete from";
					String table = "TaskTable";
					String dataBase = "TaskListManagerProDB";

					String SELECT = "SELECT *";
					String DROPDATABASE = "DROP DATABASE";
					String DROPTABLE = "DROP TABLE";
					String DELETE = "DELETE FROM";
					String TABLE = "TASKTABLE";
					String DATABASE = "TaskListManagerProDB";

					if (priority.contains("Low") && in.contains("Low:")) {
						CharSequence medium = "medium:";
						CharSequence Medium = "Medium:";
						CharSequence high = "high:";
						CharSequence High = "High:";

						if (in.contains(medium)) {
							restrictInputmedium(in, medium);
						}

						if (in.contains(Medium)) {
							restrictInputMedium(in, Medium);
						}

						if (in.contains(high)) {
							restrictInputmedium(in, high);
						}
						if (in.contains(High)) {
							restrictInputmedium(in, High);
						}
					}
					if (priority.contains("Medium") && in.contains("Medium:")) {
						CharSequence low = "low:";
						CharSequence Low = "Low:";
						CharSequence high = "high:";
						CharSequence High = "High:";
						if (in.contains(low)) {
							restrictInputmedium(in, low);
						}

						if (in.contains(Low)) {
							restrictInputLow(in, Low);
						}
						if (in.contains(high)) {
							restrictInputmedium(in, high);
						}
						if (in.contains(High)) {
							restrictInputmedium(in, High);
						}
					}
					if (priority.contains("High") && in.contains("High:")) {
						CharSequence low = "low:";
						CharSequence Low = "Low:";
						CharSequence medium = "medium:";
						CharSequence Medium = "Medium:";
						if (in.contains(low)) {
							restrictInputmedium(in, low);
						}

						if (in.contains(Low)) {
							restrictInputLow(in, Low);
						}
						if (in.contains(medium)) {
							restrictInputmedium(in, medium);
						}

						if (in.contains(Medium)) {
							restrictInputMedium(in, Medium);
						}
					}
					if (in.contains(select) || in.contains(SELECT)
							|| in.contains(dropdatabase)
							|| in.contains(DROPDATABASE)
							|| in.contains(dropTable) || in.contains(DROPTABLE)
							|| in.contains(delete) || in.contains(DELETE)
							|| in.contains(table) || in.contains(TABLE)
							|| in.contains(dataBase) || in.contains(DATABASE)) {
						addButton.setEnabled(false);
						overwriteButton.setEnabled(false);
						clearButton.performClick();
					}

				}
			}

			/* methods for filtering */
			public void restrictInputLow(String in, CharSequence Low) {
				String result = in.replaceAll((String) Low, "");
				inputfield.setText(result);
				inputfield.setSelection(result.length());
				Toast.makeText(
						MainActivity.this,
						"Priority already defined. Set spinner to " + Low
								+ " to change.", Toast.LENGTH_SHORT).show();
			}

			public void restrictInputMedium(String in, CharSequence Medium) {
				String result = in.replaceAll((String) Medium, "");
				inputfield.setText(result);
				inputfield.setSelection(result.length());
				Toast.makeText(
						MainActivity.this,
						"Priority already defined. Set spinner to " + Medium
								+ " to change.", Toast.LENGTH_SHORT).show();
			}

			public void restrictInputmedium(String in, CharSequence medium) {
				String result = in.replaceAll((String) medium, "");
				inputfield.setText(result);
				inputfield.setSelection(result.length());
				Toast.makeText(
						MainActivity.this,
						"Priority already defined. Set spinner to " + medium
								+ " to change.", Toast.LENGTH_SHORT).show();
			}

		};
		// listener for adding items priority appends to item
		OnClickListener addItem = new OnClickListener() {

			@Override
			public void onClick(View v) {

				String in = inputfield.getText().toString();

				String priority = priorityspinner.getSelectedItem().toString();

				if (in.contains("Low:")) {
					String strLow = in.replace("Low:", "");
					String fullLow = priority + "" + ": " + strLow;

					itemsArrayList.add(priority + "" + ": " + strLow);
					getDatabase();
					ContentValues values = new ContentValues();
					values.put(DBHelper.KEY_TASKNAME, fullLow);
					values.put(DBHelper.KEY_TASKFLAG, 0);
					db.insertOrThrow(DBHelper.TABLENAME, null, values);
				}
				if (in.contains("Medium:")) {
					String strMed = in.replace("Medium:", "");
					String fullMed = priority + "" + ": " + strMed;

					itemsArrayList.add(priority + "" + ": " + strMed);
					getDatabase();
					ContentValues values = new ContentValues();
					values.put(DBHelper.KEY_TASKNAME, fullMed);
					values.put(DBHelper.KEY_TASKFLAG, 0);
					db.insertOrThrow(DBHelper.TABLENAME, null, values);
				}
				if (in.contains("High:")) {
					String strHigh = in.replace("High:", "");
					String fullHigh = priority + "" + ": " + strHigh;

					itemsArrayList.add(priority + "" + ": " + strHigh);
					getDatabase();
					ContentValues values = new ContentValues();
					values.put(DBHelper.KEY_TASKNAME, fullHigh);
					values.put(DBHelper.KEY_TASKFLAG, 0);
					db.insertOrThrow(DBHelper.TABLENAME, null, values);
				}

				adapter.notifyDataSetChanged();

				updateItemCount();
				if (list.getCount() > 0) {
					deleteButton.setEnabled(true);
				}

			}

		};
		// listener for overwriting item entry
		OnClickListener overWriteEntry = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ArrayList<String> overwritereference = new ArrayList<>();// convenience
				// arraylist
				// string
				// for
				// storing
				// string
				// value
				// temporarily
				overwritereference.add(contentString);

				adapter.notifyDataSetChanged();

				AlertDialog.Builder confirmoverwrite = new AlertDialog.Builder(
						MainActivity.this);
				confirmoverwrite.setTitle("Overwrite Existing Entry");

				confirmoverwrite
						.setMessage("Overwrite entry with new entry?")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

										String priority = priorityspinner
												.getSelectedItem().toString();
										String in = inputfield.getText()
												.toString();
										String str = null;
										if (in.contains("Low:")) {
											str = in.replace("Low:", "");
										}
										if (in.contains("Medium:")) {
											str = in.replace("Medium:", "");
										}
										if (in.contains("High:")) {
											str = in.replace("High:", "");
										}
										itemsArrayList.set(itempositionInt,
												priority + "" + ": " + str);

										// getDatabase();
										db.execSQL("update TaskTable set TaskName="
												+ "'"
												+ in
												+ "'"
												+ " where TaskName="
												+ "'"
												+ contentString + "';");

										adapter.notifyDataSetChanged();
										updateItemCount();

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				confirmoverwrite.show();
			}
		};
		// listener for clear inputfield
		OnClickListener clearInput = new OnClickListener() {

			@Override
			public void onClick(View v) {
				inputfield.setText("");
				Toast.makeText(
						MainActivity.this,
						"Input field cleared, no entries may be added unless there is a "
								+ "priority tag present. Add a priority tag by toggling the priority spinner. See help for more.",
						Toast.LENGTH_LONG).show();
				addButton.setEnabled(false);
				overwriteButton.setEnabled(false);

			}
		};
		// listener for deleting marked/checked items
		OnClickListener deleteDone = new OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder confirmDeleteSelected = new AlertDialog.Builder(
						MainActivity.this);
				confirmDeleteSelected.setTitle("Delete checked Items");

				confirmDeleteSelected
						.setMessage(
								"Delete checked Items? This cannot be reversed.")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										itemsArrayList.clear();
										getDatabase();
										db.execSQL("delete from TaskTable where TaskFlag=1;");
										Cursor c = getAllRows();
										transferCursorDataToArrayList(c);
										refresh();
										c.close();
										checkedItems = 0;
										doneButton.setEnabled(false);

										updateItemCount();

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				confirmDeleteSelected.show();

			}
		};
		// listener for deleting all items
		OnClickListener deleteAll = new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder confirmdelete = new AlertDialog.Builder(
						MainActivity.this);

				confirmdelete.setTitle("Delete all items!");

				confirmdelete
						.setMessage(
								"Are you sure you want to continue? This action is not reversible.")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										itemsArrayList.clear();
										adapter.notifyDataSetChanged();
										updateItemCount();
										deleteButton.setEnabled(false);
										overwriteButton.setEnabled(false);
										getDatabase();
										db.execSQL("delete from sqlite_sequence where name='"
												+ DBHelper.TABLENAME + "';");

										db.execSQL("delete from TaskTable;");

										createTable();

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				confirmdelete.show();

			}
		};
		/* adding the defined listeners to UI elements */
		list.setOnItemClickListener(listclick);
		list.setOnItemLongClickListener(onlongitem);
		priorityspinner.setOnItemSelectedListener(priorityselect);
		inputfield.addTextChangedListener(inputwatcher);
		addButton.setOnClickListener(addItem);
		overwriteButton.setOnClickListener(overWriteEntry);
		clearButton.setOnClickListener(clearInput);
		doneButton.setOnClickListener(deleteDone);
		deleteButton.setOnClickListener(deleteAll);

		refresh();// call method to build item listview
		updateItemCount();// update item count

		// toggle deleteAll function
		if (list.getCount() > 0) {
			deleteButton.setEnabled(true);
		} else {
			deleteButton.setEnabled(false);
		}

	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		db.close();
	}

	public void getDatabase() {
		db = helper.getWritableDatabase();
	}

	// refresh view or recreate list items
	public void refresh() {
		adapter = new MyAdapter(this, R.layout.item, R.id.texthere,
				itemsArrayList);
		boolean enable = adapter.areAllItemsEnabled();
		Log.d("enabled", enable + "");
		setListAdapter(adapter);
	}

	// create DB table
	public void createTable() {
		helper = new DBHelper(this);
		db = helper.getWritableDatabase();
	}

	// update and show amount of items by criteria low,med and high
	public void updateItemCount() {
		int totalCount = list.getCount();
		int totalChecked = 0;
		int HighCount = 0;
		int MedCount = 0;
		int LowCount = 0;

		int itemsArrayListsize = itemsArrayList.size();
		for (int i = 0; i < itemsArrayListsize; i++) {
			if (itemsArrayList.get(i).contains("High:")) {
				HighCount++;
			}
			if (itemsArrayList.get(i).contains("Medium:")) {
				MedCount++;
			}
			if (itemsArrayList.get(i).contains("Low:")) {
				LowCount++;
			}
		}

		// set text for item counters
		totalitemsTextView.setText(totaliteminlistString + totalCount);
		totalcheckeditemsTextView.setText(totalcheckedString + totalChecked);
		totalHighTextView.setText(totalhighString + HighCount);
		totalMedTextView.setText(totalmediumString + MedCount);
		totalLowTextView.setText(totallowString + LowCount);
	}

	// listener for deleting individual item
	public void deleteItem(final int position) {
		AlertDialog.Builder confirmdeleteitem = new AlertDialog.Builder(
				MainActivity.this);

		confirmdeleteitem.setTitle("Delete selected items?");

		confirmdeleteitem
				.setMessage("Delete item/entry?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String content = itemsArrayList.get(position)
										.toString();
								itemsArrayList.remove(position);
								adapter.notifyDataSetChanged();
								updateItemCount();
								getDatabase();
								db.execSQL("delete from " + DBHelper.TABLENAME
										+ " where " + DBHelper.KEY_TASKNAME
										+ "=" + "'" + content + "';");
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		confirmdeleteitem.show();
	}

	// cursor method to get all data
	public Cursor getAllRows() {
		Cursor c = db.query(false, DBHelper.TABLENAME, DBHelper.ALLROWS, null,
				null, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// transfer cursor data and append to arraylist
	public void transferCursorDataToArrayList(Cursor c) {
		int cursorLength = c.getCount();
		c.moveToFirst();

		for (int i = 0; i < cursorLength; i++) {
			String fromCursor = c.getString(c
					.getColumnIndex(DBHelper.KEY_TASKNAME));
			itemsArrayList.add(fromCursor);
			c.moveToNext();
		}
	}

	// viewholder class for optimal listview scrolling
	private class ViewHolder {
		CheckBox checkhere;
		ImageView delete;
		TextView texthere;
	}

	// custom adapter
	public class MyAdapter extends ArrayAdapter<String> {

		public MyAdapter(Context context, int resource, int textViewResourceId,
				List<String> list) {
			super(context, R.layout.item, R.id.texthere, list);
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			final ViewHolder viewHolder;

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item,
						parent, false);

				viewHolder = new ViewHolder();
				viewHolder.checkhere = (CheckBox) convertView
						.findViewById(R.id.checkhere);
				viewHolder.delete = (ImageView) convertView
						.findViewById(R.id.delete);
				viewHolder.texthere = (TextView) convertView
						.findViewById(R.id.texthere);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			String fromList = itemsArrayList.get(position);
			// this code block defines methods to truncate items more than 40
			// characters
			String shortened;
			int maxLength = appPreferences.retrieveMaxCharSettings();
			Spanned seeMore = Html.fromHtml("<b> See More...</b>");
			if (fromList.length() > maxLength) {
				shortened = fromList.substring(0, maxLength) + " " + seeMore;
			} else {
				shortened = fromList;
			}

			viewHolder.texthere.setText(shortened);

			if (fromList.contains("High:")) {
				viewHolder.texthere.setBackgroundColor(Color.RED);
			}
			if (fromList.contains("Medium:")) {
				viewHolder.texthere.setBackgroundColor(Color.YELLOW);
			}
			if (fromList.contains("Low:")) {
				viewHolder.texthere.setBackgroundColor(Color.GREEN);
			}

			if (fromList.contains(doneString)) {
				viewHolder.checkhere.setChecked(true);
				viewHolder.texthere.setPaintFlags(viewHolder.texthere
						.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				checkedItems++;
				totalcheckeditemsTextView.setText(totalcheckedString
						+ checkedItems);
				doneButton.setEnabled(true);
			} else {
				doneButton.setEnabled(false);
			}
			// listeners for item checkbox
			OnCheckedChangeListener checkherelistener = new OnCheckedChangeListener() {
				String fromtexthere = itemsArrayList.get(position);
				String flagged = fromtexthere + doneString;

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						getDatabase();

						db.execSQL("update TaskTable set TaskFlag=" + 1
								+ " where TaskName=" + "'" + fromtexthere
								+ "';");

						db.execSQL("update TaskTable set TaskName=" + "'"
								+ flagged + "'" + " where TaskName=" + "'"
								+ fromtexthere + "';");

						viewHolder.texthere.setPaintFlags(viewHolder.texthere
								.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

						viewHolder.texthere.setText(fromtexthere + doneString);
						itemsArrayList.set(position, fromtexthere + doneString);

						checkedItems++;
						totalcheckeditemsTextView.setText(totalcheckedString
								+ checkedItems);

						doneButton.setEnabled(true);

					} else {

						String retrievedString = viewHolder.texthere.getText()
								.toString();

						if (retrievedString.contains(doneString)) {
							String replacementString = retrievedString.replace(
									doneString, "");
							viewHolder.texthere.setText(replacementString);
							itemsArrayList.set(position, replacementString);
						}

						viewHolder.texthere.setPaintFlags(viewHolder.texthere
								.getPaintFlags()
								& (~Paint.STRIKE_THRU_TEXT_FLAG));
						getDatabase();
						db.execSQL("update TaskTable set TaskFlag=" + 0
								+ " where TaskName=" + "'" + flagged + "';");

						if (flagged.contains(doneString)) {
							String unflagged = flagged.replace(doneString, "");
							db.execSQL("update TaskTable set TaskName=" + "'"
									+ unflagged + "'" + " where TaskName="
									+ "'" + flagged + "';");
						}

						checkedItems--;
						totalcheckeditemsTextView.setText(totalcheckedString
								+ checkedItems);
						if (checkedItems <= 0) {
							doneButton.setEnabled(false);
						}
					}

				}
			};
			// listeners for delete button
			OnClickListener deletelistener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteItem(position);
					updateItemCount();
				}
			};

			viewHolder.checkhere.setOnCheckedChangeListener(checkherelistener);
			viewHolder.delete.setOnClickListener(deletelistener);

			return convertView;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.importtextfile, menu);
		getMenuInflater().inflate(R.menu.importcsvfile, menu);
		getMenuInflater().inflate(R.menu.exporttextfile, menu);
		getMenuInflater().inflate(R.menu.exportcsvfile, menu);
		getMenuInflater().inflate(R.menu.preferences, menu);
		getMenuInflater().inflate(R.menu.help, menu);
		getMenuInflater().inflate(R.menu.exit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.importtextfile) {
			try {
				return importTextFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (id == R.id.importcsvfile) {
			try {
				return importCSVFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (id == R.id.exporttextfile) {
			try {
				return exportToTextFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (id == R.id.exportcsvfile) {
			try {
				return exportToCSVFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (id == R.id.preferences) {

			return appPreferences.appPreferencesDialog();
		}
		if (id == R.id.help) {
			return optionsMenu.optionsHelp();
		}
		if (id == R.id.exit) {
			return exitApp();
		}

		return super.onOptionsItemSelected(item);
	}

	private boolean importTextFile() throws IOException {

		String filename = appPreferences.retrieveTextFileName();
		String path = "/sdcard/";
		// File file = new File("/sdcard/tasklistmanagerprotasks.txt");
		File file = new File(path + filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String readit = null;
		int index = 0;
		while ((readit = reader.readLine()) != null) {
			itemsArrayList.add(index, readit);

			adapter.notifyDataSetChanged();
			updateItemCount();
			index++;

			ContentValues values = new ContentValues();
			values.put(helper.KEY_TASKNAME, readit);
			values.put(DBHelper.KEY_TASKFLAG, 0);
			db.insert(helper.TABLENAME, null, values);
		}
		reader.close();
		deleteButton.setEnabled(true);
		Toast.makeText(this, "File " + filename + " succesfully imported.",
				Toast.LENGTH_SHORT).show();
		return true;
	}

	public boolean exportToTextFile() throws IOException {
		String filename = appPreferences.retrieveTextFileName();
		String path = "/sdcard/";
		File file = new File(path + filename);
		file.createNewFile();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < itemsArrayList.size(); i++) {

				writer.write(itemsArrayList.get(i) + "\r\n");
			}
			writer.flush();
			writer.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		}
		Toast.makeText(this, "File " + filename + " successfully exported.",
				Toast.LENGTH_SHORT).show();
		return true;

	}

	private boolean importCSVFile() throws IOException {

		String filename = appPreferences.retrieveTextFileName();
		String filenameCSV = filename.replace(".txt", ".csv");
		String path = "/sdcard/";
		File file = new File(path + filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String replace;
		String readit = null;
		int index = 0;
		while ((readit = reader.readLine()) != null) {
			replace = readit.replaceAll(",", "");
			itemsArrayList.add(index, replace);

			adapter.notifyDataSetChanged();
			updateItemCount();
			index++;

			ContentValues values = new ContentValues();
			values.put(helper.KEY_TASKNAME, readit);
			values.put(DBHelper.KEY_TASKFLAG, 0);
			db.insert(helper.TABLENAME, null, values);
		}
		reader.close();
		deleteButton.setEnabled(true);
		Toast.makeText(this, "File " + filenameCSV + " succesfully imported.",
				Toast.LENGTH_SHORT).show();
		return true;
	}

	public boolean exportToCSVFile() throws IOException {
		String filename = appPreferences.retrieveTextFileName();
		String filenameCSV = filename.replace(".txt", ".csv");
		String path = "/sdcard/";
		File file = new File(path + filenameCSV);
		file.createNewFile();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < itemsArrayList.size(); i++) {
				String[] fullString = itemsArrayList.get(i).split(":");
				String priority = fullString[0];
				String separator = ": ";
				String taskDetail = fullString[1];
				writer.append(priority);
				writer.append(separator);
				writer.append(',');
				writer.append(taskDetail);
				writer.append("\r\n");

			}
			writer.flush();
			writer.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		}
		Toast.makeText(this, "File " + filenameCSV + " successfully exported.",
				Toast.LENGTH_SHORT).show();
		return true;

	}

	public boolean exitApp() {
		String togglereminder = appPreferences
				.retrieveSharedPrefsforAutoDelete();
		AlertDialog.Builder confirmexit = new AlertDialog.Builder(
				MainActivity.this);

		confirmexit.setTitle("Exit the Application");
		if (togglereminder.equals("disabled")) {
			confirmexit
					.setMessage("Autodelete checked items disabled. All checked/marked items will not be deleted on app exit. You can enable this option in preferences option menu located in the upper right.");
		}
		if (togglereminder.equals("enabled")) {

			confirmexit
					.setMessage("Autodelete checked items enabled. All checked/marked items will be deleted on app exit. You can disable this option in preferences option menu located in the upper right.");
		}

		confirmexit

				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String fromsharedpreferences = appPreferences
										.retrieveSharedPrefsforAutoDelete();// convenience
								// string
								// to
								// get
								// data
								// from
								// autodelete
								// string
								if (fromsharedpreferences.equals("enabled")) {
									getDatabase();
									db.execSQL("delete from TaskTable where TaskFlag=1;");
									itemsArrayList.clear();
									Cursor c = getAllRows();
									transferCursorDataToArrayList(c);
									c.close();
									refresh();
									updateItemCount();
								}
								if (fromsharedpreferences.equals("disabled")) {
									Toast.makeText(
											MainActivity.this,
											"Checked/Marked items will remain.",
											Toast.LENGTH_SHORT).show();
								}
								moveTaskToBack(true);

							}

						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		confirmexit.show();
		return true;
	}

}
