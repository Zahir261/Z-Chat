package com.a2z.zchat.helpers;

public class Validator {
    public static boolean isNullOrEmpty(String text){
        return text.isEmpty() || text == null;
    }
}
