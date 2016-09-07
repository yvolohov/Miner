package com.yvolohov.miner;

import android.graphics.*;

/**
 * Класс представляет модель игры "Сапер" с
 * шестиугольными ячейками
 * @author Ярослав Волохов
 */
@SuppressWarnings("serial")
public class HexGameField extends GameField {

	private int level;
	
    /**
     * @param level уровень сложности игры,
     * может принимать значения констант 
     * SIMPLE_LEVEL_HEX, MEDIUM_LEVEL_HEX и 
     * HARD_LEVEL_HEX;   
     */
    public HexGameField(int level) { 
    	super(level); 
    	this.level = level;
    }    
    
    /**
     * @param width ширина поля;
     * @param height высота поля;
     * @param mines количество мин;
     */    
    public HexGameField(int width, int height, int mines) {      
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
			return new HexGameField(level);
		else
			return new HexGameField(getFWidth(), getFHeight(), getMines());
	}  
    
    /**
     * Возвращает уровень сложности игры -
     * значения констант SIMPLE_LEVEL_HEX, 
     * MEDIUM_LEVEL_HEX, HARD_LEVEL_HEX или 0
     * для игры со своими настройками; 
     * @return уровень сложности игры
     */
    public int getLevel() {return level;}    
    
    /**
     * Получает угол прямой, которая выходит из основы 360-градусной системы
     * координат и проходит через некоторую вторую точку
     * @param pressPoint вторая точка;
     * @param basePoint точка, лежащая в основе координат;
     * @return угол в градусах, число >= 0 и < 360 
     */
    public float getDirection(PointF pressPoint, PointF basePoint) {       
        
        // Получаем расстояние между точками по осям
        float cathX = pressPoint.x - basePoint.x;
        float cathY = pressPoint.y - basePoint.y;
        float absX = Math.abs(cathX);
        float absY = Math.abs(cathY);
   
        // получаем арктангенс (угол)
        double atg = Math.toDegrees(Math.atan(absY / absX));
        
        // делаем поправку на знак 
        if (cathX >= 0 && cathY >= 0) atg = atg + 0.0;
        else if (cathX < 0 && cathY >= 0) atg = (90.0 - atg) + 90.0;
        else if (cathX < 0 && cathY < 0) atg = atg + 180.0;
        else if (cathX >= 0 && cathY < 0) atg = (90.0 - atg) + 270.0;
           
        return (float) atg;
    }

    /**
     * Возвращает координаты ячейки поля, соответствующие определенной точке, 
     * в которой было сделано нажатие на поле. Реализация абстрактного метода
     * для поля с шестиугольными ячейками
     * @param pointCoords координаты точки нажатия на поле, в пикселах;
     * @param cellSizes размеры ячейки поля;
     * @return координаты ячейки поля или null в случае, если нажатие было
     * сделано в области поля без ячейки
     */    
    @Override
    public Point fieldPointToFieldCell(PointF pointCoords, PointF cellSizes) {
               
        /* Небольшой фокус, получаем все ячейки, которые попадают
           в область, состоящую из единственной точки */
        RectF fieldArea = new RectF(pointCoords.x, pointCoords.y, 
            pointCoords.x, pointCoords.y);
        DrawableCells dc = getDrawableCells(fieldArea, cellSizes);
        
        float width50 = cellSizes.x * 0.50f;
        float height75 = cellSizes.y * 0.75f;
        
        for (int row = dc.firstRow; row <= dc.lastRow; row++) {
            
            int firstCol = (row % 2 == 0) ? dc.firstEvenCol : dc.firstOddCol;
            int lastCol = (row % 2 == 0) ? dc.lastEvenCol : dc.lastOddCol;
            
            for (int col = firstCol; col <= lastCol; col++) {
                                
                // Получаем координаты сторон ячейки на поле
                Point cellCoords = new Point(col, row);
                RectF cellArea = getCellArea(cellCoords, cellSizes, false);
                
                // Проверяем исходные данные
                if (cellArea == null) continue;
                
                // Определяем три опорные точки на вершинах гексагона            
                PointF firstPoint = new PointF(cellArea.left + width50, cellArea.top);
                PointF secondPoint = new PointF(cellArea.left, cellArea.top + height75);
                PointF thirdPoint = new PointF(cellArea.right, cellArea.top + height75);
                
                // Определяем углы между опорными точками и точкой нажатия 
                float firstAngle = getDirection(pointCoords, firstPoint);
                float secondAngle = getDirection(pointCoords, secondPoint);
                float thirdAngle = getDirection(pointCoords, thirdPoint);
                
                // Определяем, попадают ли эти углы в заданные диапазоны
                boolean firstMatch = ((firstAngle >= 30.0f && firstAngle < 150.0f) || 
                    Float.isNaN(firstAngle));
                boolean secondMatch = ((secondAngle >= 270.0f && secondAngle < 360.0f) ||
                    (secondAngle >= 0.0f && secondAngle < 30.0f) || 
                    Float.isNaN(secondAngle));               
                boolean thirdMatch = ((thirdAngle >= 150.0f && thirdAngle < 270.0f) ||
                    Float.isNaN(thirdAngle));
                
                if (firstMatch && secondMatch && thirdMatch) return new Point(col, row);
            }
        }        
        
        return null;
    }     
    
