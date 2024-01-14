package com.dianping.util;

import com.dianping.dto.UserDTO;
import com.dianping.entity.User;

public class UserHolder {

    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO userId) {
        tl.set(userId);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}
