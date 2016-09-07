package com.yvolohov.miner;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

/**
 * Класс Activity справки по игре
 * @author yvolohov
 */
public class Rules extends Activity {
	
	/**
	 * При создании Activity
	 */		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rules);
		WebView wvHelp = (WebView) findViewById(R.id.wvHelp);
				
		Configuration sysConfig = getResources().getConfiguration();
		String language = sysConfig.locale.getLanguage();
		String helpUrl;
		
		/*if (language.equals("en")) 
			helpUrl = "file:///android_asset/help_en.html"; */
		
		if (language.equals("uk")) 
			helpUrl = "file:///android_asset/help_uk.html";
		else 
			helpUrl = "file:///android_asset/help_ru.html";
		
		wvHelp.loadUrl(helpUrl);
	}
	
	/**
	 * Обработчик нажатия на кнопки
	 * @param v кнопка
	 */	
	public void onClick(View v) {
		
		switch (v.getId()) {
		
		case R.id.btnOK:
			finish();
			break;
		}
	}	
}
