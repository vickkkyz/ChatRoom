package BIO.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Author qq
 * @Date 2022/4/20
 */
public class ChatHandler implements Runnable{

    private ChatClient client;

    public ChatHandler(ChatClient client){
        this.client = client;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        try {
            //reader读取用户输入的消息
            reader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String message = reader.readLine();
                client.send(message);
                //System.out.println(message);
                if(client.quit(message)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
