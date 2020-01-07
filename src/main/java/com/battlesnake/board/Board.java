package com.battlesnake.board;

import com.battlesnake.data.Move;
import com.battlesnake.data.MovePoint;
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

    private static final int IGNORE_SIZE = 4;

    //Game Map
    private transient Tile[][] board;
    private transient Integer[][] regions;

    private void setupBoard() {
        this.board = new Tile[width][height];
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
                }
                else {
                    board[body.get(i).getX()][body.get(i).getY()] = Tile.WALL;
                }
            }

            if (snake.equals(you())) {
                board[head.getX()][head.getY()] = Tile.ME;
            }
            else {
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
                //check dead end
                if(isDeadEnd(board, you.getTail(), initial, 30)){
                    return true;
                }
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
        return board[point.getX()][point.getY()] == Tile.FAKE_WALL
                || board[point.getX()][point.getY()] == Tile.TAIL;
    }

    public boolean isDeadEnd(Tile[][] board, Point exit, Point point, int searchDepth) {
        if (!exists(point)) return true;

        boolean locations[][] = new boolean[width][height];
        int depth = 0;
        Point currentLocation;
        Stack<Point> stack = new Stack<>();
        
        stack.push(point);

        while (!stack.isEmpty() && !(stack.peek().equals(exit) && depth < searchDepth)) {

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
        if (stack.isEmpty())
            System.out.println("FOUND DEAD END: " + point.toString());
        return stack.isEmpty();
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
            System.out.println("MIN COLLISION");
            value = Board.MIN;
        } else if (checkCollision(enemy, snake)) {
            System.out.println("MAX COLLISION");
            value = Board.MAX;
        } else if (this.board[snake.getHead().getX()][snake.getHead().getY()] == Tile.FOOD) {
            System.out.println("FOOD");
            value = Board.FOOD;
        } else if (!exists(snake.getHead())) value = Board.MIN;
        return value;
    }

    private MoveValue minimax(Tile[][] board, int depth, Snake snake, Snake enemy, double alpha, double beta) {
        System.out.println("Position: " + snake.getHead().getX() + ", " + snake.getHead().getY());
        if (depth == 3) {
            return new MoveValue(Board.NONE);
        }

        List<Move> moves = getPossibleMoves(board, snake.getHead());
        Iterator<Move> movesIterator = moves.iterator();
        double value = boardValue(snake, enemy);
        boolean isMaximizing = (snake.equals(you()));

        //base case
        if (value != -1) {
            return new MoveValue(value);
        }
        if (moves.isEmpty()) {
            return new MoveValue(Board.MIN);
        }

        MoveValue returnMove;
        MoveValue bestMove = null;

        //Iterate through possible moves
        if (isMaximizing) {
            while (movesIterator.hasNext()) {
                Move currentMove = movesIterator.next();
                snake.applyMove(board, currentMove);
                returnMove = minimax(board, depth + 1, enemy, snake, alpha, beta);
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
                snake.applyMove(board, currentMove);
                returnMove = minimax(board, depth + 1, enemy, snake, alpha, beta);
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

    public void init(Snake you) {
        this.you = you;
        setupBoard();
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

    public int longestSnake(){
        int len = 0;
        for(Snake s : snakes){
            if(s.length() > len && !s.equals(you)){
                len = s.length();
            }
        }
        return len;
    }
}
