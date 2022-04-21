package BIO.client;

import java.io.*;
import java.net.Socket;

/**
 * @Author qq
 * @Date 2022/4/20
 */
public class ChatClient {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private final String QUIT = "quit";
    private final Integer PORT = 8888;
    private final String IP = "127.0.0.1";

    //向服务器发送数据
    public void send(String message) throws IOException {
        //输出流还没有关闭
        if(!socket.isOutputShutdown()){
            writer.write(message+"\n");
            writer.flush();
        }
    }

    //从服务器接收数据
    public String receive() throws IOException {
        if(!socket.isInputShutdown()){
            String message = reader.readLine();
            return message;
        }
        return null;
    }

    public boolean quit(String message){
        return QUIT.equals(message);
    }

    //真正的业务逻辑
    public void start(){
        try {
            socket = new Socket(IP,PORT);
            //创建客户端的IO流和缓冲区
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //创建线程等待用户输入，然后向服务器写入消息
            new Thread(new ChatHandler(this)).start();
            //接收来自服务器的消息
            String message = null;
            while ((message = receive())!= null){
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }
}
