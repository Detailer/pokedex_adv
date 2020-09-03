package com.example.pokedex;

public class Pokemon {
    private String name;
    private String url;

    Pokemon(String Name, String url){
        this.name = Name;
        this.url = url;
    }

    public String getName(){
        return name;
    }
    public String getUrl(){
        return url;
    }
}
