package org.example.sudoku;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Game extends Activity {

	public static final String KEY_DIFFICULTY = "org.example.sudoku.difficulty";
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;
	
	private final int used[][][] = new int[9][9][];
	
	private int puzzle[] = new int[9 * 9];
	private PuzzleView puzzleView;
	
	private static final String TAG = "Sudoku.Game";
	@Override
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		int diff = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
		//puzzle = getPuzzle(diff);
		//calculateUsedTiles();
		
		puzzleView = new PuzzleView(this);
		setContentView(puzzleView);
		puzzleView.requestFocus();
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
		//setTile(x, y, value);
		//calculateUsedTiles();
		return true;
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
			//Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
			//Dialog  v = new Keypad(this, puzzleView);
			//v.show();
		}
	}
	
	private int  getTile(int x, int y) {
		return puzzle[y * 9 + x];
	}
}
