package com.panozona.converter.utils;

public class Naming {

    public static final String LETTERS = "letters";
    public static final String NUMBERS = "numbers";
    private String type = LETTERS;

    public void setType(String type) {
        if (type.equals(LETTERS) || type.equals(NUMBERS)) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Invalid Naming type value: " + type);
        }
    }

    public String getType() {
        return type;
    }

    public String getFront() {
        if (type.equals(LETTERS)) {
            return "_f";
        } else {
            return "_0";
        }
    }

    public String getRight() {
        if (type.equals(LETTERS)) {
            return "_r";
        } else {
            return "_1";
        }
    }

    public String getBack() {
        if (type.equals(LETTERS)) {
            return "_b";
        } else {
            return "_2";
        }
    }

    public String getLeft() {
        if (type.equals(LETTERS)) {
            return "_l";
        } else {
            return "_3";
        }
    }

    public String getUp() {
        if (type.equals(LETTERS)) {
            return "_u";
        } else {
            return "_4";
        }
    }

    public String getDown() {
        if (type.equals(LETTERS)) {
            return "_d";
        } else {
            return "_5";
        }
    }

    public String getSelection(){
        if (type.equals(LETTERS)) {
            return "[f|r|b|l|u|d]";
        } else {
            return "[0|1|2|3|4|5]";
        }
    }
    
    public static boolean recognizeFront(String name){
        return(name.matches("^.+(_f|_0|_11|_front)$"));
    }
    
    public static boolean recognizeRight(String name){
        return(name.matches("^.+(_r|_1|_22|_right)$"));
    }
    
    public static boolean recognizeBack(String name){
        return(name.matches("^.+(_b|_2|_33|_back)$"));
    }
    
    public static boolean recognizeLeft(String name){
        return(name.matches("^.+(_l|_3|_44|_left)$"));
    }
    
    public static boolean recognizeUp(String name){
        return(name.matches("^.+(_u|_4|_55|_up)$"));
    }
    
    public static boolean recognizeDown(String name){
        return(name.matches("^.+(_d|_5|_66|_down)$"));
    }
}
