package com.productivity.tasklistmanagerpro.preferences;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.productivity.tasklistmanagerpro.R;
import com.productivity.tasklistmanagerpro.launcher.MainActivity;

//class that provides savepreferences for autodelete feature
public class TaskListSharedPreferences {
	SharedPreferences autoDeletesharedPreferences, maxCharLimitSharedPrefs,
			textFileName;

	String autodelete = "autodelete";// sharedpreference key for setting to
										// toggle autodelete
	String maxChars = "maxChars";// sharedpreferences key for maxchar setting
	String fileName = "fileName";// sharedpreferences key for filename
	String currentLimitString = "Current Limit is:";

	SeekBar charlimiterseekbar;
	TextView currentLimitTextView, nameofFile;
	int currentprogressposition;
	int maxCharSharePrefsInt;
	String currentFileName;
	String fileNameInputFieldInfo = "Current name of file is:";

	Context context;
	SQLiteDatabase db;
	MainActivity main;
	ArrayList<String> itemsArrayList;

	EditText filenameinputfield;

	public TaskListSharedPreferences(Context context) {
		this.context = context;
	}

	// retrieved saved autodelete setting
	public String retrieveSharedPrefsforAutoDelete() {
		autoDeletesharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String autodeleteSharePrefsString = autoDeletesharedPreferences
				.getString(autodelete, "disabled");
		return autodeleteSharePrefsString;
	}

	// save autodelete features to enable
	public void saveAutoDeleteSettingtoEnable() {
		Toast.makeText(context, "Autodelete enabled", Toast.LENGTH_SHORT)
				.show();
		Editor edit = autoDeletesharedPreferences.edit();
		edit.putString(autodelete, "enabled");
		edit.commit();
	}

	// save autodelete features to disable
	public void saveAutoDeleteSettingtoDisable() {
		Toast.makeText(context, "Autodelete enabled", Toast.LENGTH_SHORT)
				.show();
		Editor editAutoDelete = autoDeletesharedPreferences.edit();
		editAutoDelete.putString(autodelete, "disabled");
		editAutoDelete.commit();
	}

	// retrieved saved filename
	public String retrieveTextFileName() {
		textFileName = PreferenceManager.getDefaultSharedPreferences(context);
		String textFileNameString = textFileName.getString(fileName,
				"tasklistmanagerprotasks.txt");
		Toast.makeText(context, "Filename for export is " + textFileNameString,
				Toast.LENGTH_SHORT).show();
		return textFileNameString;
	}

	// save filename
	public void savedTextFileName() {
		String newFileName = filenameinputfield.getText().toString();
		String extension = ".txt";
		String fullFileName = newFileName + extension;
		textFileName = PreferenceManager.getDefaultSharedPreferences(context);
		Editor textFileNameEditor = textFileName.edit();
		textFileNameEditor.putString(fileName, fullFileName);
		textFileNameEditor.commit();

	}

	// retrieved saved maxChar setting
	public int retrieveMaxCharSettings() {
		maxCharLimitSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int maxCharSharePrefsInt = maxCharLimitSharedPrefs.getInt(maxChars, 40);
		return maxCharSharePrefsInt;
	}

	// retrieved saved autodelete setting
	public void savedMaxCharSettings() {
		Toast.makeText(context,
				"Character Limit set to: " + currentprogressposition,
				Toast.LENGTH_SHORT).show();
		Editor editMaxChar = maxCharLimitSharedPrefs.edit();
		editMaxChar.putInt(maxChars, currentprogressposition);
		editMaxChar.commit();
	}

	// UI for displaying info about autodelete and setting it to
	// enabled/disabled
	public boolean appPreferencesDialog() {
		final Dialog preferences = new Dialog(context);
		preferences.setContentView(R.layout.preferencesdialog);
		preferences.setTitle("Preferences");
		preferences.setCancelable(false);
		preferences.setCanceledOnTouchOutside(false);

		final CheckBox enabledisable = (CheckBox) preferences
				.findViewById(R.id.enabledisable);

		currentLimitTextView = (TextView) preferences
				.findViewById(R.id.currentlimit);
		charlimiterseekbar = (SeekBar) preferences
				.findViewById(R.id.charlimiterseekbar);
		filenameinputfield = (EditText) preferences
				.findViewById(R.id.filenameinputfield);
		nameofFile = (TextView) preferences.findViewById(R.id.nameofFile);
		currentFileName = retrieveTextFileName();
		nameofFile.setText(fileNameInputFieldInfo + currentFileName);

		Button savepreferences = (Button) preferences
				.findViewById(R.id.savepreferences);
		Button cancel = (Button) preferences.findViewById(R.id.cancel);

		if (retrieveSharedPrefsforAutoDelete().contains("enabled")) {
			enabledisable.setChecked(true);
		} else {
			enabledisable.setChecked(false);
		}
		// listener for button to save and exit dialog
		OnClickListener saveandexit = new OnClickListener() {

			@Override
			public void onClick(View v) {
				savedTextFileName();
				nameofFile.setText(fileNameInputFieldInfo + currentFileName);
				preferences.dismiss();
			}
		};

		// listener for cancel and don't do any changes
		OnClickListener cancelchanges = new OnClickListener() {

			@Override
			public void onClick(View v) {
				preferences.dismiss();
				Toast.makeText(context, "No changes have been made",
						Toast.LENGTH_SHORT).show();
			}
		};

		// checkchangedlistener for enable/disable of autodelete function
		OnCheckedChangeListener toggle = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					saveAutoDeleteSettingtoEnable();
				} else {
					saveAutoDeleteSettingtoDisable();
				}

			}

		};

		OnSeekBarChangeListener setlimit = new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				savedMaxCharSettings();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				currentLimitTextView.setText(currentLimitString + progress);
				currentprogressposition = progress;
			}
		};

		// adding listeners to preferences dialog UI
		enabledisable.setOnCheckedChangeListener(toggle);

		charlimiterseekbar.setOnSeekBarChangeListener(setlimit);

		int maxCharSharePrefsInt = maxCharLimitSharedPrefs.getInt(maxChars, 40);
		charlimiterseekbar.setProgress(maxCharSharePrefsInt);
		savepreferences.setOnClickListener(saveandexit);
		cancel.setOnClickListener(cancelchanges);
		preferences.show();
		return true;
	}
}
