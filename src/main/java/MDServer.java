import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;

class MDMessage implements Serializable {
    public String text;
    public long time;

    MDMessage(String content) {
        this.text = content;
        this.time = System.currentTimeMillis();
    }
}

public class MDServer extends Thread implements Runnable {

    private final MDStrManager mdStrManager;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    long lastTimeStamp;
    boolean isServer;

    public MDServer(Socket socket, MDStrManager mdStrManager, boolean isServer) {
        lastTimeStamp = -1;
        this.isServer = isServer;
        this.mdStrManager = mdStrManager;
        this.socket = socket;
        try {
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!this.socket.isClosed()) {
            try {
                MDMessage mdMessage = (MDMessage) this.objectInputStream.readObject();
                System.out.println("get message: " + mdMessage.text);
                if (mdMessage.time > this.lastTimeStamp) {
                    this.lastTimeStamp = mdMessage.time;
                    synchronized (this.mdStrManager) {
                        if (this.isServer) {
                            this.mdStrManager.mergeMdStr(mdMessage.text);
                        } else {
                            this.mdStrManager.setMdStr(mdMessage.text);
                            this.mdStrManager.needUpdateNow();
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
