package com.busfixer.avlpositionlogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class SubHelp extends Activity {
	int StringResourceID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subhelp);

		// Read the arguments from the Intent object.
		Intent in = getIntent();
		StringResourceID = in.getIntExtra(HelpInfo.HELP_TEXT_ID, 0);
		if (StringResourceID <= 0)
			StringResourceID = R.string.nohelp;

		TextView textView = (TextView) findViewById(R.id.topic_text);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		textView.setText(Html.fromHtml(getString(StringResourceID)));
	}
}
