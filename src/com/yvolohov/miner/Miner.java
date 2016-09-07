package com.yvolohov.miner;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

/**
 * Класс Activity, на которой происходит игра.
 * Содержит игровое поле, кнопки, счетчики времени и флажков.
 * @author yvolohov
 */
public class Miner extends Activity {

	private GlobalData globalData;
	private MinerScreen svMinerScreen;
	private TextView tvTime, tvFlags;
	private Handler handler;

	/**
	 * При создании Activity
	 */	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        Intent intent = getIntent();
        setResult(RESULT_OK, intent); 
        
        globalData = (GlobalData) getApplicationContext();
        setContentView(R.layout.miner);
        
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvFlags = (TextView) findViewById(R.id.tvFlags);
        svMinerScreen = (MinerScreen) findViewById(R.id.svMinerScreen);
        
        handler = new Handler() {
        	public void handleMessage(Message msg) {messageProc(msg);}
        };        
	}

	/**
	 * Возвращает Handler, с помощью которого GameField
	 * передает информацию индикаторам отсчета времени и 
	 * количества установленных флажков;
	 * @return Handler
	 */
	public Handler getHandler() {return handler;}
	
	/**
	 * Обработчик нажатия на кнопки
	 * @param v кнопка
	 */
    public void onClick(View v) {
    	
    	switch (v.getId()) {
    	
    	case R.id.btnMax:
    		zoom(true);
    		break;
    		
    	case R.id.btnMin:
    		zoom(false);
    		break;
    		
    	case R.id.btnRestart:
    		restart();
    		break;
    		
    	case R.id.btnExit:
    		finish();
    		break;
    	}
    }
    
    /**
     * Обрабатывает сообщения, которые GameField 
     * присылает Handler. Обновляет индикаторы
     * игрового времени и установленных флажков. 
     * @param msg сообщение;
     */
    private void messageProc(Message msg) {
		
		@SuppressWarnings("unchecked")
		Map<String, String> params = (Map<String, String>) msg.obj; 
		
		switch (msg.what) {
		
		case GameField.MSG_UPDATE:
			tvTime.setText(params.get("time"));
			tvFlags.setText(params.get("flags"));
			break;
		
		case GameField.MSG_WIN:
			long curRecord = Long.parseLong(params.get("time_msec"));
			int level = Integer.parseInt(params.get("level"));			
			long preRecord = globalData.getRecord(level);

			if (preRecord > curRecord || preRecord == 0) {
				globalData.setRecord(level, curRecord);
				String messageText = getResources().getString(R.string.win_record)
						+ " " + params.get("time");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.app_name);
				builder.setIcon(R.drawable.ic_launcher);
				builder.setMessage(messageText);
				builder.setPositiveButton("OK", null);
				builder.show();
			}
			break;		
		}		
    }
    
    /**
     * Перезапускает игру с теми же параметрами,
     * что были и у старой игры
     */
	private void restart() {
		
		/* останавливаем поток, ответственный за перерисовку экрана */
		svMinerScreen.surfaceDestroyed(null);
		
		/* создаем новый экземпляр игры на основании текущего,
		 * сохраняя все настройки */
		globalData.setScreen(globalData.getScreen().reCreate());
		
		/* возобновляем поток, ответственный за перерисовку экрана */
		svMinerScreen.surfaceCreated(null);
	}

	/**
	 * Увеличивает или уменьшает масштаб на игровом экране;
	 * @param plus значение масштаба, одна из констант
	 * класса GameScreen - LOW_SCALE, MEDIUM_SCALE, HIGH_SCALE;
	 */
    private void zoom(boolean plus) {
    	
        int scale = globalData.getScreen().getScale();
        
        if (plus && scale < GameScreen.HIGH_SCALE) scale++;
        else if (!(plus) && scale > GameScreen.LOW_SCALE) scale--;
        else return;
        
        globalData.getScreen().onScaleChangedScreen(scale);   
    }
}
