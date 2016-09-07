package com.yvolohov.miner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;

/**
 * Класс отвечает за вывод игры "Сапер" на экран устройства
 * а также за взаимодействие с пользователем
 * Если gameField реализует Model игры, то этот класс View+Controller
 * @author yvolohov
 */
@SuppressWarnings("serial")
abstract class GameScreen implements Serializable {
    
    public static final int LOW_DENSITY = 0;
    public static final int MEDIUM_DENSITY = 1;
    public static final int HIGH_DENSITY = 2;

    public static final int LOW_SCALE = 0;
    public static final int MEDIUM_SCALE = 1;
    public static final int HIGH_SCALE = 2;   
    
    public static final int BACKGROUND_COLOR = 0xFF505050;
    
    private GameField gameField;
    private int densityGroup, scale;      
    private transient PointF cellSizes;
    
    private transient Point screen;
    private transient Rect borders;
    private transient Rect scrollbars;
    private transient Point scrollbox;
    private transient RectF fieldbox;
    private transient Point presspoint;
    
    private transient HashMap<Integer, SoftReference<Bitmap>> bitmaps;
    
    /**
     * @param gameField игровое поле;
     * @param screenSizes размеры экрана в пикселах;
     * @param densityGroup плотность экрана, точек на дюйм
     */
	public GameScreen(GameField gameField, Point screenSizes, int densityGroup) {
		this.gameField = gameField;
		this.densityGroup = densityGroup;
		scale = MEDIUM_SCALE;
		cellSizes = getCellSizes(densityGroup, scale);

		screen = screenSizes;
		borders = getBorders(densityGroup);
		scrollbars = getScrollbars();
		scrollbox = getScrollbox();
		fieldbox = new RectF(0f, 0f, (float) (scrollbox.x - 1),
				(float) (scrollbox.y - 1));

		bitmaps = new HashMap<Integer, SoftReference<Bitmap>>();
		centerScreen(true, true);
	}
    
    // -= EVENT PROCESSING =-
    
    /**
     * Обработчик изменения размеров экрана
     * @param newSizes новые размеры экрана в пикселах; 
     */
    public synchronized void onSizeChangedScreen(Point newSizes) {
 
        // Запоминаем центр старого экрана
        Point centerScreen = new Point();
        centerScreen.x = (int)(fieldbox.left + (scrollbox.x / 2f));
        centerScreen.y = (int)(fieldbox.top + (scrollbox.y / 2f));        
 
        // Устанавливаем размеры нового экрана, рассчитываем полосы прокрутки
        screen = newSizes;
        scrollbars = getScrollbars();  
        scrollbox = getScrollbox();

        // Совмещаем центры старого и нового экранов
        fieldbox.left = (float)(centerScreen.x - (scrollbox.x / 2));
        fieldbox.top = (float)(centerScreen.y - (scrollbox.y / 2));
        fieldbox.right = (float)(fieldbox.left + scrollbox.x - 1);
        fieldbox.bottom = (float)(fieldbox.top + scrollbox.y - 1);
        
        // Подгоняем экран под новые параметры
        adjustScreen();
    }
    
