package com.yvolohov.miner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;

/**
 * Класс представляет заготовку для модели игры "Сапер"
 * с квадратными или шестиугольными ячейками
 * @author Ярослав Волохов
 */
@SuppressWarnings("serial")
abstract class GameField implements Serializable, Runnable {	
       
    public static final int SIMPLE_LEVEL = 1;
    public static final int MEDIUM_LEVEL = 2;
    public static final int HARD_LEVEL = 3;
    public static final int SIMPLE_LEVEL_HEX = 4;
    public static final int MEDIUM_LEVEL_HEX = 5;
    public static final int HARD_LEVEL_HEX = 6;            
	
    public static final int OPENED_CELLS = 0;
    public static final int CLOSED_UNMARKED_CELLS = 10;
    public static final int CLOSED_MARKED_QUESTION_CELLS = 20;
    public static final int CLOSED_MARKED_FLAG_CELLS = 30;
    public static final int BLASTED_CELLS = 40;
    
    public static final int CELL_NOT_EXIST = -1;
    public static final int EMPTY_CELL = 0;    
    public static final int MINE_CELL = 9;

    public static final int LOSE_GAME_STATE = 1;
    public static final int ACTION_GAME_STATE = 2;
    public static final int WIN_GAME_STATE = 3;

    public static final int MSG_UPDATE = 1;
    public static final int MSG_WIN = 2;    
    
    private byte[][] field;
    private boolean[][] processedCells;    
    private int width, height, mines, flags;
    private int restCells;
    private int gameState;
    private long time; 
    private transient Thread thread;
    private transient Handler handler;
    private transient boolean run;    
	
    /**
     * @param level уровень сложности игры,
     * может принимать значения констант SIMPLE_LEVEL,
     * MEDIUM_LEVEL, HARD_LEVEL, SIMPLE_LEVEL_HEX, 
     * MEDIUM_LEVEL_HEX и HARD_LEVEL_HEX; 
     */
	public GameField(int level) {
		switch (level) {
		
		case MEDIUM_LEVEL:
		case MEDIUM_LEVEL_HEX:
			width = 16;
			height = 16;
			mines = 40;
			break;
		
		case HARD_LEVEL:
		case HARD_LEVEL_HEX:
			width = 30;
			height = 16;
			mines = 99;
			break;
		
		default:
			width = 9;
			height = 9;
			mines = 10;
		}

		restCells = (width * height) - mines;
		gameState = ACTION_GAME_STATE;
		makeField();
	}
	
    /**
     * @param width ширина поля;
     * @param height высота поля;
     * @param mines количество мин;
     */
	public GameField(int width, int height, int mines) {
		this.width = width;
		this.height = height;
		this.mines = mines;

		restCells = (width * height) - mines;
		gameState = ACTION_GAME_STATE;
		makeField();
	}
        
