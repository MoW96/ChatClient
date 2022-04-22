import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private JTextArea incomingMessages;
    private JTextArea outgoingMessages;
    private JFrame frame;
    private JButton detachButton;
    private BufferedReader reader;
    private BufferedReader readerFile;
    private PrintWriter writer;
    private PrintWriter writerFile;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Socket socket;
    private Socket socketFile;
    private ServerSocket socketReceiveFileString;
    private static final String IPADRESSSERVER = "127.0.0.1"; // 192.168.178.87
    private static final int PORTSERVER = 5000;
    private static final int PORTFILESERVER = 5001;
    public static int SERVERSENDDATAPORT = 5002;
    public static int SERVERSENDDATARECEIVEPORT = 5003;
    private String username;
    private Settings settings = new Settings();
    private boolean isFileToSend = false;
    private String filePathToSend;
    private long fileSizeToSend;
    private String fileNameToSend;
    private String ownFileString;
    private static final String IS_FILE_STRING = ";Data_Sending;";
    private static final String SEND_BACK_STRING = ";Data_Sending_Back;";
    private static final int FILE_SENDER_POS = 0;
    private static final int FILE_NAME_POS = 2;
    private static final int FILE_SIZE_POS = 3;
    private boolean isAbleToDetach = false;
    private String ip;
    private FileClient fileClient;

    public ChatClient() {
        this.frame = new JFrame("ChatClient");

        // Panel -> Display Messages
        JPanel hauptPanel = new JPanel();
        hauptPanel.setLayout(new BorderLayout());

        // Panel -> Send Messages
        JPanel sendMessagePanel = new JPanel();
        sendMessagePanel.setLayout(new BorderLayout());

        // Panel -> Attach Detach
        JPanel attachDetachPanel = new JPanel();
        attachDetachPanel.setLayout(new BorderLayout());

        // TextArea -> Display Messages
        incomingMessages = new JTextArea();
        incomingMessages.setLineWrap(true);
        incomingMessages.setWrapStyleWord(true);
        incomingMessages.setEditable(false);

        // Scroller -> TextArea
        JScrollPane scroller = new JScrollPane(incomingMessages);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Input -> Send Messages
        Border border = BorderFactory.createLineBorder(Color.darkGray);
        outgoingMessages = new JTextArea();
        outgoingMessages.setLineWrap(true);
        outgoingMessages.setWrapStyleWord(true);
        outgoingMessages.setEditable(true);
        outgoingMessages.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Button -> Send Messages
        JButton sendenButton = new JButton("Senden");
        sendenButton.addActionListener(new SendenButtonListener());

        // Button -> Settings
        ImageIcon settingIcon = new ImageIcon("resources\\setting.png");
        JButton settingsButton = new JButton(settingIcon);
        settingsButton.setBorderPainted(false);
        settingsButton.addActionListener(new SettingsButtonListener());
        settingsButton.setToolTipText("Einstellungen");

        // Button -> Attach File
        ImageIcon fileIcon = new ImageIcon("resources\\anfügen.png");
        JButton fileButton = new JButton(fileIcon);
        fileButton.setToolTipText("Datei versenden");
        fileButton.addActionListener(new FileButtonListener());

        // Button -> Detach
        ImageIcon deatchIcon = new ImageIcon("resources\\schließen.png");
        detachButton = new JButton(deatchIcon);
        detachButton.setToolTipText("Datei entfernen");
        detachButton.addActionListener(new DetachButtonListener());
        detachButton.setVisible(false);

        // Style -> Panel Display Messages
        hauptPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        hauptPanel.add(scroller, BorderLayout.CENTER);
        hauptPanel.setBackground(Color.white);

        // Style -> Attach Detach Panel
        attachDetachPanel.add(fileButton, BorderLayout.BEFORE_LINE_BEGINS);
        attachDetachPanel.add(detachButton, BorderLayout.AFTER_LINE_ENDS);

        // Style -> Send Messages
        sendMessagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sendMessagePanel.add(attachDetachPanel, BorderLayout.BEFORE_LINE_BEGINS);
        sendMessagePanel.add(outgoingMessages, BorderLayout.CENTER);
        sendMessagePanel.add(sendenButton, BorderLayout.EAST);
        sendMessagePanel.setBackground(Color.white);

        // Find Username
        detectUsername();
        buildDetectionForFile();

        setUpNetwork();

        Thread readThread = new Thread(new IncomingReader());
        readThread.start();

        // Style -> Window
        frame.getContentPane().add(BorderLayout.BEFORE_FIRST_LINE, settingsButton);
        frame.getContentPane().add(BorderLayout.CENTER, hauptPanel);
        frame.getContentPane().add(BorderLayout.AFTER_LAST_LINE, sendMessagePanel);
        frame.setSize(700, 600);
        frame.setMinimumSize(new Dimension(350, 300));
        frame.getContentPane().setBackground(Color.white);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowClosingListener());
    }

    private void setUpNetwork() {
        try {
            fileClient = new FileClient();
            socket = new Socket(IPADRESSSERVER, PORTSERVER);
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(socket.getOutputStream());

            socketFile = new Socket(IPADRESSSERVER, SERVERSENDDATAPORT);
            writerFile = new PrintWriter(socketFile.getOutputStream());

            socketReceiveFileString = new ServerSocket(SERVERSENDDATARECEIVEPORT);

            Thread threadFileListener = new Thread(this::setUpFileListener);
            threadFileListener.start();

            System.out.println("connected...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
            ip = NetworkUtil.getCurrentEnvironmentNetworkIp();
        try {
            String message = username + ";" + ip + "; online :)";
//            try {
//                message = ReadWriteDES.encode(message.getBytes(StandardCharsets.ISO_8859_1));
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
            writer.println(message);
            writer.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setUpFileListener() {
        while(true) {
            try {
                Socket receiveFileStringSocket = socketReceiveFileString.accept();
                InputStreamReader streamReaderFile = new InputStreamReader(receiveFileStringSocket.getInputStream());
                readerFile = new BufferedReader(streamReaderFile);

                Thread readFileThread = new Thread(new IncomingFileReader());
                readFileThread.start();
            } catch (IOException ioEX) {
                ioEX.printStackTrace();
            }
        }
    }

    private void detectUsername() {
        if (settings.getUsername() == null || settings.getUsername().equals("")) {
            SettingsUI settingsDialog = new SettingsUI(settings, new JFrame());
            settingsDialog.setModal(true);
        }
        username = settings.getUsername();
    }

    private void buildDetectionForFile() {
        ownFileString = username + SEND_BACK_STRING;
    }

    private void sendOfflineToServer() {
        writer.println(username + ";" + ip + ";Offline_User_Logout");
        writer.flush();
    }

    public class SendenButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!outgoingMessages.getText().isBlank()) {
                if (isFileToSend) {
                    String messageFile = username + ";" + ip + IS_FILE_STRING + fileNameToSend + ";" + fileSizeToSend;
                    // TODO Encrypt nachricht
//                    try {
//                        messageFile = ReadWriteDES.encode(messageFile.getBytes(StandardCharsets.ISO_8859_1));
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
                    writerFile.println(messageFile);
                    writerFile.flush();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException intEx) {
                        intEx.printStackTrace();
                    }
                    try {
                        fileClient.sendFile(filePathToSend);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    detachButton.setVisible(false);
                    isAbleToDetach = false;
                    isFileToSend = false;
                    filePathToSend = "";
                    outgoingMessages.setEditable(true);
                    outgoingMessages.setText("");
                    detachButton.setVisible(false);
                    System.out.println("File sent.");
                } else {
                    try {
                        String message = username + ";" + ip + ";: " + outgoingMessages.getText();
                        // TODO Encrypt nachricht
//                        try {
//                            message = ReadWriteDES.encode(message.getBytes(StandardCharsets.ISO_8859_1));
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
                        writer.println(message);
                        writer.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    outgoingMessages.setText("");
                    outgoingMessages.requestFocus();
                }
            } else {
                outgoingMessages.setText("");
            }
        }
    }

    public class FileButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            chooser.setVisible(true);
            final int result = chooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File choosedFile = chooser.getSelectedFile();
                fileSizeToSend = choosedFile.length();
                filePathToSend = choosedFile.getPath();
                fileNameToSend = choosedFile.getName();
                System.out.println("Datei-Pfad: " + filePathToSend);
                isFileToSend = true;
                isAbleToDetach = true;
                outgoingMessages.setEditable(false);
                outgoingMessages.setText(filePathToSend);
                detachButton.setVisible(true);
            } else {
                isFileToSend = false;
                isAbleToDetach = false;
                outgoingMessages.setEditable(true);
                detachButton.setVisible(false);
            }
        }
    }

    public class DetachButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isAbleToDetach) {
                detachButton.setVisible(false);
                isAbleToDetach = false;
                isFileToSend = false;
                filePathToSend = "";
                outgoingMessages.setEditable(true);
                outgoingMessages.setText("");
                detachButton.setVisible(false);
                System.out.println("File detached.");
            }
        }
    }

    public class SettingsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SettingsUI settingsDialog = new SettingsUI(settings, new JFrame());
            settingsDialog.setModal(true);

            username = settings.getUsername();
        }
    }

    public class IncomingReader implements Runnable {
        public void run() {
            String nachricht;
            try {
                while ((nachricht = reader.readLine()) != null) {
                    // TODO decrypt nachricht
//                    nachricht = ReadWriteDES.decode(nachricht);
                    System.out.println("Received: " + nachricht);
                    incomingMessages.append(nachricht + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class IncomingFileReader implements Runnable {
        public void run() {
            String nachricht;
            try {
                while ((nachricht = readerFile.readLine()) != null) {
                    // TODO decrypt nachricht
//                    nachricht = ReadWriteDES.decode(nachricht);
                    if (!nachricht.contains(ownFileString)) {
                        fileClient.receiveFile(getFileSize(nachricht), getFileName(nachricht));
                        incomingMessages.append("Datei: " + getFileName(nachricht) + " von " + getFileSender(nachricht) + " empfangen." + "\n");
                    } else {
                        incomingMessages.append("Datei: " + getFileName(nachricht) + " gesendet." + "\n");
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private long getFileSize(String nachricht) {
            String[] splittedStrings = nachricht.split(";");
            return Long.parseLong(splittedStrings[FILE_SIZE_POS]);
        }

        private String getFileName(String nachricht) {
            String[] splittedStrings = nachricht.split(";");
            return splittedStrings[FILE_NAME_POS];
        }

        private String getFileSender(String nachricht) {
            String[] splittedStrings = nachricht.split(";");
            return splittedStrings[FILE_SENDER_POS];
        }
    }

    public class WindowClosingListener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {
            sendOfflineToServer();
        }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowIconified(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }
    }
}

