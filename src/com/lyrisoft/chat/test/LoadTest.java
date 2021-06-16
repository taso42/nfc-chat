package com.lyrisoft.chat.test;

import java.util.Properties;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.DumbClient;
import com.lyrisoft.chat.server.local.CommandProcessorLocal;

public class LoadTest extends Thread {
    private static CommandProcessorLocal commandProcessor = null;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("usage: LoadTest [host] [port] [# of rooms] [users per room] ([first room])");
            System.exit(1);
        }

        try {
            int pause = 12000;
            String key = String.valueOf(System.currentTimeMillis());
            int nRooms = Integer.parseInt(args[2]);
            int usersPerRoom = Integer.parseInt(args[3]);
            int userNum = 0;
            int roomAdd = 0;
            try {
                roomAdd = Integer.parseInt(args[4]);
            }
            catch (Exception e) {
            }

            Thread[] testers = new Thread[usersPerRoom * nRooms];
        
            int roomNum = 0;
            for (int i=0; i < testers.length; i++) {
                System.err.println("initing user " + userNum);
                LoadTestClient client = new LoadTestClient("tester" + key + userNum, 
                                                           "loadtestroom-" + (roomNum+roomAdd), 
                                                           args[0], Integer.parseInt(args[1]), pause);
                
                if (i == 0) {
                    initCommandsAndMessages(client);
                }
                client.setAttribute("commandProcessor", commandProcessor);
                testers[userNum] = new Thread(client);
                roomNum++;
                if (roomNum >= nRooms) {
                    roomNum = 0;
                }
                userNum++;
            }

            for (int i=0; i < testers.length; i++) {
                testers[i].start();
                try { Thread.sleep(250); } catch (InterruptedException e) {}

                if (i > 0 && i % 25 == 0) {
                    System.err.println("pausing for breathing room...");
                    try { Thread.sleep(20000); } catch (InterruptedException e) {}
                }
            }
            System.err.println("Everybody's running!!!!!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void initCommandsAndMessages(DumbClient client) throws Exception {
        Properties p = client.getProperties("commandProcessors.properties");
        if (p != null) {
            commandProcessor = new CommandProcessorLocal(p);
        } else {
            throw new Exception("Could not load commandProcessors.properties");
        }
        p = client.getProperties("messages.properties");
        if (p != null) {
            Translator.init(p);
        } else {
            throw new Exception("Could not load messages.properties");
        }
    }
}
