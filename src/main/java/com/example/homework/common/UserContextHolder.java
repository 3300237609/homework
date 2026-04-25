package com.example.homework.common;

public class UserContextHolder {

    // 内部类，同时存 userId 和 role
    public static class UserInfo {
        public String userId;
        public String role;
    }

    private static final ThreadLocal<UserInfo> USER_INFO = new ThreadLocal<>();

    // 存入 userId
    public static void setUserId(String userId) {
        UserInfo info = USER_INFO.get();
        if (info == null) info = new UserInfo();
        info.userId = userId;
        USER_INFO.set(info);
    }

    // 存入 role
    public static void setRole(String role) {
        UserInfo info = USER_INFO.get();
        if (info == null) info = new UserInfo();
        info.role = role;
        USER_INFO.set(info);
    }

    // 获取 userId
    public static String getUserId() {
        UserInfo info = USER_INFO.get();
        return info == null ? null : info.userId;
    }

    // 获取 role
    public static String getRole() {
        UserInfo info = USER_INFO.get();
        return info == null ? null : info.role;
    }

    // 清空
    public static void clear() {
        USER_INFO.remove();
    }
}