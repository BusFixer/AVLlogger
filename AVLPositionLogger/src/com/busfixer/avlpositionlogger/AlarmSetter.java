package com.busfixer.avlpositionlogger;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmSetter {

	private Integer secs;
	private Integer Mins;

	protected Context mContext;

	public AlarmSetter(Integer Minutes, Integer Seconds, Context ctx) {

		secs = Seconds;
		Mins = Minutes;
		mContext = ctx;
	};

	public void sendRepeatingAlarm() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, secs);

		// Get an intent to invoke
		Intent intent = new Intent(this.mContext, LogData.class);

		PendingIntent pi = this.getDistinctPendingIntent(intent, 2);
		// Schedule the alarm!
		AlarmManager am = (AlarmManager) this.mContext
				.getSystemService(Context.ALARM_SERVICE);

		am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				Mins * 60 * 1000, pi);
	}

	protected PendingIntent getDistinctPendingIntent(Intent intent,
			int requestId) {

		PendingIntent pi = PendingIntent.getBroadcast(mContext, // context
				requestId, // request id
				intent, // intent to be delivered
				0); // pending intent flags
		return pi;
	}

	public void cancelRepeatingAlarm() {
		// Get an intent to invoke
		Intent intent = new Intent(this.mContext, LogData.class);

		PendingIntent pi = this.getDistinctPendingIntent(intent, 2);

		// Schedule the alarm!
		AlarmManager am = (AlarmManager) this.mContext
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
	}

}
