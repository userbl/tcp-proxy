import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: BaiLong on  2017-09-05  4:34 PM
 */
public class TCPProxy {

    private static final int buf_size = 1024*200;
    public static void main(String[] args) throws Exception {

        String[] splitLocal = args[0].split(":");
        String[] splitRemote = args[1].split(":");

        String remoteIP = splitRemote[0];
        int remotePort = Integer.parseInt(splitRemote[1]);
        String localIP = splitLocal[0];
        int localPort = Integer.parseInt(splitLocal[1]);

        ServerSocket listener = new ServerSocket(localPort);
        try {
            while (true) {
                Socket socket = listener.accept();
                try {
                    Socket remote = new Socket(remoteIP, remotePort);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            transfer(socket, remote);
                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            transfer(remote, socket);
                        }
                    }).start();
                } finally {
//                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

    private static  void transfer(Socket socketIn, Socket socketOut) {
        try {
            if(!socketIn.isClosed() && !socketOut.isClosed()) {
                InputStream inputStream = socketIn.getInputStream();
                OutputStream s_out = socketOut.getOutputStream();
                while (true) {
                    int len = 0;
                    byte[] buf = new byte[buf_size];
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

