import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class FileClient {

    public static final int PORTSERVER_DATA = 5001;
    private static final String IPADRESSSERVER = "127.0.0.1"; // 192.168.178.87
    private Socket filesocket;
    private String pathDocuments;
    private BufferedReader reader;
    private Socket socket;

    public FileClient() {
        pathDocuments = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
    }

    public void sendFile(String file) throws IOException {
        try {
            filesocket = new Socket(IPADRESSSERVER, PORTSERVER_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int count;
        DataOutputStream dos = new DataOutputStream(filesocket.getOutputStream());
        File sendFile = new File(file);
        FileInputStream fis = new FileInputStream(sendFile);
        byte[] buffer = new byte[8192];

        while ((count = fis.read(buffer)) > 0) { // funktioniert beim 2ten mal nicht mehr
            dos.write(buffer, 0, count);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException intEx) {
            intEx.printStackTrace();
        }

        fis.close();
        dos.flush();
        filesocket.close();
    }

    public void receiveFile(long fileSize, String fileName) {
        try {
            filesocket = new Socket(IPADRESSSERVER, PORTSERVER_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DataInputStream dataInputStream = new DataInputStream(filesocket.getInputStream());
            FileOutputStream fos = new FileOutputStream(pathDocuments + "\\" + fileName);
            byte[] buffer = new byte[8192];

            int read = 0;
            int totalRead = 0;
            int remaining = (int) fileSize;
            while ((read = dataInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                System.out.print("\033[2K");
                System.out.print("read " + totalRead + " bytes.");
                fos.write(buffer, 0, read);
            }

            fos.close();
            dataInputStream.close();
            filesocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}