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

    //Game Map
    private Tile[][] board;

    private void setupBoard(){
        this.board = new Tile[getWidth()][getHeight()];
        //set all values on the board to empty
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                board[x][y] = Tile.EMPTY;
            }
        }

        //fill in food positions
        for(Point f : food){
            board[f.getX()][f.getY()] = Tile.FOOD;
        }

        //fill in board with snake positions
        for(Snake snake : snakes){
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
        }
    }

    public Move getMove(){
        int x = you().getHead().getX();
        int y = you().getHead().getY();

        //check up
        if(board[x][y-1] == Tile.EMPTY && y != 0) return Move.UP;
        else if(board[x][y+1] == Tile.EMPTY && y != height) return Move.DOWN;
        else if(board[x-1][y] == Tile.EMPTY && x != 0) return  Move.LEFT;
        else if(board[x+1][y] == Tile.EMPTY && x != width) return Move.RIGHT;
        return Move.RIGHT;
    }

    public void init(Snake you){
        this.you = you;
        setupBoard();
    }

    private Snake you(){
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
