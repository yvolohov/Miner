package com.yvolohov.miner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * Класс отвечает за работу с главным меню игры 
 * @author yvolohov
 */
public class Menu extends Activity implements AdapterView.OnItemClickListener {

	public static final int HEAD_MENU = 0;
	public static final int SUB_MENU_SQU = 1;
	public static final int SUB_MENU_HEX = 2;

	private final int REQUEST_CODE_MINER = 10;
	private final int REQUEST_CODE_OWN_GAME_SQU = 11;
	private final int REQUEST_CODE_OWN_GAME_HEX = 12;

	private GlobalData globalData;
	private ArrayList<Map<String, String>> adapterData = new ArrayList<Map<String, String>>();
	private ListView lvMenuView;

	/**
	 * При создании Activity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		lvMenuView = (ListView) findViewById(R.id.lvMenuView);
		lvMenuView.setAdapter(new SimpleAdapter(this, adapterData,
				R.layout.menu_item, new String[] { "name", "desc", "groupId",
						"itemId" }, new int[] { R.id.tvMenuItemName,
						R.id.tvMenuItemDesc }));

		lvMenuView.setOnItemClickListener(this);

		globalData = (GlobalData) getApplicationContext();
		globalData.loadGlobalData();
		changeMenu(globalData.getMenuGroup());
	}

	/**
	 * При разрушении Activity
	 */
	@Override
	protected void onDestroy() {
		globalData.saveGlobalData();
		super.onDestroy();
	}

	/**
	 * Обработка возврата от разных Activities
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {

		case REQUEST_CODE_MINER:
			changeMenu(HEAD_MENU);
			break;

		case REQUEST_CODE_OWN_GAME_SQU:
			startGame(true, globalData.getDefWidth(),
					globalData.getDefHeight(), globalData.getDefMines());
			break;

		case REQUEST_CODE_OWN_GAME_HEX:
			startGame(false, globalData.getDefWidth(),
					globalData.getDefHeight(), globalData.getDefMines());
			break;

		}
	}

	/**
	 * Создает и открывает новую игру с предопределенным набором параметров
	 * @param level идентификатор набора параметров, значение одной из констант
	 * класса GameField: SIMPLE_LEVEL, MEDIUM_LEVEL, HARD_LEVEL,
	 * SIMPLE_LEVEL_HEX, MEDIUM_LEVEL_HEX, HARD_LEVEL_HEX
	 */
	private void startGame(int level) {

		Intent startGame = new Intent(this, Miner.class);
		Point screenSizes = new Point(240, 240);
		int densityGroup = getDensityGroup(getWindowManager());

		switch (level) {
		case GameField.SIMPLE_LEVEL:
		case GameField.MEDIUM_LEVEL:
		case GameField.HARD_LEVEL:
			globalData.setScreen(new SquGameScreen(new SquGameField(level),
					screenSizes, densityGroup));
			break;
		case GameField.SIMPLE_LEVEL_HEX:
		case GameField.MEDIUM_LEVEL_HEX:
		case GameField.HARD_LEVEL_HEX:
			globalData.setScreen(new HexGameScreen(new HexGameField(level),
					screenSizes, densityGroup));
			break;
		}

		startActivityForResult(startGame, REQUEST_CODE_MINER);
	}

	/**
	 * Создает и открывает новую игру с собственным набором параметров
	 * @param mode true - квадратные ячейки, false - шестиугольные ячейки;
	 * @param width ширина игрового поля;
	 * @param height высота игрового поля;
	 * @param mines количество мин на игровом поле;
	 */
	private void startGame(boolean mode, int width, int height, int mines) {

		Intent startGame = new Intent(this, Miner.class);
		Point screenSizes = new Point(240, 240);
		int densityGroup = getDensityGroup(getWindowManager());

		if (mode) {
			globalData.setScreen(new SquGameScreen(new SquGameField(width,
					height, mines), screenSizes, densityGroup));
		} else {
			globalData.setScreen(new HexGameScreen(new HexGameField(width,
					height, mines), screenSizes, densityGroup));
		}

		startActivityForResult(startGame, REQUEST_CODE_MINER);
	}

