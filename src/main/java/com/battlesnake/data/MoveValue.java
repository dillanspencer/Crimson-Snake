package com.battlesnake.data;

public class MoveValue {

    public double returnValue;
    public Move returnMove;

    public MoveValue() {
        returnValue = 0;
    }

    public MoveValue(double returnValue) {
        this.returnValue = returnValue;
    }

    public MoveValue(double returnValue, Move returnMove) {
        this.returnValue = returnValue;
        this.returnMove = returnMove;
    }
}
