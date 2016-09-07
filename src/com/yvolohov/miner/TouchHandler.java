package com.yvolohov.miner;

import android.view.*;
import android.graphics.*;

/**
 * Класс выполняет обработку событий
 * @author Ярослав Волохов
 */
public class TouchHandler implements Runnable {

    public static final int CLICK = 0;
    public static final int MOVE = 1;
    public static final int POST_CLICK = 2;    
    public static final long MIN_TIME_TO_LONG_CLICK = 750L;
    
    private final GameScreen gameScreen;
    private int touchState;
    private PointF downPoint, prevPoint;
    private long downTime;
    private float maxDistance;
    
    /**
     * @param viewScreen виджет для отображения игрового экрана;
     * @param gameScreen объект игрового экрана;
     * @param downEvent событие нажатия на экран (DOWN), первое в цепочке;
     */
    public TouchHandler(GameScreen gameScreen, MotionEvent downEvent) {
        this.gameScreen = gameScreen;
        touchState = CLICK;
        downPoint = new PointF(downEvent.getX(), downEvent.getY());
        prevPoint = new PointF(downEvent.getX(), downEvent.getY());
        downTime = downEvent.getEventTime();
        maxDistance = 0f;
    }
    
    /**
     * Основной метод класса, обрабатывает все события 
     * цепочки (MOVE и UP) за исключением первого (первое
     * событие, DOWN, обрабатывает конструктор)  
     * @param event событие;
     */
    public void setNextEvent(MotionEvent event) {
        
        if (touchState == CLICK) 
        {
            boolean result = touchIsMove(event);
            if (result) touchState = MOVE;
        }
                
        if (touchState == CLICK && event.getAction() == MotionEvent.ACTION_UP) 
        {
            clickHandle(event);
            touchState = POST_CLICK;
        }
        else if (touchState == MOVE) 
            moveHandle(event);      
    }
    
    /**
     * Определяет характер обрабатываемой классом цепочки событий
     * (перетягивание или щелчок)
     * @param event событие;
     * @return true - цепочка событий является перетягиванием,
     * false - цепочка события являеться щелчком
     */
    private boolean touchIsMove(MotionEvent event) {
        
        double cathW = Math.abs((double)(event.getX() - downPoint.x));
        double cathH = Math.abs((double)(event.getY() - downPoint.y));       
        float distance = (float) Math.sqrt(Math.pow(cathW, 2.0) + Math.pow(cathH, 2.0));
        maxDistance = (distance > maxDistance) ? distance : maxDistance;  
        float halfCell = (float) (gameScreen.getAverageCellSize() / 2);
        
        if (maxDistance > halfCell) return true;
        else return false;
    }

    /**
     * Метод интерфейса Runnable, работает в отдельном потоке, 
     * в автоматическом режиме обрабатывает переход от короткого 
     * к длинному нажатию
     */
    public void run() {

		long preTime = System.currentTimeMillis();		
		long curTime, interval;    	
    	
        while (true) 
        {
            if (touchState != CLICK) 
            	break;

            curTime = System.currentTimeMillis();
            interval = curTime - preTime;
            
            if (interval < MIN_TIME_TO_LONG_CLICK) 
            	continue;
            
            Point pointCoords = new Point((int)(downPoint.x),(int)(downPoint.y));
            gameScreen.onShortToLongClickScreen(pointCoords);                      
            break;
        }
    }    
    
    /**
     * Обрабатывает короткий или длинный щелчок
     * @param event событие;
     */
    private void clickHandle(MotionEvent event) {

        long interval = event.getEventTime() - downTime;
        Point pointCoords = new Point((int)(event.getX()),(int)(event.getY()));
        
        if (interval < MIN_TIME_TO_LONG_CLICK) 
            gameScreen.onShortClickScreen(pointCoords);
        else
            gameScreen.onLongClickScreen(pointCoords);                
    }
    
    /**
     * Обрабатывает перетягивание экрана
     * @param event событие; 
     */
    private void moveHandle(MotionEvent event) {
        
        int histSize = event.getHistorySize();
        int arraySize = histSize + 2;         
        PointF[] points = new PointF[arraySize];      

        points[0] = new PointF(prevPoint.x, prevPoint.y);

        for (int count = 0; count < histSize; count++)               
            points[count + 1] = new PointF(event.getHistoricalX(count),
            event.getHistoricalY(count));

        points[arraySize - 1] = new PointF(event.getX(), event.getY());

        for (int count = 1; count < arraySize; count++)
        {
            int pcount = count - 1;
            int moveH = (int)(points[count].x - points[pcount].x);
            int moveW = (int)(points[count].y - points[pcount].y);
            Point scr = new Point(-moveH, -moveW);
            gameScreen.onMoveScreen(scr);
        }

        prevPoint.x = event.getX();
        prevPoint.y = event.getY();    
    }
}
