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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Snake {

    private static int MAX_HEALTH = 100;
    private static int MIN_HEALTH = 0;

    private String id;        //UUID
    private String name;      //string
    private String taunt;     // optional
    private int health; //0..100
    private Point position;
    private List<Point> body;

    public Snake() {
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

    @JsonProperty("health_points")
    public int getHealth() {
        return this.health;
    }


}
