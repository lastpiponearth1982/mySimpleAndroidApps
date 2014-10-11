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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.productivity.tasklistmanagerpro.R;
import com.productivity.tasklistmanagerpro.launcher.MainActivity;

//class that provides savepreferences for autodelete feature
public class TaskListSharedPreferences {
	SharedPreferences autoDeletesharedPreferences, maxCharLimitSharedPrefs;

	String autodelete = "autodelete";// sharedpreference key for setting to
										// toggle autodelete
	String maxChars = "maxChars";// sharedpreferences key for maxchar setting
	String currentLimitString = "Current Limit is:";

	SeekBar charlimiterseekbar;
	TextView currentLimitTextView;
	int currentprogressposition;
	int maxCharSharePrefsInt;

	Context context;
	SQLiteDatabase db;
	MainActivity main;
	ArrayList<String> itemsArrayList;

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

		Button savepreferences = (Button) preferences
				.findViewById(R.id.savepreferences);

		if (retrieveSharedPrefsforAutoDelete().contains("enabled")) {
			enabledisable.setChecked(true);
		} else {
			enabledisable.setChecked(false);
		}
		// listener for button to save and exit dialog
		OnClickListener saveandexit = new OnClickListener() {

			@Override
			public void onClick(View v) {
				preferences.dismiss();

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

		preferences.show();
		return true;
	}
}
