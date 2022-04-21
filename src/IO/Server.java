package IO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author qq
 * @Date 2022/4/19
 */
public class Server {
    private  Integer DEFAULT_PORT = 8888;
    public void server() {
        //首先确定服务器端监听的端口号
        ServerSocket serverSocket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，开始监听"+DEFAULT_PORT+"端口");

            while (true) {
                //监听客户端,如果没有客户端发送消息，服务端会在这里阻塞
                Socket socket = serverSocket.accept();
                //client socket 的端口是自动分配的
                System.out.println("服务器监听到了客户端["+socket.getPort()+"]的连接");
                //监听到了，说明客户端要发送数据了，因此服务器端要创建一个缓冲区来接收消息

                //接收客户端的输入流
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //向客户端发送输出流
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                //读取缓冲区中的数据

                //这里也是，循环的条件不要写成 while((massage2 = reader.readLine())!= null)
                while (true){
                    String massage = reader.readLine();
                    System.out.println("接收客户端["+socket.getPort()+"]发送的数据：["+massage+"]");

                    //每次接收一行数据，服务器端就给客户端回复一条相同的数据
                    //write()将内容存到缓冲区中
                    //这里也是，注意加换行符
                    writer.write(massage+"\n");
                    //flush()冲刷出缓冲池的数据,将所有的缓存数据强行发送到目的地
                    writer.flush();
                    if("quit".equals(massage)){
                        break;
                    }
                }
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
    public static void main(String[] args){
        Server server1 = new Server();
        server1.server();
    }
}
