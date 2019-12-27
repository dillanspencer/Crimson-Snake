package com.battlesnake.board;

import com.battlesnake.data.Move;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

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
        }
    }

    private List<Move> getPossibleMoves(Point point) {
        int x = point.getX();
        int y = point.getY();
        List<Move> moves = new ArrayList<>();

        // Check all directions
        for (int i = 0; i < 4; i++) {
            if (x != 0 && (board[x - 1][y] == Tile.EMPTY || board[x - 1][y] == Tile.FOOD || board[x - 1][y] == Tile.TAIL)) {
                moves.add(Move.LEFT);
            }
            if (x != width - 1 && (board[x + 1][y] == Tile.EMPTY || board[x + 1][y] == Tile.FOOD || board[x + 1][y] == Tile.TAIL)) {
                moves.add(Move.RIGHT);
            }
            if (y != 0 && (board[x][y - 1] == Tile.EMPTY || board[x][y - 1] == Tile.FOOD || board[x][y - 1] == Tile.TAIL)) {
                moves.add(Move.UP);
            }
            if (y != height - 1 && (board[x][y + 1] == Tile.EMPTY || board[x][y + 1] == Tile.FOOD || board[x][y + 1] == Tile.TAIL)) {
                moves.add(Move.DOWN);
            }
        }
        return moves;
    }

    private boolean isAnyMoves(Point point) {
        return !getPossibleMoves(point).isEmpty();
    }

    private boolean checkCollision(Point head, Snake other) {
        for (int i = 0; i < other.getBody().size() - 1; i++) {
            if (head.getX() == other.getBody().get(i).getX()) {
                if (head.getX() == other.getBody().get(i).getY()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int minimax(Tile[][] board, int depth, boolean isMaximizing, Snake current, Snake enemy, int alpha, int beta) {

        Tile[][] currentBoard = board;
        Point position = current.getHead();
        List<Move> possibleMoves = getPossibleMoves(position);

        //check if dead
        if (checkCollision(position, enemy)) {
            return Board.MIN;
        } else if (checkCollision(enemy.getHead(), you())) {
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
                    current.getHead().setY(position.getY() - 1);
                }else if (possibleMoves.get(i).equals(Move.DOWN)) {
                    //change to head later
                    currentBoard[position.getX()][position.getY() + 1] = Tile.WALL;
                    current.getHead().setY(position.getY() + 1);
                }else if (possibleMoves.get(i).equals(Move.LEFT)) {
                    //change to head later
                    currentBoard[position.getX() - 1][position.getY()] = Tile.WALL;
                    current.getHead().setX(position.getX() - 1);
                }else if (possibleMoves.get(i).equals(Move.RIGHT)) {
                    //change to head later
                    currentBoard[position.getX() + 1][position.getY()] = Tile.WALL;
                    current.getHead().setX(position.getX() + 1);
                }
                int val = minimax(currentBoard, depth + 1, false, enemy, current, alpha, beta);
                best = Math.max(best, val);
                alpha = Math.max(alpha, best);

                //Alpha beta pruning
                if (beta <= alpha) break;
            }
            return best;
        }else{
            int best = Board.MAX;

            for (int i = 0; i < possibleMoves.size() - 1; i++) {
                if (possibleMoves.get(i).equals(Move.UP)) {
                    //change to head later
                    currentBoard[position.getX()][position.getY() - 1] = Tile.WALL;
                    current.getHead().setY(position.getY() - 1);
                }else if (possibleMoves.get(i).equals(Move.DOWN)) {
                    //change to head later
                    currentBoard[position.getX()][position.getY() + 1] = Tile.WALL;
                    current.getHead().setY(position.getY() + 1);
                }else if (possibleMoves.get(i).equals(Move.LEFT)) {
                    //change to head later
                    currentBoard[position.getX() - 1][position.getY()] = Tile.WALL;
                    current.getHead().setX(position.getX() - 1);
                }else if (possibleMoves.get(i).equals(Move.RIGHT)) {
                    //change to head later
                    currentBoard[position.getX() + 1][position.getY()] = Tile.WALL;
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

        public Move getMove () {
            int x = you().getHead().getX();
            int y = you().getHead().getY();

            if(snakes.size() > 1) {
                int score = minimax(board, 0, false, you(), snakes.get(0), Board.MIN, Board.MAX);
                if (score > Board.MAX) {
                    return Move.UP;
                }
            }
            return Move.DOWN;
        }

        public void init (Snake you){
            this.you = you;
            setupBoard();
        }

        private Snake you () {
            return you;
        }

        public int getWidth () {
            return width;
        }

        public void setWidth ( int width){
            this.width = width;
        }

        public int getHeight () {
            return height;
        }

        public void setHeight ( int height){
            this.height = height;
        }

        @JsonProperty("food")
        public List<Point> getFood () {
            return food;
        }

        public void setFood (List < Point > food) {
            this.food = food;
        }

        public ArrayList<Snake> getSnakes () {
            return snakes;
        }

        public void setSnakes (ArrayList < Snake > snakes) {
            this.snakes = snakes;
        }

        @JsonProperty("dead_snakes")
        public ArrayList<Snake> getDeadSnakes () {
            return this.deadSnakes;
        }

        public void setDeadSnakes (ArrayList < Snake > deadSnakes) {
            this.deadSnakes = deadSnakes;
        }
    }
