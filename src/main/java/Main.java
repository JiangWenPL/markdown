import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ServerAccepter extends Thread implements Runnable {
    ServerSocket serverSocket;
    final int maxThreads = 100;
    MDStrManager mdStrManager;

    ServerAccepter(MDStrManager mdStrManager) {
        this.mdStrManager = mdStrManager;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);
        ArrayList<Socket> clientSocketList = new ArrayList<>();
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                MDServer mdServer = new MDServer(clientSocket, mdStrManager, true);
                MDClient mdClient = new MDClient(clientSocket, mdStrManager);
                clientSocketList.add(clientSocket);
                threadPool.submit(mdServer);
                threadPool.submit(mdClient);
            }
        } catch (IOException e) {
            for (Socket clientSocket : clientSocketList) {
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                }
            }
            threadPool.shutdownNow();
            System.out.println("Server accepter closed");
        }
    }
}

class StateController {
    public boolean isSharing;
    public boolean isConnecting;

    StateController() {
        isSharing = false;
        isConnecting = false;
    }
}

class MarkdownEditor extends JFrame {
    String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }


    public MarkdownEditor() {
        super("Markdown editor");
        JEditorPane editPane = new JEditorPane("text/plain", "");
        MDStrManager mdStrManager = new MDStrManager();
        ServerAccepter serverAccepter = new ServerAccepter(mdStrManager);
        JEditorPane previewPane = new JEditorPane("text/html", "");
        previewPane.setEditable(false);
        JEditorPane outlinePane = new JEditorPane("text/html", "");
        final Timer timer = new Timer(1500, e1 -> {
//            System.out.println("No update in 1000ms");
            System.out.println("Weak up");
            synchronized (mdStrManager) {
                System.out.println("Check is dirty:" + mdStrManager.isDirty());
                if (mdStrManager.isDirty()) {
                    System.out.println("Updating content");
                    mdStrManager.notifyAll();
                    mdStrManager.lastModifiedTime = System.currentTimeMillis();
                }
                mdStrManager.done();
                previewPane.setText(mdStrManager.getHtmlStr());
                outlinePane.setText(mdStrManager.getOutlineStr());
                editPane.setText(mdStrManager.getMdStr());
            }
        });
        timer.setRepeats(true);
        timer.start();
        editPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    synchronized (mdStrManager) {
                        mdStrManager.setMdStr(e.getDocument().getText(0, e.getDocument().getLength()));
//                    System.out.println(e.getDocument().getText(0, e.getDocument().getLength()));
                        previewPane.setText(mdStrManager.getHtmlStr());
                        outlinePane.setText(mdStrManager.getOutlineStr());
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                timer.setDelay(1500);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    synchronized (mdStrManager) {
                        mdStrManager.setMdStr(e.getDocument().getText(0, e.getDocument().getLength()));
//                    System.out.println(e.getDocument().getText(0, e.getDocument().getLength()));
                        previewPane.setText(mdStrManager.getHtmlStr());
                        outlinePane.setText(mdStrManager.getOutlineStr());
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                timer.setDelay(1500);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                System.out.println("update:\n" + e);
            }
        });
        editPane.setEditable(true);
        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editPane, previewPane);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, outlinePane, mainPane);
        this.setContentPane(new JScrollPane(splitPane));
        this.setPreferredSize(new Dimension(1024, 640));
        this.pack();
        splitPane.setDividerLocation(.15f);
        mainPane.setDividerLocation(.5f);

        final StateController stateController = new StateController();

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem menuOpen = new JMenuItem("Open");
        JMenuItem menuSave = new JMenuItem("Save");
        JMenuItem menuExport = new JMenuItem("Export");
        JMenu menuRemote = new JMenu("Remote");
        JMenuItem menuShare = new JMenuItem("Share");
        JMenuItem menuConnect = new JMenuItem("Connect");
        JMenuItem menuStopShare = new JMenuItem("Stop Share");
        JMenuItem menuStopConnect = new JMenuItem("Stop Connect");

        menuShare.addActionListener(e -> {
            System.out.println("Share performed");
            String text = JOptionPane.showInputDialog("please input your port number:");
            try {
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(text));
                serverAccepter.setServerSocket(serverSocket);
                serverAccepter.start();
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "Open socket fail, maybe change a port number", "Alert", JOptionPane.ERROR_MESSAGE);
            }
        });

        menuConnect.addActionListener(e -> {
            System.out.println("Connect action performed");
            String ip = JOptionPane.showInputDialog("please input server ip:");
            String port = JOptionPane.showInputDialog("Please input server port");
            try {
                Socket socket = new Socket(ip, Integer.parseInt(port));
                MDClient mdClient = new MDClient(socket, mdStrManager);
                mdClient.start();
                MDServer mdServer = new MDServer(socket, mdStrManager, false);
                mdServer.start();
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "Open socket fail, please check your port number", "Alert", JOptionPane.ERROR_MESSAGE);
            }
        });
        menuOpen.addActionListener(e -> {
            JFileChooser openFileChooser = new JFileChooser();
            openFileChooser.setDialogTitle("Please choose your *.md file");
            openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.md", "md");
            openFileChooser.setFileFilter(filter);

            int option = openFileChooser.showOpenDialog(null);
            String path = null;
            if (JFileChooser.APPROVE_OPTION == option) {
                path = openFileChooser.getSelectedFile().getAbsolutePath();
            }
            if (path == null) {
                return;
            }
            try {
                String newFileStr = readFile(path);
                synchronized (mdStrManager) {
                    mdStrManager.setMdStr(newFileStr);
                    editPane.setText(mdStrManager.getMdStr());
                    previewPane.setText(mdStrManager.getHtmlStr());
                    outlinePane.setText(mdStrManager.getOutlineStr());
                }

            } catch (Exception exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null, "Open file failed", "Alert", JOptionPane.ERROR_MESSAGE);
            }
        });
        menuSave.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog("please input a file name:");
            if (fileName == null || fileName.equals("")) {
                return;
            }
            if (!fileName.endsWith(".md")) {
                fileName += ".md";
            }

            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setDialogTitle("select a save directory");
            saveFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int option = saveFileChooser.showOpenDialog(null);
            String path = null;
            if (JFileChooser.APPROVE_OPTION == option) {
                path = saveFileChooser.getSelectedFile().getAbsolutePath();
            }
            if (path == null) {
                return;
            }
            try {
                FileWriter fileWriter = new FileWriter(path + "/" + fileName);
                synchronized (mdStrManager) {
                    fileWriter.write(mdStrManager.getMdStr());
                }
                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null, "Save failed!", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(null, "Save success!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        menuExport.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog("please input a file name:");
            if (fileName == null || fileName.equals("")) {
                return;
            }
            if (!fileName.endsWith(".html")) {
                fileName += ".html";
            }

            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setDialogTitle("select a save directory");
            saveFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int option = saveFileChooser.showOpenDialog(null);
            String path = null;
            if (JFileChooser.APPROVE_OPTION == option) {
                path = saveFileChooser.getSelectedFile().getAbsolutePath();
            }
            if (path == null) {
                return;
            }
            try {
                FileWriter fileWriter = new FileWriter(path + "/" + fileName);
                synchronized (mdStrManager) {
                    fileWriter.write(mdStrManager.getHtmlStr());
                }
                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null, "Save failed!", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(null, "Save success!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        menu.add(menuOpen);
        menu.add(menuSave);
        menu.add(menuExport);
        menuRemote.add(menuShare);
        menuRemote.add(menuConnect);
        menuBar.add(menu);
        menuBar.add(menuRemote);
        setJMenuBar(menuBar);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        System.out.println("Socket closed");
        this.setVisible(true);
    }
}

public class Main extends JFrame {
    public static void main(String[] args) {
        MarkdownEditor markdownEditor = new MarkdownEditor();
    }
}