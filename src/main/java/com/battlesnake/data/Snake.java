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

import com.fasterxml.jackson.annotation.JsonProperty;

public class Snake {

  private String id;        //UUID
  private String name;      //string
  private String taunt;     // optional
  private int health; //0..100
  private int[][] coords;   // array of coordinates 

  public Snake() {
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getTaunt() {
    return this.taunt;
  }

  @JsonProperty("health_points")
  public int getHealth() {
    return this.health;
  }

  public int[][] getCoords() {
    return this.coords;
  }


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

  public void setCoords(int[][] coords) {
    this.coords = coords;
  }

}
