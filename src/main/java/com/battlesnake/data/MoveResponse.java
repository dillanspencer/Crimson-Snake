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

public class MoveResponse {
  // required fields with default values
  private Move move = Move.RIGHT;

	// optional parameters
  private String taunt;

  public MoveResponse() {
  }

  public MoveResponse(Move move) {
    this.move = move;
  }

  public MoveResponse(Move move, String taunt) {
    this.move = move;
    this.taunt = taunt;
  }


  public Move getMove() {
    return this.move;
  }

  public String getTaunt() {
    return this.taunt;
  }

  // setters for method chaining
  public MoveResponse setMove(Move move) {
    this.move = move;
    return this;
  }

  public MoveResponse setTaunt(String taunt) {
    this.taunt = taunt;
    return this;
  }
}
