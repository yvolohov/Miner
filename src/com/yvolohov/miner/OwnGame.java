package com.yvolohov.miner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Класс Activity игровых настроек (для режима игры с
 * собственными настройками)
 * @author yvolohov
 */
public class OwnGame extends Activity implements View.OnFocusChangeListener {

	public static final int MAX_WIDTH = 99;
	public static final int MAX_HEIGHT = 99;
		
	private GlobalData globalData;
	private EditText etFieldWidth, etFieldHeight, etFieldMines;
	
	/**
	 * При создании Activity
	 */		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.own_game);
		
		globalData = (GlobalData) getApplicationContext();
		
		etFieldWidth = (EditText) findViewById(R.id.etFieldWidth);
		etFieldHeight = (EditText) findViewById(R.id.etFieldHeight);
		etFieldMines = (EditText) findViewById(R.id.etFieldMines);

		etFieldWidth.setText(Integer.toString(globalData.getDefWidth()));
		etFieldHeight.setText(Integer.toString(globalData.getDefHeight()));
		etFieldMines.setText(Integer.toString(globalData.getDefMines()));		
	
		etFieldWidth.setOnFocusChangeListener(this);
		etFieldHeight.setOnFocusChangeListener(this);
		etFieldMines.setOnFocusChangeListener(this);
	}

	/**
	 * Обработчик нажатия на кнопки
	 * @param v кнопка
	 */
	public void onClick(View v) {
		
		switch (v.getId()) {
		
		case R.id.btnToGame:
			verifyEdits();
			globalData.setDefWidth(Integer.parseInt(etFieldWidth.getText().toString()));
			globalData.setDefHeight(Integer.parseInt(etFieldHeight.getText().toString()));
			globalData.setDefMines(Integer.parseInt(etFieldMines.getText().toString()));
			Intent intent = getIntent();
			setResult(RESULT_OK, intent);
			finish();
			break;
		
		case R.id.btnCancel:
			finish();
			break;
		}	
	}

	/**
	 * При изменении фокуса экранного элемента
	 */
	public void onFocusChange(View v, boolean hasFocus) {
		
		switch (v.getId()) {
		
		case R.id.etFieldWidth:
		case R.id.etFieldHeight:
		case R.id.etFieldMines:
			if (!hasFocus) verifyEdits();
			break;
		}	
	}
	
	/**
	 * Проверяет правильность всех игровых настроек
	 */
	private void verifyEdits() {
		int width = verifyEdit(etFieldWidth, MAX_WIDTH);
		int height = verifyEdit(etFieldHeight, MAX_HEIGHT);
		verifyEdit(etFieldMines, width * height);
	}
	
	/**
	 * Проверяет правильность одной игровой настройки
	 * @param edit EditText, хранящий игровую настройку;
	 * @param maxValue максимально допустимое значение настройки;
	 * @return игровая настройка;
	 */
	private int verifyEdit(EditText edit, int maxValue) {
		
		int value = 0;
		String valueStr = edit.getText().toString();
		
		try { value = Integer.parseInt(valueStr); }
		catch (NumberFormatException e) { value = 0; }
		
		if (value < 0) value = 0;
		if (value > maxValue) value = maxValue;
	
		edit.setText(Integer.toString(value));
		return value;
	}
}
