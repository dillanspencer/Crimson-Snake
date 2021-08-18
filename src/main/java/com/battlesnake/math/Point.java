package com.battlesnake.math;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Point implements Serializable {

    private int x;
    private int y;

    public Point() {
        this(0, 0);
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static boolean equals(Point point, Point other) {
        if (point.getX() == other.getX() && point.getY() == other.getY()) return true;
        return false;
    }

    public static double distance(Point point, Point other) {
        double dx = point.getX() - other.getX();
        double dy = point.getY() - other.getY();
        return Math.abs(dx) + Math.abs(dy);
    }

    public boolean equals(Point other){
        if(x == other.getX() && y == other.getY()) return true;
        return false;
    }

    public Point delta(Point point) {
        return new Point(getX() - point.getX(), getY() - point.getY());
    }

    @Override
    public String toString() {
        return "X: " + x + ", " + "Y: " + y;
    }

    @JsonProperty("x")
    public int getX() {
        return x;
    }

    @JsonProperty("y")
    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
