package com.example.project.smartsilent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;


public class HelpActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		getLayoutInflater().inflate(R.layout.help, frameLayout);

		mDrawerList.setItemChecked(position, true);
		setTitle(listArray[position]);

	}
}