    /**
     * Обработчик изменения масштаба игрового поля
     * @param newScale новый масштаб игрового поля; 
     */
    public synchronized void onScaleChangedScreen(int newScale) {
        
        // Получаем координаты точки поля в центре экрана
        PointF centerPoint = new PointF();
        centerPoint.x = (fieldbox.right + fieldbox.left) / 2.0f;
        centerPoint.y = (fieldbox.bottom + fieldbox.top) / 2.0f;
        
        // Получаем размеры поля
        PointF fieldSizes = new PointF();
        fieldSizes.x = gameField.getFWidthInPixels(cellSizes.x);
        fieldSizes.y = gameField.getFHeightInPixels(cellSizes.y);
        
        // Определяем относительное положение точки в центре экрана
        PointF relativePos = new PointF();
        relativePos.x = centerPoint.x / fieldSizes.x;
        relativePos.y = centerPoint.y / fieldSizes.y;
        
        // Изменяем масштаб и размеры ячеек
        scale = newScale;
        cellSizes = getCellSizes(densityGroup, scale);
        scrollbars = getScrollbars();  
        scrollbox = getScrollbox();        
        
        // Получаем новые размеры игрового поля
        fieldSizes.x = gameField.getFWidthInPixels(cellSizes.x);
        fieldSizes.y = gameField.getFHeightInPixels(cellSizes.y);
        
        // Пересчитываем координаты точки поля в центре экрана
        centerPoint.x = (float) Math.floor(relativePos.x * fieldSizes.x);
        centerPoint.y = (float) Math.floor(relativePos.y * fieldSizes.y);
        
        // Устанавливаем новые значения в fieldbox
        fieldbox.left = (float) Math.floor(centerPoint.x - (scrollbox.x / 2));
        fieldbox.top = (float) Math.floor(centerPoint.y - (scrollbox.y / 2));
        fieldbox.right = fieldbox.left + (scrollbox.x - 1);
        fieldbox.bottom = fieldbox.top + (scrollbox.y - 1);
        
        // Подгоняем экран под новые параметры
        adjustScreen();
    }
    
    /**
     * Обработчик короткого нажатия на экран, срабатывает 
     * в момент отпускания
     * @param pointCoords координаты нажатия в пикселах;
     */
    public synchronized void onShortClickScreen(Point pointCoords) {
        Point cellCoords = screenPointToFieldCell(pointCoords);
        presspoint = null;
        
        if (cellCoords != null) 
            gameField.openCell(cellCoords.x, cellCoords.y);
    }
    
    /**
     * Обработчик перехода от короткого к длинному нажатию 
     * на экран, срабатывает в этот момент
     * @param pointCoords координаты нажатия в пикселах;
     */
    public synchronized void onShortToLongClickScreen(Point pointCoords) {
        presspoint = new Point(pointCoords.x, pointCoords.y);
    }
    
    /**
     * Обработчик длинного нажатия на экран, срабатывает
     * в момент отпускания
     * @param pointCoords координаты нажатия в пикселах;
     */
    public synchronized void onLongClickScreen(Point pointCoords) {
        Point cellCoords = screenPointToFieldCell(pointCoords);
        presspoint = null;
        
        if (cellCoords != null) 
            gameField.markCell(cellCoords.x, cellCoords.y);
    }
    
    /**
     * Обработчик протягивания по экрану, срабатывает при протягивании
     * @param moveSizes величины протягивания в пикселах 
     * по горизонтали и вертикали;
     */
    public synchronized void onMoveScreen(Point moveSizes) {
        
        presspoint = null;
        
        float moveW = calcScreenMoving((float) moveSizes.x, fieldbox.left, 
            fieldbox.right, gameField.getFWidthInPixels(cellSizes.x));
        float moveH = calcScreenMoving((float) moveSizes.y, fieldbox.top,
            fieldbox.bottom, gameField.getFHeightInPixels(cellSizes.y));
        
        fieldbox.left += moveW;
        fieldbox.top += moveH;
        fieldbox.right += moveW;
        fieldbox.bottom += moveH;
    }
    
    /**
     * Служебная функция, определяющая допустимые размеры протягивания 
     * по ширине или высоте с учетом возможности выхода за границы поля
     * @param pixels размер протягивания, каким он получен от экрана;
     * @param beginScrPos начало области экрана относительно поля;
     * @param endScrPos конец области экрана относительно поля;
     * @param fieldSize размер поля;
     * @return максимально допустимый размер протягивания, может быть 
     * <= параметра pixels
     */
    private float calcScreenMoving(float pixels, float beginScrPos, 
        float endScrPos, float fieldSize) {
        
        float screenSize = (endScrPos - beginScrPos) + 1f;
        if (screenSize >= fieldSize) return 0;
   
        float beginField = 0f;
	float endField = fieldSize - 1f;        
        float beginNextScrPos = beginScrPos + pixels;
        float endNextScrPos = endScrPos + pixels;        
        
	if (beginNextScrPos < beginField) return (- beginScrPos);
	else if (endNextScrPos > endField) return (endField - endScrPos); 

	return pixels;
    }    
    
    // -= DRAWING METHODS =-
    
