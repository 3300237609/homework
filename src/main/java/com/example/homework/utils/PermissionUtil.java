package com.example.homework.utils;

import com.example.homework.common.UserContextHolder;

/**
 * 权限工具类（返回 true/false）
 * 仅支持 3 种角色：1管理员 2老师 3学生
 */
public class PermissionUtil {

    // 角色ID
    public static final String ADMIN = "1";
    public static final String TEACHER = "2";
    public static final String STUDENT = "3";

    // ====================== 是否登录 ======================
    public static boolean isLogin() {
        return UserContextHolder.getUserId() != null;
    }

    // ====================== 是否管理员 ======================
    public static boolean isAdmin() {
        if (!isLogin()) return false;
        return ADMIN.equals(UserContextHolder.getRole());
    }

    // ====================== 是否老师 ======================
    public static boolean isTeacher() {
        if (!isLogin()) return false;
        return TEACHER.equals(UserContextHolder.getRole());
    }

    // ====================== 是否学生 ======================
    public static boolean isStudent() {
        if (!isLogin()) return false;
        return STUDENT.equals(UserContextHolder.getRole());
    }

    // ====================== 是否 管理员 OR 老师 ======================
    public static boolean isAdminOrTeacher() {
        if (!isLogin()) return false;
        String role = UserContextHolder.getRole();
        return ADMIN.equals(role) || TEACHER.equals(role);
    }

    // ====================== 是否自己 或 管理员 ======================
    public static boolean isSelfOrAdmin(Long targetUserId) {
        if (!isLogin()) return false;

        // 管理员直接放行
        if (isAdmin()) return true;

        // 检查是否是自己
        String currentId = UserContextHolder.getUserId();
        return currentId.equals(targetUserId.toString());
    }
}