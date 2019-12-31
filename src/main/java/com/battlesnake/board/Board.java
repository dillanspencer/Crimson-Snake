package com.battlesnake.board;

import com.battlesnake.data.Move;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
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
    private static final int NONE = 0;
    private static final int MAX = 1000;
    private static final int FOOD = 2000;

    //Game Map
    private Tile[][] board;

    private void setupBoard() {
        this.board = new Tile[getWidth()][getHeight()];
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

    private boolean checkCollision(Snake snake, Snake other) {
        Point head = snake.getHead();
        for (int i = 0; i < other.getBody().size() - 1; i++) {
            if (head.getX() == other.getBody().get(i).getX()) {
                if (head.getX() == other.getBody().get(i).getY()) {
                    if (i == 0 && other.longerThan(snake)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private int minimax(Tile[][] board, int depth, boolean isMaximizing, Snake current, Snake enemy, int alpha, int beta) {

        Tile[][] currentBoard = board;
        Point position = current.getHead();
        Point tail = current.getTail();
        List<Move> possibleMoves = getPossibleMoves(currentBoard, position);

        //check if dead
        if (checkCollision(current, enemy)) {
            return Board.MIN;
        } else if (checkCollision(enemy, you())) {
            return Board.MAX;
        } else if (depth == 3) {
            return Board.NONE;
        }

        if (isMaximizing) {
            int best = Board.MIN;

            for (int i = 0; i < possibleMoves.size() - 1; i++) {
                if (possibleMoves.get(i).equals(Move.UP)) {
                    //change to head later
                    currentBoard[position.getX()][position.getY() - 1] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setY(position.getY() - 1);
                } else if (possibleMoves.get(i).equals(Move.DOWN)) {
                    //change to head later
                    currentBoard[position.getX()][position.getY() + 1] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setY(position.getY() + 1);
                } else if (possibleMoves.get(i).equals(Move.LEFT)) {
                    //change to head later
                    currentBoard[position.getX() - 1][position.getY()] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setX(position.getX() - 1);
                } else if (possibleMoves.get(i).equals(Move.RIGHT)) {
                    //change to head later
                    currentBoard[position.getX() + 1][position.getY()] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setX(position.getX() + 1);
                }
                int val = minimax(currentBoard, depth + 1, false, enemy, current, alpha, beta);
                best = Math.max(best, val);
                alpha = Math.max(alpha, best);

                //Alpha beta pruning
                if (beta <= alpha) break;
            }
            return best;
        } else {
            int best = Board.MAX;

            for (int i = 0; i < possibleMoves.size() - 1; i++) {
                if (possibleMoves.get(i).equals(Move.UP)) {
                    //change to head later
                    currentBoard[position.getX()][position.getY() - 1] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setY(position.getY() - 1);
                } else if (possibleMoves.get(i).equals(Move.DOWN)) {
                    //change to head later
                    currentBoard[position.getX()][position.getY() + 1] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setY(position.getY() + 1);
                } else if (possibleMoves.get(i).equals(Move.LEFT)) {
                    //change to head later
                    currentBoard[position.getX() - 1][position.getY()] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setX(position.getX() - 1);
                } else if (possibleMoves.get(i).equals(Move.RIGHT)) {
                    //change to head later
                    currentBoard[position.getX() + 1][position.getY()] = Tile.WALL;
                    currentBoard[tail.getX()][tail.getY()] = Tile.EMPTY;
                    current.getHead().setX(position.getX() + 1);
                }

                int val = minimax(currentBoard, depth + 1, true, enemy, current, alpha, beta);
                best = Math.min(best, val);
                beta = Math.min(beta, best);

                //Alpha Beta Pruning
                if (beta <= alpha) break;
            }
            return best;
        }
    }

    public Move getMove() {
        Snake enemy = null;
        int count = 0;
        for (Snake sn : snakes) {
            if (!sn.equals(you())) {
                enemy = sn;
            }
        }
        int[] score = {0, 0, 0, 0};
        int best = Board.MIN;
        Move move = Move.RIGHT;
        List<Move> possibleMoves = getPossibleMoves(board, you().getHead());

        for (int i = 0; i < possibleMoves.size() - 1; i++) {
            Snake s = you();
            if (possibleMoves.get(i).equals(Move.UP)) {
                s.getHead().setY(s.getHead().getY() - 1);
                score[0] = minimax(board, 0, true, s, enemy, Board.MAX, Board.MIN);
                count++;
                if (score[0] > best) {
                    move = Move.UP;
                    best = score[0];
                }
            } else if (possibleMoves.get(i).equals(Move.DOWN)) {
                s.getHead().setY(s.getHead().getY() + 1);
                score[1] = minimax(board, 0, true, s, enemy, Board.MAX, Board.MIN);
                count++;
                if (score[1] > best) {
                    move = Move.DOWN;
                    best = score[1];
                }
            } else if (possibleMoves.get(i).equals(Move.LEFT)) {
                s.getHead().setX(s.getHead().getX() - 1);
                score[2] = minimax(board, 0, true, s, enemy, Board.MAX, Board.MIN);
                count++;
                if (score[2] > best) {
                    move = Move.LEFT;
                    best = score[2];
                }
            } else if (possibleMoves.get(i).equals(Move.RIGHT)) {
                s.getHead().setX(s.getHead().getX() + 1);
                score[3] = minimax(board, 0, true, s, enemy, Board.MAX, Board.MIN);
                count++;
                if (score[3] > best) {
                    move = Move.RIGHT;
                    best = score[3];
                }
            }
        }
        return move;
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
