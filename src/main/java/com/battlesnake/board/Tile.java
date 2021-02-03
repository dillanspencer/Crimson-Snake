package com.battlesnake.board;

public class Tile {

    private final int rowNumber;
    private final int colNumber;
    private int x;
    private int y;

    private TileType tileType;
    private int score;
    private Tile parent;
    private boolean open;

    public Tile(TileType tileType, int x, int y) {
        rowNumber = x;
        colNumber = y;
        this.tileType = tileType;
        this.x = x;
        this.y = y;
        score = 0;
        parent = null;
        open = true;
    }

    public Tile(Tile tile) {
        rowNumber = tile.rowNumber;
        colNumber = tile.colNumber;
        this.tileType = tile.getTileType();
        this.x = tile.getX();
        this.y = tile.getY();
        score = 0;
        parent = null;
        open = true;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getColNumber() {
        return colNumber;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public TileType getTileType() {
        return tileType;
    }

    public void setTileType(TileType tileType) {
        this.tileType = tileType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Tile getParent() {
        return parent;
    }

    public void setParent(Tile parent) {
        this.parent = parent;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tile) {
            return rowNumber == ((Tile) obj).rowNumber && colNumber == ((Tile) obj).colNumber;
        }
        return false;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
