package test;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.TcpPacket;

import java.util.Arrays;
import java.util.List;

/**
 * @author rabbit-cs
 * @create 2020-03-05 20:01
 */
public class Client_Re {
    public static void main(String[] args) throws PcapNativeException {
        try {
            GetPackage test = new GetPackage(8888,"127.0.0.1",15*1000);//15秒
            List<TcpPacket> tcpPackets=test.getPackage(20*1000);

            for(int i=0;i<tcpPackets.size();i++)
            {
                int all=tcpPackets.get(i).toHexString().length();
                int head=tcpPackets.get(i).getHeader().toHexString().length();
                if(all!=head){
                    //输出字节数组
                    byte[] testData=tcpPackets.get(i).getPayload().getRawData();
                    System.out.println(Arrays.toString(testData));
                    System.out.println(testData.length);//字节数*8=位数
                    showBit(testData[0]);
                    testData[0]=changeBit6(testData[0]);
                    showBit(testData[0]);
                    System.out.println(Arrays.toString(testData));
                }
//            System.out.println("all_rawData:"+Arrays.toString(rawData));
            }
            System.out.println("捕获报文线程已结束");

            //7979  yy
            AnotherSend testSend=new AnotherSend("127.0.0.1",8888,tcpPackets);
            testSend.startSend();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
    }
    public static byte changeBit6(byte b){//改变第6位
        byte afterChange=b;
        if(getBit(b,1)==1){
            afterChange=(byte)(b&0xFD);//置0 1101
        }
        else
            afterChange=(byte)(b|0x02);
        return afterChange;
    }
    public static void showBit(byte b){
        for (int i=0;i<8;i++)
        {
            int bit=((b>>(7-i))&0x1);
            System.out.print(bit);
        }
        System.out.println();
    }

    public static int getBit(byte b, int i){
        int bit=((b>>i)&0x1);
        return  bit;
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