    /**
     * Выполняет перерисовку экрана, должен быть вызван, если один из 
     * обработчиков событий возвращает true
     * @param canvas холст экрана;
     * @param res ссылка на ресурсы проекта; 
     */
    public synchronized void redrawScreen(Canvas canvas, Resources res) {   
    	
    	if (canvas == null) return;
    	
        /* Рисуем ячейки, полосы прокрутки, бегунки, след от нажатия */
    	drawCells(canvas, res);
    	drawScrollbars(canvas, res);
    	drawRunners(canvas, res);
    	drawPresspoint(canvas, res);
    }
    
    /**
     * Отвечает за отрисовку ячеек на экране
     * @param canvas холст экрана; 
     * @param res ссылка на ресурсы проекта;
     */
    private void drawCells(Canvas canvas, Resources res) {
        
        // Определяем текущую битовую карту
    	int mapId = getMapId(densityGroup, scale);
        Bitmap map = getBitmap(res, mapId);
        
        // Закрашиваем фон
        Rect bck = new Rect();
        bck.left = borders.left + scrollbars.left;
        bck.top = borders.top + scrollbars.top;
        bck.right = borders.left + scrollbars.left + scrollbox.x;
        bck.bottom = borders.top + scrollbars.top + scrollbox.y; 

        Paint bckPaint = new Paint();
        bckPaint.setColor(BACKGROUND_COLOR);        
        canvas.drawRect(bck, bckPaint);
       
        // Получаем диапазоны видимых ячеек и выводим их на экран
        DrawableCells dc = gameField.getDrawableCells(fieldbox, cellSizes);
                
        for (int row = dc.firstRow; row <= dc.lastRow; row++) {
            
            int firstCol = (row % 2 == 0) ? dc.firstEvenCol : dc.firstOddCol;
            int lastCol = (row % 2 == 0) ? dc.lastEvenCol : dc.lastOddCol;
            
            for (int col = firstCol; col <= lastCol; col++) {
                
                // Получаем значение ячейки
                int cellValue = gameField.getCell(col, row); 
                
                // Получаем координаты сторон ячейки на поле
                Point cellCoords = new Point(col, row);
                RectF cellArea = gameField.getCellArea(cellCoords, cellSizes, false);
                
                // Проверяем исходные данные
                if (cellArea == null) continue;
                
                // Получаем координаты областей битовой карты
                Rect[] srcs = getSourceAreas(cellArea, cellValue);
                
                // Получаем координаты отрисовки на экране
                RectF dst = getDestinationArea(cellArea);

                // Проверяем исходные данные
                if (srcs == null || dst == null) continue;
                
                // Последовательно перерисовываем области битовой карты
                for (int count = 0; count < srcs.length; count++)
                    canvas.drawBitmap(map, srcs[count], dst, null);
            }
        }    
    }
    
    /**
     * Отвечает за отрисовку полос прокрутки на экране
     * @param canvas холст экрана;
     * @param res ссылка на ресурсы проекта;
     */
    private void drawScrollbars(Canvas canvas, Resources res) {
        
        Rect rightBar = null, bottomBar = null, corner = null;
        
        if (scrollbars.right > 0) rightBar = new Rect(
            screen.x - borders.right - scrollbars.right,
            borders.top + scrollbars.top,
            screen.x - borders.right,
            borders.top + scrollbars.top + scrollbox.y);
        
        if (scrollbars.bottom > 0) bottomBar = new Rect(
            borders.left + scrollbars.left,
            screen.y - borders.bottom - scrollbars.bottom,
            borders.left + scrollbars.left + scrollbox.x,
            screen.y - borders.bottom);
        
        if (scrollbars.right > 0 && scrollbars.bottom > 0) corner = new Rect(
            screen.x - borders.right - scrollbars.right,
            screen.y - borders.bottom - scrollbars.bottom,
            screen.x - borders.right,
            screen.y - borders.bottom);
         
        if (rightBar != null || bottomBar != null)
        {
            Bitmap scrollbar = getBitmap(res, R.drawable.scrollbar);
        	byte[] chunk = scrollbar.getNinePatchChunk();
            NinePatch ninePatch = new NinePatch(scrollbar, chunk, null);
            
            if (rightBar != null) ninePatch.draw(canvas, rightBar);
            if (bottomBar != null) ninePatch.draw(canvas, bottomBar);
        }
         
        if (corner != null) 
        {
            Bitmap runner = getBitmap(res, R.drawable.runner);
        	byte[] chunk = runner.getNinePatchChunk(); 
            NinePatch ninePatch = new NinePatch(runner, chunk, null);        
            ninePatch.draw(canvas, corner);
        }             
    } 
    
