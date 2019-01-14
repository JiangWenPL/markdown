import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MDClient extends Thread implements Runnable {
    private Socket socket;
    private final MDStrManager mdStrManager;
    private ObjectOutputStream objectOutputStream;

    public MDClient(Socket socket, MDStrManager mdStrManager) {
        this.mdStrManager = mdStrManager;
        this.socket = socket;
        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        boolean isFirst = true;
        while (!this.socket.isClosed()) {
            try {
                MDMessage message;
                synchronized (this.mdStrManager) {
                    if (!isFirst)
                        this.mdStrManager.wait();
                    isFirst = false;
                    message = new MDMessage(this.mdStrManager.getMdStr());
                }
                this.objectOutputStream.writeObject(message);
                System.out.println("Send: " + message.text);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
