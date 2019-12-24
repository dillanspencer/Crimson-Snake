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

import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveRequest {
  private String gameId; // UUID	 
  private String you; // UUID 

  private int turn; // The current turn.
  private int width; //integer	 
  private int height; //integer	 

  private int[][] food; //Array of all food currently on the board
  private ArrayList<Snake> snakes; //	Array of all living snakes in the game
  private ArrayList<Snake> deadSnakes; //Array of all dead snakes in the game

  public MoveRequest() {
  }


  @JsonProperty("game_id")
  public String getGameId() {
    return this.gameId;
  }

  public String getYou() {
    return this.you;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  public int getTurn() {
    return this.turn;
  }

  public int[][] getFood() {
    return this.food;
  }

  public ArrayList<Snake> getSnakes() {
    return this.snakes;
  }

  @JsonProperty("dead_snakes")
  public ArrayList<Snake> getDeadSnakes() {
    return this.deadSnakes;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public void setYou(String you) {
    this.you = you;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setTurn(int turn) {
    this.turn = turn;
  }

  public void setFood(int[][] food) {
    this.food = food;
  }

  public void setSnakes(ArrayList<Snake> snakes) {
    this.snakes = snakes;
  }

  public void setDeadSnakes(ArrayList<Snake> deadSnakes) {
    this.deadSnakes = deadSnakes;
  }
}
