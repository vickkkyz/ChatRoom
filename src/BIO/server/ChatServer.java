package BIO.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author qq
 * @Date 2022/4/20
 */

/**
 * BIO：即Blocking-IO，阻塞IO
 * 在BIO中，Socket socket = serverSocket.accept();是阻塞进行的，当没有客户端发送连接请求时，服务器会阻塞住，直到有客户端进行连接，返回客户端的这个socket
 * reader.readLine()也是阻塞的，当用户没有向缓冲区输入数据时，这会一直阻塞（流strean的读写都是阻塞式的）
 * 因此无法在同一个线程中处理多个IO，所以需要有一个主线程，来监听当前端口是否有连接请求，然后分配一个线程给这个客户端
 */
public class ChatServer {
    //阻塞式的聊天室，对于服务器端，需要有一个线程来监听客户端的连接请求，并将连接分配给处理客户端线程的线程
    //这个线程不能阻塞，需要实时监听端口
    private final Integer PORT = 8888;
    private final String QUIT = "quit";
    private Map<Integer, BufferedWriter> conectedLists;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public ChatServer(){
        conectedLists = new HashMap<>();
        executorService = Executors.newFixedThreadPool(10);;
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if(socket!= null){
            int port = socket.getPort();
            if(!conectedLists.containsKey(port)){
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                conectedLists.put(port,writer);
            }
        }
    }

    public synchronized void deleteClient(Socket socket) throws IOException {
        if(socket!= null){
            int port = socket.getPort();
            if(conectedLists.containsKey(port)){
                conectedLists.get(port).close();
                conectedLists.remove(port);
            }
        }
    }

    public synchronized void forwardMsg(Socket socket,String message) throws IOException {
        for (Integer port :
                conectedLists.keySet()) {
            if (port != socket.getPort()){
                BufferedWriter writer = conectedLists.get(port);
                writer.write(message);
                writer.flush();
            }
        }
    }

    public boolean quit(String message){
        return QUIT.equals(message);
    }

    public void start(){
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("启动服务器，监听端口"+PORT);
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("客户端["+socket.getPort()+"]:已经连接到服务器...");
                //将这个客户端连接交给新的处理线程
                //new Thread(new ChatHandler(this,socket)).start();
                executorService.execute(new ChatHandler(this,socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
