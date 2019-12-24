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

import com.fasterxml.jackson.annotation.JsonValue;

public enum HeadType {
  BENDR("bendr"),
  DEAD("dead"),
  FANG("fang"),
  PIXEL("pixel"),
  REGULAR("regular"),
  SAFE("safe"),
  SANDWORM("sand-worm"),
  SHADES("shades"),
  SMILE("smile"),
  TONGUE("tongue");

  private String name;
  private HeadType(String name) {
    this.name= name;
  }
  @JsonValue
  public String getName() {
	  return name;
  }
}
