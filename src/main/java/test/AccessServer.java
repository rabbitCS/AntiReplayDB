package test;

import Util.AccessDBUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author rabbit-cs
 * @create 2020-07-24 20:13
 */
public class AccessServer {
    // private final static Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);
    public static void main(String[] args) {
        AccessServer server=new AccessServer();
        server.startAction();
    }
    public static void startAction(){
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket(7777);  //端口号
//            LOGGER.info("服务端服务启动监听：");
            System.out.println("Access服务端服务启动监听：");
            //通过死循环开启长连接，开启线程去处理消息
            while(true){
                Socket socket=serverSocket.accept();
                //String message = socket.getInetAddress().getHostAddress().toString();
                System.out.println("客户端：" + socket.getPort() + "已连接~");
                Connection connection= AccessDBUtils.getConn();
                new Thread(new AccessServer.MyRuns(socket,connection)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket!=null) {
                    serverSocket.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    static class MyRuns implements Runnable{

        Socket socket;
        Connection connection;
        BufferedReader reader;
        BufferedWriter writer;

        public MyRuns(Socket socket,Connection connection) {
            super();
            this.connection=connection;
            this.socket = socket;
        }

        public void run() {
            try {
                Statement stmt=this.connection.createStatement();
                ResultSet rs=null;
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//读取客户端消息
                writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));//向客户端写消息
                String lineString="";

                while(true){
                    lineString=reader.readLine();
                    int port=socket.getPort();
                    //System.out.println("收到来自"+port+"客户端的发送的消息是：" + lineString);
                    String sql;
                    if(lineString.equals("bye")) {
                        System.out.println("收到来自"+port+"客户端的发送的消息是：断开连接");
                        writer.write("服务器返回：断开连接\n");
                        writer.flush();
                        AccessDBUtils.close(connection,null,null);
                        break;
                    }
                    int flag=AnalysisPackage(lineString);//分析包内容
                    if(flag==1){//选择语句
                        String result="result:"+"\n";
                        sql=getSQL(lineString);
                        rs=stmt.executeQuery(sql);
                        int clumn=rs.getMetaData().getColumnCount();
                        while (rs.next()){
                            for(int i=1;i<=clumn;i++)
                                result+=rs.getString(i)+" ";
                            result+="\n";
                        }
                        System.out.println("收到来自"+port+"客户端的发送的消息是：查询请求");
                        writer.write("服务器返回：查询结果"+"\n"+result);
                    }
                    else if(flag==0){//更新语句
                        System.out.println("收到来自"+port+"客户端的发送的消息是：更新请求");
                        sql=getSQL(lineString);
                        int sqlFlag=stmt.executeUpdate(sql);
                        if(sqlFlag>=1){
                            writer.write("服务器返回：ok  执行成功\n");
                        }else
                            writer.write("服务器返回：no  执行失败\n");
                    }

                    writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader!=null) {
                        reader.close();
                    }
                    if (writer!=null) {
                        writer.close();
                    }
                    if (socket!=null) {
                        socket.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        public static int AnalysisPackage(String lineString){
            String test=lineString.substring(0,lineString.indexOf(" "));
            if(test.equals("1"))
                return 1;
            return 0;
        }
        public static String getSQL(String lineString){
            String sql=lineString.substring(lineString.indexOf(" "));
            //System.out.println(sql);
            return  sql;
        }

    }
}

