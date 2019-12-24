package com.battlesnake.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {

    @JsonProperty("you")
    private String you;

    public void setYou(String you){
        this.you = you;
    }
    public String getYou(){
        return you;
    }
}
