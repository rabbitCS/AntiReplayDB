package test;

import java.io.*;
import java.net.Socket;
import java.sql.*;

import Util.MySQLDBUtils;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * @author rabbit-cs
 * @create 2020-03-05 20:05
 */
//捕获TCP数据包
class GetPackage{
    List<PcapNetworkInterface> alldev = Pcaps.findAllDevs();
    boolean loop_flag=false;
    StartCapture myCapture;

    GetPackage(int port,String ip,int time) throws PcapNativeException {
        //开启监听设备
        for(int i = 0; i < alldev.size(); ++i) {

            if (alldev.get(i).isLoopBack()) {
                System.out.println("序号:" + i + alldev.get(i).getName() + alldev.get(i).getDescription());
                loop_flag=true;
                this.myCapture = new StartCapture(alldev.get(i).getName(), port, ip, time);
                Thread t = new Thread(this.myCapture);
                t.start();
            }
        }
        if(loop_flag==false)
        {
            System.out.println("无本地loopback接口");
        }
    }
    //计时同步，获取数据包信息
    public List<TcpPacket> getPackage (int time){
        long start=System.currentTimeMillis();
        long end;
        do{
            end=System.currentTimeMillis();
        }while(end-start<=(long)time);
        MyPackageListenter.showPacket();
        return MyPackageListenter.listTCP;
    }
}
class MyPackageListenter implements PacketListener{
    //构造可用的tcp、ip数据报
    public  static List<TcpPacket> listTCP =new ArrayList<TcpPacket>();
    public  static List<BsdLoopbackPacket> listLoop =new ArrayList<BsdLoopbackPacket>();
    private String data;
    private int test_pcount=0;
    public  static StringBuffer recData=new StringBuffer("Packet::");

    public  MyPackageListenter(){

    }
    //观察者模式，抓到报文回调gotPacket方法
    public  void gotPacket( PcapPacket pcapPacket){
        //获得的是整个环回测试的原始数据，包括LoopBack、Ipv4头、Tcp头以及data
        if(pcapPacket.contains(TcpPacket.class)) {
            test_pcount++;
            System.out.println("当前为第"+test_pcount+"个报文");
            BuildMyTcpPacket(pcapPacket.get(TcpPacket.class));
            BuildMyBsdLoopbackPacket(pcapPacket.get(BsdLoopbackPacket.class));

            this.data=pcapPacket.get(TcpPacket.class).toHexString();

            recData.append(this.data);
        }
    }
    //获得的数据添加到列表
    public void BuildMyTcpPacket(TcpPacket packet){

        TcpPacket tcpPacket=packet;
        listTCP.add(tcpPacket);
    }
    public void BuildMyBsdLoopbackPacket(BsdLoopbackPacket packet){
        BsdLoopbackPacket BsdLoopbackPacket=packet;
        listLoop.add(BsdLoopbackPacket);
    }
    public static void showPacket(){
        System.out.println("捕获数据帧的输出测试...");

        for(int i=0;i<listLoop.size();i++)
        {
            if(listLoop.isEmpty())
            {
                System.out.println("没有捕获到的环回测试报文...");
                break;
            }
            System.out.println("*当前第"+i+"个环回测试报文");
            String rawData=listLoop.get(i).toHexString();
            System.out.println(rawData);
        }
    }
}
//数据信息存储
class StartCapture implements Runnable {
    private static final String READ_TIMEOUT_KEY = AntiReplay.class.getName() + ".readTimeout";
    private static final int READ_TIMEOUT = Integer.getInteger(READ_TIMEOUT_KEY, 10); // [ms]
    private static final String SNAPLEN_KEY = AntiReplay.class.getName() + ".snaplen";
    private static final int SNAPLEN = Integer.getInteger(SNAPLEN_KEY, 65536); // [bytes]
    private  String dev_name;
    private int port;//服务器端口号
    private String ip;//服务器ip
    private int time;
    PcapNetworkInterface nif;
    private PcapHandle handle;

    public StartCapture(String dev_name, int port, String ip, int time) throws PcapNativeException {
        this.dev_name=dev_name;
        this.ip = ip;
        this.port = port;
        this.time = time;
        this.nif=Pcaps.getDevByName(dev_name);
    }

