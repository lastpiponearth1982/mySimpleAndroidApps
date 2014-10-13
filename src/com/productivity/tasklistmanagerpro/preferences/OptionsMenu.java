package com.productivity.tasklistmanagerpro.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class OptionsMenu {

	// this long text is the text displayed when pressing the help options menu
	String helpString = 
			"1. This help menu can be accessed from the top right corner. \n"
			+ "2. To add an item, click on the Add button after entering text in the inputfield. "
			+ "Do not remove the priority tags as it disables adding entries. \n"
			+ "3. There are three priority settings that can be set for any task. "
			+ "Low, Medium and High. Low is a low priority task, Medium is a medium priority task and High is a high priority task. "
			+ "You can set the priority by clicking on the drop down spinner found at the bottom of the UI. "
			+ "Priorities have colors. Low is Green, Medium is Yellow, Red is High. These values will be appended to task for easy viewing. \n"
			+ "4. For each item in the list, the maximum number of characters shown is set to 40 characters only. "
			+ "Beyond 40 characters the text displayed will be truncated. To view the entire item, long click on the item and "
			+ "its entire contents will be displayed in a dialog. You can also mark the item as done from the display dialog. "
			+ " Also the character limit of 40 can be set to any setting from 0 up to 200 characters in the preferences menu.\n"
			+ "5. To overwrite an existing item, click on the item and its contents will be displayed in the inputfield below. "
			+ "You can then edit its contents in the inputfield and click the Overwrite button. "
			+ "A confirmation dialog will appear before any changes can be made. \n"
			+ "6. To change the priority of a task without altering its contents, first set the spinner to a desired priority value."
			+ "You can do this by clicking on the dropdown spinner and choosing the preferred priority value. "
			+ "Click on the item you want to edit for it to appear on the inputfield. Enter your changes and press Overwrite and confirm your changes. \n"
			+ "7. To clear all items in the inputfield, click Clear. \n"
			+ "8. When the inputfield is cleared, there is no way to get the priority text in the inputfield. "
			+ "This can be restored by clicking the spinner and setting it to another setting and reverting back to your desired setting. \n"
			+ "9. To mark an item as completed, press the checkbox on the left to set it as done. Checked items are crossed with a text strikethrough. "
			+ "By default checked items are not deleted on app exit. This option can be enabled in the preferences setting. \n"
			+ "10. Items marked/checked as completed are appended with a tag of <DONE> on app restart. On most occasions this does not occur since apps "
			+ "will only be restarted on two conditions. One is when the Android device is restarted and the app is run again. Another is when the app is closed "
			+ "due to insufficient resources and run again. Items with a tag of <DONE> will have a checked mark and a text strikethrough . "
			+ "The <DONE> tag can be removed by unchecking the checkbox and restarting the app, but this may be unnecessary. \n"
			+ "11. You can delete an item by clicking the X button beside the checkbox. "
			+ "This will lead to a confirmation dialog. Press OK to delete and cancel to cancel item deletion. \n"
			+ "12. You can delete marked/checked items by clicking Delete Done. This button is disabled by default "
			+ "but is enabled when there is at least 1 marked/checked item. A confirmation dialog will open before any action is done. \n"
			+ "13. To delete all items, click Delete All. A confirmation dialog will open to confirm the action. \n"
			+ "14. You can import text files containing tasks. The app will immediately recognize it as a task entry as long as it has the tags 'Low: ', 'Medium: ' , and 'High:'. "
			+ "Otherwise, you can simply import it and edit changes to the correct format. You can also import CSV files but they also have to have the tags.\n"
			+ "15. To export tasks to text or CSV file, go to upper right and select Export Text File or Export CSV file. The exported file can be found in the default location of your SD card. \n"
			+ "16. Note that when performing import or export of tasks via text or CSV file, the filename should be specified in the preferences menu otherwise the default filename will be used. \n"
			+ "17. After a text or CSV file has been imported, they can be exported to the same file. This feature is handy after importing a file and then performing changes such as adding "
			+ "or removing items to the list before exporting them to text or CSV. \n"
			+ "18. To exit the application, click Exit on the ActionBar options menu at top right corner. A confirmation dialog will appear to confirm exit. "
			+ "By default all marked/checked items are not deleted on Exit. This can be enabled in the options menu located in the ActionBar at the top right. \n";

	Context context;

	public OptionsMenu(Context context) {
		this.context = context;
	}

	public boolean optionsHelp() {
		AlertDialog.Builder apphelp = new AlertDialog.Builder(context);

		apphelp.setTitle("How to use the Application");

		apphelp.setMessage(helpString).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		apphelp.show();
		return true;
	}

}
