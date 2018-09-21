package copycat;

import copycat.socket.Client;
import copycat.socket.Server;

import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tanmoy Krishna Das
 */
public class App {
    public static void main(String[] args) throws IOException {
        if(args.length==0) {
            Server.start();
        }

        if("host".equals(args[0])) {
            Server.start();
        }

        if("client".equals(args[0])) {
            Client.start();
        }
    }
}
