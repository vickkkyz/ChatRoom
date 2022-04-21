package IO;

import java.io.*;
import java.net.Socket;

/**
 * @Author qq
 * @Date 2022/4/19
 */
public class Client {

    //服务器的端口
    private Integer PORT = 8888;
    //服务器的主机
    private String IP = "127.0.0.1";
    public void client(){
        Socket socket = null;
        BufferedReader reader =null;
        BufferedWriter writer = null;
        BufferedReader reader1 =null;

        try {
            //创建一个客户端socket,port是要连接的服务器的端口号
            socket = new Socket(IP,PORT);

            //创建发送和接收缓冲区
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //数据从用户在控制台输入,创建一个缓冲区来存放用户输入的数据
            reader1 = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("开始读取控制台的数据:");
            while (true){
                String massage = reader.readLine();
                System.out.println("写入到服务器缓冲区中...");
                //这里注意一定要加上换行符\n，否则服务器端无法正确读到一行数据
                writer.write(massage+"\n");
                writer.flush();
                if("quit".equals(massage)){
                    System.out.println("客户端停止发送..");
                    break;
                }
            }
            //reader.readLine()这个方法有坑，当读取不到数据，即缓冲区内没有数据时，会阻塞
            // 循环的条件不要写成 while((massage2 = reader.readLine())!= null)
            // 所以我们在读取最后一个数据后要break手动跳出while循环
            //最后一行数据的标志是我们规定的"quit"
            while (true){
                String massage2 = reader.readLine();
                System.out.println("读取服务器发送的数据：["+massage2+"]");
                if("quit".equals(massage2)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader1 != null){
                try {
                    reader1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        Client client = new Client();
        client.client();
    }
}
