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
public class SquGameScreen extends GameScreen {

    /**
     * @param squGameField игровое поле с квадратными ячейками;
     * @param screenSizes размеры экрана устройства;
     * @param densityGroup плотность, точек на дюйм; 
     */    
    public SquGameScreen(SquGameField squGameField, Point screenSizes, 
        int densityGroup) {
        super(squGameField, screenSizes, densityGroup);
    }

    // -= ABSTRACT METHODS IMPLEMENTATION =-    

    /**
     * Создает новый экземпляр игрового экрана,
     * копируя все настройки текущего экземпляра;
     * @return новый экземпляр игрового экрана
     */	
	public GameScreen reCreate() {
		SquGameField field = (SquGameField)(getGameField().reCreate());
		Point screenSizes = new Point(getSWidth(), getSHeight());
		return new SquGameScreen(field, screenSizes, getDensityGroup());
	}    
    
    /**
     * Получает размеры ячеек при разных плотностях и масштабах.
     * Это реализация метода для поля с квадратными ячейками.
     * @param densityGroup плотность, одна из констант LOW_DENSITY,
     * MEDIUM_DENSITY, HIGH_DENSITY;
     * @param scale масштаб, одна из констант LOW_SCALE, 
     * MEDIUM_SCALE, HIGH_SCALE;
     * @return размеры ячейки в пикселах, ширина и высота
     */     
    @Override
    public PointF getCellSizes(int densityGroup, int scale) {
        
        int[][][] sizes = new int[][][] {
            {{16, 16}, {32, 32}, {48, 48}},
            {{22, 22}, {44, 44}, {66, 66}},
            {{32, 32}, {66, 66}, {98, 98}}
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
				mapId = R.drawable.squ_map_16x16;
			else if (scale <= GameScreen.MEDIUM_SCALE)
				mapId = R.drawable.squ_map_32x32;
			else
				mapId = R.drawable.squ_map_48x48;
		} else if (densityGroup <= GameScreen.MEDIUM_DENSITY) {
			if (scale <= GameScreen.LOW_SCALE)
				mapId = R.drawable.squ_map_22x22;
			else if (scale <= GameScreen.MEDIUM_SCALE)
				mapId = R.drawable.squ_map_44x44;
			else
				mapId = R.drawable.squ_map_66x66;
		} else {
			if (scale <= GameScreen.LOW_SCALE)
				mapId = R.drawable.squ_map_32x32;
			else if (scale <= GameScreen.MEDIUM_SCALE)
				mapId = R.drawable.squ_map_66x66;
			else
				mapId = R.drawable.squ_map_98x98;
		}

		return mapId;
	}  
}