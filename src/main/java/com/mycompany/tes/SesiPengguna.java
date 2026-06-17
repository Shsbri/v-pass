package com.mycompany.tes;

/**
 * * @author Ahmad
 */
public class SesiPengguna {
    private static int idPengguna;
    private static String nama;
    private static String usernameAktif;
    private static String email;
    private static String role;

    public static int getIdPengguna() {
        return idPengguna;
    }

    public static void setIdPengguna(int id) {
        idPengguna = id;
    }

    public static String getNama() {
        return nama;
    }

    public static void setNama(String name) {
        nama = name;
    }

    public static String getUsernameAktif() {
        return usernameAktif;
    }

    public static void setUsernameAktif(String username) {
        usernameAktif = username;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String mail) {
        email = mail;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String statusRole) {
        role = statusRole;
    }

    public static void clearSesi() {
        idPengguna = 0;
        nama = null;
        usernameAktif = null;
        email = null;
        role = null;
    }
}