import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: BaiLong on  2017-09-05  4:34 PM
 */
public class Proxy5_Load_Blance {


    private static final int buf_size = 1024 * 1024*2;

    // 待路由的Ip列表，Key代表Ip，Value代表该Ip的权重

    public static HashMap<String, Integer> serverWeightMap =
            new HashMap<String, Integer>();

    static {
        for(int  i=0; i<=10; i++) {
            serverWeightMap.put("192.168.122.211:"+(1080+i), 1);
        }
//        serverWeightMap.put("127.0.0.1:1081", 1);
//        serverWeightMap.put("127.0.0.1:1082", 1);
//        serverWeightMap.put("127.0.0.1:1083", 1);
    }

    public static String getServer() {
        // 重建一个Map，避免服务器的上下线导致的并发问题
        Map<String, Integer> serverMap =
                new HashMap<String, Integer>();
        serverMap.putAll(serverWeightMap);

        // 取得Ip地址List
        Set<String> keySet = serverMap.keySet();
        ArrayList<String> keyList = new ArrayList<String>();
        keyList.addAll(keySet);

        java.util.Random random = new java.util.Random();
        int randomPos = random.nextInt(keyList.size());

        return keyList.get(randomPos);
    }

    public static void main(String[] args) throws Exception {

        String server = getServer();

        String[] splitLocal = server.split(":");

        int remotePort = Integer.parseInt(splitLocal[1]);
        String remoteIP = splitLocal[0];

        ServerSocket listener = new ServerSocket(8888);
        try {
            while (true) {
                Socket socket = listener.accept();
                new Thread(() -> {
                    SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                    System.out.println("Process request from " + remoteSocketAddress);

                    try {
                        Socket remote = new Socket(remoteIP, remotePort);
                        new Thread(() -> transfer(socket, remote)).start();
                        new Thread(() -> transfer(remote, socket)).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

    private static void transfer(Socket socketIn, Socket socketOut) {
        try {
            if (!socketIn.isClosed() && !socketOut.isClosed()) {
                try (InputStream inputStream = socketIn.getInputStream();
                     OutputStream s_out = socketOut.getOutputStream()) {
                    byte[] buf = new byte[buf_size];
                    while (true) {
                        int len = 0;
                        len = inputStream.read(buf);
//                    System.out.println(len);
                        if (len > 0) {
                            s_out.write(buf, 0, len);
                            s_out.flush();
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            if (null != socketIn) {
                try {
                    socketIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != socketOut) {
                try {
                    socketOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("request complete!");
        }
    }
}
