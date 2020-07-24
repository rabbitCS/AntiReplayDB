package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author rabbit-cs
 * @create 2020-03-05 20:01
 */
public class Server_Long {
    // private final static Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);
    public static void main(String[] args) {
        Server_Long server_long=new Server_Long();
        Server_Long.startAction();
    }
    public static void startAction(){
        //List<Socket> sockets=new ArrayList<Socket>();
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket(8888);  //端口号
//            LOGGER.info("服务端服务启动监听：");
            System.out.println("服务端服务启动监听：");
            //通过死循环开启长连接，开启线程去处理消息
            while(true){
                Socket socket=serverSocket.accept();
                //String message = socket.getInetAddress().getHostAddress().toString();
                System.out.println("客户端：" + socket.getPort() + "已连接~");

                new Thread(new MyRuns(socket)).start();
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
        BufferedReader reader;
        BufferedWriter writer;

        public MyRuns(Socket socket) {
            super();
            this.socket = socket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//读取客户端消息
                writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));//向客户端写消息
                String lineString="";

                while(true){
                    lineString=reader.readLine();
                    int port=socket.getPort();
                    System.out.println("收到来自"+port+"客户端的发送的消息是：" + lineString);

                    if(lineString.equals("bye"))
                    {
                        System.out.println("客户端"+port+"断开连接"+"\n");
                        writer.write("服务器返回：连接断开"+"\n");
                        writer.flush();
                        break;
                    }
                    writer.write("服务器返回：ok"+"\n");
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

    }
}