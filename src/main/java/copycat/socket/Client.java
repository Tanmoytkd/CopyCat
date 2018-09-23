package copycat.socket;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static Boolean running = true;
    static Socket s;
    static String clipboard = "";
    static ObjectInputStream input;
    static ObjectOutputStream output;
    static Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void start() {
        try {
            String ip;
            System.out.println("Please enter host ip:");
            Scanner sc = new Scanner(System.in);
            ip = sc.next();
            s = new Socket(ip, 3333);
            System.out.println("Connected to:"+s.getInetAddress().getHostName());

            input = new ObjectInputStream(s.getInputStream());
            output = new ObjectOutputStream(s.getOutputStream());

            Thread sender = new Thread(() -> {
                while (running) {
                    try {
                        String newClipboard = clipboard;
                        if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                            newClipboard = (String) systemClipboard.getData(DataFlavor.stringFlavor);
                            if (!newClipboard.equals(clipboard)) {
                                output.writeObject(newClipboard);
                                output.flush();
                                clipboard = newClipboard;
                            }
                        }
                        Thread.sleep(80);
                    } catch (Exception e) {
                        if (e.toString().contains("java.io.EOFException")) {
                            break;
                        }
                        System.out.println(e);
                    }
                }
            });
            sender.start();

            Thread receiver = new Thread(() -> {
                while (running) {
                    try {
                        String newClipboard = (String) input.readObject();
                        System.out.println("New Data: ");
                        System.out.println(newClipboard);
                        if (!clipboard.equals(newClipboard) && !newClipboard.equals("")) {
                            systemClipboard.setContents(new StringSelection(newClipboard), null);
                            clipboard = newClipboard;
                        }
                        Thread.sleep(90);
                    } catch (Exception e) {
                        if (e.toString().contains("java.io.EOFException")) {
                            break;
                        }
                        if (e.toString().contains("java.net.SocketException: Connection reset")) {
                            break;
                        }
                        System.out.println(e);
                    }
                }
            });
            receiver.start();

            sender.join();
            receiver.join();
            if(output!=null) output.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        start();
    }
}
