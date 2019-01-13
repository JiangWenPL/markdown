import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
        final Timer timer = new Timer(1000, e1 -> {
            System.out.println("No update in 1000ms");
            //TODO: send message to peers.
        });
        timer.setRepeats(true);
        timer.start();
        JEditorPane previewPane = new JEditorPane("text/html", "");
        previewPane.setEditable(false);
        JEditorPane outlinePane = new JEditorPane("text/html", "");
        editPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    mdStrManager.setMdStr(e.getDocument().getText(0, e.getDocument().getLength()));
                    System.out.println(e.getDocument().getText(0, e.getDocument().getLength()));
                    previewPane.setText(mdStrManager.getHtmlStr());
                    outlinePane.setText(mdStrManager.getOutlineStr());
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                timer.setDelay(1000);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    mdStrManager.setMdStr(e.getDocument().getText(0, e.getDocument().getLength()));
                    System.out.println(e.getDocument().getText(0, e.getDocument().getLength()));
                    previewPane.setText(mdStrManager.getHtmlStr());
                    outlinePane.setText(mdStrManager.getOutlineStr());
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                timer.setDelay(1000);
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

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem menuOpen = new JMenuItem("Open");
        JMenuItem menuSave = new JMenuItem("Save");
        JMenuItem menuExport = new JMenuItem("Export");
        JMenuItem menuShare = new JMenuItem("Share");
        JMenuItem menuConnect = new JMenuItem("Connect");
        menuShare.addActionListener(e -> {
            System.out.println("Share performed");
        });
        menuConnect.addActionListener(e -> {
            System.out.println("Connect action performed");
        });
        menuOpen.addActionListener(e -> {
            JFileChooser openFileChooser = new JFileChooser();
            openFileChooser.setDialogTitle("Please choose your *.cad file");
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
                mdStrManager.setMdStr(newFileStr);
                editPane.setText(mdStrManager.getMdStr());
                previewPane.setText(mdStrManager.getHtmlStr());
                outlinePane.setText(mdStrManager.getOutlineStr());

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
                fileWriter.write(mdStrManager.getMdStr());
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
                fileWriter.write(mdStrManager.getHtmlStr());
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
        menu.add(menuShare);
        menu.add(menuConnect);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.setVisible(true);
    }
}

public class Main extends JFrame {
    public static void main(String[] args) {
        MarkdownEditor markdownEditor = new MarkdownEditor();
    }
}