package com.battlesnake.board;

import com.battlesnake.data.Move;
import com.battlesnake.data.MoveValue;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

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

    private void applyMove(Tile[][] currBoard, Snake snake, Snake enemy, Move move) {
        for (Point p : snake.getBody()) {
            currBoard[p.getX()][p.getY()] = Tile.EMPTY;
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

        currBoard[head.getX()][head.getY()] = Tile.HEADS;

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
                && board[point.getX()][point.getY()] != Tile.TAIL
                && board[point.getX()][point.getY()] != Tile.HEADS;
    }

    private boolean movable(Point point) {
        return !isFilled(point);
    }

    private boolean movable(Point point, Tile[][] board) {
        return !isFilled(point, board);
    }

    public boolean isDeadEnd(Tile[][] board, Point head, Point point, int searchDepth) {
        if (!exists(point)) return true;

        boolean locations[][] = new boolean[width][height];
        int depth = 0;
        Point currentLocation;
        Stack<Point> stack = new Stack<>();

        locations[head.getX()][head.getY()] = true;
        stack.push(point);

        while (!stack.isEmpty() && depth < searchDepth) {

            //set current location to top of stack
            currentLocation = stack.peek();

            //set location as visited
            locations[currentLocation.getX()][currentLocation.getY()] = true;

            //check up
            if (currentLocation.getY() != 0 && locations[currentLocation.getX()][currentLocation.getY() - 1] == false
                    && movable(Move.UP.translate(currentLocation), board)) {
                stack.push(Move.UP.translate(currentLocation));
            }
            //check down
            else if (currentLocation.getY() != height - 1 && locations[currentLocation.getX()][currentLocation.getY() + 1] == false
                    && movable(Move.DOWN.translate(currentLocation), board)) {
                stack.push(Move.DOWN.translate(currentLocation));
            }
            //check right
            else if (currentLocation.getX() != width - 1 && locations[currentLocation.getX() + 1][currentLocation.getY()] == false
                    && movable(Move.RIGHT.translate(currentLocation), board)) {
                stack.push(Move.RIGHT.translate(currentLocation));
            }
            //check left
            else if (currentLocation.getX() != 0 && locations[currentLocation.getX() - 1][currentLocation.getY()] == false
                    && movable(Move.LEFT.translate(currentLocation), board)) {
                stack.push(Move.LEFT.translate(currentLocation));
            } else {
                stack.pop();
            }
            depth++;
        }
        System.out.println("DEAD END: " + stack.isEmpty());
        System.out.println(point.getX() + ", " + point.getY());
        return stack.isEmpty();
    }

    private List<Move> getPossibleMoves(Tile[][] currentBoard, Point point) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Move, Point> move : Move.adjacent(point).entrySet()) {
            if (movable(move.getValue(), currentBoard) && !isDeadEnd(currentBoard, point, move.getValue(), you.length() * 2))
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

    private double boardValue(Snake snake, Snake enemy) {
        double value = -1;
        //base case
        if (Point.equals(snake.getHead(), enemy.getHead()) && snake.longerThan(enemy)) {
            System.out.println("MAX: ENEMY HEAD - " + snake.getName());
            value = Board.MAX;
        } else if (Point.equals(snake.getHead(), enemy.getHead()) && enemy.longerThan(snake)) {
            System.out.println("MIN: ENEMY HEAD - " + snake.getName());
            value = Board.MIN;
        } else if (checkCollision(snake, enemy)) {
            //check head collision
            System.out.println("MIN");
            value = Board.MIN;
        } else if (checkCollision(enemy, snake)) {
            value = Board.MAX;
        } else if (this.board[snake.getHead().getX()][snake.getHead().getY()] == Tile.FOOD) {
            System.out.println("FOOD");
            value = Board.FOOD;
        }
        return value;
    }

    private MoveValue minimax(Tile[][] board, int depth, Snake snake, Snake enemy, double alpha, double beta) {
        if (depth == 3) {
            return new MoveValue(Board.NONE);
        }

        List<Move> moves = getPossibleMoves(board, snake.getHead());
        Iterator<Move> movesIterator = moves.iterator();
        double value = boardValue(snake, enemy);
        boolean isMaximizing = (snake.equals(you()));

        //base case
        if (moves.isEmpty()) {
            return new MoveValue(Board.MIN);
        }
        if (value != -1) {
            return new MoveValue(value);
        }

        MoveValue returnMove;
        MoveValue bestMove = null;

        //Iterate through possible moves
        if (isMaximizing) {
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                Tile[][] tempBoard = board;
                applyMove(board, snake, enemy, currentMove);
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
                applyMove(board, snake, enemy, currentMove);
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

    public Move findTail() {

        //check directions
        if (you.getHead().getX() < you.getTail().getX() && !isFilled(Move.RIGHT.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.RIGHT.translate(you.getHead()), you.length() * 2)) {
            System.out.println("RIGHT");
            return Move.RIGHT;
        }
        if (you.getHead().getX() > you.getTail().getX() && !isFilled(Move.LEFT.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.LEFT.translate(you.getHead()), you.length() * 2)) {
            System.out.println("LEFT");
            return Move.LEFT;
        }
        if (you.getHead().getY() < you.getTail().getY() && !isFilled(Move.DOWN.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.DOWN.translate(you.getHead()), you.length() * 2)) {
            System.out.println("DOWN");
            return Move.DOWN;
        }
        if (you.getHead().getY() > you.getTail().getY() && !isFilled(Move.UP.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.UP.translate(you.getHead()), you.length() * 2)) {
            System.out.println("UP");
            return Move.UP;
        }
        return getMove();
    }

    public Move findFood() {
        if (food.isEmpty()) return getMove();

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
        if (you.getHead().getX() < foodPoint.getX() && !isFilled(Move.RIGHT.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.RIGHT.translate(you.getHead()), you.length() * 2)) {
            System.out.println("RIGHT");
            return Move.RIGHT;
        }
        if (you.getHead().getX() > foodPoint.getX() && !isFilled(Move.LEFT.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.LEFT.translate(you.getHead()), you.length() * 2)) {
            System.out.println("LEFT");
            return Move.LEFT;
        }
        if (you.getHead().getY() < foodPoint.getY() && !isFilled(Move.DOWN.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.DOWN.translate(you.getHead()), you.length() * 2)) {
            System.out.println("DOWN");
            return Move.DOWN;
        }
        if (you.getHead().getY() > foodPoint.getY() && !isFilled(Move.UP.translate(you.getHead()))
                && !isDeadEnd(board, you.getHead(), Move.UP.translate(you.getHead()), you.length() * 2)) {
            System.out.println("UP");
            return Move.UP;
        }
        return getMove();
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
