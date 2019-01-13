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

public class MDServer implements Runnable {

    private final MDStrManager mdStrManager;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    long lastTimeStamp;

    public MDServer(Socket socket, MDStrManager mdStrManager) {
        lastTimeStamp = -1;
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
                if (mdMessage.time > this.lastTimeStamp) {
                    this.lastTimeStamp = mdMessage.time;
                    synchronized (this.mdStrManager) {
                        this.mdStrManager.mergeMdStr(mdMessage.text);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
