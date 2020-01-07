package com.battlesnake.data;

import com.battlesnake.math.Point;

public class MovePoint {

    private Move initialMove;
    private Move move;
    private Point point;
    private int length;

    public MovePoint(Move move, Point point, Move initialMove) {
        this.move = move;
        this.point = point;
        this.initialMove = initialMove;
        setLength(0);
    }

    public boolean equals(MovePoint other) {
        return getPoint().equals(other.getPoint());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MovePoint) return equals((MovePoint) other);
        return false;
    }

    public int getLength(){
        return length;
    }

    public void setLength(int i){
        length = i;
    }

    public Point getPoint(){
        return point;
    }

    public Move getMove(){
        return move;
    }

    public Move getInitialMove(){
        return initialMove;
    }
}
