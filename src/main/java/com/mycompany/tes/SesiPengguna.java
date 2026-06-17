package com.mycompany.tes;

/**
 * 
 * @author Ahmad
 */
public class SesiPengguna {
    private static String usernameAktif;

    public static String getUsernameAktif() {
        return usernameAktif;
    }

    public static void setUsernameAktif(String username) {
        usernameAktif = username;
    }

    public static void clearSesi() {
        usernameAktif = null;
    }
}