    /**
     * Отвечает за отрисовку бегунков на экране
     * @param canvas холст экрана;
     * @param res ссылка на ресурсы проекта; 
     */
    private void drawRunners(Canvas canvas, Resources res) {
               
        Rect rightRunner = null, bottomRunner = null;
               
        // Получаем размеры игрового поля
        float fieldWidth = gameField.getFWidthInPixels(cellSizes.x);
        float fieldHeight = gameField.getFHeightInPixels(cellSizes.y);
        
        // Рассчитываем позиции бегунков
        int beginHrz = borders.left + scrollbars.left +
            (int)((scrollbox.x - 1) * (fieldbox.left / (fieldWidth - 1)));
        int endHrz = borders.left + scrollbars.left +
            (int)((scrollbox.x - 1) * (fieldbox.right / (fieldWidth - 1)));   
        int beginVert = borders.top + scrollbars.top +
            (int)((scrollbox.y - 1) * (fieldbox.top / (fieldHeight - 1)));
        int endVert = borders.top + scrollbars.top +
            (int)((scrollbox.y - 1) * (fieldbox.bottom / (fieldHeight - 1)));
        
        // Определяем области рисования бегунков        
        if (scrollbars.right > 0) rightRunner = new Rect(
            screen.x - borders.right - scrollbars.right, beginVert,
            screen.x - borders.right, endVert);
        
        if (scrollbars.bottom > 0) bottomRunner = new Rect(
            beginHrz, screen.y - borders.bottom - scrollbars.bottom,
            endHrz, screen.y - borders.bottom);
        
        Bitmap runner = getBitmap(res, R.drawable.runner);
        byte[] chunk = runner.getNinePatchChunk(); 
        NinePatch ninePatch = new NinePatch(runner, chunk, null);
                      
        if (rightRunner != null) ninePatch.draw(canvas, rightRunner);
        if (bottomRunner != null) ninePatch.draw(canvas, bottomRunner);
    }
    
    /**
     * Отвечает за отрисовку следа нажатия на экране
     * @param canvas холст экрана; 
     * @param res ссылка на ресурсы проекта;  
     */
    private void drawPresspoint(Canvas canvas, Resources res) {
        
        if (presspoint == null) return;
        
        Bitmap ppoint = getBitmap(res, R.drawable.presspoint);
        int bitmapWidth = ppoint.getWidth();
        int bitmapHeight = ppoint.getHeight();
        
        RectF area = new RectF();
        area.left = presspoint.x - (bitmapWidth / 2);
        area.top = presspoint.y - (bitmapHeight / 2);
        area.right = area.left + bitmapWidth;
        area.bottom = area.top + bitmapHeight;
        
        RectF template = new RectF();
        template.left = borders.left + scrollbars.left;
        template.top = borders.top + scrollbars.top;
        template.right = template.left + scrollbox.x;
        template.bottom = template.top + scrollbox.y;
                        
        RectF dst = gameField.clipArea(area, template);
                
        Rect src = new Rect();
        src.left = (int)(dst.left - area.left);
        src.top = (int)(dst.top - area.top);
        src.right = (int)(dst.right - area.left);
        src.bottom = (int)(dst.bottom - area.top);
        
        canvas.drawBitmap(ppoint, src, dst, null);
    }
    
