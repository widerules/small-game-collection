package org.example.sudoku;

import android.provider.BaseColumns;

public interface Contants extends BaseColumns {

	public static final String TABLE_NAME = "puzzles";
	
	public static  final String PUZZLE = "puzzle";
	public static final  String ANSWER = "answer";
	public static final String DIFFICULTY = "difficulty";
	public static final String HITS = "hits";
	public static final String SHORTEST_TIME = "shortesttime";
	public static final String LAST_PLAY_TIME = "lastplaytime";
}
