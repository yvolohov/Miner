package com.yvolohov.miner;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Класс представляет модель игры "Сапер" с
 * квадратными ячейками
 * @author Ярослав Волохов
 */
@SuppressWarnings("serial")
public class SquGameField extends GameField {

	private int level;
	
    /**
     * @param level уровень сложности игры,
     * может принимать значения констант SIMPLE_LEVEL,
     * MEDIUM_LEVEL и HARD_LEVEL;
     */    
    public SquGameField(int level) {
        super(level);
        this.level = level;
    }
    
    /**
     * @param width ширина поля;
     * @param height высота поля;
     * @param mines количество мин;
     */    
    public SquGameField(int width, int height, int mines) {      
        super(width, height, mines);
        this.level = 0;
    }
    
    // -= ABSTRACT METHODS IMPLEMENTATION =-

    /**
     * Создает новый экземпляр игрового поля,
     * копируя все настройки текущего экземпляра;
     * @return новый экземпляр игрового поля
     */
	public GameField reCreate() {
		if (level > 0)
			return new SquGameField(level);
		else
			return new SquGameField(getFWidth(), getFHeight(), getMines());
	}    
    
    /**
     * Возвращает уровень сложности игры -
     * значения констант SIMPLE_LEVEL,
     * MEDIUM_LEVEL, HARD_LEVEL или 0
     * для игры со своими настройками; 
     * @return уровень сложности игры
     */
    public int getLevel() {return level;}   
    
    /**
     * Возвращает координаты ячейки поля, соответствующие определенной точке, 
     * в которой было сделано нажатие на поле. Реализация абстрактного метода
     * для поля с квадратными ячейками
     * @param pointCoords координаты точки нажатия на поле, в пикселах;
     * @param cellSizes размеры ячейки поля;
     * @return координаты ячейки поля или null в случае, если нажатие было
     * сделано в области поля без ячейки
     */    
    @Override
    public Point fieldPointToFieldCell(PointF pointCoords, PointF cellSizes) {
        
        Point cellCoords = new Point();
        cellCoords.x = (int) Math.floor(pointCoords.x / cellSizes.x);
        cellCoords.y = (int) Math.floor(pointCoords.y / cellSizes.y);       
        
        return cellCoords;
    }    
    
    /**
     * Этот метод возвращает координаты ячеек вокруг текущей ячейки,
     * для квадратной ячейки их восемь пар
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     * @return двумерный массив с координатами
     */
    @Override
    public int[][] getCoordsAround(int posW, int posH) {
        
        int coords[][] = new int[][] {
            {posW - 1, posH - 1},           
            {posW - 1, posH},           
            {posW - 1, posH + 1},
            {posW, posH - 1},     
            {posW, posH + 1},            
            {posW + 1, posH - 1},
            {posW + 1, posH},
            {posW + 1, posH + 1}
        };
        
        return coords;
    }

    /**
     * Возвращает ширину поля в пикселях:
     * @param cellWidth размер ячейки по ширине в пикселях; 
     * @return размер поля
     */     
    @Override
    public float getFWidthInPixels(float cellWidth) {      
        return getFWidth() * cellWidth;        
    }
    
    /**
     * Возвращает высоту поля в пикселях:
     * @param cellHeight размер ячейки по высоте в пикселях; 
     * @return размер поля
     */    
    @Override
    public float getFHeightInPixels(float cellHeight) {
        return getFHeight() * cellHeight;    
    }

    /**
     * Возвращает диапазоны номеров ячеек, отображаемых на экране. Реализация 
     * абстрактного метода для поля с квадратными ячейками
     * @param fieldArea часть поля, отображаемая на экране, в пикселах;
     * @param cellSizes размеры ячейки поля; 
     * @return диапазоны номеров ячеек, отображаемых на экране
     */    
    @Override
    public DrawableCells getDrawableCells(RectF fieldArea, PointF cellSizes) {               
    
        // если fieldArea выходит за границы поля, выполним ее обрезку 
        float indW = getFWidthInPixels(cellSizes.x) - 1f;
        float indH = getFHeightInPixels(cellSizes.y) - 1f;
        RectF template = new RectF(0f, 0f, indW, indH);      
        RectF area = clipArea(fieldArea, template);
        
        // получим ячейки внутри fieldArea
        DrawableCells drawableCells = new DrawableCells();
        drawableCells.firstEvenCol = drawableCells.firstOddCol = 
            (int) Math.floor(area.left / cellSizes.x);
        drawableCells.lastEvenCol = drawableCells.lastOddCol = 
            (int) Math.floor(area.right / cellSizes.x);
        drawableCells.firstRow = (int) Math.floor(area.top / cellSizes.y);      
        drawableCells.lastRow = (int) Math.floor(area.bottom / cellSizes.y);
        
        return drawableCells;
    } 
       
    /**
     * Функция определяет границы отрисовки ячейки на поле:
     * @param cellCoords - координаты ячейки;
     * @param cellSizes - размеры ячейки по ширине и высоте;
     * @param overlap - наложение границ ячеек на 1px (будет нужно для отрисовки);
     * @return левая, верхняя, правая и нижняя границы области 
     * поля, в которой будет отрисована ячейка
     */
    @Override
    public RectF getCellArea(Point cellCoords, PointF cellSizes, boolean overlap) {       
        if (!cellExist(cellCoords.x, cellCoords.y)) return null;      
        
        float overlapValue = (!overlap) ? 1f : 0f;
        
        RectF cellArea = new RectF();
        cellArea.left = cellCoords.x * cellSizes.x;
        cellArea.top = cellCoords.y * cellSizes.y;
        cellArea.right = cellArea.left + (cellSizes.x - overlapValue);
        cellArea.bottom = cellArea.top + (cellSizes.y - overlapValue);
        return cellArea;
    }
}