    /**
     * Получает Bitmap из кэша или загружает из ресурса 
     * @param res ссылка на ресурсы проекта;
     * @param id идентификатор графического ресурса;
     * @return Bitmap;
     */
    private Bitmap getBitmap(Resources res, int id) {
 	   
 	   Bitmap bmp = null;
 	   
 	   /* пробуем получить из кэша мягкую ссылку */
 	   SoftReference<Bitmap> ref = bitmaps.get(id);
 	   
 	   /* по мягкой ссылке пробуем получить жесткую ссылку */
 	   if (ref != null) bmp = ref.get();
 	   
 	   /* если это не удалось, создаем Bitmap, затем мягкую ссылку
 	    * на него и помещаем ее в кэш */
 	   if (bmp == null) {
 	        BitmapFactory.Options opts = new BitmapFactory.Options();
 	        opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
 	        opts.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
 	        bmp = BitmapFactory.decodeResource(res, id, opts);
 	        ref = new SoftReference<Bitmap>(bmp);
 	        bitmaps.put(id, ref);
 	   }
 	   return bmp;
    }    
       
    // -= SERVICE METHODS =-
 
    /**
     * Возвращает игровое поле
     * @return игровое поле 
     */
    public GameField getGameField() {return gameField;}    

    /**
     * Устанавливает игровое поле
     * @param gameField игровое поле;
     */
    public void setGameField(GameField gameField) {this.gameField = gameField;}    
    
    /**
     * Возвращает размер экрана по ширине
     * @return размер экрана по ширине
     */
    public int getSWidth() {return screen.x;}
    
    /**
     * Возвращает размер экрана по высоте
     * @return размер экрана по высоте
     */    
    public int getSHeight() {return screen.y;} 

    /**
     * Возвращает группу плотности экрана
     * @return группа плотности экрана
     */    
    public int getDensityGroup() {return densityGroup;}
    
    /**
     * Возвращает текущий масштаб игрового поля, значение одной
     * из констант LOW_SCALE, MEDIUM_SCALE, HIGH_SCALE
     * @return 
     */
    public int getScale() {return scale;}        
    
    /**
     * Возвращает усредненный размер ячейки (ширина + высота) / 2
     * @return усредненный размер ячейки в пикселах
     */
    public int getAverageCellSize() {
        return (int)((cellSizes.x + cellSizes.y) / 2f);
    }
    
    /**
     * Возвращает координаты ячейки поля, соответствующие определенной точке
     * экрана, в которой было сделано нажатие.
     * @param pointCoords координаты точки нажатия на экран, в пикселах;
     * @return координаты ячейки поля или null в случае, если нажатие было
     * сделано в области поля без ячейки
     */
    private Point screenPointToFieldCell(Point pointCoords) {
        
        int leftBorder = borders.left + scrollbars.left;
        int rightBorder = screen.x - scrollbars.right - borders.right;
        int topBorder = borders.top + scrollbars.top;
        int bottomBorder = screen.y - scrollbars.bottom - borders.bottom;
        
        // Точка в области границ или скроллбаров на экране
        if (!(pointCoords.x >= leftBorder && pointCoords.x < rightBorder &&
            pointCoords.y >= topBorder && pointCoords.y < bottomBorder))
            return null;
        
        float posW = fieldbox.left + (float)(pointCoords.x - leftBorder);
        float posH = fieldbox.top + (float)(pointCoords.y - topBorder);        
        float fieldWidth = gameField.getFWidthInPixels(cellSizes.x);
        float fieldHeight = gameField.getFHeightInPixels(cellSizes.y);
        
        if (posW >= 0 && posW < fieldWidth && posH >= 0 && posH < fieldHeight) 
            return gameField.fieldPointToFieldCell(new PointF(posW, posH), cellSizes);
        
        // Точка вне поля
        return null;
    }    
    
    /**
     * Подстраивает экран под размеры поля после изменения 
     * масштаба поля или размеров экрана
     */
    private void adjustScreen() {
        
        // Если экран по одному из измерений больше поля
        float fieldWidth = gameField.getFWidthInPixels(cellSizes.x);
        float fieldHeight = gameField.getFHeightInPixels(cellSizes.y);
        boolean centerWidth = (scrollbox.x >= fieldWidth) ? true : false;
        boolean centerHeight = (scrollbox.y >= fieldHeight) ? true : false;

        // Корректируем позицию экрана (если он выходит за границы поля)
        if (!centerWidth) 
        {
            float moveW = calcScreenMoving(0f, fieldbox.left, fieldbox.right, fieldWidth);
            fieldbox.left += moveW;
            fieldbox.right += moveW;
        }        

        if (!centerHeight) 
        {
            float moveH = calcScreenMoving(0f, fieldbox.top, fieldbox.bottom, fieldHeight);
            fieldbox.top += moveH;
            fieldbox.bottom += moveH;
        }
        
        // Центрируем экран, если он больше поля
        centerScreen(centerWidth, centerHeight);     
    }    
    
