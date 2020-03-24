/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rogueparkour_placeholder;

import java.sql.*;

/**
 *
 * @author Admin
 */
public class con_database{
  public String user;
  public String database;
  public String password;
  public String port;
  public String hostname;
  public Connection connection;
  
  public con_database(String hostname, String port, String database, String username, String password){
    this.hostname = hostname;
    this.port = port;
    this.database = database;
    this.user = username;
    this.password = password;
    this.connection = null;
  }
  
  public Connection openConnection(){
    try{
      Class.forName("com.mysql.jdbc.Driver");
      
      Connection conn = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/", this.user, this.password);
      
      Statement s = conn.createStatement();
      int Result = s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + this.database + ";");
      conn.close();
      
      this.connection = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user, this.password);
    }
    catch (SQLException e){
      System.err.println("Error MYSQL: " + e.getMessage());
    }
    catch (ClassNotFoundException e){
      System.err.println("JDBC Driver not found!");
    }
    return this.connection;
  }
  
  public boolean checkConnection(){
    return this.connection != null;
  }
  
  public Connection getConnection(){
    return this.connection;
  }
  
  public void closeConnection(){
    if (this.connection != null) {
      try{
        this.connection.close();
      }
      catch (SQLException e){
        System.err.println("Error closing the MySQL Connection!");
        e.printStackTrace();
      }
    }
  }
}
