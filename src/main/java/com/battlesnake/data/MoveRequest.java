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
import com.battlesnake.minimax.Minimax;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveRequest {
  private String gameId; // UUID
  private Snake you; // UUID

  private int turn; // The current turn.

  private Minimax board;

  public MoveRequest() {
  }

  @JsonProperty("board")
  public Minimax getBoard(){return this.board;}

  @JsonProperty("game_id")
  public String getGameId() {
    return this.gameId;
  }

  @JsonProperty("you")
  public Snake getYou() {
    return this.you;
  }

  public int getTurn() {
    return this.turn;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public void setYou(Snake you) {
    this.you = you;
  }

  public void setTurn(int turn) {
    this.turn = turn;
  }

}
