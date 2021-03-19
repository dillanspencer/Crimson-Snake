package com.battlesnake.minimax;

import com.battlesnake.board.Board;
import com.battlesnake.board.Tile;
import com.battlesnake.board.TileType;
import com.battlesnake.data.Move;
import com.battlesnake.data.MoveValue;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.battlesnake.pathfinding.Pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Minimax {

    private static final int MIN = -999999;
    private static final int NONE = -50;
    private static final int MAX = 999999;

    private Tile[][] tiles;
    private Snake mySnake;
    private Snake enemy;
    private List<Snake> snakes;
    private List<Point> food;
    private Pathfinding pathfinding;

    private int width;
    private int height;

    public Minimax(Tile[][] tiles, Snake mySnake, List<Snake> snakes, List<Point> food){
        this.tiles = tiles;
        this.mySnake = mySnake;
        this.snakes = snakes;
        this.food = food;
        pathfinding = new Pathfinding();

        this.width = tiles[0].length;
        this.height = tiles.length;
        this.enemy = findEnemySnake();
    }

    public MoveValue maximize(){
        MoveValue move = maximize(tiles, mySnake, enemy, 0, Minimax.MIN, Minimax.MAX);
       // System.out.println(move.returnMove + ", " + move.returnValue);
        return move;
    }

    public MoveValue maximize(Tile[][] board, Snake player, Snake enemy, int depth, double alpha, double beta){
        boolean isMaximizing = (depth % 2 == 0);

        MoveValue returnMove;
        MoveValue bestMove = null;

        if(isMaximizing){

            // check snake state
            List<Move> moves = getPossibleMoves(board, player.getHead(), true);
            if(moves.size() == 0){
                System.out.println("NO move for me");
                return new MoveValue(MIN);
            }

            for (Move currentMove : moves) {
                Tile[][] tempBoard = board.clone();
                Snake tempSnake = (Snake) player.clone();
                tempSnake.applyMove(currentMove);
                //System.out.println("X: " + tempSnake.getHead().getX() + ", Y: " + tempSnake.getHead().getY());
                tempBoard = updateBoard(tempBoard, tempSnake, enemy);
                returnMove = maximize(tempBoard, tempSnake, enemy, depth+1, alpha, beta);

               if(bestMove == null || returnMove.returnValue > alpha){
                   bestMove = returnMove;
                   bestMove.returnMove = currentMove;
                   alpha = returnMove.returnValue;
               }
            }
        }else {

            if(depth == 3){
                int value = evaluate(player, enemy);
                return new MoveValue(value);
            }

            // check snake state
            List<Move> moves = getPossibleMoves(board, enemy.getHead(), true);
            if(moves.size() == 0){
                System.out.println("NO move for ENEMY");
                return new MoveValue(MAX);
            }

            for (Move currentMove : moves) {
                Tile[][] tempBoard = board.clone();
                Snake tempSnake = (Snake) enemy.clone();
                tempSnake.applyMove(currentMove);
                tempBoard = updateBoard(tempBoard, player, tempSnake);
                returnMove = maximize(tempBoard, player, tempSnake, depth+1, alpha, beta);

                if(returnMove.returnValue < beta){
                    beta = returnMove.returnValue;
                }
                bestMove = returnMove;
            }
        }

        return bestMove;
    }

    private int evaluate(Snake snake, Snake enemy){
        int score = -5;

        Point center = new Point(width/2, height/2);
        score -= Math.abs(snake.getHead().getX() - center.getX()) + Math.abs(snake.getHead().getY()-center.getY());

        if(snake.length() <= 3 && snake.getHead() == snake.getTail()){
            score = -1000;
        }

        if(snake.longerThan(enemy) && snake.checkCollision(enemy) != -1){
            score = 1000;
            System.out.println("GOOD, " + snake.length() + ", " + enemy.length() + ", " + snake.getName());
        }
        else if(!snake.longerThan(enemy) && snake.checkCollision(enemy) != -1){
            score = -1000;
            System.out.println("BAD");
        }
        return score;
    }

    // Checks if point exist within the bounds of the board
    public boolean exists(Point point) {
        if (point.getX() < 0) return false;
        if (point.getY() < 0) return false;
        if (point.getX() > width-1) return false;
        if (point.getY() > height-1) return false;
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
                    && board[point.getX()][point.getY()].getTileType() != TileType.HEADS
                    && board[point.getX()][point.getY()].getTileType() != TileType.FAKE_WALL;
        }
        else{
            return board[point.getX()][point.getY()].getTileType() != TileType.EMPTY
                    && board[point.getX()][point.getY()].getTileType() != TileType.FOOD
                    && board[point.getX()][point.getY()].getTileType() != TileType.TAIL
                    && board[point.getX()][point.getY()].getTileType() != TileType.HEADS;
        }
    }


    private boolean movable(Tile[][] board, Point point, boolean flag) {
        return !isFilled(point, board, flag);
    }

    private List<Move> getPossibleMoves(Tile[][] board, Point point, boolean flag) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Move, Point> move : Move.adjacent(point).entrySet()) {
            if (movable(board, move.getValue(), flag))
                moves.add(move.getKey());
        }
        return moves;
    }

    private Snake findEnemySnake(){
        Snake enemy = null;
        //get max distance
        double distance = 1000;
        for (Snake s : snakes) {
            if (!s.equals(mySnake)) {
                double dist = Point.distance(mySnake.getHead(), s.getHead());
                if(dist < distance){
                    distance = dist;
                    enemy = s;
                }
            }
        }
        return enemy;
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
            dist = Math.abs((tiles[0].length / 2) - snack.getX()) + (Math.abs((tiles.length / 2) - snack.getY()));
            if (dist < min) {
                min = dist;
                found = snack;
            }
        }
        return found;
    }

    public Move findFood(Point current) {
        List<Tile> path = pathfinding.getRoute(tiles, current, nearestFood(current));
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);
        System.out.println("Current Position: " + current + ", Tile Position: " + path.get(path.size() - 2).getX() + ", " + path.get(path.size() - 2).getY());

        return move;
    }

    public Move findTail(Point current) {
        List<Tile> path = pathfinding.getRoute(tiles, current, mySnake.getTail());
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);

        return move;
    }

    public Move findHead(Point current, Snake enemy) {
        if (enemy == null || enemy.longerThan(mySnake)) return findTail(current);
        List<Tile> path = pathfinding.getRoute(tiles, current, enemy.getHead());
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);

        return move;
    }

    public Move findCenter(Point current) {
        Point center = new Point(tiles[0].length / 2, tiles.length / 2);
        List<Tile> path = pathfinding.getRoute(tiles, current, center);
        if (path.size() <= 1) return null;
        Move move = moveToTile(path.get(path.size() - 2), current);

        return move;
    }

    public Move findExit(Point current) {
        System.out.println("FINDING EXIT");
        Move move = getPossibleMoves(tiles, current, true).get(0);
        if (move == null) return Move.UP;
        return move;
    }

    public int longestSnake() {
        int len = 0;
        for (Snake s : snakes) {
            if (s.length() > len && !s.equals(mySnake)) {
                len = s.length();
            }
        }
        return len;
    }

    private Tile[][] updateBoard(Tile[][] b, Snake s, Snake e) {
        Tile[][] board = b.clone();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                board[x][y] = new Tile(TileType.EMPTY, x, y);
            }
        }

        for (Point snack : food) {
            board[snack.getX()][snack.getY()] = new Tile(TileType.FOOD, snack.getX(), snack.getY());
        }

        for (Snake snake : snakes) {
            if(snake.equals(s)) snake = s;
            else if(snake.equals(e)) snake = e;

            List<Point> body = snake.getBody();
            Point head = body.get(0);
            for (int i = 1; i < body.size(); i++) {
                if ((i == body.size() - 1)
                        && body.size() > 1
                        && !snake.justAte()) {
                    board[body.get(i).getX()][body.get(i).getY()] = new Tile(TileType.TAIL, body.get(i).getX(), body.get(i).getY());
                } else {
                    try {
                        board[body.get(i).getX()][body.get(i).getY()] = new Tile(TileType.WALL, body.get(i).getX(), body.get(i).getY());
                    }catch (ArrayIndexOutOfBoundsException arr){
                        System.out.println("Out of bounds at index: " + i);
                    }
                }
            }

            if (snake.equals(s)) {
                try {
                    board[head.getX()][head.getY()] = new Tile(TileType.ME, head.getX(), head.getY());
                }catch (ArrayIndexOutOfBoundsException as){
                    System.out.println("Out of bounds at index: " + head.getX() + ", " + head.getY());
                }
            } else {
                board[head.getX()][head.getY()] = new Tile(TileType.HEADS, head.getX(), head.getY());

                if (!s.longerThan(snake)) {
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
        return b;
    }

    private void clearSnake(Snake snake){
        for(Point p: snake.getBody()){
            tiles[p.getX()][p.getY()] = new Tile(TileType.EMPTY, p.getX(), p.getY());
        }
    }

    public void printBoard(Tile[][] board) {
        System.out.println("----------------------------");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[j][i].getTileType() == TileType.WALL) System.out.print("W, ");
                if (board[j][i].getTileType()  == TileType.ME) System.out.print("M, ");
                if (board[j][i].getTileType()  == TileType.EMPTY) System.out.print("E, ");
                if (board[j][i].getTileType()  == TileType.HEADS) System.out.print("H, ");
                if (board[j][i].getTileType()  == TileType.TAIL) System.out.print("T, ");
                if (board[j][i].getTileType()  == TileType.FAKE_WALL) System.out.print("F, ");
            }
            System.out.println();
        }
        System.out.println("----------------------------");

    }

}
