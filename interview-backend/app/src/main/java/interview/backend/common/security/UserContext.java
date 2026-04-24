package interview.backend.common.security;

import interview.backend.modules.user.dto.UserInfo;

public final class UserContext {

    private static final ThreadLocal<UserInfo> currentUser = new ThreadLocal<>();

    private UserContext() {}

    public static void setUser(UserInfo user) {
        currentUser.set(user);
    }

    public static UserInfo getUser() {
        return currentUser.get();
    }

    public static Long getUserId() {
        UserInfo user = getUser();
        return user != null ? user.id() : null;
    }

    public static String getUsername() {
        UserInfo user = getUser();
        return user != null ? user.username() : null;
    }

    public static void clear() {
        currentUser.remove();
    }
}
