/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author Ahmad
 */
public class KoneksiDB {
private static final String URL = "jdbc:mysql://localhost:3306/db_vpass";
    private static final String USER = "root";  
    private static final String PASSWORD = ""; 

    public static Connection getKoneksi() {
        Connection koneksi = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            

            koneksi = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("berhasil konek");
        } catch (ClassNotFoundException e) {
            System.out.println("ga ketemu");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Gagal konek");
            e.printStackTrace();
        }
        return koneksi;
    }

} 