    /**
     * Делает ход в игре (открывает ячейку) 
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     */
    public void openCell(int posW, int posH) {

		/* игра уже закончена, ячейка недоступна */
		if (gameState != ACTION_GAME_STATE)
			return;

		/* ячейки не существует, она уже открыта или флаггирована */
		if (!(isValidCell(posW, posH)))
			return;

		/* если в ячейке мина, открываем ее и завершаем игру */
		if (field[posW][posH] == CLOSED_MARKED_QUESTION_CELLS + MINE_CELL
				|| field[posW][posH] == CLOSED_UNMARKED_CELLS + MINE_CELL) {
			field[posW][posH] = BLASTED_CELLS + MINE_CELL;
			endGame(LOSE_GAME_STATE);
			return;
		}

		/* создаем список координат всех ячеек, которые будут открыты
		 * по щелчку на текущей ячейке */
		List<Point> coordsList = new ArrayList<Point>();
		coordsList.add(new Point(posW, posH));

		/* если координаты ячейки попадают в список то делаем
		 * отметку в служебном массиве, чтобы избежать дублирования в списке */
		processedCells[posW][posH] = true;

		for (int indexOne = 0; indexOne < coordsList.size(); indexOne++) {

			Point coords = coordsList.get(indexOne);

			/* открываем ячейку, уменьшаем счетчик */
			if (field[coords.x][coords.y] >= CLOSED_MARKED_QUESTION_CELLS)
				field[coords.x][coords.y] -= 20; // to OPENED_CELLS
			else
				field[coords.x][coords.y] -= 10; // to OPENED_CELLS

			restCells--;

			/* если все ячейки открыты, завершаем игру победой */
			if (restCells == 0) {
				endGame(WIN_GAME_STATE);
				return;
			}

			if (field[coords.x][coords.y] != EMPTY_CELL)
				continue;
			
			/* получаем соседние ячейки текущей ячейки */
			int cells[][] = getCoordsAround(coords.x, coords.y);

			for (int indexTwo = 0; indexTwo < cells.length; indexTwo++) {

				int twoW = cells[indexTwo][0];
				int twoH = cells[indexTwo][1];

				if (!(isValidCell(twoW, twoH)))
					continue;

				/* если ячейки с такими координатами еще нет с списке,  
				 * то добавляем ее координаты туда */
				if (!(processedCells[twoW][twoH])) {
					coordsList.add(new Point(twoW, twoH));
					processedCells[twoW][twoH] = true;
				}
			}
		}
	}
    
    /**
     * Устанавливает или снимает отметку в ячейке 
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     */
	public void markCell(int posW, int posH) {
		
		if (gameState != ACTION_GAME_STATE)
			return;
		if (!(cellExist(posW, posH)))
			return;

		if (field[posW][posH] >= CLOSED_MARKED_FLAG_CELLS) {
			field[posW][posH] -= 10; // to CLOSED_MARKED_QUESTION_CELLS
			flags--;
		} else if (field[posW][posH] >= CLOSED_MARKED_QUESTION_CELLS) {
			field[posW][posH] -= 10; // to CLOSED_UNMARKED_CELLS
		} else if (field[posW][posH] >= CLOSED_UNMARKED_CELLS) {
			field[posW][posH] += 20; // to CLOSED_MARKED_FLAG_CELLS
			flags++;
		} else
			return;
	}

    /**
     * Завершает игру, устанавливая статус выигрыша WIN_GAME_STATE или
     * проигрыша LOSE_GAME_STATE, обрабатывает поле
     * @param state статус звершения игры
     */
	public void endGame(int state) {
		// game is not action
		if (gameState != ACTION_GAME_STATE)
			return;

		gameState = state;

		for (int countHeight = 0; countHeight < height; countHeight++) {
			for (int countWidth = 0; countWidth < width; countWidth++) {
				int value = (int) field[countWidth][countHeight];

				if (value == CLOSED_UNMARKED_CELLS + MINE_CELL
						&& state == LOSE_GAME_STATE)
					field[countWidth][countHeight] = OPENED_CELLS + MINE_CELL;
				else if (value == CLOSED_MARKED_QUESTION_CELLS + MINE_CELL
						&& state == LOSE_GAME_STATE)
					field[countWidth][countHeight] = OPENED_CELLS + MINE_CELL;
				else if (value == CLOSED_UNMARKED_CELLS + MINE_CELL
						&& state == WIN_GAME_STATE)
					field[countWidth][countHeight] = CLOSED_MARKED_FLAG_CELLS
							+ MINE_CELL;
				else if (value == CLOSED_MARKED_QUESTION_CELLS + MINE_CELL
						&& state == WIN_GAME_STATE)
					field[countWidth][countHeight] = CLOSED_MARKED_FLAG_CELLS
							+ MINE_CELL;
			}
		}
	}	

    /**
     * Возвращает значение ячейки
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     * @return значение ячейки или -1, если 
     * ячейки с такими координатами не существует
     */
    public byte getCell(int posW, int posH) {
        if (cellExist(posW, posH)) return field[posW][posH];		
        else return CELL_NOT_EXIST;		
    }

