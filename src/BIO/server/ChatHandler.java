package BIO.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @Author qq
 * @Date 2022/4/20
 */
public class ChatHandler implements Runnable {

    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server,Socket socket){
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            server.addClient(socket);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String massage = null;
            while ((massage=reader.readLine()) != null){
                String forwardMsg = "客户端["+socket.getPort()+"]:"+massage+"\n";
                //服务器接收到客户端的消息
                System.out.print(forwardMsg);
                //服务器转发消息给其他客户端
                server.forwardMsg(socket,forwardMsg);
                //客户端连接退出
                if(server.quit(massage)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                server.deleteClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
