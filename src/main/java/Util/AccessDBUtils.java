package Util;

/**
 * @author rabbit-cs
 * @create 2020-03-05 19:51
 */
import java.sql.*;

public class AccessDBUtils {
    private static final String dbURL = "jdbc:ucanaccess://" +
            "E:\\java\\DatabaseTest.accdb";

    static {
        try {
        // Step 1: Loading or registering Oracle JDBC driver class
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (
         // 加载驱动
         ClassNotFoundException cnfex) {
            System.out.println("Problem in loading or registering MS Access JDBC driver");
            cnfex.printStackTrace();
        }
    }

    //建立连接
    public static Connection getConn() {
        try {
        // Step 2: Opening database connection
        // Step 2.A: Create and get connection using DriverManager class
            return DriverManager.getConnection(dbURL);
        } catch (Exception e) {
            System.out.println("AccessDB connection fail");
            e.printStackTrace();
        }
        return null;
    }

    // 关闭资源
    public static void close(Connection con, Statement ps, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (con != null)
                    try {
                        con.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }
    }
    public static void main(String[] args) throws SQLException {
        String sql="select * from student";
        Connection connection=getConn();
        Statement stmt=connection.createStatement();
        ResultSet rs=stmt.executeQuery(sql);
        while(rs.next()){
            System.out.println(rs.getString("ID"));
            System.out.println(rs.getString("age"));
            System.out.println(rs.getString("test_name"));
        }
        sql="update student set age=age-1 where test_name='xiaoming'";
        stmt.executeUpdate(sql);

        close(connection,stmt,rs);
    }
}
