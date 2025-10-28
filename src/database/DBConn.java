/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;
import java.sql.*;

/**
 *
 * @author Fcaty
 */
public class DBConn {
    private static final String url = "jdbc:mysql://localhost:3306/accountingsystem";
    private static final String user = "root";
    private static final String pass = "";
    
    public static Connection attemptConnection(){
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: "+e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection error: "+e.getMessage());
        }
        
        return con;
    }
}
