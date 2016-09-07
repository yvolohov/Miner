package com.yvolohov.miner;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Класс отвечает за вывод игры "Сапер" на экран устройства
 * а также за взаимодействие с пользователем.
 * Если gameField реализует Model игры, то этот класс View+Controller.
 * @author yvolohov
 */
@SuppressWarnings("serial")
public class HexGameScreen extends GameScreen {
    
    /**
     * @param hexGameField игровое поле с шестиугольными ячейками;
     * @param screenSizes размеры экрана устройства;
     * @param densityGroup плотность, точек на дюйм; 
     */
    public HexGameScreen(HexGameField hexGameField, Point screenSizes, 
        int densityGroup) {
        super(hexGameField, screenSizes, densityGroup);
    }
    
    // -= ABSTRACT METHODS IMPLEMENTATION =-

    /**
     * Создает новый экземпляр игрового экрана,
     * копируя все настройки текущего экземпляра;
     * @return новый экземпляр игрового экрана
     */	
	public GameScreen reCreate() {
		HexGameField field = (HexGameField)(getGameField().reCreate());
		Point screenSizes = new Point(getSWidth(), getSHeight());
		return new HexGameScreen(field, screenSizes, getDensityGroup());
	}    
    
    /**
     * Получает размеры ячеек при разных плотностях и масштабах.
     * Это реализация метода для поля с шестиугольными ячейками.
     * @param densityGroup плотность, одна из констант LOW_DENSITY,
     * MEDIUM_DENSITY, HIGH_DENSITY;
     * @param scale масштаб, одна из констант LOW_SCALE, 
     * MEDIUM_SCALE, HIGH_SCALE;
     * @return размеры ячейки в пикселах, ширина и высота
     */    
    @Override
    public PointF getCellSizes(int densityGroup, int scale) {
    
        int[][][] sizes = new int[][][] {
            {{16, 20}, {32, 36}, {48, 56}},
            {{22, 24}, {44, 52}, {66, 76}},
            {{32, 36}, {66, 76}, {98, 112}}
        };
        
        float width = (float) sizes[densityGroup][scale][0];
        float height = (float) sizes[densityGroup][scale][1];
        
        return new PointF(width, height);
    }
    
	@Override
	public int getMapId(int densityGroup, int scale) {

		int mapId = 0;

		if (densityGroup <= GameScreen.LOW_DENSITY) {
			if (scale <= GameScreen.LOW_SCALE)
				mapId = R.drawable.hex_map_16x20;
			else if (scale <= GameScreen.MEDIUM_SCALE)
				mapId = R.drawable.hex_map_32x36;
			else
				mapId = R.drawable.hex_map_48x56;
		} else if (densityGroup <= GameScreen.MEDIUM_DENSITY) {
			if (scale <= GameScreen.LOW_SCALE)
				mapId = R.drawable.hex_map_22x24;
			else if (scale <= GameScreen.MEDIUM_SCALE)
				mapId = R.drawable.hex_map_44x52;
			else
				mapId = R.drawable.hex_map_66x76;
		} else {
			if (scale <= GameScreen.LOW_SCALE)
				mapId = R.drawable.hex_map_32x36;
			else if (scale <= GameScreen.MEDIUM_SCALE)
				mapId = R.drawable.hex_map_66x76;
			else
				mapId = R.drawable.hex_map_98x112;
		}

		return mapId;
	}   
}