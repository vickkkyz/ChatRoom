package NIO.server;

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
public class ChatServer {
    private static final int PORT = 8888;
    //用户退出聊天室的时候输入这个
    private final String QUIT = "quit";
    private final int BUFFER_CAPACITY = 1024;
    //一个服务器对应一个通道
    //ServerSocketChannel是一个基于通道的socket监听器
    private ServerSocketChannel serverSocketChannel;
    //这里的read实际上是对通道来说的，这个类对应的通道想读某个通道，需要将读出来的数据写入它的readBuffer
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    //write也是对通道来说的，这个类对应的通道想往某个通道中写数据，需要先将数据写入writeBuffer暂存，然后将writeBuffer中的数据写入到通道中
    private ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    // writeBuffer------>Channel------->readBuffer

    private Selector selector;
    //用户可以自定义服务器想要监听的端口
    private int port;
    //定义交换的文本格式
    private Charset charset = Charset.forName("UTF-8");

    public ChatServer(){
        this(PORT);
    }

    public ChatServer(int port){
        this.port = port;
    }

    public void start(){

        try {
            //打开这个通道，绑定到监听端口
            serverSocketChannel = ServerSocketChannel.open();
            //serverSocketChannel可以支持阻塞式以及非阻塞式调用，默认是阻塞式
            //设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //把这个通道关联的serverSocket绑定到监听端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            //创建selector
            selector = Selector.open();
            //首先将ACCEPT事件注册到selector上，使selector可以监听这个实践
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，开始监听"+port+"端口..");
            while(true){
                //selector开始监听是否有事件发生,阻塞式
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key :
                        selectionKeys) {
                    handles(key);
                }
                //清空本次的key集合
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(selector != null){
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void handles(SelectionKey key) throws IOException {
        //对于服务器来说
        //accpet客户端连接事件
        if(key.isAcceptable()){
            //肯定是服务器触发的事件，因为是服务器的accept事件
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            //要将客户端的read时间注册到监听器上
            clientChannel.configureBlocking(false);
            clientChannel.register(selector,SelectionKey.OP_READ);
            System.out.println("客户端["+clientChannel.socket().getPort()+"]已经连接到服务器...");
        }
        //读时间，即客户端向服务器发送数据-->写入到服务器通道的readBuffer
        else if(key.isReadable()){
            //服务器要把数据从客户端通道对应的缓冲区中读处来，然后转发给其它的客户端,即写入他们的缓冲区
            SocketChannel clientChannel = (SocketChannel) key.channel();
            String forwardMessage = receive(clientChannel);
            if(forwardMessage.isBlank()){
                //客户端出现了异常，不再监听
                //将这个通道注册到selector上的事件取消，不再监听
                key.cancel();
                //通知监听器发生的变化
                selector.wakeup();
            }else{
                System.out.println(forwardMessage);
                //服务器转发数据
                forwardMessage(clientChannel,forwardMessage);
                if(quit(forwardMessage)){
                    //客户端用户要退出了
                    //取消监听
                    key.cancel();
                    //通知监听器
                    selector.wakeup();
                    System.out.println("客户端["+clientChannel.socket().getPort()+"]断开连接...");
                }
            }

        }
    }

    private boolean quit(String message){
        return QUIT.equals(message);
    }
    private void forwardMessage(SocketChannel clientChannel, String forwardMessage) throws IOException {
        //找到目前在线的客户端，将数据发送给他们
        //目前注册在selectors上的通道
        for (SelectionKey key :
                selector.keys()) {
            if (key.channel() instanceof ServerSocketChannel){
                continue;
            }
            SocketChannel channel = (SocketChannel) key.channel();
            if(key.isValid() && !clientChannel.equals(channel)){
                writeBuffer.clear();
                //服务器将数据写入它自己的wirteBuffer，以便后面向通道中写入
                writeBuffer.put(charset.encode("客户端["+clientChannel.socket().getPort()+"]:"+forwardMessage));
                //切换为读
                writeBuffer.flip();
                while (writeBuffer.hasRemaining()){
                    channel.write(writeBuffer);
                }
            }
        }

    }

    /**
     * 服务器从readBuffer中读数据
     * @param clientChannel
     * @return
     */
    private String receive(SocketChannel clientChannel) throws IOException {
        readBuffer.clear();
        //只要客户端通道中有数据，就一直读，写入readBuffer
        while (clientChannel.read(readBuffer) > 0);
        //将buffer切换为读模式
        readBuffer.flip();
        return String.valueOf(charset.decode(readBuffer));

    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(8888);
        chatServer.start();
    }


}
