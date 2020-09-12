package com.zahir.zchat.helpers;

public class Validator {
    public static boolean isNullOrEmpty(String text){
        return text.isEmpty() || text == null;
    }
}
