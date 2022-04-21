package NIO.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @Author qq
 * @Date 2022/4/21
 */
public class ChatClient {
    private final String QUIT = "quit";
    private static final Integer PORT = 8888;
    private static final String IP = "127.0.0.1";
    private final int BUFFER_CAPACITY = 1024;

    private SocketChannel clientChannel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);

    private Selector selector;
    //想要连接的端口，可以自定义
    private int port;
    private String ip;
    //定义交换的文本格式
    private Charset charset = Charset.forName("UTF-8");

    public ChatClient(){
        this(PORT,IP);
    }
    public ChatClient(int port,String ip){
        this.port = port;
        this.ip = ip;
    }

    public boolean quit(String message){
        return QUIT.equals(message);
    }

    public void start(){

        try {
            //打开客户端的通道
            clientChannel = SocketChannel.open();
            //将通道改为非阻塞！！！
            clientChannel.configureBlocking(false);
            selector = Selector.open();
            //将客户端的连接请求事件注册到监听器上
            clientChannel.register(selector, SelectionKey.OP_CONNECT);
            //绑定想要连接的端口
            clientChannel.connect(new InetSocketAddress(ip,port));
            while (true){
                //开始监听
                selector.select();
                //被触发的事件集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key :
                        selectionKeys) {
                    handles(key);
                }
                selectionKeys.clear();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handles(SelectionKey key) throws IOException {
        //服务器响应与客户端的连接
        if(key.isConnectable()){
            SocketChannel clientChannel = (SocketChannel) key.channel();
            /**  isConnectionPending()
             * 判断此通道上是否正在进行连接操作
             * 返回：当且仅当已在此通道上发起连接操作，但是尚未通过调用 finishconnect 方法完成连接时才返回 true,否则就是现在还没准备好，需要等待
             */
            if(clientChannel.isConnectionPending()){
                clientChannel.finishConnect();
                //连接建立完成，用户可以去输入啦！
                new Thread(new ChatHandler(this)).start();
                clientChannel.register(selector,SelectionKey.OP_READ);
            }
        }
        //read事件，服务器转发消息，客户端需要读
        else if(key.isReadable()){
            SocketChannel clientChannel = (SocketChannel) key.channel();
            String msg = receive(clientChannel);
            if(msg.isBlank()){
                if(selector!= null){
                    selector.close();
                }
            }else{
                System.out.println(msg);
            }
        }

    }

    /**
     * 客户端接收数据
     * @param clientChannel
     * @return
     */
    private String receive(SocketChannel clientChannel) throws IOException {
        readBuffer.clear();
        while (clientChannel.read(readBuffer) > 0);
        readBuffer.flip();
        return String.valueOf(charset.decode(readBuffer));

    }

    /**
     * 客户端向服务器通道写入数据
     * @param message
     */
    public void send(String message) throws IOException {
        if(message.isBlank()){
            return;
        }
        //把用户输入的消息写入writebuffer
        writeBuffer.clear();
        writeBuffer.put(charset.encode(message));
        writeBuffer.flip();
        //由bufffer中读出来，写入对应的通道,然后再由通道发生给服务器通道
        while (writeBuffer.hasRemaining()){
            clientChannel.write(writeBuffer);
        }
        if(QUIT.equals(message)){
            if(selector!=null){
                selector.close();
            }
        }

    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient(8888,"127.0.0.1");
        chatClient.start();

    }
}