	/**
	 * Обрабатывает нажатие на пункт меню
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		SimpleAdapter adapter = (SimpleAdapter) (lvMenuView.getAdapter());

		@SuppressWarnings("unchecked")
		Map<String, String> item = (Map<String, String>) (adapter
				.getItem(position));
		int itemId = Integer.valueOf(item.get("itemId"));

		switch (itemId) {

		case 100:
			Intent startGame = new Intent(this, Miner.class);
			startActivityForResult(startGame, REQUEST_CODE_MINER);
			break;

		case 200:
			changeMenu(SUB_MENU_SQU);
			break;

		case 201:
			startGame(GameField.SIMPLE_LEVEL);
			break;

		case 202:
			startGame(GameField.MEDIUM_LEVEL);
			break;

		case 203:
			startGame(GameField.HARD_LEVEL);
			break;

		case 204:
			Intent intentOwnGameSqu = new Intent(this, OwnGame.class);
			startActivityForResult(intentOwnGameSqu, REQUEST_CODE_OWN_GAME_SQU);
			break;

		case 300:
			changeMenu(SUB_MENU_HEX);
			break;

		case 301:
			startGame(GameField.SIMPLE_LEVEL_HEX);
			break;

		case 302:
			startGame(GameField.MEDIUM_LEVEL_HEX);
			break;

		case 303:
			startGame(GameField.HARD_LEVEL_HEX);
			break;

		case 304:
			Intent intentOwnGameHex = new Intent(this, OwnGame.class);
			startActivityForResult(intentOwnGameHex, REQUEST_CODE_OWN_GAME_HEX);
			break;

		case 205:
		case 305:
			changeMenu(HEAD_MENU);
			break;

		case 400:
			Intent intentResults = new Intent(this, Results.class);
			startActivity(intentResults);
			break;

		case 500:
			Intent intentRules = new Intent(this, Rules.class);
			startActivity(intentRules);
			break;

		case 600:
			finish();
			break;
		}
	}

	/**
	 * Переопределяет клавишу BACK, чтобы нажатие на нее не закрывало Activity а
	 * выполняло переход на более высокий уровень меню
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && globalData.getMenuGroup() != HEAD_MENU) {
			changeMenu(HEAD_MENU);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Изменяет состав меню, выводит указанную группу пунктов меню
	 * @param menuGroup группа пунктов меню, которую следует вывести;
	 */
	private void changeMenu(int menuGroup) {

		globalData.setMenuGroup(menuGroup);
		adapterData.clear();

		int[][] menuStruct = getMenuStruct();

		for (int i = 0; i < menuStruct.length; i++) {

			int groupId = menuStruct[i][0];
			int itemId = menuStruct[i][1];

			// пункт меню "Продолжить" имеет смысл только если существует screen
			if (itemId == 100 && globalData.getScreen() == null)
				continue;

			String name = (menuStruct[i][2] > 0) ? getResources().getString(
					menuStruct[i][2]) : "";
			String desc = (menuStruct[i][3] > 0) ? getResources().getString(
					menuStruct[i][3]) : "";

			if (groupId == menuGroup) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("groupId", String.valueOf(groupId));
				item.put("itemId", String.valueOf(itemId));
				item.put("name", name);
				item.put("desc", desc);
				adapterData.add(item);
			}
		}

		SimpleAdapter adapter = (SimpleAdapter) (lvMenuView.getAdapter());
		adapter.notifyDataSetChanged();
	}

	/**
	 * Возвращает двумерный массив с данными для создания меню, массивы второго
	 * уровня содержат: идентификатор группы пунктов меню; идентификатор пункта
	 * меню; идентификатор ресурса строки названия пункта меню; идентификатор
	 * ресурса строки описания пункта меню;
	 * @return двумерный массив;
	 */
	private int[][] getMenuStruct() {

		// group id, item id, item name id, item desc id
		int[][] menuStruct = new int[][] {
				{ HEAD_MENU, 100, R.string.item_continue, R.string.sub_item_continue },
				{ HEAD_MENU, 200, R.string.item_new, R.string.sub_item_new_squ },
				{ SUB_MENU_SQU, 201, R.string.item_new_simple, R.string.sub_item_new_simple },
				{ SUB_MENU_SQU, 202, R.string.item_new_medium, R.string.sub_item_new_medium },
				{ SUB_MENU_SQU, 203, R.string.item_new_hard, R.string.sub_item_new_hard },
				{ SUB_MENU_SQU, 204, R.string.item_new_own, R.string.sub_item_new_own },
				{ SUB_MENU_SQU, 205, R.string.item_return, R.string.sub_item_return },
				{ HEAD_MENU, 300, R.string.item_new, R.string.sub_item_new_hex },
				{ SUB_MENU_HEX, 301, R.string.item_new_simple, R.string.sub_item_new_simple },
				{ SUB_MENU_HEX, 302, R.string.item_new_medium, R.string.sub_item_new_medium },
				{ SUB_MENU_HEX, 303, R.string.item_new_hard, R.string.sub_item_new_hard },
				{ SUB_MENU_HEX, 304, R.string.item_new_own, R.string.sub_item_new_own },
				{ SUB_MENU_HEX, 305, R.string.item_return, R.string.sub_item_return },
				{ HEAD_MENU, 400, R.string.item_results, R.string.sub_item_results },
				{ HEAD_MENU, 500, R.string.item_rules, R.string.sub_item_rules },
				{ HEAD_MENU, 600, R.string.item_exit, R.string.sub_item_exit } };

		return menuStruct;
	}

	/**
	 * Возвращает группу плотности экрана, значение одной 
	 * из констант класса GameScreen - LOW_DENSITY,
	 * MEDIUM_DENSITY или HIGH_DENSITY;
	 * @param wm объект, из которого получаем плотность экрана; 
	 * @return группа плотности экрана;
	 */
	private int getDensityGroup(WindowManager wm) {

		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);

		if (metrics.densityDpi <= DisplayMetrics.DENSITY_LOW)
			return GameScreen.LOW_DENSITY;
		else if (metrics.densityDpi <= DisplayMetrics.DENSITY_MEDIUM)
			return GameScreen.MEDIUM_DENSITY;
		else if (metrics.densityDpi <= DisplayMetrics.DENSITY_HIGH)
			return GameScreen.HIGH_DENSITY;
		else
			return GameScreen.HIGH_DENSITY;
	}
}
