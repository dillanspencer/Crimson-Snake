/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.battlesnake.data;

import com.battlesnake.board.Board;
import com.battlesnake.board.BoardGame;
import com.battlesnake.math.Point;
import com.battlesnake.minimax.Minimax;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

public class Snake implements Serializable {

    public enum SnakeState {
        HUNGRY,
        AGRESSIVE,
        FINDTAIL,
        SMART,
        CENTER
    }

    private static int MAX_HEALTH = 100;
    private static int MIN_HEALTH = 0;

    private String id;        //UUID
    private String name;      //string
    private String taunt;     // optional
    private int health; //0..100
    private int turn;
    private int size;
    private Point position;
    private List<Point> body;
    private Stack<List<Point>> previousBody;

    //snake state
    private SnakeState state;

    public Snake() {
        super();
        state = SnakeState.HUNGRY;
        previousBody = new Stack<>();
        size = 0;
    }

    public Snake(String id, String name, int health, List<Point> body) {
        super();
        this.id = id;
        this.body = body;
        this.name = name;
        this.health = health;
        state = SnakeState.HUNGRY;
        previousBody = new Stack<>();
        size = 0;
    }


    public boolean checkCollision(Snake other) {
        return this.getHead().equals(other.getHead());
    }

    public boolean checkCollision(Point other) {
        if (getHead().getX() == other.getX()) {
            if (getHead().getY() == other.getY()) {
                return true;
            }
        }
        return false;
    }

    public void applyMove(Move move, List<Point> food) {
        //System.out.println("Before: " + body.get(0));
        for(Point f : food){
            if(getHead().equals(f)) health = MAX_HEALTH;
            size += 1;
        }
        if (move == null) {
            System.out.println("MOVE IS NULL");
            move = Move.UP;
        }
        if(!justAte()) {
            for (int i = body.size() - 1; i > 0; i--) {
                body.get(i).setX(body.get(i - 1).getX());
                body.get(i).setY(body.get(i - 1).getY());
                if (body.get(i).getX() == -1 || body.get(i).getX() == 11)
                    System.out.print("why are you out of bounds?");
                if (body.get(i).getY() == -1 || body.get(i).getY() == 11)
                    System.out.print("why are you out of bounds?");
            }
            body.set(0, move.translate(body.get(0)));
        }else{
            body.add(0, move.translate(body.get(0)));
        }

        //System.out.println("After: " + body.get(0));
    }

    public void undoMove() {
        body = previousBody.pop();
    }


    public SnakeState getState(Minimax board, Snake enemy) {
        if (health < 25) {
            System.out.println("HUNGRY");
            return SnakeState.HUNGRY;
        }

        return SnakeState.SMART;
    }
    
    public Move move(Minimax board, Snake enemy) {

        SnakeState state = getState(board, enemy);
        this.turn = board.getTurn();
        Move move = null;
        switch (state) {
            case HUNGRY:
                System.out.println("HUNGRY");
                move = board.findFood(getHead());
                if (move == null) {
                    System.out.println("Hungry was null my dude");
                    move = board.maximize().returnMove;
                }
                if (move == null) {
                    System.out.println("MAXimize was null my dude");
                    move = board.findTail(getHead());
                }
                break;
            case AGRESSIVE:
                System.out.println("AGGRESSIVE");
                move = board.findHead(getHead(), enemy);
                if (move == null) {
                    move = board.maximize().returnMove;
                }
                if (move == null) {
                    move = board.findTail(getHead());
                }
                break;
            case FINDTAIL:
                System.out.println("FINDTAIL");
                move = board.findTail(getHead());
                if (move == null) {
                    move = board.maximize().returnMove;
                }
                if (move == null) {
                    move = board.findHead(getHead(), enemy);
                }
                break;
            case CENTER:
                System.out.println("CENTER");
                move = board.findCenter(getHead());
                if (move == null) {
                    move = board.maximize().returnMove;
                }
                if (move == null) {
                    System.out.println("Food was null");
                    move = board.findTail(getHead());
                }
                break;
            case SMART:
                if(enemy == null) {
                    return board.findFood(getHead());
                }
                move = board.maximize().returnMove;
                if (move == null) {
                    System.out.println("Minimax was null");
                    move = board.findFood(getHead());
                }
                if (move == null) {
                    System.out.println("Food was null");
                    move = board.findTail(getHead());
                }
        }
        if (move == null) {
            System.out.println("Tail was null...finding exit");
            return board.findExit(getHead());
        }

        return move;
    }

    public Move moveMinMax(Minimax board, Snake enemy, Point current) {
        SnakeState state = getState(board, enemy);
        Move move = null;
        switch (state) {
            case HUNGRY:
                System.out.println("HUNGRY");
                move = board.findFood(current);
                if (move == null) {
                    move = board.findCenter(current);
                }
                if (move == null) {
                    move = board.findTail(current);
                }
                break;
            case AGRESSIVE:
                System.out.println("AGGRESSIVE");
                move = board.findHead(current, enemy);
                if (move == null) {
                    move = board.findCenter(current);
                }
                if (move == null) {
                    move = board.findTail(current);
                }
                break;
            case FINDTAIL:
                System.out.println("FINDTAIL");
                move = board.findTail(current);
                if (move == null) {
                    move = board.findCenter(current);
                }
                if (move == null) {
                    move = board.findHead(current, enemy);
                }
                break;
            case CENTER:
                System.out.println("CENTER");
                move = board.findCenter(current);
                if (move == null) {
                    move = board.findFood(current);
                }
                if (move == null) {
                    move = board.findTail(current);
                }
        }
        if (move == null) return board.findExit(current);

        return move;
    }

    public int distance(Snake other){
        Point pos = other.getHead();
        return Math.abs(getHead().getX() - pos.getX()) + Math.abs(getHead().getY() - pos.getY());
    }

    public int distance(Point other){ ;
        return Math.abs(getHead().getX() - other.getX()) + Math.abs(getHead().getY() - other.getY());
    }


    public boolean equals(Object other) {
        if (other instanceof Snake) return equals((Snake) other);
        return false;
    }

    public boolean equals(Snake other) {
        return getId().equals(other.getId());
    }

    public void setBody(List<Point> body) {
        this.body = body;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Point getHead() {
        return this.body.get(0);
    }

    public Point getTail() {
        return this.body.get(this.body.size() - 1);
    }

    public boolean isDead() {
        return getHealth() <= MIN_HEALTH;
    }

    public boolean justAte() {
        return getHealth() == MAX_HEALTH;
    }

    public int length() {
        return this.body.size() + size;
    }

    public boolean longerThan(int len) {
        return length() > len;
    }

    public boolean longerThan(Snake other) {
        return longerThan(other.length());
    }

    public String getId() {
        return this.id;
    }

    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    @JsonProperty("body")
    public List<Point> getBody() {
        return this.body;
    }

    public String getTaunt() {
        return this.taunt;
    }

    @JsonProperty("health")
    public int getHealth() {
        return this.health;
    }

    public void setHead(Point point) {
        this.body.set(0, point);
    }

    public void setTail(Point point) {
        this.body.set(this.body.size() - 1, point);
    }

}
