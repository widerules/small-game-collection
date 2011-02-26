package org.example.sudoku;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static org.example.sudoku.Contants.TABLE_NAME;
import static org.example.sudoku.Contants.PUZZLE;
import static org.example.sudoku.Contants.ANSWER;
import static org.example.sudoku.Contants.DIFFICULTY;
import static org.example.sudoku.Contants.HITS;
import static org.example.sudoku.Contants.SHORTEST_TIME;
import  static org.example.sudoku.Contants.LAST_PLAY_TIME;

public class PuzzlesData extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "puzzles.db";
	private static final int DATABASE_VERSION = 1;
	
	public PuzzlesData(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ PUZZLE  + " TEXT UNIQUE, "
				+ ANSWER + " TEXT, "
				+ DIFFICULTY + " INTEGER NOT NULL, "
				+ HITS + " INTEGER, "
				+ SHORTEST_TIME + " INTEGER, "
				+ LAST_PLAY_TIME + " INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	}

}
