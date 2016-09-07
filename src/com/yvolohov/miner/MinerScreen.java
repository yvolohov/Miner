package com.yvolohov.miner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Класс View, на который выводится игровой экран
 * @author yvolohov
 */
public class MinerScreen extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	private GlobalData globalData;
	private Thread thread;
	private SurfaceHolder holder;
	private TouchHandler touchHandler;
	private boolean run;
		
	/**
	 * Конструктор класса
	 */
	public MinerScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		globalData = (GlobalData)(context.getApplicationContext());
        holder = getHolder();
        holder.addCallback(this);
	}
	
	/**
	 * Метод отвечает за перерисовку игрового экрана 
	 * в отдельном потоке
	 */
	public void run() {
					
		while (run) {
			Canvas canvas = null;
			
			try {
				canvas = holder.lockCanvas();
				
				synchronized (holder) {
					globalData.getScreen().redrawScreen(canvas, getResources());						
				}
			} finally { 
				if (canvas != null) holder.unlockCanvasAndPost(canvas); 
			}
		}	
	}
	
	/**
	 * Выполняется после создания экземпляра класса
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		run = true;
		thread = new Thread(this);
		thread.start();
		Miner act = (Miner) getContext();
		globalData.getScreen().getGameField().startTiming(act.getHandler());
	}

	/**
	 * Выполняется перед разрушением экземпляра класса
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		run = false;
        
		/* После выполнения join() на первом витке цикла главный поток ожидает
		 * завершения потока отрисовки экрана и цикл дальше не выполняется. 
		 * Но если произойдет Exception а поток отрисовки еще не закончил работу, 
		 * то будет выполнен следующий виток цикла и главный поток снова будет
		 * переведен в режим ожидания. */
        while (thread.isAlive()) {
            try {thread.join();} 
            catch (InterruptedException e) {}
        }
        
        globalData.getScreen().getGameField().stopTiming();
	}

	/**
	 * Выполняется при изменении размеров игрового экрана
	 * (например после поворота экрана устройства)
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Point newSizes = new Point(width, height);
		globalData.getScreen().onSizeChangedScreen(newSizes);		
	}
   
    /**
     * Обработчик события касания к экрану
     * @param event событие;
     * @return true;
     */    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
           	
        if (event.getAction() == MotionEvent.ACTION_DOWN) {            
            touchHandler = new TouchHandler(globalData.getScreen(), event);
            Thread thread = new Thread((Runnable)(touchHandler));
            thread.start();
        }
        else if (touchHandler != null) {
            touchHandler.setNextEvent(event);
        }
        
        return true;
    }   
}
