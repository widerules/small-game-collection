package org.example.sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import static android.provider.BaseColumns._ID;
import static org.example.sudoku.Contants.TABLE_NAME;
import static org.example.sudoku.Contants.PUZZLE;
import static org.example.sudoku.Contants.ANSWER;
import static org.example.sudoku.Contants.HITS;
import static org.example.sudoku.Contants.DIFFICULTY;
import static org.example.sudoku.Contants.SHORTEST_TIME;
import static org.example.sudoku.Contants.LAST_PLAY_TIME;

public class Game extends Activity {

	public static final String KEY_DIFFICULTY = "org.example.sudoku.difficulty";
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;
	
	private final int used[][][] = new int[9][9][];
	
	private int puzzle[] = new int[9 * 9];
	private PuzzleView puzzleView;
	
	private PuzzlesData puzzles;
	
	private static final String TAG = "Sudoku.Game";
	private static final String PREF_PUZZLE = "puzzle";
	protected static final int DIFFICULTY_CONTINUE = -1;
	
	private  final String easyPuzzle = 
		"360000000004230800000004200" +
		"070460003820000014500013020" +
		"001900000007048300000000045";
//	private final String mediumPuzzle = 
//		"650000070000506000014000005" +
//		"007009000002314700000700800" +
//		"500000630000201000030000097";
//	private final String hardPuzzle =
//		"009000000080605020501078000" +
//		"000000700706040102004000000" +
//		"000720903090301080000000600";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		puzzles = new PuzzlesData(this);
		
		int diff = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
		puzzle = getPuzzle(diff);
		calculateUsedTiles();
		
		puzzleView = new PuzzleView(this);
		setContentView(puzzleView);
		puzzleView.requestFocus();
		
		getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Music.play(this, R.raw.game);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Music.stop(this);
		
