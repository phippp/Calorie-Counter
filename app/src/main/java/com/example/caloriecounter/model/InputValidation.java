package com.example.caloriecounter.model;

import android.content.Context;
import android.util.Patterns;

public class InputValidation {

    private Context context;

    public InputValidation(Context context){
        this.context = context;
    }

    public boolean isEmailValid(String email){
        if(email == null){
            return false;
        }
        if(email.contains("@")){
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }else{
            return !email.trim().isEmpty();
        }
    }

    public boolean isUsernameValid(String name){
        return name != null && name.trim().length() > 2 ;
    }

    public boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 2;
    }

   public boolean doPasswordsMatch(String p1, String p2){
        return p1.equals(p2);
   }
}