    /**
     * Возвращает ширину поля в ячейках 
     * @return ширина поля
     */
    public int getFWidth() {return width;}

    /**
     * Возвращает высоту поля в ячейках
     * @return высота поля
     */   
    public int getFHeight() {return height;}
    
    /**
     * Возвращает количество мин на поле
     * @return количество мин
     */
    public int getMines() {return mines;}
  
    /**
     * Возвращает количество доступных для 
     * установки флажков, возможно отрицательное
     * значение при превышении лимита
     * @return количество флажков
     */
    public int getFlags() {return flags;}
    
    /**
     * Возвращает текущее состояние игры, одно из значений констант:
     * LOSE_GAME_STATE, ACTION_GAME_STATE, WIN_GAME_STATE при 
     * поражении, игре и победе соответственно;  
     * @return текущее состояние игры
     */
    public int getGameState() {return gameState;}
          
    /**
     * Проверяет наличие ячейки с указанными индексами в пределах поля
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     * @return true если ячейка есть или false - при ее отсутствии
     */
    public boolean cellExist(int posW, int posH) {
        if (!(posW < 0 || posW >= width || posH < 0 || posH >= height)) return true;
        else return false;
    }

    /**
     * Возвращает количество мин в 8 смежных ячейках 
     * вокруг указанной ячейки
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     * @return количество мин
     */
	private byte countMinesAroundCell(int posW, int posH) {
		byte mine = (byte) (CLOSED_UNMARKED_CELLS + MINE_CELL);
		byte result = 0;
		int coords[][] = getCoordsAround(posW, posH);

		for (int count = 0; count < coords.length; count++) {
			boolean cellExist = cellExist(coords[count][0], coords[count][1]);
			boolean cellIsMine = (getCell(coords[count][0], coords[count][1]) == mine);
			if (cellExist && cellIsMine)
				result++;
		}

		return result;
	}

    /**
     * Создает игровое поле
     */
	private void makeField() {
		
		field = new byte[width][height];
		processedCells = new boolean[width][height];
		
		Random rand = new Random();
		byte mine = (byte) (CLOSED_UNMARKED_CELLS + MINE_CELL);
		List<Point> coordsList = new LinkedList<Point>(); 

		for (int countHeight = 0; countHeight < height; countHeight++)
			for (int countWidth = 0; countWidth < width; countWidth++)
				coordsList.add(new Point(countWidth, countHeight));
		
		// set mines
		for (int countMines = 0; countMines < mines; countMines++) {
			int index = rand.nextInt(coordsList.size());
			Point cellCoords = coordsList.get(index);
			field[cellCoords.x][cellCoords.y] = mine;
			coordsList.remove(index);
		}
		
		// set other cells
		for (int countHeight = 0; countHeight < height; countHeight++) {
			for (int countWidth = 0; countWidth < width; countWidth++) {
				if (field[countWidth][countHeight] == mine)
					continue;
				byte minesAround = countMinesAroundCell(countWidth, countHeight);
				field[countWidth][countHeight] = (byte) (GameField.CLOSED_UNMARKED_CELLS + minesAround);
			}
		}
	}

	/**
	 * Проверяет, что ячейка с указанными координатами существует,
	 * закрыта и не отмечена флажком 
	 * @param posW индекс ячейки по ширине;
	 * @param posH индекс ячейки по высоте;
	 * @return true - если ячейка прошла проверку, иначе false
	 */
	private boolean isValidCell(int posW, int posH) {

		byte value = getCell(posW, posH);

		if (value == CELL_NOT_EXIST)
			return false;
		if (value < CLOSED_UNMARKED_CELLS)
			return false;
		if (value >= CLOSED_MARKED_FLAG_CELLS)
			return false;

		return true;
	}
   
