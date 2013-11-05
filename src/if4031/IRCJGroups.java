/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package if4031;

import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 *
 * @author User
 */
public class IRCJGroups extends ReceiverAdapter {

    private Map<String,Channel> mapchannel;
    private String username; //System.getProperty("user.name", "n/a");

    public IRCJGroups(String ipaddr) {
        System.setProperty("jgroups.bind_addr", ipaddr);//"192.168.43.56"
    }

    public void start() throws Exception {
        //mychannel = new JChannel();
        //mychannel.setReceiver(this);
        mapchannel = new TreeMap<>();
        username = generateRandomString(5);
        System.out.println("Welcome " + username);
        eventLoop();
        //mychannel.close();
    }

    public String generateRandomString(int sequences) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < sequences; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public void eventLoop() {
        
        while (true) {
            System.out.print("> ");
            Scanner usrInput = new Scanner(System.in);
            String message = usrInput.nextLine();
            String[] arrInput = message.split(" ");


            try {
                if (arrInput[0].equalsIgnoreCase("/exit")) { //exit the application
                    if (mapchannel.size() > 0) {
                        Set<String> keys = mapchannel.keySet();
                        for (String name : keys) {
                            mapchannel.get(name).close();
                        }
                    } 
                    break;
                } else if (arrInput[0].equalsIgnoreCase("/join")) { //join a room
                    if (arrInput[1] != null) {
                        Channel temp = new JChannel("config2.xml");
                        //ProtocolStack stack = temp.getProtocolStack();
                        //System.out.println(stack.printProtocolSpecAsXML());
                        mapchannel.put(arrInput[1],temp);
                        mapchannel.get(arrInput[1]).connect(arrInput[1]);
                        mapchannel.get(arrInput[1]).setReceiver(this);
                        System.out.println("Connected to " + arrInput[1]);
                    } else {
                        System.out.println("You must specify a channel name");
                    }
                } else if (arrInput[0].equalsIgnoreCase("/nick")) { //specify a nickname
                    if (arrInput[1] != null) {
                        username = arrInput[1];
                        System.out.println("Your name : " + arrInput[1]);
                    } else {
                        System.out.println("You must specify a new name");
                    }
                } else if (arrInput[0].equalsIgnoreCase("/leave")) { //specify a nickname
                    if (arrInput[1] != null) {
                        if(mapchannel.containsKey(arrInput[1])){
                            mapchannel.get(arrInput[1]).disconnect();
                            mapchannel.get(arrInput[1]).close();
                            mapchannel.remove(arrInput[1]);
                            System.out.println("Disconnected from " + arrInput[1]);
                        }else{
                            System.out.println("Channel not found !");
                        }
                    } else {
                        System.out.println("You must specify channel name");
                    }
                } else if (arrInput[0].startsWith("@")) { //specify message to a channel
                    message = "";
                    if (arrInput[1].length() > 1) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < arrInput.length; i++) {
                            sb.append(arrInput[i] + " ");
                        }
                        message = sb.toString();
                    }

                    if(mapchannel.containsKey(arrInput[0].substring(1, arrInput[0].length()))){
                        message = "[" + arrInput[0].substring(1, arrInput[0].length()) + "]" + "(" + username + ") " + message;
                        Message msg = new Message(null, null, message);
                        mapchannel.get(arrInput[0].substring(1, arrInput[0].length())).send(msg);
                    }else{
                        System.out.println("You must connect to that channel to send !");
                    }
                        
                } else { //start chat!
                    if (mapchannel.size() > 0) {
                        Set<String> keys = mapchannel.keySet();
                        for (String name : keys) {
                            String messagetosend = "[" + name + "]" + "(" + username + ") " + message;
                            Message msg = new Message(null, null, messagetosend);
                            mapchannel.get(name).send(msg);
                        }
                    } 

                }
            } catch (Exception e) {
            }

        }
    }

    /* inherited */
    @Override
    public void receive(Message msg) {
        super.receive(msg);
        System.out.println(msg.getObject().toString());
        System.out.print("> ");
    }

    @Override
    public void viewAccepted(View view) {
        super.viewAccepted(view);
        System.out.println("** view: " + view);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new IRCJGroups(args[0]).start();
        } catch (Exception ex) {
            Logger.getLogger(IRCJGroups.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
