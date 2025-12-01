//package com.stoliar.util;
//
//import com.stoliar.entity.Role;
//
//public class RoleUtils {
//
//    public static String toSpringSecurityRole(Role role) {
//        return "ROLE_" + role.name();
//    }
//
//    public static boolean isAdmin(Role role) {
//        return Role.ADMIN.equals(role);
//    }
//
//    public static void validateRoleCreation(Role currentUserRole, Role targetRole) {
//        if (isAdmin(targetRole) && !isAdmin(currentUserRole)) {
//            throw new SecurityException("Only ADMIN can create users with ADMIN role");
//        }
//    }
//}