package com.example.sudoku;

public class TwoPoints {
    int point1;
    String point2;

    public TwoPoints(int a, String b) {
        point1 = a;
        point2 = b;
    }

    /* Returns the second point (which is the value, either 1-9 or empty string) */
    public String secondPoint() {
        return point2;
    }
    // Returns the first point (which is the coordinate in the array)
    public int firstPoint() {
        return point1;
    }
}