package com.expensetracker.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hash(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt(10));
    }

    public static boolean mathches(String pwd, String hash){
        return BCrypt.checkpw(pwd,hash);
    }
}
