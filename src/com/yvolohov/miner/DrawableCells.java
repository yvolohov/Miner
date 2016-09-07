package com.yvolohov.miner;

/**
 * Служебная структура данных для хранения диапазона номеров
 * ячеек, отображаемых на экране в конкретный момент времени
 * @author Ярослав Волохов
 */
class DrawableCells {  
    public int firstEvenCol, lastEvenCol;
    public int firstOddCol, lastOddCol;
    public int firstRow, lastRow;
}