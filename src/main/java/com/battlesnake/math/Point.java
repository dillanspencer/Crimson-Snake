package com.battlesnake.math;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Point {

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
        return Math.sqrt(Math.pow(other.getX() - point.getX(), 2) +
                Math.pow(other.getY() - point.getY(), 2));
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
