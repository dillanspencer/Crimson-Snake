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

import com.battlesnake.math.Point;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Move {
  UP,
  DOWN,
  LEFT,
  RIGHT;

  public Point translate(Point point) {
    switch (this) {
      case UP:
        return new Point(point.getX(), point.getY() - 1);
      case DOWN:
        return new Point(point.getX(), point.getY() + 1);
      case LEFT:
        return new Point(point.getX() - 1, point.getY());
      default:
        return new Point(point.getX() + 1, point.getY());
    }
  }

  public static Map<Move, Point> adjacent(Point point) {
    Map<Move, Point> moves = new HashMap<>();
    moves.put(Move.UP, Move.UP.translate(point));
    moves.put(Move.DOWN, Move.DOWN.translate(point));
    moves.put(Move.LEFT, Move.LEFT.translate(point));
    moves.put(Move.RIGHT, Move.RIGHT.translate(point));
    return moves;
  }

  public static List<Move> allMoves(){
    List<Move> moves = new ArrayList<Move>();
    moves.add(Move.UP);
    moves.add(Move.DOWN);
    moves.add(Move.LEFT);
    moves.add(Move.RIGHT);
    return moves;
  }

  @JsonValue
  public String getName() {
    return name().toLowerCase();
  }
}
