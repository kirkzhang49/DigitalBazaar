package com.csm117.digitalbazaar;

import com.google.firebase.auth.*;

public class Globals {
    private static Globals instance;

    // Global variable
    String userId;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}
