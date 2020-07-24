package test;

import org.pcap4j.core.PcapNativeException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author rabbit-cs
 * @create 2020-03-05 20:01
 */
public class Client_Long {
    public static void main(String[] args) throws PcapNativeException {
        Client_Long client=new Client_Long();
        client.startAction();
    }

    static void readSocketInfo(BufferedReader reader){
        new Thread(new MyRuns(reader)).start();
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
            socket=new Socket("127.0.0.1", 8888);
            reader = new BufferedReader(new InputStreamReader(System.in));
            reader2=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            readSocketInfo(reader2);
            String lineString="";
            while(!(lineString=reader.readLine()).equals("exit")){
                writer.write(lineString+"\n");//输入bye\n结束
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
