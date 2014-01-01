package com.busfixer.avlpositionlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GpsReceiver extends BroadcastReceiver {
	static String Data = "";

	@Override
	public void onReceive(Context context, Intent intent) {

		Bundle extra = intent.getExtras();
		Data = extra.getString("DAT");
	}

	public static String GetData() {
		return Data;
	}
}