		getPreferences(MODE_PRIVATE).edit().putString(PREF_PUZZLE, 
				toPuzzleString(puzzle)).commit();
	}
	
	protected int[] getUsedTiles(int x, int y) {
		return used[x][y];
	}
	
	protected boolean setTileIfValid(int x, int  y,  int value) {
		int tiles[] = getUsedTiles(x, y);
		if(value != 0) {
			for(int tile : tiles) {
				if(tile == value)
					return false;
			}
		}
		setTile(x, y, value);
		calculateUsedTiles();
		return true;
	}
	
	private void calculateUsedTiles() {
		for(int x = 0; x < 9; x++) {
			for(int y = 0; y < 9; y++) {
				used[x][y] = calculateUsedTiles(x, y);
			}
		}
	}
	
	private int[] calculateUsedTiles(int  x, int y) {
		int c[] = new int[9];
		for(int i = 0; i < 9; i++) {
			if(i == y) continue;
			int t = getTile(x, i);
			if(t != 0) c[t-1] = t;
		}
		for(int i = 0; i < 9;  i++)  {
			if(i == x) continue;
			int t = getTile(i, y);
			if(t !=  0) c[t - 1] = t;
		}
		int startx = (x / 3) * 3;
		int starty = (y  /  3) * 3;
		for(int i = startx; i < startx + 3; i++) {
			for(int j = starty; j < starty  + 3; j++){
				if(i  == x && j == y)
					continue;
				int t = getTile(i,  j);
				if(t != 0)
					c[t - 1] = t;
			}
		}
		int nused = 0;
		for(int t : c) {
			if(t != 0) nused++;
		}
		
		int c1[] = new int[nused];
		nused = 0;
		for(int t : c) {
			if(t != 0) {
				c1[nused++] = t;
			}
		}
		return c1;
	}
	
	protected  String getTileString(int x, int y) {
		int v = getTile(x, y);
		if(v == 0) return "";
		else return String.valueOf(v);
	}
	
	protected void showKeypadOrError(int x, int  y) {
		int tiles[] = getUsedTiles(x, y);
		if(tiles.length == 9) {
			Toast toast  = Toast.makeText(this, R.string.no_moves_label, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} else {
			Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
			Dialog  v = new Keypad(this, tiles, puzzleView);
			v.show();
		}
	}
	
	private int  getTile(int x, int y) {
		return puzzle[y * 9 + x];
	}
	
	private void setTile(int x, int y, int value) {
		puzzle[y *  9 + x] = value;
	}
	
	private int[] getPuzzle(int diff) {
		String puz = null;
		if(diff == DIFFICULTY_CONTINUE) {
			puz = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE, easyPuzzle);
		} else  {
			puz = getPuzzleFromLocal(diff);
			if(puz == null) {
				puz = getPuzzleFromInternet(diff);
			}
		}
		if(puz  == null)  {
			puz = easyPuzzle;
		}
		return fromPuzzleString(puz);
	}
	
	private String getPuzzleFromLocal(int diff) {
		String puz = null;
		long hits = 0;
		SQLiteDatabase db = puzzles.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[]{_ID, PUZZLE, ANSWER, HITS}, 
				DIFFICULTY + "=" + diff
				+ " AND " + SHORTEST_TIME + "=0", null, null, null, HITS + " ASC", "1");
		if(cursor.moveToNext()) {
			puzId = cursor.getLong(0);
			puz = cursor.getString(1);
			//puzAnsw = cursor.getString(2);
			hits = cursor.getLong(3);
		}
		cursor.close();
		db.close();
		if(puz != null) {
			updatePuzzleHits(++hits);
		}
		return puz;
	}
	
	private void updatePuzzleHits(long hits) {
		SQLiteDatabase db = puzzles.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HITS, hits);
		values.put(LAST_PLAY_TIME, System.currentTimeMillis());
		db.update(TABLE_NAME, values, _ID + "=" + puzId, null);
		db.close();
	}
	
	private long addPuzzle(String puz, int diff, boolean  selectedPuz) {
		SQLiteDatabase db = puzzles.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PUZZLE, puz);
		values.put(HITS, selectedPuz ? 0 : 1);
		values.put(DIFFICULTY, diff);
		values.put(SHORTEST_TIME, 0);
		values.put(LAST_PLAY_TIME, 0);
		long id = db.insert(TABLE_NAME, null, values);
		if(!selectedPuz) {
			puzId = id;
		}
		db.close();
		return  id;
	}
	
	private long puzId =  -1;
	//private String puzAnsw = null;
	
	private final static String SITE="http://192.168.1.101:8080/sudoku-servlet/sudoku?level=";
	private String getPuzzleFromInternet(int diff) {
		String result = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(SITE + diff);
			conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String payload = reader.readLine();
			reader.close();
			
			JSONObject jsonObject = new JSONObject(payload);
			if(!jsonObject.has("error"))  {
				JSONArray puzs = jsonObject.getJSONArray("puzzle");
				if(puzs != null) {
					boolean selectedPuz = false;
					for(int i = 0; i < puzs.length(); i++) {
						String puz = puzs.getString(i);
						if(addPuzzle(puz, diff, selectedPuz) != -1 && !selectedPuz) {
							selectedPuz = true;
							result = puz;
						}
					}
				}
			}
		} catch (IOException e) {
			Log.e(TAG,  "IOException", e);
		} catch (JSONException e) {
			Log.e(TAG, "JSONException", e);
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
		
		Log.d(TAG, "Get puzzle from Internet: " + result);
		return  result;
	}
	
	static private String toPuzzleString(int[] puz) {
		StringBuilder buf = new StringBuilder();
		for(int element : puz) {
			buf.append(element);
		}
		return buf.toString();
	}
	
	static protected int[] fromPuzzleString(String string) {
		int[] puz = new int[string.length()];
		for(int i = 0; i < puz.length; i++) {
			puz[i] = string.charAt(i) - '0';
		}
		return puz;
	}
}
