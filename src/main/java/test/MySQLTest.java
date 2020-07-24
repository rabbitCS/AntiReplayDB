package test;

/**
 * @author rabbit-cs
 * @create 2020-03-05 19:50
 */
import Util.MySQLDBUtils;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.TcpPacket;

import java.sql.*;
import java.util.List;

public class MySQLTest {
    public static void main(String[] args) throws PcapNativeException, SQLException, InterruptedException {
        GetPackage test = new GetPackage(3306,"127.0.0.1",15*1000);//10秒
        Thread.currentThread().sleep(5000);//5秒
        System.out.println("开始执行数据库语句...");
        Connection connection= MySQLDBUtils.getConn("root","123456");
        Statement stmt=connection.createStatement();
        String sql="CREATE USER U1 IDENTIFIED BY 'u1'";
        stmt.executeUpdate(sql);
        System.out.println("创建用户U1成功....");

        List<TcpPacket> tcpPackets=test.getPackage(15*1000);
        System.out.println("捕获报文线程已结束");

        sql="DROP USER U1;";
        stmt.executeUpdate(sql);
        System.out.println("删除用户成功...");
        MySQLDBUtils.close(connection,stmt,null);

        AnotherSend testSend=new AnotherSend("127.0.0.1",3306,tcpPackets);
        testSend.startSend();
        boolean test_flag=false;
        System.out.println("数据报重新发送完毕");

        Connection connection2=MySQLDBUtils.getConn("root","123456");
        Statement stmt2=connection2.createStatement();
        ResultSet rs=null;
        rs=stmt2.executeQuery("select user from mysql.user;");
        while (rs.next()){
            if(rs.getString("user").equals("U1")) {
                test_flag = true;
                break;
            }
            //System.out.println(rs.getString("user"));
        }
        if(test_flag){
            System.out.println("存在U1用户");
        }
        else
            System.out.println("不存在U1用户，重放报文无正常响应");
        MySQLDBUtils.close(connection2,stmt2,rs);
    }

}

