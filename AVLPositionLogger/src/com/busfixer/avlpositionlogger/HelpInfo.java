package com.busfixer.avlpositionlogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HelpInfo extends Activity {

	static public final String HELP_TEXT_ID = "text_id";
	int textId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainhelp);
	}

	public void onClickHelp(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.mainbut:
			textId = R.string.main_info_html;
			break;
		case R.id.ftphelpbut:
			textId = R.string.ftp_info_html;
			break;
		case R.id.savesetbut:
			textId = R.string.SaveSettings_info_html;
			break;
		case R.id.aboutbut:
			textId = R.string.About_info_html;
			break;
		default:
			break;
		}

		if (textId >= 0) {
			Intent intent = (new Intent(this, SubHelp.class));
			intent.putExtra(HELP_TEXT_ID, textId);
			startActivity(intent);
		}
	}
}