    /**
     * Выполняет обрезку прямоугольной области согласно шаблону,
     * от области будут отсечены все ее части, которые не попадают
     * в шаблон, обрезанная область будет возвращена:
     * @param area область для обрезки;
     * @param template шаблон;
     * @return обрезанная область 
     */
    public RectF clipArea(RectF area, RectF template) {
        
        // создаем копию области
        RectF clone = new RectF(area.left, area.top, area.right, area.bottom);
        
        // область и шаблон не пересекаються 
        if (template.bottom < clone.top || template.top > clone.bottom ||
            template.right < clone.left || template.left > clone.right)
           return null;
        
        // обрезаем область по шаблону
        clone.left = (template.left < clone.left) ? clone.left : template.left; 
        clone.top = (template.top < clone.top) ? clone.top : template.top;
        clone.right = (template.right > clone.right) ? clone.right : template.right;
        clone.bottom = (template.bottom > clone.bottom) ? clone.bottom : template.bottom;
                
        return clone;
    }
            
    /**
     * Запускает поток, который отсчитывает время игры
     * и обновляет занчения счетчиков времени и установленных
     * флажков в интерфейсе
     * @param handler объект для связи с TextViews на
     * которые выводится значение пройденного времени и 
     * установленных флажков
     */
    public void startTiming(Handler handler) {    	
    	if (thread != null) return;
    	this.handler = handler;
    	thread = new Thread(this);
    	run = true;
    	thread.start();
    }
    
    /**
     * Останавливает поток, который отсчитывает время игры
     * и обновляет значения счетчиков времени и установленных
     * флажков в интерфейсе 
     */
    public void stopTiming() {
    	
    	if (thread == null) return;
    	run = false;

        while (thread.isAlive()) {
            try {thread.join();} 
            catch (InterruptedException e) {}
        }
        thread = null;
    }
    
    /**
     * Выполняется в отдельном потоке,
     * отвечает за подсчет времени и передачу
     * данных TextViews в интерфейсе игры
     */
    public void run() {
    	
    	final long UPD_INTERVAL = 100L;

		long preTime = System.currentTimeMillis();		
		long curTime, interval;
		
		int preState = gameState;
		int curState;
			
		while (run) {
			curTime = System.currentTimeMillis();
			interval = curTime - preTime;
			
			if (interval < UPD_INTERVAL) 
				continue;
			
			preTime = curTime;
			
			if (gameState == ACTION_GAME_STATE) 
				addTime(interval);
			
			sendMessageToHandler(MSG_UPDATE);
			curState = gameState;
			
			/* отправлено сообщение о победе */
			if (preState == ACTION_GAME_STATE && curState == WIN_GAME_STATE) {
				if (getLevel() > 0) sendMessageToHandler(MSG_WIN);
			}
			
			preState = curState;
		}		
    }
    
    /**
     * Отправляет сообщение объекту Handler в главном потоке 
     * @param msgType тип сообщения: MSD_UPDATE - обновление
     * значений счетчиков времени и флажков или MSG_WIN - сообщение
     * о победе в игре 
     */
    private void sendMessageToHandler(int msgType) {

    	Map<String, String> params = new HashMap<String, String>();
    	params.put("time", getTimeAsString(time));
    	
    	switch (msgType) {
    	
    	case MSG_UPDATE:
    		params.put("flags", String.format("%03d", getFlags()));
    		break;
    		
    	case MSG_WIN:
    		params.put("level", String.valueOf(getLevel()));
    		params.put("time_msec", String.valueOf(time));
    		break;
    	}
    	  	
		Message msg = handler.obtainMessage(msgType, params);
		handler.sendMessage(msg);    	
    }  
    
    /**
     * Увеличивает прошедшее игровое время
     * @param addTime добавляемое количество миллисекунд
     */
    private void addTime(long addTime) {
		if (time > Long.MAX_VALUE - addTime) 
			time = 0;
		time += addTime;    	
    }
    
