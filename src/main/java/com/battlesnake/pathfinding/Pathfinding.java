package com.battlesnake.pathfinding;

import com.battlesnake.board.Tile;
import com.battlesnake.board.TileType;
import com.battlesnake.math.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Pathfinding {

    private Tile[][] tiles;
    private Tile currentTile;
    private Point endPosition;
    private int maxWidth;
    private int maxHeight;
    private int newestScore;

    private final TileScoreComparator tileScoreComparator = new TileScoreComparator();

    public List<Tile> getRoute(Tile[][] tiles, Integer[][] regions, Point startPosition, Point endPosition) {
        this.tiles = tiles;
        this.maxWidth = tiles.length;
        this.maxHeight = tiles[0].length;
        this.endPosition = endPosition;
        this.newestScore = 0;

        resetAllTiles();

        PriorityQueue<Tile> queue = new PriorityQueue<>(tileScoreComparator);
        queue.add(tiles[startPosition.getX()][startPosition.getY()]);

        boolean routeAvailable = false;

        while (!queue.isEmpty()) {

            do {
                if (queue.isEmpty()) break;
                currentTile = queue.remove();
            } while (!currentTile.isOpen());

            currentTile.setOpen(false);

            int currentX = currentTile.getRowNumber();
            int currentY = currentTile.getColNumber();
            int currentScore = currentTile.getScore();

            if (currentTile.getRowNumber() == endPosition.getX() && currentTile.getColNumber() == endPosition.getY()) {
                // at the end, return path
                routeAvailable = true;
                break;
            }

            // loop through neighbours and get scores. add these onto temp open list
            int smallestScore = 9999999;
            for (int x = -1; x <= 1; x+=2) {
                int nextX = currentX + x;
                // currentY is now nextY
                if (validTile(nextX, currentY)) {
                    int score = getScoreOfTile(regions, tiles[nextX][currentY], currentScore);
                    newestScore += score;
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    Tile thisTile = tiles[nextX][currentY];
                    thisTile.setScore(score);
                    queue.add(thisTile);
                    thisTile.setParent(currentTile);
                }
            }

            for (int y = -1; y <= 1; y+=2) {
                // currentX is now nextX
                int nextY = currentY + y;
                if (validTile(currentX, nextY)) {
                    int score = getScoreOfTile(regions, tiles[currentX][nextY], currentScore);
                    newestScore += score;
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    Tile thisTile = tiles[currentX][nextY];
                    thisTile.setScore(score);
                    queue.add(thisTile);
                    thisTile.setParent(currentTile);
                }
            }

        }

        // get List of tiles using current tile
        // returns reverse list btw
        if (routeAvailable) return getPath(currentTile);
        return new ArrayList<>();
    }

    private void resetAllTiles() {
        for (Tile[] tile : tiles) {
            for (int col = 0; col < tiles[0].length; col++) {
                tile[col].setOpen(true);
                tile[col].setParent(null);
                tile[col].setScore(0);

            }
        }
    }

    private List<Tile> getPath(Tile currentTile) {
        List<Tile> path = new ArrayList<>();
        while (currentTile != null) {
            path.add(currentTile);
            currentTile = currentTile.getParent();
        }
        return path;
    }

    private int distanceScoreAway(Tile currentTile) {
        return Math.abs(endPosition.getX() - currentTile.getColNumber()) + Math.abs(endPosition.getY() - currentTile.getRowNumber());
    }

    private int distanceFromEdges(Tile currentTile){
        return Math.abs(currentTile.getX() - (maxWidth/2)) + Math.abs(currentTile.getY() - (maxHeight/2));
    }

    public int getScoreOfTile(Integer[][] regions, Tile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);
        int centerCost = distanceFromEdges(tile) * 10;
        int neighborCost = (regions[tile.getX()][tile.getY()]);
        int extraMovementCost = 0;
        if (tile.getTileType() == TileType.FAKE_WALL) {
            extraMovementCost+=1000;
        }
        if(tile.getTileType() == TileType.FOOD){
            extraMovementCost -= 50;
        }
        int movementScore = currentScore + 1;
        return guessScoreLeft + movementScore + extraMovementCost + centerCost - neighborCost;
    }

    public int evaluateTile(Tile tile){
        int centerCost = distanceFromEdges(tile) * 10;
        int neighborCost = checkNeighbours(tile);
        int extraMovementCost = 0;
        if (tile.getTileType() == TileType.FAKE_WALL) {
            extraMovementCost+=1000;
        }

        return centerCost;
    }

    public int checkNeighbours(Tile tile){
        int filled = 0;
        for (int x = -1; x <= 1; x+=2) {
            if(!validTile(tile.getX()+x, tile.getY())) filled++;
        }
        for (int y = -1; y <= 1; y+=2) {
            if(!validTile(tile.getX(), tile.getY()+y)) filled++;
        }
        return filled * 10;
    }

    private boolean validTile(int nextX, int nextY) {
        if (nextX >= 0 && nextX < maxWidth) {
            if (nextY >= 0 && nextY < maxHeight) {
                return tiles[nextX][nextY].isOpen() &&
                        tiles[nextX][nextY].getTileType() != TileType.WALL
                        && tiles[nextX][nextY].getTileType() != TileType.FAKE_WALL;

            }
        }
        return false;
    }

    public int getNewestScore(){ return newestScore; }

}
