package cinelux.bioskopcinelux.util;

import cinelux.bioskopcinelux.model.Karyawan;

public class Session {
    private static Karyawan loggedUser;

    public static void setLoggedUser(Karyawan user) {
        loggedUser = user;
    }

    public static Karyawan getLoggedUser() {
        return loggedUser;
    }

    public static String getLoggedUsername() {
        return loggedUser != null ? loggedUser.getUsername() : null;
    }

    public static String getLoggedName() {
        return loggedUser != null ? loggedUser.getNama() : null;
    }

    public static int getLoggedUserId() {
        return loggedUser != null ? loggedUser.getId() : -1;
    }

    public static void clear() {
        loggedUser = null;
    }
}