    /**
     * Этот метод возвращает координаты ячеек вокруг текущей ячейки,
     * для шестиугольной ячейки их шесть пар
     * @param posW индекс ячейки по ширине;
     * @param posH индекс ячейки по высоте;
     * @return двумерный массив с координатами
     */
    @Override
    public int[][] getCoordsAround(int posW, int posH) {
        
        int coords[][];
        
        // pair rows
        if (posH % 2 == 0) {
            coords = new int[][] {
                {posW, posH - 1}, {posW - 1, posH - 1},
                {posW + 1, posH}, {posW - 1, posH + 1},
                {posW, posH + 1}, {posW - 1, posH}
            };          
        }
                
        // non pair rows
        else {
            coords = new int[][] {
                {posW, posH - 1}, {posW + 1, posH - 1},
                {posW + 1, posH}, {posW + 1, posH + 1},
                {posW, posH + 1}, {posW - 1, posH}
            };       
        } 
        
        return coords;
    }
    
    /**
     * Возвращает ширину поля в пикселях:
     * @param cellWidth размер ячейки по ширине в пикселях; 
     * @return размер поля
     */
    @Override
    public float getFWidthInPixels(float cellWidth) {
        return (getFWidth() * cellWidth) + (cellWidth * 0.5f);  
    }

    /**
     * Возвращает высоту поля в пикселях:
     * @param cellHeight размер ячейки по высоте в пикселях; 
     * @return размер поля
     */    
    @Override
    public float getFHeightInPixels(float cellHeight) {
        return (getFHeight() * cellHeight * 0.75f) + (cellHeight * 0.25f);
    }

    /**
     * Возвращает диапазоны номеров ячеек, отображаемых на экране. Реализация 
     * абстрактного метода для поля с шестиугольными ячейками
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
        
        // получим координаты ячеек в fieldArea
        DrawableCells drawableCells = new DrawableCells();
        
        // координаты первой и последней строки
        float height25 = cellSizes.y * 0.25f;
        float height75 = cellSizes.y * 0.75f;
        
        drawableCells.firstRow = (area.top >= height25) 
            ? (int) Math.floor((area.top - height25) / height75) : 0;
        
        drawableCells.lastRow = (area.bottom < (getFHeight() * height75))
            ? (int) Math.floor(area.bottom / height75) : (getFHeight() - 1);

        // координаты первого и последнего столбцов в нечетной строке
        float width50 = cellSizes.x * 0.50f;
        
        drawableCells.firstOddCol = (area.left >= width50)
            ? (int) Math.floor((area.left - width50) / cellSizes.x) : 0;
        
        drawableCells.lastOddCol = (area.right >= width50)
            ? (int) Math.floor((area.right - width50) / cellSizes.x) : 0;
        
        // координаты первого и последнего столбцов в четной строке 
        float cellsLengthPx = getFWidth() * cellSizes.x;
        int cellsIndex = getFWidth() - 1;
        
        drawableCells.firstEvenCol = (area.left < cellsLengthPx)
            ? (int) Math.floor(area.left / cellSizes.x) : cellsIndex;
        
        drawableCells.lastEvenCol = (area.right < cellsLengthPx)
            ? (int) Math.floor(area.right / cellSizes.x) : cellsIndex;
        
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
        
        float shift = (cellCoords.y % 2 > 0) ? (cellSizes.x / 2f) : 0f;
        float overlapValue = (!overlap) ? 1f : 0f;
        
        RectF cellArea = new RectF();
        cellArea.left = (cellCoords.x * cellSizes.x) + shift;
        cellArea.right = cellArea.left + (cellSizes.x - overlapValue);        
        cellArea.top = cellCoords.y * (cellSizes.y * 0.75f);
        cellArea.bottom = cellArea.top + (cellSizes.y - overlapValue);
        
        return cellArea;
    }  
}