    /**
     * Возвращает в ремя в виде отформатированной 
     * строки "HH:mm:ss"
     * @param time время в миллисекундах;
     * @return время в виде отформатированной строки
     */
    public static String getTimeAsString(long time) {
    	
    	final String fs = "%02d";
    	final String divider = ":";
    	
    	long fullSeconds = time / 1000L;
    	long fullMinutes = fullSeconds / 60L;
    	long fullHours = fullMinutes / 60L;
    	long seconds = fullSeconds - (fullMinutes * 60L);
    	long minutes = fullMinutes - (fullHours * 60L);    	   	
    	
    	StringBuilder builder = new StringBuilder();
    	builder.append(String.format(fs, fullHours));
    	builder.append(divider);
    	builder.append(String.format(fs, minutes));
    	builder.append(divider);
    	builder.append(String.format(fs, seconds));
    	
    	return builder.toString();
    }     
           
    // -= ABSTRACT METHODS =-

    /**
     * Создает новый экземпляр игрового поля,
     * копируя все настройки текущего экземпляра;
     * @return новый экземпляр игрового поля
     */
    public abstract GameField reCreate();
    
    /**
     * Возвращает уровень сложности игры -
     * значения констант SIMPLE_LEVEL,
     * MEDIUM_LEVEL, HARD_LEVEL, SIMPLE_LEVEL_HEX, 
     * MEDIUM_LEVEL_HEX, HARD_LEVEL_HEX или 0
     * для игры со своими настройками; 
     * @return уровень сложности игры
     */
    public abstract int getLevel();    
    
    /**
     * Возвращает координаты ячейки поля, соответствующие определенной точке, 
     * в которой было сделано нажатие на поле. Метод абстрактный, так как для 
     * поля из квадратных и поля из шестиугольных ячеек вычисление происходит 
     * по разному
     * @param pointCoords координаты точки нажатия на поле, в пикселах;
     * @param cellSizes размеры ячейки поля;
     * @return координаты ячейки поля или null в случае, если нажатие было
     * сделано в области поля без ячейки
     */
    public abstract Point fieldPointToFieldCell(PointF pointCoords, PointF cellSizes);    
    
    /**
     * Возвращает координаты ячеек вокруг текущей ячейки, для квадратной 
     * ячейки их восемь пар а для шестиугольной - шесть, поэтому метод 
     * абстрактный, и должен быть реализован в потомках по разному, 
     * зависимо от типа поля
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     * @return двумерный массив с координатами
     */
    public abstract int[][] getCoordsAround(int posW, int posH);    
    
    /**
     * Возвращает ширину поля в пикселях:
     * @param cellWidth размер ячейки по ширине в пикселях; 
     * @return размер поля
     */     
    public abstract float getFWidthInPixels(float cellWidth);
   
    /**
     * Возвращает высоту поля в пикселях:
     * @param cellHeight размер ячейки по высоте в пикселях; 
     * @return размер поля
     */     
    public abstract float getFHeightInPixels(float cellHeight);
    
    /**
     * Возвращает диапазоны номеров ячеек, отображаемых на экране
     * @param fieldArea часть поля, отображаемая на экране, в пикселах;
     * @param cellSizes размеры ячейки поля; 
     * @return диапазоны номеров ячеек, отображаемых на экране
     */
    public abstract DrawableCells getDrawableCells(RectF fieldArea, PointF cellSizes);
    
    /**
     * Функция определяет границы отрисовки ячейки на поле:
     * @param cellCoords - координаты ячейки;
     * @param cellSizes - размеры ячейки по ширине и высоте;
     * @param overlap - наложение границ ячеек на 1px (будет нужно для отрисовки);
     * @return левая, верхняя, правая и нижняя границы области 
     * поля, в которой будет отрисована ячейка
     */
    public abstract RectF getCellArea(Point cellCoords, PointF cellSizes, boolean overlap);
}