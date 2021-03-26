package com.battlesnake.minimax;

import com.battlesnake.board.Board;
import com.battlesnake.board.Tile;
import com.battlesnake.board.TileType;
import com.battlesnake.data.Move;
import com.battlesnake.data.MovePoint;
import com.battlesnake.data.MoveValue;
import com.battlesnake.data.Snake;
import com.battlesnake.math.Point;
import com.battlesnake.pathfinding.Pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Minimax {

    private static final int MIN = -999999;
    private static final int NONE = -50;
    private static final int MAX = 999999;

    private transient Tile[][] tiles;
    private transient Integer[][] regions;
    private Snake mySnake;
    private Snake enemy;
    private List<Snake> snakes;
    private List<Point> food;
    private Pathfinding pathfinding;

    private int width;
    private int height;
    private int turn;

    public Minimax(Tile[][] tiles, Snake mySnake, List<Snake> snakes, List<Point> food, int turn){
        this.tiles = tiles;
        this.mySnake = mySnake;
        this.snakes = snakes;
        this.food = food;
        this.turn = turn;
        this.pathfinding = new Pathfinding();

        this.width = tiles[0].length;
        this.height = tiles.length;
        this.enemy = findEnemySnake();
        this.regions = new Integer[width][height];
        fillIn(tiles, this.regions, mySnake);
    }

    public MoveValue maximize(){
        MoveValue move = maximize(tiles, mySnake, enemy, 0, Minimax.MIN, Minimax.MAX);
       // System.out.println(move.returnMove + ", " + move.returnValue);
        return move;
    }

    public MoveValue maximize(Tile[][] board, Snake player, Snake enemy, int depth, double alpha, double beta){
        boolean isMaximizing = (depth % 2 == 0);

        int value = evaluate(board, player, enemy);
        if(value == MAX || value == -MIN) return new MoveValue(value);

        MoveValue returnMove;
        MoveValue bestMove = null;

        if(isMaximizing){

            // check snake state
            List<Move> moves = getPossibleMoves(board, player.getHead(), false);
            if(moves.size() == 0){
                System.out.println("NO move for me " + depth + ", " + player.getHead());
                return new MoveValue(MIN);
            }

            for (Move currentMove : moves) {
                Tile[][] tempBoard = board.clone();
                Snake tempSnake = (Snake) player.clone();
                tempSnake.applyMove(currentMove);
                tempBoard = updateBoard(tempBoard, tempSnake, enemy);
                returnMove = maximize(tempBoard, tempSnake, enemy, depth+1, alpha, beta);

               if(bestMove == null || returnMove.returnValue > bestMove.returnValue){
                   bestMove = returnMove;
                   bestMove.returnMove = currentMove;
                   bestMove.returnValue = returnMove.returnValue;
               }
               if(returnMove.returnValue > alpha){
                   alpha = returnMove.returnValue;
               }
               if(beta <= alpha) break;
            }
        }else {

            if(depth == 3){
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

                if(bestMove == null || returnMove.returnValue < bestMove.returnValue){
                    bestMove = returnMove;
                    bestMove.returnValue = returnMove.returnValue;
                }
                if(returnMove.returnValue < beta){
                    beta = returnMove.returnValue;
                }
                if(beta <= alpha) break;
            }
        }

        return bestMove;
    }

    private int evaluate(Tile[][] board, Snake snake, Snake enemy){
        int score = 0;

        Integer[][] regions = new Integer[width][height];
        fillIn(board, regions, snake);
        Point head = snake.getHead();

        for (Map.Entry<Move, Point> move : Move.adjacent(head).entrySet()) {
            if (movable(board, move.getValue(), true)) {
                score += regions[move.getValue().getX()][move.getValue().getY()];
            }
        }
        Point center = new Point(width/2, height/2);
        score -= (Math.abs(snake.getHead().getX() - center.getX()) + Math.abs(snake.getHead().getY()-center.getY())) * 5;
        score += (Math.abs(snake.getHead().getX() - enemy.getHead().getX()) + Math.abs(snake.getHead().getY()-enemy.getHead().getY()));

        for(Point f : food)
            if(snake.getHead().equals(f)) score += (1000/snake.getHealth());

        if(snake.longerThan(enemy) && snake.checkCollision(enemy) != -1){
            score = MAX;
            System.out.println("GOOD, " + snake.getHead() + ", " + snake.getName());
        }
        else if(!snake.longerThan(enemy) && snake.checkCollision(enemy) != -1){
            score = MIN;
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

    private List<MovePoint> getPossibleMoves(Tile[][] board, MovePoint point, boolean excludeDanger) {
        ArrayList<MovePoint> moves = new ArrayList<>();
        Move initial = point.getInitialMove();
        for (Map.Entry<Move, Point> move : Move.adjacent(point.getPoint()).entrySet()) {
            if (movable(board, move.getValue(), excludeDanger)) {
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

    private void fillIn(Tile[][] tiles, Integer[][] regions, Snake s) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isFilled(new Point(x, y), tiles, true)) {
                    regions[x][y] = 0;
                }
            }
        }
        for (Snake snake : snakes) {
            if (snake.equals(s) || snake.length() <= 1) continue;
            Point head = snake.getHead();
            Point neck = snake.getBody().get(1);
            Point delta = head.delta(neck);
            for (int i = 1; i <= 2; i++) {
                fill(regions, new Point(head.getX() + delta.getX() * i, head.getY() + delta.getY() * i));
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
                List<MovePoint> region = floodFill(tiles, new Point(x, y), condition, false);
                for (MovePoint point : region) {
                    regions[point.getPoint().getX()][point.getPoint().getY()] = region.size();
                }
            }
        }
    }

    private interface Exit {
        boolean shouldExit(MovePoint point, Point initial);

        List<MovePoint> onFailure(List<MovePoint> path);
    }

    private void fill(Integer[][] regions, Point point) {
        if (!exists(point)) return;
        regions[point.getX()][point.getY()] = 0;
    }

    protected List<MovePoint> floodFill(Tile[][] tiles, Point point, Exit condition, boolean excludeDanger) {
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
            List<MovePoint> moves = getPossibleMoves(tiles, loopPoint, excludeDanger);
            for (MovePoint move : moves) {
                move.setLength(loopPoint.getLength() + 1);
                if (list.contains(move)) continue;
                points.add(move);
                list.add(move);
            }
        }
        return condition.onFailure(visited);
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
            dist = Math.abs((current.getX()) - snack.getX()) + (Math.abs((current.getY()) - snack.getY()));
            dist -= regions[snack.getX()][snack.getY()];
            if (dist < min) {
                min = dist;
                found = snack;
            }
        }
        if(regions[found.getX()][found.getY()] < mySnake.length()*2) return null;
        return found;
    }

    public Move findFood(Point current) {
        Point food = nearestFood(current);
        if(food == null) return null;
        List<Tile> path = pathfinding.getRoute(tiles, current, food);
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

    private Tile[][] updateBoard(Tile[][] b, Snake sn, Snake e) {
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
            if(snake.equals(sn)) snake = (Snake) sn.clone();
            else if(snake.equals(e)) snake = (Snake) e.clone();

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

            if (snake.equals(mySnake)) {
                try {
                    board[head.getX()][head.getY()] = new Tile(TileType.ME, head.getX(), head.getY());
                }catch (ArrayIndexOutOfBoundsException as){
                    System.out.println("Out of bounds at index: " + head.getX() + ", " + head.getY());
                }
            } else {
                board[head.getX()][head.getY()] = new Tile(TileType.HEADS, head.getX(), head.getY());

                if (!mySnake.longerThan(snake)) {
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
        System.out.println("---------------------------- Turn: " + turn);
        for (int i = height-1; i >= 0; i--) {
            for (int j = 0; j < width-1; j++) {
                if (board[j][i].getTileType() == TileType.WALL) System.out.print("W, ");
                if (board[j][i].getTileType()  == TileType.ME) System.out.print("M, ");
                if (board[j][i].getTileType()  == TileType.EMPTY) System.out.print("E, ");
                if (board[j][i].getTileType()  == TileType.HEADS) System.out.print("H, ");
                if (board[j][i].getTileType()  == TileType.TAIL) System.out.print("T, ");
                if (board[j][i].getTileType()  == TileType.FAKE_WALL) System.out.print("F, ");
                if (board[j][i].getTileType()  == TileType.FOOD) System.out.print("X, ");
            }
            System.out.println();
        }
        System.out.println("----------------------------");

    }

}
