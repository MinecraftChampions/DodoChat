package me.qscbm.plugins.dodochat.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataStorage {
    public static Connection conn = null;

    public static Statement stmt = null;


    public static void init(String url,String name,String password) {
       try {
           Class.forName("com.mysql.cj.jdbc.Driver");
           conn = DriverManager.getConnection(url,name,password);
           stmt = conn.createStatement();
           stmt.execute("create database if not exists dodochat");
           conn =  DriverManager.getConnection(url + "/dodochat",name,password);
            /*
                    绑定玩家的数据库格式
                    其实可以不搞主键，可以重复循环获取，有兴趣可以改一下
                   ---------- ---------------------------------------------------------------------------------
                   |   id    |                                    text                                         |
                   ---------- ----------------------------------------------------------------------------------
                   |  123456 | ["F6503A7C-D2EA-F622-D979-C1A1F0A2FDD5","453DD8CE-E48F-6ACD-2958-44323ECF4439"] |
                   ---------- ----------------------------------------------------------------------------------
             */
           String sql =
                   """
                           create table if not exists users (
                           id int primary key,
                           data text not null
                           )""";
           stmt = conn.createStatement();
           stmt.execute(sql);
       } catch (SQLException e) {
           e.printStackTrace();
       } catch (ClassNotFoundException e) {
           System.out.println("找不到MySQL驱动!");
           e.printStackTrace();
       }
   }
}
