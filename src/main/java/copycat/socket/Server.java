package copycat.socket;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    public static Boolean running = true;
    private static ArrayList<ObjectOutputStream> outputList = new ArrayList<>();
    private static ArrayList<Socket> sockets = new ArrayList<>();
    private static String clipboard = "";
    private static Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private static Thread sender = new Thread() {
        int x;

        public void run() {

            while (running) {
                try {
                    String newClipboard = clipboard;
                    if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                        newClipboard = (String) systemClipboard.getData(DataFlavor.stringFlavor);
                    }

                    if (newClipboard!=null && !newClipboard.equals(clipboard) && !newClipboard.equals("")) {
                        System.out.println("New Data:");
                        System.out.println(newClipboard);

                        sendData(newClipboard);
                        System.out.println("SendData Returned");
                        clipboard = newClipboard;
                    }
                    sleep(80);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    };

    static class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream input;

        public ClientHandler(Socket s) throws IOException {
            System.out.println("Input handler for "+ s.getInetAddress().getHostName()+" will be created");
            this.socket = s;
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Input handler for "+ s.getInetAddress().getHostName()+" has been created");
        }

        @Override
        public void run() {
            System.out.println("Input handler for "+ socket.getInetAddress().getHostName()+" has started");
            while (running) {
                String newClipboard = "";
                try {
                    newClipboard = (String) input.readObject();
                    System.out.println("New Data: ");
                    System.out.println(newClipboard);
                } catch (IOException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception e) {
                    System.out.println("e");
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }


                try {
                    if (newClipboard!=null && !clipboard.equals(newClipboard) && !newClipboard.equals("")) {
                        systemClipboard.setContents(new StringSelection(newClipboard), null);
                        sendData(newClipboard);
                        clipboard = newClipboard;
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    private static void sendData(String newClipboard) {
        for(ObjectOutputStream output: outputList) {
            try {
                System.out.println("Trying to send data");
                output.writeObject(newClipboard);
                output.flush();
                System.out.println("Data Sent");
            } catch (IOException e) {
                System.out.println("Data Sending Failure");
            }
        }
    }

    public static void start() {
        try {
            try {
                Transferable clipboardContent = systemClipboard.getContents(null);
                if (clipboardContent != null && clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    clipboard = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
                    System.out.println(clipboard);
                }
            } catch (IllegalStateException e) {
                System.out.println("Clipboard unavailabe");
            }

            System.out.println(clipboard);

            ServerSocket ss = new ServerSocket(3333);

            sender.start();

            System.out.println("Listening on 3333");
            while(running) {
                Socket s = ss.accept();
                System.out.println("Connected to: "+s.getInetAddress().getHostName());
                outputList.add(new ObjectOutputStream(s.getOutputStream()));
                sockets.add(s);
                new ClientHandler(s).start();

            }

            sender.join();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        start();
    }
}