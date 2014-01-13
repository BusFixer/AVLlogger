package com.busfixer.avlpositionlogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
//import android.location.Location;

@SuppressLint({ "SimpleDateFormat", "UseValueOf" })
public class LogData extends BroadcastReceiver {
	Context cntx = null;
	boolean append = true;

	public static final String PREFERENCES_FILE = "avlpositionlogger";
	private FtpUploadAsyncTask UploadData = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		cntx = context;

		Toast.makeText(cntx, "Logging Position", Toast.LENGTH_LONG).show();

		SharedPreferences p = cntx.getSharedPreferences(PREFERENCES_FILE, 0);

		final String blockid = p.getString("BLOCK", "");
		final String operatorid = p.getString("OPERATOR", "");
		final String vehicleid = p.getString("VEHICLE", "");

		long rightnow = 0;
		String date = "";
		String outString = "";
		String TimeStampString = "";

		Date devicetime = new Date();
		rightnow = devicetime.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		TimeStampString = sdf.format(new Date(rightnow));
		sdf.applyPattern("MM-dd-yy");
		date = sdf.format(new Date(rightnow));

		outString = TimeStampString + "," + vehicleid + "," + blockid + ","
				+ operatorid + "," + GpsReceiver.GetData() + '\n';

		// lets write to FTP or SD
		UploadData = new FtpUploadAsyncTask();
		UploadData.execute(outString, vehicleid, date);
	} // end on receive

	class FtpUploadAsyncTask extends AsyncTask<String, Void, Void> {

		File sdcard = Environment.getExternalStorageDirectory();
		File path = cntx.getFilesDir();
		String pathname = path.getPath();
		String fileName = "";
		String logfileName = "";

		@Override
		protected Void doInBackground(String... instrings) {

			SharedPreferences p = cntx
					.getSharedPreferences(PREFERENCES_FILE, 0);

			final String Host = p.getString("HOST", "");
			final String Login = p.getString("NAME", "");
			final String Passw = p.getString("PWRD", "");
			final String Dir = p.getString("DIR", "");
			final Integer Port = p.getInt("PORT", 21);
			final boolean ftpfile = p.getBoolean("FFBOOL", false);
			final boolean ftplog = p.getBoolean("FLBOOL", false);
			final boolean sdfile = p.getBoolean("SFBOOL", false);
			final boolean sdlog = p.getBoolean("SLBOOL", false);

//			 String replys = ""; // uncomment these for debugging FTP setup

			if (ftplog || sdlog) {
				logfileName = instrings[1] + "_" + instrings[2] + ".LOG";
			}
			fileName = instrings[1] + ".DAT";

			if (ftpfile || sdfile) {
				createAndWriteNewFile(path, fileName, instrings[0]);
			}
			File file = new File(path, "/" + fileName);

			if (ftplog || ftpfile) {
				BusfixerFTP ftpagent = new BusfixerFTP();

				if (ftpagent.ftpConnect(Host, Login, Passw, Port)) {
//					replys = "Connect, " + ftpagent.getReplyString() + '\n';
					ftpagent.ftpChangeDirectory(Dir);
//					 replys = replys + "Change dir, " + ftpagent.getReplyString() + '\n';

					if (ftplog) {

						if (ftpagent.checkFileExists(logfileName)) {
//							replys = replys + "check Exists, " + ftpagent.getReplyString() + '\n';
							ftpagent.AppendTextToFtpFile(logfileName,
									instrings[0]);
//							replys = replys + "Append, " + ftpagent.getReplyString() + '\n';

						} else {
							// file not on FTP site
							// see if we have it locally
							if (!file.exists()) {
								createAndWriteNewFile(path, fileName,
										instrings[0]);
							}
							ftpagent.ftpUpload(pathname + "/" + fileName,
									logfileName);
//							replys = replys + "Upload, " + ftpagent.getReplyString() + '\n';
						}
					} // end if log
					if (ftpfile) {
						if (ftpagent.checkFileExists(fileName)) {
//							replys = replys + "Check Exists, " + ftpagent.getReplyString() + '\n';
							ftpagent.ftpRemoveFile(fileName);
//							replys = replys + "Remove, " + ftpagent.getReplyString() + '\n';
						}
						ftpagent.ftpUpload(pathname + "/" + fileName, fileName);
//						replys = replys + "Upload, " + ftpagent.getReplyString() + '\n';
					}// End if file
					ftpagent.ftpDisconnect();
//					replys = replys + "Disconnect, " + ftpagent.getReplyString() + '\n';
				}
//				else{ replys = replys + "Connection Failed " + '\n';}
			}// END if ftplog or ftpfile

			if (sdfile) {
				File sddatfile = new File(sdcard, "/" + Dir + "/" + fileName);

				if (sddatfile.exists()) {
					sddatfile.delete();
				}
				createAndWriteNewFile(sdcard, "/" + Dir + "/" + fileName,
						instrings[0]);
			}
			if (sdlog) {
				appendToFileOnSD(Dir + "/" + logfileName, instrings[0], 128);
			}
//			 appendToFileOnSD(Dir + "/" + "Reply.txt", replys, 512); // not
			// checking for SD card
			return null;
		} // end doInBackground

		@Override
		// close dialog at end of process
		protected void onPostExecute(Void result) {
			super.onPostExecute(null);
			// empty cache
			File path = cntx.getFilesDir();
			File[] files = path.listFiles();
			if (files != null) {
				for (File file : files)
					file.delete();
			}
		}

		// ************ createNewFile unbuffered **********
		private boolean createAndWriteNewFile(File filepath, String name,
				String data) {

			boolean ret = true;

			File file = new File(filepath, name);
			try {
				FileWriter fWriter = new FileWriter(file, true);
				fWriter.write(data);
				fWriter.flush();
				fWriter.close();
			} catch (Exception e) {
				ret = false;
			}
			return ret;
		}

	}// END AsyncTask

	// ***************
	private void ensure(String Filename) {
		File sdcard = Environment.getExternalStorageDirectory();
		File file = new File(sdcard, "/" + Filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Toast.makeText(cntx, "Error creating file!", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	/**
	 * appendToFileOnSD(String FileName, String outText, int buffersize) String
	 * FileName may include path/FileName the path must exist String outText is
	 * the text that will be appended to the file int buffersize is the maximum
	 * number of characters that will be used
	 */
	private void appendToFileOnSD(String FileName, String outText,
			int buffersize) {
		ensure(FileName);
		File sdcard = Environment.getExternalStorageDirectory();
		File file = new File(sdcard, "/" + FileName);
		try {
			FileWriter fw = new FileWriter(file, append);
			BufferedWriter bw = new BufferedWriter(fw, buffersize);
			bw.write(outText);
			// bw.newLine();
			bw.close();
		} catch (IOException e) {
			Toast.makeText(cntx, "Error writing to file on SD!",
					Toast.LENGTH_LONG).show();
		}
	}

}
