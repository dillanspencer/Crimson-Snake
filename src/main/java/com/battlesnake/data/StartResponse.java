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


public class StartResponse {
    // required fields with default values
    private String name = "no-name";
    private String color = "#FF0000";
    private String apiversion = "1";

    // optional parameters
    private String headUrl;
    private HeadType headType;
    private TailType tailType;
    private String taunt;
    private String secondaryColor;

    public StartResponse() {
    }


    public String getName() {
        return this.name;
    }

    public String getColor() {
        return this.color;
    }

    @JsonProperty("apiversion")
    public String getapiversion() {
        return this.apiversion;
    }

    @JsonProperty("headUrl")
    public String getHeadUrl() {
        return this.headUrl;
    }

    @JsonProperty("headType")
    public HeadType getHeadType() {
        return this.headType;
    }

    @JsonProperty("tailType")
    public TailType getTailType() {
        return this.tailType;
    }

    public String getTaunt() {
        return this.taunt;
    }

    @JsonProperty("secondaryColor")
    public String getSecondaryColor() {
        return this.secondaryColor;
    }

    // setters for method chaining
    public StartResponse setApiVersion(String apiversion) {
        this.apiversion = apiversion;
        return this;
    }

    public StartResponse setName(String name) {
        this.name = name;
        return this;
    }

    public StartResponse setColor(String color) {
        this.color = color;
        return this;
    }

    public StartResponse setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
        return this;
    }

    public StartResponse setHeadType(HeadType headType) {
        this.headType = headType;
        return this;
    }

    public StartResponse setTailType(TailType tailType) {
        this.tailType = tailType;
        return this;
    }

    public StartResponse setTaunt(String taunt) {
        this.taunt = taunt;
        return this;
    }

    public StartResponse setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
        return this;
    }
}
