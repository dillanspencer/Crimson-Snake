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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Minimax {

    private static final int MIN = -999999;
    private static final int NONE = -50;
    private static final int MAX = 999999;

    private Tile[][] tiles;
    private Snake mySnake;
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
    }

    public MoveValue maximize(){
       return maximize(findEnemySnake(), 0, Minimax.MIN, Minimax.MAX);
    }

    public MoveValue maximize(Snake enemy, int depth, double alpha, double beta){
        boolean isMaximizing = (depth % 2 == 0);

        MoveValue returnMove;
        MoveValue bestMove = new MoveValue();

        if(!isMaximizing){

            // get value for pathfinding
            enemy.moveMinMax(this, mySnake, enemy.getHead());
            int value = pathfinding.getNewestScore();
            if(depth == 3) return new MoveValue(value);

            // check snake state
            List<Move> moves = getPossibleMoves(enemy.getHead());
            Iterator<Move> movesIterator = moves.iterator();
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                enemy.applyMove(currentMove);
                updateBoard(tiles, enemy);
                returnMove = maximize(enemy, depth + 1, alpha, beta);
                enemy.undoMove();
                updateBoard(tiles, enemy);

                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue > alpha) {
                    alpha = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    //System.out.println("Beta <= Alpha: " + beta + ", " + alpha);
                    bestMove.returnValue = beta;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }else {

            // get value for pathfinding
            mySnake.moveMinMax(this, enemy, mySnake.getHead());
            int value = pathfinding.getNewestScore();
            if(depth == 3) return new MoveValue(value);

            // check snake state
            List<Move> moves = getPossibleMoves(enemy.getHead());
            Iterator<Move> movesIterator = moves.iterator();
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                mySnake.applyMove(currentMove);
                updateBoard(tiles, mySnake);
                returnMove = maximize(enemy, depth + 1, alpha, beta);
                mySnake.undoMove();
                updateBoard(tiles, mySnake);

                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue < beta) {
                    beta = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    //System.out.println("Beta <= Alpha: " + beta + ", " + alpha);
                    bestMove.returnValue = alpha;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }
    }

    // Checks if point exist within the bounds of the board
    public boolean exists(Point point) {
        if (point.getX() < 0) return false;
        if (point.getY() < 0) return false;
        if (point.getX() > tiles[0].length - 1) return false;
        if (point.getY() > tiles.length - 1) return false;
        return true;
    }

    // Takes in a point and returns all adjacent points
    private List<Point> findAdjacent(Point point) {
        return new ArrayList<>(Move.adjacent(point).values());
    }

    private boolean isFilled(Point point, Tile[][] board) {
        if (!exists(point)) return true;
        return board[point.getX()][point.getY()].getTileType() != TileType.EMPTY
                && board[point.getX()][point.getY()].getTileType() != TileType.FOOD
                && board[point.getX()][point.getY()].getTileType() != TileType.TAIL
                && board[point.getX()][point.getY()].getTileType() != TileType.HEADS;
    }


    private boolean movable(Point point) {
        return !isFilled(point, tiles);
    }

    private List<Move> getPossibleMoves(Point point) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Move, Point> move : Move.adjacent(point).entrySet()) {
            if (movable(move.getValue()))
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
        Move move = getPossibleMoves(current).get(0);
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

    private void updateBoard(Tile[][] board, Snake snake) {
        List<Point> body = snake.getBody();
        Point head = body.get(0);
        for (int i = 0; i < body.size(); i++) {
            if ((i == body.size() - 1)
                    && body.size() > 1
                    && !snake.justAte()) {
                board[body.get(i).getX()][body.get(i).getY()] = new Tile(TileType.TAIL, body.get(i).getX(), body.get(i).getY());
            } else {
                if (body.get(i).getX() < 0 || body.get(i).getY() < 0)
                    System.out.println(body.get(i).getX() + ", " + body.get(i).getY());
                board[body.get(i).getX()][body.get(i).getY()] = new Tile(TileType.WALL, body.get(i).getX(), body.get(i).getY());
            }
        }

        if (snake.equals(mySnake)) {
            board[head.getX()][head.getY()] = new Tile(TileType.ME, head.getX(), head.getY());
        } else {
            board[head.getX()][head.getY()] = new Tile(TileType.HEADS, head.getX(), head.getY());

            if (!mySnake.longerThan(snake)) {
                List<Point> around = findAdjacent(head);
                for (Point point : around) {
                    if (exists(point)) {
                        if (board[point.getX()][point.getY()].getTileType() == TileType.EMPTY
                                || board[point.getX()][point.getY()].getTileType() == TileType.FOOD) {
                            board[point.getX()][point.getY()].setTileType(TileType.FAKE_WALL);
                        }
                    }
                }
            }
        }
    }

}
