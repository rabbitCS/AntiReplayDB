package Util;

import java.sql.*;

/**
 * @author rabbit-cs
 * @create 2020-03-05 19:56
 */
public class MySQLDBUtils {
    private static String driver="com.mysql.jdbc.Driver";
    private static String url="jdbc:mysql://127.0.0.1:3306";

    public  static Connection getConn(String user, String password){
        Connection connection=null;
        try{
            Class.forName(driver);
            System.out.println("Connection to DBMS.....");
            System.out.println("使用" + user + "连接数据库...");
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return  connection;
    }
    public  static void close(Connection conn, Statement stmt, ResultSet rs){
        try {
            if(rs!=null){
                rs.close();
            }
            if(stmt!=null){
                stmt.close();
            }
            if(conn!=null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
