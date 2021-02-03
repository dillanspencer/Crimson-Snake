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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Stack;

public class Snake {

    public enum SnakeState{
        HUNGRY,
        AGRESSIVE,
        FINDTAIL,
        SMART
    }

    private static int MAX_HEALTH = 100;
    private static int MIN_HEALTH = 0;

    private String id;        //UUID
    private String name;      //string
    private String taunt;     // optional
    private int health; //0..100
    private Point position;
    private List<Point> body;
    private Stack<List<Point>> previousBody;

    //snake state
    private SnakeState state;

    public Snake() {
        state = SnakeState.HUNGRY;
        previousBody = new Stack<>();
    }

    public int checkCollision(Snake other) {
        for (int i = 0; i < other.getBody().size() - 1; i++) {
            if (getHead().getX() == other.getBody().get(i).getX()) {
                if (getHead().getY() == other.getBody().get(i).getY()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean checkCollision(Point other) {
        if (getHead().getX() == other.getX()) {
            if (getHead().getY() == other.getY()) {
                return true;
            }
        }
        return false;
    }

    public void applyMove(Move move){
        if(move == null){
            System.out.println("MOVE IS NULL");
            move = Move.UP;
        }
        previousBody.push(body);
        for(int i = body.size() - 1; i > 0; i--){
            body.get(i).setX(body.get(i-1).getX());
            body.get(i).setY(body.get(i-1).getY());
            if(body.get(i).getX() == -1 || body.get(i).getX() == 11 ) System.out.print("why are you out of bounds?");
            if(body.get(i).getY() == -1 || body.get(i).getY() == 11 ) System.out.print("why are you out of bounds?");
        }
        body.set(0, move.translate(body.get(0)));
    }

    public void undoMove(){
        body = previousBody.pop();
    }


    public SnakeState getState(Board board, Snake enemy){
       if(health < 50){
           System.out.println("HUNGRY");
           return SnakeState.HUNGRY;
       }
       //else if(length() > board.longestSnake() && Point.distance(getHead(), enemy.getHead()) < 4){
//           System.out.println("AGRESSIVE");
//           return SnakeState.AGRESSIVE;
//       }else if(Point.distance(getHead(), enemy.getHead()) < 4 && length() < board.longestSnake()){
//           System.out.println("SMART");
//           return SnakeState.SMART;
//       }else if(length() > board.longestSnake() + 4){
//           return SnakeState.FINDTAIL;
//       }
        return SnakeState.HUNGRY;
       //return SnakeState.HUNGRY;
    }

    public Move move(BoardGame board){
       Move move = board.findFood(getHead());
       if(move == null){
           move = board.findTail(getHead());
       }
       return move;
    }

    public Move move(Board board, Snake enemy) {
        SnakeState state = getState(board, enemy);
        Move move = Move.UP;
        switch (state) {
            case HUNGRY:
                move = board.findFood(getHead());
                if (move == null) {
                    move = board.moveAggressive(getHead());
                }
                if (move == null) {
                    move = board.goToTail(getHead());
                }
                break;
            case AGRESSIVE:
                move = board.moveAggressive(getHead());
                if (move == null) {
                    move = board.findFood(getHead());
                }
                if (move == null) {
                    move = board.goToTail(getHead());
                }
                break;
            case FINDTAIL:
                move = board.goToTail(getHead());
                if (move == null) {
                    move = board.findFood(getHead());
                }
                if (move == null) {
                    move = board.moveAggressive(getHead());
                }
                break;
            case SMART:
                move = board.moveSmart(enemy);
                System.out.println(move.getName() + ", X: " + getHead().getX() + ", Y: " + getHead().getY() + ", Enemy: " + enemy.getName());
                System.out.println(move == null);
                if (move == null) {
                    move = board.goToTail(getHead());
                }
                if (move == null) {
                    move = board.moveAggressive(getHead());
                }
                break;
        }
        return move;
    }

    public boolean equals(Object other) {
        if (other instanceof Snake) return equals((Snake) other);
        return false;
    }

    public boolean equals(Snake other) {
        return getId().equals(other.getId());
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
        return this.body.size();
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

    public void setHead(Point point){
        this.body.set(0, point);
    }

    public void setTail(Point point){
         this.body.set(this.body.size() - 1, point);
    }

}
