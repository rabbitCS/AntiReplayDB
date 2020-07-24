package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author rabbit-cs
 * @create 2020-03-05 19:49
 */
public class AccessClient {
    public static void main(String[] args){
        AccessClient client=new AccessClient();
        client.startAction();
    }

    static void readSocketInfo(BufferedReader reader){
        new Thread(new AccessClient.MyRuns(reader)).start();
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

    public static void startAction(){
        Socket socket=null;
        BufferedReader reader=null;
        BufferedWriter writer=null;
        BufferedReader reader2=null;
        try {
            socket=new Socket("127.0.0.1", 7777);
            reader = new BufferedReader(new InputStreamReader(System.in));
            reader2=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            readSocketInfo(reader2);
            String lineString="";
            while(!(lineString=reader.readLine()).equals("exit")){//输入exit结束
                String myData=getDataPackage(lineString);
                writer.write(myData+"\n");//输入bye结束
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
    public static String getDataPackage(String inputData){

        String out=null;
        if(inputData.indexOf(" ")!=-1){//存在空格，则为数据库语句
            String test=inputData.substring(0,inputData.indexOf(" "));
            if(check(test)){
                out="1 "+inputData;//协议标志1为选择语句
            }
            else
                out="0 "+inputData;//协议标志0为其他语句
        }else//否则为连接语句
            out=inputData;
        return out;
    }
    public static boolean check(String test){
        if(test.equals("select")){
            return true;
        }
        return false;
    }
}
