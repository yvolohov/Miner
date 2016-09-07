package com.yvolohov.miner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Класс Activity, отбражающей игровые рекорды 
 * @author yvolohov
 */
public class Results extends Activity {
	
	private GlobalData globalData;
	private TextView tvSimpleSqu, tvSimpleHex;
	private TextView tvMediumSqu, tvMediumHex;
	private TextView tvHardSqu, tvHardHex;

	/**
	 * При создании Activity
	 */		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);
		globalData = (GlobalData) getApplicationContext();
		tvSimpleSqu = (TextView) findViewById(R.id.tvSimpleSqu);
		tvSimpleHex = (TextView) findViewById(R.id.tvSimpleHex);
		tvMediumSqu = (TextView) findViewById(R.id.tvMediumSqu);
		tvMediumHex = (TextView) findViewById(R.id.tvMediumHex);
		tvHardSqu = (TextView) findViewById(R.id.tvHardSqu);
		tvHardHex = (TextView) findViewById(R.id.tvHardHex);
		loadRecords();
	}

	/**
	 * Выводит рекорды на экран
	 */
	private void loadRecords() {
		tvSimpleSqu.setText(GameField.getTimeAsString(globalData
				.getRecord(GameField.SIMPLE_LEVEL)));
		tvSimpleHex.setText(GameField.getTimeAsString(globalData
				.getRecord(GameField.SIMPLE_LEVEL_HEX)));
		tvMediumSqu.setText(GameField.getTimeAsString(globalData
				.getRecord(GameField.MEDIUM_LEVEL)));
		tvMediumHex.setText(GameField.getTimeAsString(globalData
				.getRecord(GameField.MEDIUM_LEVEL_HEX)));
		tvHardSqu.setText(GameField.getTimeAsString(globalData
				.getRecord(GameField.HARD_LEVEL)));
		tvHardHex.setText(GameField.getTimeAsString(globalData
				.getRecord(GameField.HARD_LEVEL_HEX)));	
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
		
		case R.id.btnClear:
			globalData.clearRecords();
			loadRecords();
			break;
		}	
	}	
}
