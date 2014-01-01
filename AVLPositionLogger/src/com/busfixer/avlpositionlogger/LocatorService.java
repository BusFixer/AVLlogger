package com.busfixer.avlpositionlogger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocatorService extends Service implements LocationListener {

	private LocationManager locationManager;
	private Context ctx = this;

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {

		if (intent.getAction().equals("startListening")) {
			locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					intent.getIntExtra("UPDATE_INTERVAL", 60), 0, this);
		} else {
			if (intent.getAction().equals("stopListening")) {
				locationManager.removeUpdates(this);
				locationManager = null;
			}
		}

		return START_STICKY;

	}

	@Override
	public void onLocationChanged(Location location) {

		Intent i = new Intent(ctx, GpsReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putString(
				"DAT",
				String.valueOf(location.getLongitude()) + ","
						+ String.valueOf(location.getLatitude()) + ","
						+ String.valueOf(location.getTime())// );
						+ "," + String.valueOf(location.getAccuracy()));

		i.putExtras(bundle);
		sendBroadcast(i);

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