    /**
     * Центрирует экран относительно поля по ширине и (или) высоте
     * @param width если true, центрировать по ширине;
     * @param height если true, центрировать по высоте;
     */
    private void centerScreen(boolean width, boolean height) {
        
        if (width)
        {
            float fieldWidth = gameField.getFWidthInPixels(cellSizes.x);
            fieldbox.left = (fieldWidth - scrollbox.x) / 2;
            fieldbox.right = fieldbox.left + (scrollbox.x - 1);
        }
        
        if (height) 
        {
            float fieldHeight = gameField.getFHeightInPixels(cellSizes.y);
            fieldbox.top = (fieldHeight - scrollbox.y) / 2;
            fieldbox.bottom = fieldbox.top + (scrollbox.y - 1);
        }        
    }

    /**
     * Возвращает массив участков битовой карты, которые нужно
     * перерисовать на экране, для вывода определенной ячейки
     * @param cellArea область ячейки относительно поля;
     * @param cellValue значение рисуемой ячейки;
     * @return массив участков битовой карты 
     */
    private Rect[] getSourceAreas(RectF cellArea, int cellValue) {
        
        // Получаем массив смещений для испольуемых участков битовой карты
        int[] shifts = getMapShifts(cellValue);
        
        // Получаем отображаемую на экране область ячейки
        RectF clipArea = gameField.clipArea(cellArea, fieldbox);       
        if (clipArea == null) return null; 
        
        // Получаем положение отображаемой области относительно ячейки
        Rect baseArea = new Rect();
        baseArea.left = (int) Math.floor(clipArea.left - cellArea.left);
        baseArea.top = (int) Math.floor(clipArea.top - cellArea.top);
        baseArea.right = (int) Math.ceil(clipArea.right - cellArea.left);
        baseArea.bottom = (int) Math.ceil(clipArea.bottom - cellArea.top);
        
        // Получаем координаты исходных областей с битовой карты
        Rect[] sourceAreas = new Rect[shifts.length];
        
        for (int count = 0; count < shifts.length; count++)
        {
            sourceAreas[count] = new Rect();
            sourceAreas[count].left = baseArea.left + shifts[count];
            sourceAreas[count].right = baseArea.right + shifts[count];
            sourceAreas[count].top = baseArea.top;
            sourceAreas[count].bottom = baseArea.bottom;
            sourceAreas[count].right++;
            sourceAreas[count].bottom++;
        }
        
        return sourceAreas;
    }    
    
