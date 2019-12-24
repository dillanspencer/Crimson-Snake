package com.battlesnake.data;

import com.battlesnake.math.Point;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private int width;
    private int height;
    private List<Point> food; //Array of all food currently on the board
    private ArrayList<Snake> snakes; //	Array of all living snakes in the game
    private ArrayList<Snake> deadSnakes; //Array of all dead snakes in the game

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
