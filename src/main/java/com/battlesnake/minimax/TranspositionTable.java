package com.battlesnake.minimax;

import com.battlesnake.board.Tile;
import com.battlesnake.data.MoveValue;

import java.util.HashMap;

public class TranspositionTable {

    private int key;
    private int depth;
    private int flags;
    private MoveValue best;
    private int size;

    public enum Hash {
        EXACT,
        ALPHA,
        BETA;
    }

//    public int getKeyFromBoard(Tile[][] board){
//        for(int i = 0; i < size; i++){
//            for(int j = 0; j < size; j++){
//
//            }
//        }
//    }
}


