package com.battlesnake.board;

import com.battlesnake.data.Move;
import com.battlesnake.data.MoveValue;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Board {

    private int width;
    private int height;
    private Snake you;
    private List<Point> food; //Array of all food currently on the board
    private ArrayList<Snake> snakes; //	Array of all living snakes in the game
    private ArrayList<Snake> deadSnakes; //Array of all dead snakes in the game

    //minimax algorithm
    private static final int MIN = -1000;
    private static final int NONE = -50;
    private static final int MAX = 1000;
    private static final int FOOD = 0;

    //Game Map
    private Tile[][] board;

    private void setupBoard() {
        board = new Tile[getWidth()][getHeight()];
        //set all values on the board to empty
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                board[x][y] = Tile.EMPTY;
            }
        }

        //fill in food positions
        for (Point f : food) {
            board[f.getX()][f.getY()] = Tile.FOOD;
        }

        //fill in board with snake positions
        for (Snake snake : snakes) {
            List<Point> body = snake.getBody();
            Point head = body.get(0);

            for (int i = 0; i < body.size(); i++) {
                if ((i == body.size() - 1)
                        && body.size() > 1
                        && !snake.justAte()) {
                    board[body.get(i).getX()][body.get(i).getY()] = Tile.TAIL;
                } else {
                    board[body.get(i).getX()][body.get(i).getY()] = Tile.WALL;
                }
            }
            if (snake.equals(you())) {
                board[head.getX()][head.getY()] = Tile.ME;
            } else {
                board[head.getX()][head.getY()] = Tile.HEADS;
            }
        }
    }

    private void applyMove(Tile[][] currBoard, Snake snake, Move move) {

        for (int i = 0; i < snake.getBody().size() - 1; i++) {
            currBoard[snake.getBody().get(i).getX()][snake.getBody().get(i).getY()] = Tile.EMPTY;
        }
        snake.applyMove(currBoard, move);

        List<Point> body = snake.getBody();
        Point head = body.get(0);

        for (int i = 0; i < body.size(); i++) {
            if ((i == body.size() - 1)
                    && body.size() > 1
                    && !snake.justAte()) {
                currBoard[body.get(i).getX()][body.get(i).getY()] = Tile.TAIL;
            } else {
                currBoard[body.get(i).getX()][body.get(i).getY()] = Tile.WALL;
            }
        }
        if (snake.equals(you())) {
            currBoard[head.getX()][head.getY()] = Tile.ME;
        } else {
            currBoard[head.getX()][head.getY()] = Tile.HEADS;
        }
    }

    public boolean exists(Point point) {
        if (point.getX() < 0) return false;
        if (point.getY() < 0) return false;
        if (point.getX() > getWidth() - 1) return false;
        if (point.getY() > getHeight() - 1) return false;
        return true;
    }

    private List<Point> findAdjacent(Point point) {
        return new ArrayList<>(Move.adjacent(point).values());
    }

    private List<Point> findHeads() {
        ArrayList<Point> list = new ArrayList<>();
        for (Snake snake : snakes) {
            if (!snake.equals(you())) {
                list.addAll(findAdjacent(snake.getBody().get(0)));
                list.add(snake.getHead());
            }
        }
        return list;

    }

    public boolean isFilled(Point point) {
        return isFilled(point, board);
    }

    private boolean isFilled(Point point, Tile[][] board) {
        if (!exists(point)) return true;
        return board[point.getX()][point.getY()] != Tile.EMPTY
                && board[point.getX()][point.getY()] != Tile.FOOD
                && board[point.getX()][point.getY()] != Tile.TAIL;
    }

    private boolean movable(Point point) {
        return !isFilled(point);
    }

    private boolean movable(Point point, Tile[][] board) {
        return !isFilled(point, board);
    }


    private List<Move> getPossibleMoves(Tile[][] currentBoard, Point point) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Move, Point> move : Move.adjacent(point).entrySet()) {
            if (movable(move.getValue(), currentBoard))
                moves.add(move.getKey());
        }
        return moves;
    }

    private boolean checkCollision(Snake snake, Snake enemy) {
        if (snake.checkCollision(enemy) != -1) {
            return true;
        }

        if (!exists(snake.getHead())) return true;
        return false;
    }

    private MoveValue minimax(Tile[][] board, int depth, Snake snake, Snake enemy, double alpha, double beta) {
        if (depth == 3) {
            System.out.println("SAFE");
            printBoard(board);
            System.out.println();
            return new MoveValue(Board.NONE);
        }

        List<Move> moves = getPossibleMoves(board, snake.getHead());
        Iterator<Move> movesIterator = moves.iterator();
        double value;
        boolean isMaximizing = (snake.equals(you()));

        //base case
        if (checkCollision(snake, enemy)) {
            //check head collision
            if (Point.equals(snake.getHead(), enemy.getHead()) && snake.longerThan(enemy)) {
                value = Board.MAX;
                return new MoveValue(value);
            }
            System.out.println("MIN");
            value = Board.MIN;
            return new MoveValue(value);
        } else if (checkCollision(enemy, snake)) {
            System.out.println("MAX");
            value = Board.MAX;
            return new MoveValue(value);
        } else if (this.board[snake.getHead().getX()][snake.getHead().getY()] == Tile.FOOD) {
            System.out.println("FOOD");
            value = Board.FOOD;
            return new MoveValue(value);
        }
        MoveValue returnMove;
        MoveValue bestMove = null;

        //Iterate through possible moves
        if (isMaximizing) {
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                Tile[][] tempBoard = board;
                applyMove(board, snake, currentMove);
                returnMove = minimax(board, depth + 1, enemy, snake, alpha, beta);
                board = tempBoard;
                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue > alpha) {
                    alpha = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = beta;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        } else {
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                Tile[][] tempBoard = board;
                applyMove(board, snake, currentMove);
                returnMove = minimax(board, depth + 1, enemy, snake, alpha, beta);
                board = tempBoard;
                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue < beta) {
                    beta = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = alpha;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }
    }

    public Move findFood() {
        Point foodPoint = food.get(0);
        double closest = Point.distance(you.getHead(), foodPoint);
        for (Point f : food) {
            double dist = Point.distance(you.getHead(), f);
            if (dist < closest) {
                closest = dist;
                foodPoint = f;
            }
        }

        //check directions
        if (you.getHead().getX() < foodPoint.getX()) {
            System.out.println("RIGHT");
            return Move.RIGHT;
        }
        if (you.getHead().getX() > foodPoint.getX()) {
            System.out.println("LEFT");
            return Move.LEFT;
        }
        if (you.getHead().getY() < foodPoint.getY()) {
            System.out.println("DOWN");
            return Move.DOWN;
        }
        if (you.getHead().getY() > foodPoint.getY()) {
            System.out.println("UP");
            return Move.UP;
        }
        return null;
    }


    public Move getMove() {
        Snake enemy = null;
        for (Snake s : snakes) {
            if (!s.equals(you)) {
                enemy = s;
                break;
            }
        }
        return minimax(board, 0, you, enemy, Board.MIN, Board.MAX).returnMove;
    }

    public void printBoard(Tile[][] board) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[j][i] == Tile.WALL) System.out.print("W, ");
                if (board[j][i] == Tile.ME) System.out.print("ME, ");
                if (board[j][i] == Tile.EMPTY) System.out.print("E, ");
                if (board[j][i] == Tile.HEADS) System.out.print("H, ");
                if (board[j][i] == Tile.TAIL) System.out.print("T, ");
                if (board[j][i] == Tile.FOOD) System.out.print("F, ");
            }
            System.out.println();
        }
    }

    public void init(Snake you) {
        this.you = you;
        setupBoard();
    }

    private Snake you() {
        return you;
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

    public Tile[][] getBoard() {
        return this.board;
    }

    @JsonProperty("food")
    public List<Point> getFood() {
        return food;
    }

    public void setFood(List<Point> food) {
        this.food = food;
    }

    public ArrayList<Snake> getSnakes() {
        return snakes;
    }

    public void setSnakes(ArrayList<Snake> snakes) {
        this.snakes = snakes;
    }

    @JsonProperty("dead_snakes")
    public ArrayList<Snake> getDeadSnakes() {
        return this.deadSnakes;
    }

    public void setDeadSnakes(ArrayList<Snake> deadSnakes) {
        this.deadSnakes = deadSnakes;
    }
}