    /**
     * Возвращает массив смещений участков битовой карты, которые нужно 
     * последовательно отрисовать для вывода ячейки на экран. Битовая
     * карта состоит из 16 участков. Для рисования некоторых ячеек
     * нужно последовательно наложить несколько участков друг на друга,
     * для других ячеек достаточно единственного участка
     * @param cellValue значение рисуемой ячейки;
     * @return массив смещений участков битовой карты
     */
    private int[] getMapShifts(int cellValue) {
                
        int[] shifts;
        int cellSize = (int) cellSizes.x;
        
        // Пустая открытая ячейка 0
        if (cellValue == GameField.OPENED_CELLS + GameField.EMPTY_CELL) 
            shifts = new int[] {0 * cellSize};
        
        // Открытая ячейка с цифрами 0..8 или миной 9
        else if (cellValue > GameField.OPENED_CELLS + GameField.EMPTY_CELL 
            && cellValue <= GameField.OPENED_CELLS + GameField.MINE_CELL)           
            shifts = new int[] {0 * cellSize, cellValue * cellSize};
        
        // Закрытая ячейка без отметки
        else if (cellValue >= GameField.CLOSED_UNMARKED_CELLS + GameField.EMPTY_CELL
            && cellValue <= GameField.CLOSED_UNMARKED_CELLS + GameField.MINE_CELL)
            shifts = new int[] {13 * cellSize};
        
        // Закрытая ячейка с отметкой "?"
        else if (cellValue >= GameField.CLOSED_MARKED_QUESTION_CELLS + GameField.EMPTY_CELL
            && cellValue <= GameField.CLOSED_MARKED_QUESTION_CELLS + GameField.MINE_CELL)
            shifts = new int[] {13 * cellSize, 15 * cellSize};
        
        // Закрытая ячейка с отметкой "флажок" во время игры
        else if (cellValue >= GameField.CLOSED_MARKED_FLAG_CELLS + GameField.EMPTY_CELL
            && cellValue <= GameField.CLOSED_MARKED_FLAG_CELLS + GameField.MINE_CELL
            && gameField.getGameState() == GameField.ACTION_GAME_STATE)
            shifts = new int[] {13 * cellSize, 14 * cellSize};
        
        // Ячейка с отметкой "флажок" после окончания игры
        else if (cellValue >= GameField.CLOSED_MARKED_FLAG_CELLS + GameField.EMPTY_CELL
            && cellValue <= GameField.CLOSED_MARKED_FLAG_CELLS + GameField.MINE_CELL
            && gameField.getGameState() != GameField.ACTION_GAME_STATE) 
        {
            if (cellValue == GameField.CLOSED_MARKED_FLAG_CELLS + GameField.MINE_CELL) 
                shifts = new int[] {0 * cellSize, 9 * cellSize, 10 * cellSize};
            else 
                shifts = new int[] {0 * cellSize, 9 * cellSize, 11 * cellSize};            
        } 
        
        // Ячейка со взорванной миной
        else if (cellValue == GameField.BLASTED_CELLS + GameField.MINE_CELL)
            shifts = new int[] {0 * cellSize, 12 * cellSize};
        
        // Неправильное значение ячейки
        else shifts = new int[] {0 * cellSize};
        
        return shifts;
    }
    
    /**
     * Возвращает область экрана, где нужно нарисовать ячейку
     * @param cellArea область ячейки относительно поля;
     * @return область экрана, где нужно нарисовать ячейку
     */
    private RectF getDestinationArea(RectF cellArea) {
    
        RectF clipArea = gameField.clipArea(cellArea, fieldbox);                
        if (clipArea == null) return null;
        
        float leftShift = (float)(borders.left + scrollbars.left);
        float topShift = (float)(borders.top + scrollbars.top);
        
        RectF dst = new RectF();
        dst.left = (clipArea.left - fieldbox.left) + leftShift;
        dst.top = (clipArea.top - fieldbox.top) + topShift;        
        dst.right = (clipArea.right - fieldbox.left) + leftShift;
        dst.bottom = (clipArea.bottom - fieldbox.top) + topShift;
        
        dst.right++;
        dst.bottom++;        
        
        return dst;
    }
    
    /**
     * Вычисляет и возвращает размеры скроллбокса, области экрана без
     * границ и полос прокрутки где можно рисовать ячейки
     * @return размеры скроллбокса, ширина и высота в пикселях
     */
    private Point getScrollbox() {
        
        Point sbox = new Point();    
        sbox.x = screen.x - borders.left - scrollbars.left - 
            scrollbars.right - borders.right;
        sbox.y = screen.y - borders.top - scrollbars.top -
            scrollbars.bottom - borders.bottom;
        
        return sbox;
    }
    
