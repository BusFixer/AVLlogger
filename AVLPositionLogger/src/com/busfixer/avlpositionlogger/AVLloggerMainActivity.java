package com.busfixer.avlpositionlogger;

import java.io.File;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class AVLloggerMainActivity extends Activity {

	Button StopBut;
	Button StartBut;
	Button PauseBut;
	EditText VehID;
	EditText OppID;
	EditText BlockID;
	TextView mess;
	String vehicleid = "";
	String operatorid = "";
	String blockid = "";
	Context ctx = null;
	AlarmSetter as = null;
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	public static final String PREFERENCES_FILE = "avlpositionlogger";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.avllogger);
		@SuppressWarnings("deprecation")
		AVLloggerMainActivity PreAct = (AVLloggerMainActivity) getLastNonConfigurationInstance();
		if (PreAct != null) {
			as = PreAct.as;
		}
		VehID = (EditText) findViewById(R.id.Vehid);
		BlockID = (EditText) findViewById(R.id.BlockID);
		OppID = (EditText) findViewById(R.id.oppid);
		StopBut = (Button) findViewById(R.id.Stopbut);
		StartBut = (Button) findViewById(R.id.Startbut);
		PauseBut = (Button) findViewById(R.id.pausebut);
		mess = (TextView) findViewById(R.id.messages);
		ctx = this;
		alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ctx, LogData.class);
		alarmIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
		final Intent locaterIntent = new Intent(ctx, LocatorService.class);

		StartBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences p = ctx.getSharedPreferences(
						PREFERENCES_FILE, MODE_PRIVATE);

				vehicleid = VehID.getText().toString();
				blockid = BlockID.getText().toString();
				operatorid = OppID.getText().toString();

				if (!p.getBoolean("FFBOOL", false)
						&& !p.getBoolean("FLBOOL", false)
						&& !p.getBoolean("SFBOOL", false)
						&& !p.getBoolean("SLBOOL", false)) {
					mess.setText("Save to not set up try Menu -> Select values to Save");
					return;
				}
				// check for ftp to write to if selected
				if (p.getBoolean("FFBOOL", false)
						|| p.getBoolean("FLBOOL", false)) {
					if (p.getString("HOST", "").contentEquals("")) {
						mess.setText("FTP needs to be set up! try Menu -> FTP setup");
						return;
					}
				}
				// check for SD card to write to if selected
				if (p.getBoolean("SFBOOL", false)
						|| p.getBoolean("SLBOOL", false)) {
					if (!Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
						mess.setText("No SD card found");
						return;
					}
				}
				// be sure vehicle id filled in
				if (vehicleid.contentEquals("")
				// && operatorid.contentEquals("")
				// && blockid.contentEquals("")
				) {
					mess.setText("Need Vehicle ID to Start"); // or Operator or
																// Block to
																// Start");
					return;
				}
				Integer imins = p.getInt("MINUTES", 2);
				mess.setText("Started Logging every " + Integer.toString(imins)
						+ " minutes"); // + Sminutes
				savePreferences(); // ctx
				alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, 1000 * 3, // first
																				// time
																				// in
																				// 3
																				// seconds
						1000 * 60 * imins, // minute intervals
						alarmIntent);

				// use this to start and trigger a service

				// potentially add data to the intent
				locaterIntent.putExtra("UPDATE_INTERVAL",
						p.getInt("SECONDS", 60)); // put seconds here
				locaterIntent.setAction("startListening");
				ctx.startService(locaterIntent);

			}
		}); // set on click StartBut

		PauseBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				SharedPreferences p = ctx.getSharedPreferences(
						PREFERENCES_FILE, MODE_PRIVATE);
				SharedPreferences.Editor e = p.edit();

				if (p.getBoolean("RUNNING", false)) {

					alarmMgr.cancel(alarmIntent);

					locaterIntent.setAction("stopListening");
					ctx.startService(locaterIntent);

					e.putBoolean("PAUSED", true);
					e.putBoolean("RUNNING", false);
					e.commit();
				}
				mess.setText("Paused");
			}
		});

		StopBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				SharedPreferences p = ctx.getSharedPreferences(
						PREFERENCES_FILE, MODE_PRIVATE);
				SharedPreferences.Editor e = p.edit();

				if (p.getBoolean("RUNNING", false)) {

					alarmMgr.cancel(alarmIntent);
					locaterIntent.setAction("stopListening");
					ctx.startService(locaterIntent);
				}
				if (p.getBoolean("RUNNING", false)
						|| p.getBoolean("PAUSED", false)) {

					// clearPreference();
					if (!p.getBoolean("VIDBOOL", false)) {
						e.putString("VEHICLE", "");
					}
					if (!p.getBoolean("BLKBOOL", false)) {
						e.putString("BLOCK", "");
					}
					if (!p.getBoolean("OIDBOOL", false)) {
						e.putString("OPERATOR", "");
					}
					e.putBoolean("PAUSED", false);
					e.putBoolean("RUNNING", false);
					e.commit();
					getPreference(); // ctx
					VehID.setText(vehicleid);
					BlockID.setText(blockid);
					OppID.setText(operatorid);
				}
				mess.setText("Stopped");
			}
		}); // set on click StopBut

	}// END oncreate

	// ***** onRetainNonConfigurationInstance ********
	@Override
	public Object onRetainNonConfigurationInstance() {
		return (this);
	}

	// ********* onResume *******
	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences p = ctx.getSharedPreferences(PREFERENCES_FILE,
				MODE_PRIVATE);
		VehID.setText(p.getString("VEHICLE", ""));
		BlockID.setText(p.getString("BLOCK", ""));
		OppID.setText(p.getString("OPERATOR", ""));
		if (p.getBoolean("PAUSED", false)) {
			mess.setText("Paused");
		}
		if (p.getBoolean("RUNNING", false)) {
			mess.setText("Logging position every "
					+ Integer.toString(p.getInt("MINUTES", 2)) + " minutes");
		}
	}

	// **** savePreferences *****************
	public boolean savePreferences() { // Context c

		SharedPreferences p = ctx.getSharedPreferences(PREFERENCES_FILE,
				MODE_PRIVATE);
		SharedPreferences.Editor e = p.edit();

		e.putString("OPERATOR", operatorid);
		e.putString("VEHICLE", vehicleid);
		e.putString("BLOCK", blockid);
		e.putBoolean("RUNNING", true);
		e.putBoolean("PAUSED", false);

		return (e.commit());
	}

	// **** getPreferences *****************
	public boolean getPreference() { // Context c

		SharedPreferences p = ctx.getSharedPreferences(PREFERENCES_FILE,
				MODE_PRIVATE);
		operatorid = p.getString("OPERATOR", "");
		vehicleid = p.getString("VEHICLE", "");
		blockid = p.getString("BLOCK", "");

		return (p.contains("VEHICLE"));
	}

	// ***** checkPath ****************
	public boolean checkpath(String path) {
		boolean ret = true;

		SharedPreferences p = ctx.getSharedPreferences(PREFERENCES_FILE,
				MODE_PRIVATE);
		if (p.getBoolean("SFBOOL", false) || p.getBoolean("FLBOOL", false)) {
			File sdcard = Environment.getExternalStorageDirectory();
			File sdDir = new File(sdcard, "/" + path);
			if (!sdDir.exists()) {
				ret = sdDir.mkdirs();
			}
		}
		return (ret);
	}

	// **** clearPreference ******************
	public boolean clearPreference() { // Context c

		SharedPreferences p = ctx.getSharedPreferences(PREFERENCES_FILE,
				MODE_PRIVATE);
		SharedPreferences.Editor e = p.edit();
		if (!p.getBoolean("VIDBOOL", false)) {
			e.putString("VEHICLE", "");
		}
		if (!p.getBoolean("BLKBOOL", false)) {
			e.putString("BLOCK", "");
		}
		if (!p.getBoolean("OIDBOOL", false)) {
			e.putString("OPERATOR", "");
		}
		e.putBoolean("PAUSED", false);
		e.putBoolean("RUNNING", false);

		return (e.commit());
	}

	// ***************************************************
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.avllogger_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuftpsetup:

			final EditText host;
			final EditText name;
			final EditText pwrd;
			final EditText prt;
			final EditText dir;
			final EditText mins;
			final EditText secs;

			LayoutInflater layoutInflater = LayoutInflater.from(ctx);
			// set ftpsetup.xml to be the layout file of the alertdialog builder
			View ftpsetupView = layoutInflater.inflate(R.layout.ftpsetup, null);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					ctx);
			alertDialogBuilder.setView(ftpsetupView);

			host = (EditText) ftpsetupView.findViewById(R.id.setupHost);
			name = (EditText) ftpsetupView.findViewById(R.id.setupUname);
			pwrd = (EditText) ftpsetupView.findViewById(R.id.setupPword);
			dir = (EditText) ftpsetupView.findViewById(R.id.setupDirectory);
			prt = (EditText) ftpsetupView.findViewById(R.id.setupPort);
			mins = (EditText) ftpsetupView.findViewById(R.id.setupLogmin);
			secs = (EditText) ftpsetupView.findViewById(R.id.setupGPSsec);
			SharedPreferences p = ctx.getSharedPreferences(PREFERENCES_FILE,
					MODE_PRIVATE);
			// populate with saved values
			host.setText(p.getString("HOST", ""));
			name.setText(p.getString("NAME", ""));
			pwrd.setText(p.getString("PWRD", ""));
			dir.setText(p.getString("DIR", ""));
			prt.setText(Integer.toString((p.getInt("PORT", 21))));
			mins.setText(Integer.toString((p.getInt("MINUTES", 2))));
			secs.setText(Integer.toString((p.getInt("SECONDS", 30))));

			alertDialogBuilder

					.setCancelable(false)
					.setTitle("FTP Set up")

					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							})

					.setPositiveButton("Save",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {

									// get user input and save it to preference
									SharedPreferences p = ctx
											.getSharedPreferences(
													PREFERENCES_FILE,
													MODE_PRIVATE);
									SharedPreferences.Editor e = p.edit();
									e.putString("HOST", host.getText()
											.toString());
									e.putString("NAME", name.getText()
											.toString());
									e.putString("PWRD", pwrd.getText()
											.toString());
									e.commit();
									if (checkpath(dir.getText().toString())) {
										e.putString("DIR", dir.getText()
												.toString());
										e.commit();
									} else {
										mess.setText("Directory not working, value not changed");
									}
									// make sure minutes is bigger than seconds
									try {
										e.putInt("PORT", Integer.parseInt(prt
												.getText().toString()));
										e.putInt("MINUTES", Integer
												.parseInt(mins.getText()
														.toString()));
										e.putInt("SECONDS", Integer
												.parseInt(secs.getText()
														.toString()));
										if (Integer.parseInt(secs.getText()
												.toString()) > Integer
												.parseInt(mins.getText()
														.toString()) * 60) {
											mess.setText("Minutes * 60 MUST be bigger than Seconds, Minutes and Seconds not changed");
											return;
										}
									} catch (NumberFormatException nfe) {
										mess.setText("invalid character! Port, Minutes or Seconds not changed");
										return;
									}
									e.commit();
									return;
								}
							})

					.setNeutralButton("Clear All",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									SharedPreferences p = ctx
											.getSharedPreferences(
													PREFERENCES_FILE,
													MODE_PRIVATE);
									SharedPreferences.Editor e = p.edit();
									e.putString("VEHICLE", "");
									e.putString("BLOCK", "");
									e.putString("OPERATOR", "");
									e.putString("HOST", "");
									e.putString("NAME", "");
									e.putString("PWRD", "");
									e.putString("DIR", "");
									e.putInt("PORT", 21);
									e.putInt("MINUTES", 2);
									e.putInt("SECONDS", 30);
									e.commit();
									return;
								}
							});

			// create an alert dialog
			AlertDialog alertD = alertDialogBuilder.create();
			alertD.show();
			return true;
			// **
		case R.id.menusaveset:
			LayoutInflater layoutinflater = LayoutInflater.from(ctx);
			View savedialogView = layoutinflater.inflate(R.layout.savedialog,
					null);
			AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(
					ctx);
			alertdialogBuilder.setView(savedialogView);

			final CheckBox vid;
			final CheckBox blk;
			final CheckBox oid;
			final CheckBox ftpfile;
			final CheckBox ftplog;
			final CheckBox sdfile;
			final CheckBox sdlog;

			vid = (CheckBox) savedialogView.findViewById(R.id.checkVehid);
			blk = (CheckBox) savedialogView.findViewById(R.id.checkBlock);
			oid = (CheckBox) savedialogView.findViewById(R.id.checkOp);
			ftpfile = (CheckBox) savedialogView
					.findViewById(R.id.checkupdateFTPfile);
			ftplog = (CheckBox) savedialogView
					.findViewById(R.id.checkupdateFTPlog);
			sdfile = (CheckBox) savedialogView
					.findViewById(R.id.checkupdateSDfile);
			sdlog = (CheckBox) savedialogView
					.findViewById(R.id.checkupdateSDlog);
			SharedPreferences sp = ctx.getSharedPreferences(PREFERENCES_FILE,
					MODE_PRIVATE);
			vid.setChecked(sp.getBoolean("VIDBOOL", false));
			blk.setChecked(sp.getBoolean("BLKBOOL", false));
			oid.setChecked(sp.getBoolean("OIDBOOL", false));
			ftpfile.setChecked(sp.getBoolean("FFBOOL", false));
			ftplog.setChecked(sp.getBoolean("FLBOOL", false));
			sdfile.setChecked(sp.getBoolean("SFBOOL", false));
			sdlog.setChecked(sp.getBoolean("SLBOOL", false));

			alertdialogBuilder

					.setCancelable(false)
					.setTitle("Choose Fields to Save")
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							})

					.setPositiveButton("Save",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int id) {

									SharedPreferences p = ctx
											.getSharedPreferences(
													PREFERENCES_FILE,
													MODE_PRIVATE);
									SharedPreferences.Editor e = p.edit();
									e.putBoolean("VIDBOOL", vid.isChecked());
									e.putBoolean("BLKBOOL", blk.isChecked());
									e.putBoolean("OIDBOOL", oid.isChecked());
									e.putBoolean("FFBOOL", ftpfile.isChecked());
									e.putBoolean("FLBOOL", ftplog.isChecked());
									e.commit();
									if (sdfile.isChecked() || sdlog.isChecked()) {
										if (checkpath(p.getString("DIR", ""))) {
											e.putBoolean("SFBOOL",
													sdfile.isChecked());
											e.putBoolean("SLBOOL",
													sdlog.isChecked());
											e.commit();
										} else {
											mess.setText("Directory not working, value not changed");
										}
									}
								}
							}); // End save

			AlertDialog alert = alertdialogBuilder.create();
			alert.show();
			return true;

		case R.id.menuHelp:
			Intent intent = (new Intent(this, HelpInfo.class));
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
