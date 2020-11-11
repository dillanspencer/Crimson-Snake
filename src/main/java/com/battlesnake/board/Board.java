package com.battlesnake.board;

import com.battlesnake.data.Move;
import com.battlesnake.data.MovePoint;
import com.battlesnake.data.MoveValue;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.fasterxml.jackson.annotation.JsonProperty;
import sun.awt.SunHints;

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

    private static final int IGNORE_SIZE = 4;

    //Game Map
    private transient Tile[][] board;
    private transient Stack<Tile[][]> previousBoard;
    private transient Integer[][] regions;

    private Tile[][] setupBoard(Tile[][] currentBoard) {
        Tile[][] board = currentBoard;
        if(board == null)
            board = new Tile[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                board[x][y] = Tile.EMPTY;
            }
        }

        for (Point snack : food) {
            board[snack.getX()][snack.getY()] = Tile.FOOD;
        }

        for (Snake snake : snakes) {
            List<Point> body = snake.getBody();
            Point head = body.get(0);
            for (int i = 0; i < body.size(); i++) {
                if ((i == body.size() - 1)
                        && body.size() > 1
                        && !snake.justAte()) {
                    board[body.get(i).getX()][body.get(i).getY()] = Tile.TAIL;
                } else {
                    if(body.get(i).getX() < 0 || body.get(i).getY() < 0)
                        System.out.println(body.get(i).getX() + ", " + body.get(i).getY());
                    board[body.get(i).getX()][body.get(i).getY()] = Tile.WALL;
                }
            }

            if (snake.equals(you())) {
                board[head.getX()][head.getY()] = Tile.ME;
            } else {
                board[head.getX()][head.getY()] = Tile.HEADS;

                if (!you().longerThan(snake)) {
                    List<Point> around = findAdjacent(head);
                    for (Point point : around) {
                        if (exists(point)) {
                            if (board[point.getX()][point.getY()] == Tile.EMPTY
                                    || board[point.getX()][point.getY()] == Tile.FOOD) {
                                board[point.getX()][point.getY()] = Tile.FAKE_WALL;
                            }
                        }
                    }
                }
            }
        }
        return board;
    }

    private interface Exit {
        public boolean shouldExit(MovePoint point, Point initial);

        public List<MovePoint> onFailure(List<MovePoint> path);
    }

    private void fill(Point point) {
        if (!exists(point)) return;
        regions[point.getX()][point.getY()] = 0;
    }

    private void fillIn() {
        this.regions = new Integer[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isFilled(new Point(x, y))) {
                    regions[x][y] = 0;
                }
            }
        }
        for (Snake snake : snakes) {
            if (snake.equals(you()) || snake.length() <= 1) continue;
            Point head = snake.getHead();
            Point neck = snake.getBody().get(1);
            Point delta = head.delta(neck);
            for (int i = 1; i <= 2; i++) {
                fill(new Point(head.getX() + delta.getX() * i, head.getY() + delta.getY() * i));
            }
        }
        Exit condition = new Exit() {
            public boolean shouldExit(MovePoint point, Point initial) {
                return false;
            }

            public List<MovePoint> onFailure(List<MovePoint> path) {
                return path;
            }
        };
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (regions[x][y] != null) continue;
                List<MovePoint> region = floodFill(new Point(x, y), condition, false);
                for (MovePoint point : region) {
                    regions[point.getPoint().getX()][point.getPoint().getY()] = region.size();
                }
            }
        }
    }

    public int regionSize(Point point) {
        if (!exists(point)) return 0;
        return regions[point.getX()][point.getY()];
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
            if (!snake.equals(you()) && you.longerThan(snake)) {
                list.addAll(findAdjacent(snake.getBody().get(0)));
                list.add(snake.getHead());
            }
        }
        return list;

    }

    protected Move findPath(List<Point> destinations, Point point) {
        return findPath(destinations, point, true);
    }

    protected Move findPath(List<Point> destinations, Point point, boolean checkBox) {
        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.get(i).equals(point)) {
                destinations.remove(i);
                i--;
            }
        }
        Exit condition = new Exit() {
            public boolean shouldExit(MovePoint point, Point initial) {
                for (Point destination : destinations) {
                    if (point.getPoint().equals(destination)) {
                        int smallRegion = Math.max(IGNORE_SIZE, (int) Math.floor(you().length() / 2));
                        Point newPoint = point.getInitialMove().translate(initial);
                        int region = regionSize(newPoint);
                        if (checkBox && region <= smallRegion) {
                            return false;
                        }
                        return true;
                    }
                }
                return false;
            }

            public List<MovePoint> onFailure(List<MovePoint> path) {
                return new ArrayList<MovePoint>();
            }
        };
        List<MovePoint> path = floodFill(point, condition, true);
        if (path.isEmpty()) return null;
        return path.get(path.size() - 1).getInitialMove();
    }


    protected List<MovePoint> floodFill(Point point, Exit condition, boolean excludeDanger) {
        LinkedList<MovePoint> points = new LinkedList<>();
        ArrayList<MovePoint> list = new ArrayList<>();
        ArrayList<MovePoint> visited = new ArrayList<>();

        MovePoint loopPoint = new MovePoint(null, point, null);
        points.add(loopPoint);
        list.add(loopPoint);
        while (!points.isEmpty()) {
            loopPoint = points.pollFirst();
            visited.add(loopPoint);
            if (condition.shouldExit(loopPoint, point)) {
                return visited;
            }
            List<MovePoint> moves = getPossibleMoves(loopPoint, excludeDanger);
            for (MovePoint move : moves) {
                move.setLength(loopPoint.getLength() + 1);
                if (list.contains(move)) continue;
                points.add(move);
                list.add(move);
            }
        }
        return condition.onFailure(visited);
    }

    private List<MovePoint> getPossibleMoves(MovePoint point) {
        return getPossibleMoves(point, true);
    }

    private List<MovePoint> getPossibleMoves(MovePoint point, boolean excludeDanger) {
        ArrayList<MovePoint> moves = new ArrayList<>();
        Move initial = point.getInitialMove();
        for (Map.Entry<Move, Point> move : Move.adjacent(point.getPoint()).entrySet()) {
            if (movable(move.getValue(), excludeDanger)) {
                moves.add(new MovePoint(
                                move.getKey(),
                                move.getValue(),
                                initial != null ? initial : move.getKey()
                        )
                );
            }
        }
        return moves;
    }


    public boolean isFilled(Point point) {
        return isFilled(point, board);
    }

    private boolean isFilled(Point point, Tile[][] board) {
        if (!exists(point)) return true;
        return board[point.getX()][point.getY()] != Tile.EMPTY
                && board[point.getX()][point.getY()] != Tile.FOOD
                    && board[point.getX()][point.getY()] != Tile.TAIL
                && board[point.getX()][point.getY()] != Tile.FAKE_WALL
                && board[point.getX()][point.getY()] != Tile.HEADS;
    }

    private boolean movable(Point point) {
        return !isFilled(point);
    }

    private boolean movable(Point point, boolean excludeDanger) {
        return !isFilled(point)
                && (excludeDanger ? !isDangerousSpotFilled(point) : true);
    }

    private boolean movable(Point point, Tile[][] board) {
        return !isFilled(point, board);
    }

    public boolean isDangerousSpotFilled(Point point) {
        if (!exists(point)) return false;
        return board[point.getX()][point.getY()] == Tile.FAKE_WALL;
    }

    private List<Move> getPossibleMoves(Tile[][] currentBoard, Point point) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Move, Point> move : Move.adjacent(point).entrySet()) {
            if (movable(move.getValue(), currentBoard))
                moves.add(move.getKey());
        }
        if(moves.size() == 0) moves.add(Move.LEFT);
        return moves;
    }

    private Tile[][] applyMove(Move move, Snake snake, Tile[][] currentBoard) {
        previousBoard.push(board);
        snake.applyMove(move);
        snakes.set(snakes.indexOf(snake), snake);
        //fillIn();
        return setupBoard(currentBoard);
    }

    private Tile[][] undoMove(Snake snake, Tile[][] currentBoard) {
        board = previousBoard.pop();
        snake.undoMove();
        snakes.set(snakes.indexOf(snake), snake);
        //fillIn();
        return setupBoard(currentBoard);
    }

    private double positionHeuristic(Snake snake, Snake enemy){
        int smallRegion = Math.max(IGNORE_SIZE, (int) Math.floor(you().length() / 2));
        int region = regionSize(snake.getHead());

        return region * (Point.distance(snake.getHead(), enemy.getHead())*0.15);
    }

    private double boardValue(Snake snake, Snake enemy, int depth) {
        double value = NONE;
        //base case
       // System.out.println("Checking for Collisions");

        if (Point.equals(snake.getHead(), enemy.getHead()) && snake.longerThan(enemy)) {
            System.out.println("MAX: ENEMY HEAD - " + snake.getName());
            value = Board.MAX;
            return value;
        } else if (Point.equals(snake.getHead(), enemy.getHead()) && enemy.longerThan(snake)) {
            System.out.println("MIN: ENEMY HEAD - " + snake.getName());
            value = Board.MIN;
            return value;
        }else if(depth == 3){
            value = positionHeuristic(snake, enemy);
            System.out.println("Heuristic: " + value);
            return value;
        }

        return value;
    }

    private MoveValue minimax(Tile[][] board, int depth, Snake snake, Snake enemy, double alpha, double beta) {

        boolean isMaximizing = (depth % 2 == 0);

        MoveValue returnMove;
        MoveValue bestMove = null;

        //Iterate through possible moves
        if (isMaximizing) {
            double value = boardValue(snake, enemy, depth);
            if (value != NONE || depth == 3) {
                return new MoveValue(value);
            }
            //System.out.println("MAXIMIZING");
            List<Move> moves = getPossibleMoves(board, snake.getHead());
            Iterator<Move> movesIterator = moves.iterator();
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                //System.out.println(depth + " Start Position : " + snake.getHead().getX() + ", " + snake.getHead().getY());
                board = applyMove(currentMove, snake, board);
                // System.out.println("End Position: " + snake.getHead().getX() + ", " + snake.getHead().getY());
                returnMove = minimax(board, depth + 1, snake, enemy, alpha, beta);
                board = undoMove(snake, board);
                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue > alpha) {
                    alpha = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    System.out.println("Beta <= Alpha: " + beta + ", " + alpha);
                    bestMove.returnValue = beta;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        } else {
            // System.out.println("MINIMIZING");
            double value = boardValue(enemy, snake, depth);
            if (value != NONE || depth == 3) {
                return new MoveValue(value);
            }
            List<Move> moves = getPossibleMoves(board, enemy.getHead());
            Iterator<Move> movesIterator = moves.iterator();
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                //System.out.println(depth + " Start Position : " + enemy.getHead().getX() + ", " + enemy.getHead().getY());
                board = applyMove(currentMove, enemy, board);
                // System.out.println("End Position: " + enemy.getHead().getX() + ", " + enemy.getHead().getY());
                returnMove = minimax(board, depth + 1, snake, enemy, alpha, beta);
                board = undoMove(enemy, board);
                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue < beta) {
                    beta = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    System.out.println("Beta <= Alpha: " + beta + ", " + alpha);
                    bestMove.returnValue = alpha;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }
    }

    public Move moveSmart(Snake enemy) {
        return minimax(board, 0, you, enemy, Board.MIN, Board.MAX).returnMove;
    }

    public Move findFood(Point current) {
        return findPath(food, current);
    }

    public Move moveAggressive(Point current) {
        return findPath(findHeads(), current);
    }

    public Move goToTail(Point currentPoint) {
        Move move = null;
        for (int i = you().getBody().size() - 1; i > 0; i--) {
            move = findPath(findAdjacent(you().getBody().get(i)), currentPoint, false);
            if (move != null) return move;
        }
        return null;
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

    public void printHeuristics(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(regions[i][j] + ", ");
            }
            System.out.println();
        }
    }

    public void init(Snake you) {
        this.you = you;
        previousBoard = new Stack<>();
        this.board = setupBoard(this.board);
        fillIn();
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

    public int longestSnake() {
        int len = 0;
        for (Snake s : snakes) {
            if (s.length() > len && !s.equals(you)) {
                len = s.length();
            }
        }
        return len;
    }
}
