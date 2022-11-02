package com.example.test;

public class CounterHelper {
    static int counter = 0;
    CounterHelper(){

    }

    public static int getNextCounter(){
        return counter++;
    }
}
