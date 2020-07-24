package test;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.TcpPacket;

import java.util.List;

/**
 * @author rabbit-cs
 * @create 2020-03-05 19:49
 */
public class AccessClient_Re {
    public static void main(String[] args) throws PcapNativeException {
        GetPackage test = new GetPackage(7777, "127.0.0.1", 8 * 1000);//15秒
        List<TcpPacket> tcpPackets = test.getPackage(13 * 1000);
        for (int i = 0; i < tcpPackets.size(); i++) {
            String all = tcpPackets.get(i).toHexString();
            String head = tcpPackets.get(i).getHeader().toHexString();
//            System.out.println("all_rawData:"+Arrays.toString(rawData));
        }
        System.out.println("捕获报文线程已结束");

        //7979  yy
        AnotherSend testSend = new AnotherSend("127.0.0.1", 7777, tcpPackets);
        testSend.startSend();
    }
}
