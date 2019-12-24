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

  private String id;        //UUID
  private String name;      //string
  private String taunt;     // optional
  private int health; //0..100
  private Point position;

  private List<Point> body;

  public Snake() {
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  @JsonProperty("body")
  public List<Point> getBody(){return this.body;}

  public String getTaunt() {
    return this.taunt;
  }

  @JsonProperty("health_points")
  public int getHealth() {
    return this.health;
  }

  public Point getPosition(){return this.body.get(0);}

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTaunt(String taunt) {
    this.taunt = taunt;
  }

  public void setHealth(int health) {
    this.health = health;
  }


}
