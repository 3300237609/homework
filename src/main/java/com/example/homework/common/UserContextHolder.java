package com.example.homework.common;

public class UserContextHolder {
    private static final ThreadLocal<String> user = new ThreadLocal<>();

    public static void setUserId(String userId) {
        user.set(userId);
    }

    public static String getUserId() {
        return user.get();
    }

    public static void clear() {
        user.remove();
    }
}