    /**
     * Вычисляет и возвращает ширину полос прокрутки
     * @return ширина полос прокрутки сверху, слева, снизу и справа
     * от скроллбокса, если 0 то полосы прокрутки нет
     */
    private Rect getScrollbars() {
        
        // получаем размеры поля и размеры экрана за вычетом границ
        float fieldWidth = gameField.getFWidthInPixels(cellSizes.x);
        float fieldHeight = gameField.getFHeightInPixels(cellSizes.y);
        float screenWidth = (float)(screen.x - borders.left - borders.right);
        float screenHeight = (float)(screen.y - borders.top - borders.bottom);
 
        int[][] sizes = new int[][] {
            {0, 0, 6, 6},
            {0, 0, 9, 9},
            {0, 0, 12, 12}
        };
                    
        // выделяем место под скроллбары
        Rect bars = new Rect(); 
        bars.left = (fieldHeight > screenHeight) ? sizes[densityGroup][0] : 0;
        bars.top = (fieldWidth > screenWidth) ? sizes[densityGroup][1] : 0; 
        bars.right = (fieldHeight > screenHeight) ? sizes[densityGroup][2] : 0;
        bars.bottom = (fieldWidth > screenWidth) ? sizes[densityGroup][3] : 0;        
        
        return bars;
    }
    
    /**
     * Вычисляет и возвращает ширину границ экрана 
     * @param densityGroup значение используемой плотности экрана;
     * @return ширина границ экрана сверху, слева, снизу и справа
     */
    private Rect getBorders(int densityGroup) {
        
        int[][] sizes = new int[][] {
            {4, 4, 4, 4},
            {6, 6, 6, 6},
            {8, 8, 8, 8}
        };
        
        Rect bords = new Rect();
        bords.left = sizes[densityGroup][0];
        bords.top = sizes[densityGroup][1];
        bords.right = sizes[densityGroup][2];
        bords.bottom = sizes[densityGroup][3];
        
        return bords;
    }
    
    // -= SERIALIZATION METHODS =-
    
    /**
     * Здесь происходит ручная сериализация некоторых объектов,
     * которые не могут сериализироватся автоматом и поэтому были помечены
     * как transient
     * @param stream поток для записи;
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
        out.writeFloat(cellSizes.x);
        out.writeFloat(cellSizes.y);
        out.writeInt(screen.x);
        out.writeInt(screen.y);
        out.writeInt(borders.left);
        out.writeInt(borders.top);
        out.writeInt(borders.right);
        out.writeInt(borders.bottom);
        out.writeInt(scrollbars.left);
        out.writeInt(scrollbars.top);
        out.writeInt(scrollbars.right);
        out.writeInt(scrollbars.bottom);
        out.writeInt(scrollbox.x);
        out.writeInt(scrollbox.y);
        out.writeFloat(fieldbox.left);
        out.writeFloat(fieldbox.top);
        out.writeFloat(fieldbox.right);
        out.writeFloat(fieldbox.bottom);   	
    }
 
    /**
     * Здесь происходит ручная десериализация некоторых объектов,
     * которые не могут сериализироватся автоматом и поэтому были помечены
     * как transient
     * @param stream поток для записи;
     * @throws IOException
     */    
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		cellSizes = new PointF(in.readFloat(), in.readFloat());
		screen = new Point(in.readInt(), in.readInt());
		borders = new Rect(in.readInt(), in.readInt(), in.readInt(),
				in.readInt());
		scrollbars = new Rect(in.readInt(), in.readInt(), in.readInt(),
				in.readInt());
		scrollbox = new Point(in.readInt(), in.readInt());
		fieldbox = new RectF(in.readFloat(), in.readFloat(), in.readFloat(),
				in.readFloat());
		bitmaps = new HashMap<Integer, SoftReference<Bitmap>>();
	}
    
    // -= ABSTRACT METHODS =-

    /**
     * Создает новый экземпляр игрового экрана,
     * копируя все настройки текущего экземпляра;
     * @return новый экземпляр игрового экрана
     */	
	public abstract GameScreen reCreate();
	
    /**
     * Получает размеры ячеек при разных плотностях и масштабах. Для
     * шестиуголных и квадартных ячеек это будут разные величины, так что
     * метод абстрактный
     * @param densityGroup плотность, одна из констант LOW_DENSITY,
     * MEDIUM_DENSITY, HIGH_DENSITY;
     * @param scale масштаб, одна из констант LOW_SCALE, 
     * MEDIUM_SCALE, HIGH_SCALE;
     * @return размеры ячейки в пикселах, ширина и высота
     */
    public abstract PointF getCellSizes(int densityGroup, int scale);
    
    public abstract int getMapId(int densityGroup, int scale); 
}