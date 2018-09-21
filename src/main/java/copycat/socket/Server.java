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

    private static Boolean running = true;
    private static ArrayList<ObjectOutputStream> outputList = new ArrayList<>();
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
                        sendData(newClipboard);
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
            this.socket = s;
            input = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
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
                output.writeObject(newClipboard);
                output.flush();
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
                }
            } catch (IllegalStateException e) {
                System.out.println("Clipboard unavailabe");
            }


            ServerSocket ss = new ServerSocket(3333);

            sender.start();

            System.out.println("Listening on 3333");
            while(running) {
                Socket s = ss.accept();
                System.out.println("Connected to: "+s.getInetAddress().getHostName());
                new ClientHandler(s).start();
                outputList.add(new ObjectOutputStream(s.getOutputStream()));
            }

            sender.join();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void main(String[] args) {
        start();
    }
}