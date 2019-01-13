import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MDClient implements Runnable {
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
        while (!this.socket.isClosed()) {
            try {
                MDMessage message;
                synchronized (this.mdStrManager) {
                    this.mdStrManager.wait();
                    message = new MDMessage(this.mdStrManager.getMdStr());
                }
                this.objectOutputStream.writeObject(message);

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