    public void run() {
        try {
            String filter;
            //设置过滤规则
            if(this.port == -1) {
                filter = new String("host " + this.ip);
            } else {
                filter = new String("dst port " + this.port + " and " + "host " + this.ip);
            }
            System.out.println(filter);
            //开启网卡号，监听过滤，设置计时范围，开启新线程运行test
            this.handle = nif.openLive(SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
            System.out.println("正在监听" + this.dev_name+ "网卡"+"\t"+this.port +"号端口");
            try{
                this.handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

//                StartCapture.TimeCounter timeCounter = new StartCapture.TimeCounter(this.handle,this.time);
//                Thread t = new Thread(timeCounter);
//                t.start();
//                this.handle.loop(-1, new MyPackageListenter());
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(
                        new Runnable() {
                            public void run() {
                                while (true) {
                                    try {
                                        handle.loop(-1, new MyPackageListenter());
                                    } catch (PcapNativeException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        break;
                                    } catch (NotOpenException e) {
                                        break;
                                    }
                                }
                            }
                        });
                Thread.sleep(this.time);
                this.handle.breakLoop();
                this.handle.close();
                executor.shutdown();
                //handle.loop(30, new MyPackageListenter());
            } catch (NotOpenException e) {
                System.out.println("过滤器设置异常");
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }  catch (PcapNativeException e) {
            System.out.println("设备打开异常");
            e.printStackTrace();
        }
    }
    //子线程计时函数，到时间则关闭捕获器
    public class TimeCounter implements Runnable {
        private int time;
        private PcapHandle handle;
        public TimeCounter(PcapHandle handle,int time){
            this.handle=handle;
            this.time=time;
        }
        public void run() {
            System.out.println("计时器工作中...");
            long start = System.currentTimeMillis();

            long end;
            do {
                end = System.currentTimeMillis();
            } while(end - start <= (long)this.time);

            try {
                System.out.println(this.time+" 时间到");
                this.handle.breakLoop();
                System.out.println("breakLoop");
            } catch (NotOpenException e) {
                System.out.println("网卡端口关闭异常");
                e.printStackTrace();
            }
            this.handle.close();
        }
    }
}
//发送数据包
class SendPackage{
    List<PcapNetworkInterface> alldev = Pcaps.findAllDevs();
    private boolean loop_flag;
    StartSend mySend;

    SendPackage( ) throws PcapNativeException {
        for(int i = 0; i < alldev.size(); ++i) {
            if (alldev.get(i).isLoopBack()) {
                System.out.println("序号:" + i + alldev.get(i).getName() + alldev.get(i).getDescription());
                loop_flag=true;
                this.mySend = new StartSend(alldev.get(i).getName());
                Thread t = new Thread(this.mySend);
                t.start();
            }
        }
        if(loop_flag==false)
        {
            System.out.println("无本地loopback接口");
        }
    }
}
class StartSend implements Runnable {
    private static final String READ_TIMEOUT_KEY = StartSend.class.getName() + ".readTimeout";
    private static final int READ_TIMEOUT = Integer.getInteger(READ_TIMEOUT_KEY, 10); // [ms]
    private static final String SNAPLEN_KEY = StartSend.class.getName() + ".snaplen";
    private static final int SNAPLEN = Integer.getInteger(SNAPLEN_KEY, 65536); // [bytes]
    private  String dev_name;
    PcapNetworkInterface nif;
    private PcapHandle handlesend;

    public StartSend(String dev_name) throws PcapNativeException {
        this.dev_name=dev_name;
        this.nif=Pcaps.getDevByName(dev_name);
    }

    public void run() {
        try {
            this.handlesend = nif.openLive(SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
        } catch (PcapNativeException ex) {
            System.out.println("设备打开异常...");
            ex.printStackTrace();
        }
        System.out.println("正在向" + this.dev_name+ "网卡发送数据");
        try{
            if(!MyPackageListenter.listLoop.isEmpty()) {
                for (int i = 0; i < MyPackageListenter.listLoop.size(); i++) {
                    System.out.println("正在向" + this.dev_name + "网卡发送第" + i + "个ip报文");

                    MyPackageListenter.listLoop.get(i).getHeader();
                    handlesend.sendPacket(MyPackageListenter.listLoop.get(i));
                    String rawData = MyPackageListenter.listLoop.get(i).toHexString();
                    System.out.println(rawData);
                }
            }
            handlesend.close();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        } catch (NotOpenException e) {
            e.printStackTrace();
        }
    }
}

class AnotherSend{
    List<TcpPacket> tcpPackets=new ArrayList<TcpPacket>();
    private String host;
    private int port;
    private Socket socket=null;
    private BufferedReader reader=null;
    private BufferedWriter writer=null;
    DataOutputStream out=null;

    public AnotherSend(String host,int port,List<TcpPacket> tcpPackets){
        this.host=host;
        this.port=port;
        this.tcpPackets=tcpPackets;

        try {
            this.socket=new Socket(this.host,this.port);
            this.reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.out=new DataOutputStream((socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("套接字创建失败");
            e.printStackTrace();
        }
    }
    static void readSocketInfo(BufferedReader reader){
        new Thread(new Client_Long.MyRuns(reader)).start();
    }

    static class MyRuns implements Runnable{

        BufferedReader reader;

        public MyRuns(BufferedReader reader) {
            super();
            this.reader = reader;
        }

        public void run() {
            try {
                String lineString="";
                while( (lineString = reader.readLine())!=null ){
                    System.out.println(lineString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public boolean startSendNow(byte[] data){
        boolean result=true;
        try {
            this.out.write(data);
            this.out.flush();
        } catch (IOException e) {
            System.out.println("发送失败");
            result=false;
            e.printStackTrace();
        }
        return result;
    }
    public void closeAll(){
        if (this.reader!=null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.writer!=null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.out!=null) {
            try {
                this.out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.socket!=null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public boolean startSend(){
        boolean result=true;
        try {
            //在mysql重放时去掉，access重放时加上
            //读出服务器返回的信息，在控制台打印出来
            readSocketInfo(reader);
            for(int i=0;i<this.tcpPackets.size();i++){
                int head_length=tcpPackets.get(i).getHeader().toHexString().length();
                int all_length=tcpPackets.get(i).toHexString().length();
                if(head_length!=all_length) {
                    System.out.println("重发第"+i+"个报文");
                    byte[] sendMessage = tcpPackets.get(i).getPayload().getRawData();
//                    String sendMessage_hex = tcpPackets.get(i).getPayload().toString();
//                    writer.write(sendMessage_hex + "\n");
//                    writer.flush();
                    out.write(sendMessage);
                    out.flush();
                    Thread.sleep(2000);
                }
            }
            //在mysql重放时去掉，access重放时加上
            //重放结束时，向服务器发送“bye”，请求断开连接
            writer.write("bye"+"\n");//断开连接
            writer.flush();
            Thread.sleep(2000);
        } catch (IOException e) {
            result=false;
            e.printStackTrace();
        } catch (InterruptedException e) {
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
        return result;
    }
}

public class AntiReplay {
    public static void main(String[] args) throws InterruptedException, SQLException, PcapNativeException {
        GetPackage test = new GetPackage(5236,"127.0.0.1",15*1000);//10秒
        Thread.currentThread().sleep(5000);//5秒
        System.out.println("开始执行数据库语句...");
        Connection connection= MySQLDBUtils.getConn("root","123456");
        Statement stmt=connection.createStatement();
        String sql="CREATE USER U1 IDENTIFIED BY 123456789;";
        stmt.executeUpdate(sql);
        System.out.println("创建用户U1成功....");

        List<TcpPacket> tcpPackets=test.getPackage(15*1000);
        System.out.println("捕获报文线程已结束");

        sql="DROP USER U1 CASCADE;";
        stmt.executeUpdate(sql);
        System.out.println("删除用户成功...");
        MySQLDBUtils.close(connection,stmt,null);

        AnotherSend testSend=new AnotherSend("127.0.0.1",5236,tcpPackets);
        testSend.startSend();
//        SendPackage sendPackagetest=new SendPackage();
        boolean test_flag=false;
        System.out.println("数据报重新发送完毕");

        Connection connection2=MySQLDBUtils.getConn("SYSDBA","SYSDBA");
        Statement stmt2=connection2.createStatement();
        ResultSet rs=null;
        rs=stmt2.executeQuery("select username from dba_users;");
        while (rs.next()){
            if(rs.getString("username").equals("U1")) {
                test_flag = true;
                break;
            }
            System.out.println(rs.getString("username"));
        }
        if(test_flag){
            System.out.println("存在U1用户");
        }
        else
            System.out.println("不存在U1用户，重放报文无正常响应");
        MySQLDBUtils.close(connection2,stmt2,rs);
    }
}

