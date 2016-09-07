package com.yvolohov.miner;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Класс хранит данные приложения, доступные из любой Activity
 * @author yvolohov
 */
public class GlobalData extends Application {

	public static final String SAVE_FILE = "miner_saves";
	public static final String PRE_FILE = "miner_prefs";
	public static final String PRE_DEF_WIDTH = "pre_def_width";
	public static final String PRE_DEF_HEIGHT = "pre_def_height";
	public static final String PRE_DEF_MINES = "pre_def_mines";
	public static final String PRE_RECORD = "pre_record_";

	public static final int DEF_WIDTH = 16;
	public static final int DEF_HEIGHT = 16;
	public static final int DEF_MINES = 40;	
	
	private GameScreen screen;

	private int defWidth;
	private int defHeight;
	private int defMines;
	private int menuGroup = Menu.HEAD_MENU;
	
	private long[] records = new long[6];	
	
	/**
	 * Возвращает объект игрового экрана
	 * @return игровой экран;
	 */
	public GameScreen getScreen() {return screen;}

	/**
	 * Устанавливает объект игрового экрана
	 * @param screen игровой экран;
	 */
	public void setScreen(GameScreen screen) {this.screen = screen;}

	/**
	 * Возвращает ширину игрового поля для режима игры
	 * с собственными настройками
	 * @return ширина;
	 */
	public int getDefWidth() {return defWidth;}

	/**
	 * Устанавливает ширину игрового поля для режима игры
	 * с собственными настройками
	 * @param defWidth ширина;
	 */
	public void setDefWidth(int defWidth) {this.defWidth = defWidth;}

	/**
	 * Возвращает высоту игрового поля для режима игры
	 * с собственными настройками
	 * @return высота;
	 */	
	public int getDefHeight() {return defHeight;}

	/**
	 * Устанавливает высоту игрового поля для режима игры
	 * с собственными настройками
	 * @param defWidth высота;
	 */
	public void setDefHeight(int defHeight) {this.defHeight = defHeight;}

	/**
	 * Возвращает количество мин на поле для режима игры
	 * с собственными настройками
	 * @return количество мин на поле;
	 */	
	public int getDefMines() {return defMines;}

	/**
	 * Устанавливает количество мин на поле для режима игры
	 * с собственными настройками
	 * @param defWidth количество мин на поле;
	 */	
	public void setDefMines(int defMines) {this.defMines = defMines;}

	/**
	 * Возвращает идентификатор активной группы пунктов меню
	 * @return группа пунктов меню;
	 */
	public int getMenuGroup() {return menuGroup;}

	/**
	 * Устанавливает идентификатор активной группы пунктов меню;
	 * @param menuGroup группа пунктов меню;
	 */
	public void setMenuGroup(int menuGroup) {this.menuGroup = menuGroup;}
	
	/**
	 * Устанавливает значение игрового рекорда для
	 * указанного уровня игры 
	 * @param level уровень игры - значение одной из констант класса
	 * GameField SIMPLE_LEVEL, MEDIUM_LEVEL, HARD_LEVEL, 
	 * SIMPLE_LEVEL_HEX, MEDIUM_LEVEL_HEX и HARD_LEVEL_HEX;
	 * @param record рекорд, миллисекунд;
	 */
	public void setRecord(int level, long record) {
		if (level < 1 || level > 6) return;
		records[level - 1] = record;
	}
	
	/**
	 * Возвращает значение игрового рекорда для
	 * указанного уровня игры
	 * @param level уровень игры - значение одной из констант класса
	 * GameField SIMPLE_LEVEL, MEDIUM_LEVEL, HARD_LEVEL, 
	 * SIMPLE_LEVEL_HEX, MEDIUM_LEVEL_HEX и HARD_LEVEL_HEX;
	 * @return рекорд, миллисекунд;
	 */
	public long getRecord(int level) {
		if (level < 1 || level > 6) return 0;
		else return records[level - 1];
	}
	
	/**
	 * Очищает все игровые рекорды
	 */
	public void clearRecords() {
		for (int count = 0; count < records.length; count++)
			records[count] = 0;
	}
	
	/**
	 * Записывает состояние игры на внутреннюю память устройства
	 */
	public void saveGlobalData() {
    	SharedPreferences prefs = getSharedPreferences(GlobalData.PRE_FILE, MODE_PRIVATE);
    	Editor editor = prefs.edit();
    	
    	editor.putInt(PRE_DEF_WIDTH, defWidth);
    	editor.putInt(PRE_DEF_HEIGHT, defHeight);
    	editor.putInt(PRE_DEF_MINES, defMines);

    	for (int count = 0; count < records.length; count++)
    		editor.putLong(PRE_RECORD + String.valueOf(count), records[count]);
    	
    	editor.commit();
    	
    	try {
    		FileOutputStream fos = openFileOutput(SAVE_FILE, MODE_PRIVATE);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);
    		oos.writeObject(screen);
    		oos.close();
    	}
    	catch (Exception e) {}	
    }
    
	/**
	 * Считывает состояние игры из внутренней памяти устройства
	 */
    public void loadGlobalData() {
    	SharedPreferences prefs = getSharedPreferences(PRE_FILE, MODE_PRIVATE);
    	defWidth = prefs.getInt(PRE_DEF_WIDTH, DEF_WIDTH);
    	defHeight = prefs.getInt(PRE_DEF_HEIGHT, DEF_HEIGHT);
    	defMines = prefs.getInt(PRE_DEF_MINES, DEF_MINES);
    	
    	for (int count = 0; count < records.length; count++)
    		records[count] = prefs.getLong(PRE_RECORD + String.valueOf(count), 0);
    	
    	try {
    		FileInputStream fis = openFileInput(SAVE_FILE);
    		ObjectInputStream ois = new ObjectInputStream(fis);
    		screen = (GameScreen) ois.readObject();
    		ois.close();
    	}
    	catch (Exception e) {}
    }   
}
