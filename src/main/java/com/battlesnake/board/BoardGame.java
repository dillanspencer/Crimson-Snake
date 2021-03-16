package com.battlesnake.board;

import com.battlesnake.data.Move;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.battlesnake.pathfinding.Pathfinding;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BoardGame {

    // board size
    private int width;
    private int height;
    private final int TILE_WIDTH = 1;
    private final int TILE_HEIGHT = 1;

    // Pathfinding
    private Pathfinding pathfinding;

    // my snake
    private Snake mySnake;

    // board data
    private List<Snake> snakes;
    private List<Snake> deadSnakes;
    private List<Point> food;

    private Tile[][] board;

    public void init(Snake mySnake) {
        this.mySnake = mySnake;
        this.pathfinding = new Pathfinding();
        setupBoard();
    }

    private void setupBoard() {
        if (board == null)
            board = new Tile[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                board[x][y] = new Tile(TileType.EMPTY, x, y);
            }
        }

        for (Point snack : food) {
            board[snack.getX()][snack.getY()] = new Tile(TileType.FOOD, snack.getX(), snack.getY());
        }

        for (Snake snake : snakes) {
            List<Point> body = snake.getBody();
            Point head = body.get(0);
            for (int i = 0; i < body.size(); i++) {
                if ((i == body.size() - 1)
                        && body.size() > 1
                        && !snake.justAte()) {
                    board[body.get(i).getX()][body.get(i).getY()] = new Tile(TileType.TAIL, body.get(i).getX(), body.get(i).getY());
                } else {
                    if (body.get(i).getX() < 0 || body.get(i).getY() < 0)
                    board[body.get(i).getX()][body.get(i).getY()] = new Tile(TileType.WALL, body.get(i).getX(), body.get(i).getY());
                }
            }

            if (snake.equals(getMySnake())) {
                board[head.getX()][head.getY()] = new Tile(TileType.ME, head.getX(), head.getY());
            } else {
                board[head.getX()][head.getY()] = new Tile(TileType.HEADS, head.getX(), head.getY());

                if (!getMySnake().longerThan(snake)) {
                    List<Point> around = findAdjacent(head);
                    for (Point point : around) {
                        if (exists(point)) {
                            if (board[point.getX()][point.getY()].getTileType() == TileType.EMPTY
                                    || board[point.getX()][point.getY()].getTileType() == TileType.FOOD
                                    || board[point.getX()][point.getY()].getTileType() == TileType.TAIL) {
                                board[point.getX()][point.getY()].setTileType(TileType.FAKE_WALL);
                            }
                        }
                    }
                }
            }
        }
    }

    // Checks if point exist within the bounds of the board
    public boolean exists(Point point) {
        if (point.getX() < 0) return false;
        if (point.getY() < 0) return false;
        if (point.getX() > getWidth() - 1) return false;
        if (point.getY() > getHeight() - 1) return false;
        return true;
    }

    // Takes in a point and returns all adjacent points
    private List<Point> findAdjacent(Point point) {
        return new ArrayList<>(Move.adjacent(point).values());
    }

    private boolean isFilled(Point point, Tile[][] board, boolean flag) {
        if (!exists(point)) return true;

        if(flag) {
            return board[point.getX()][point.getY()].getTileType() != TileType.EMPTY
                    && board[point.getX()][point.getY()].getTileType() != TileType.FOOD
                    && board[point.getX()][point.getY()].getTileType() != TileType.TAIL
                    && board[point.getX()][point.getY()].getTileType() != TileType.HEADS;
        }else{
            return board[point.getX()][point.getY()].getTileType() != TileType.EMPTY
                    && board[point.getX()][point.getY()].getTileType() != TileType.FOOD
                    && board[point.getX()][point.getY()].getTileType() != TileType.TAIL
                    && board[point.getX()][point.getY()].getTileType() != TileType.HEADS
                    && board[point.getX()][point.getY()].getTileType() != TileType.FAKE_WALL;
        }
    }


    private boolean movable(Point point, boolean flag) {
        return !isFilled(point, board, flag);
    }

    private List<Move> getPossibleMoves(Point point, boolean flag) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Move, Point> move : Move.adjacent(point).entrySet()) {
            if (movable(move.getValue(), flag))
                moves.add(move.getKey());
        }
        return moves;
    }

    private Move findBestPossibleMove(Point point){

        List<Move> moves = getPossibleMoves(point, false);
        if(moves.size() < 1) {
            moves = getPossibleMoves(point, true);
        }

        if(moves.size() < 1) return Move.UP;

        Move returnMove = null;
        int smallest = 999999;
        int score = 0;
        for(Move move:moves){
            Point p = move.translate(point);
            Tile t = board[p.getX()][p.getY()];
            score = pathfinding.checkNeighbours(t);
            System.out.println("SCORE: " + score);
            if(score < smallest){
                smallest = score;
                returnMove = move;
            }
        }
        return returnMove;
    }


    private Move moveToTile(Tile tile, Point point) {
        Point p = new Point(tile.getX(), tile.getY());
        for (Map.Entry<Move, Point> move : Move.adjacent(point).entrySet()) {
            if (p.equals(move.getValue())) return move.getKey();
        }
        return null;
    }

    private Point nearestFood(Point current) {
        int min = 1000;
        int dist = 0;
        Point found = food.get(0);
        for (Point snack : food) {
            dist = Math.abs((width / 2) - snack.getX()) + (Math.abs((height / 2) - snack.getY()));
            if (dist < min) {
                min = dist;
                found = snack;
            }
        }
        return found;
    }

    public Move findFood(Point current) {
        List<Tile> path = pathfinding.getRoute(board, current, nearestFood(current));
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);
        return move;
    }

    public Move findTail(Point current) {
        List<Tile> path = pathfinding.getRoute(board, current, mySnake.getTail());
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);

        return move;
    }

    public Move findHead(Point current, Snake enemy) {
        if (enemy == null || enemy.longerThan(mySnake)) return findTail(current);
        List<Tile> path = pathfinding.getRoute(board, current, enemy.getHead());
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);

        return move;
    }

    public Move findCenter(Point current) {
        Point center = new Point(width / 2, height / 2);
        List<Tile> path = pathfinding.getRoute(board, current, center);
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);

        return move;
    }

    public Move findExit(Point current) {
        return findBestPossibleMove(current);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Snake getMySnake() {
        return mySnake;
    }

    public void setMySnake(Snake mySnake) {
        this.mySnake = mySnake;
    }

    public List<Snake> getSnakes() {
        return snakes;
    }

    public void setSnakes(List<Snake> snakes) {
        this.snakes = snakes;
    }

    public List<Snake> getDeadSnakes() {
        return deadSnakes;
    }

    public void setDeadSnakes(List<Snake> deadSnakes) {
        this.deadSnakes = deadSnakes;
    }

    @JsonProperty("food")
    public List<Point> getFood() {
        return food;
    }

    public void setFood(List<Point> food) {
        this.food = food;
    }

    public Tile[][] getBoard(){ return board;}

    public int longestSnake() {
        int len = 0;
        for (Snake s : snakes) {
            if (s.length() > len && !s.equals(mySnake)) {
                len = s.length();
            }
        }
        return len;
    }



}
