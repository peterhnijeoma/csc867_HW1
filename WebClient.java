// ################################################################
// Description: Basic web client - demonstrate the HTTP protocol. webserver runs at
//                       port 8100
//
// Author: Peter Ijeoma and [team mate]
//
// usage: java WebClient
// #################################################################

import java.net.*;
import java.io.*;
import java.time.*;

public class WebClient
{
    public static void main(String[] args) throws IOException
    {
        for( int i = 0; i < 10; i++ )
        {
            //Socket svr_socket = new Socket("localhost", WebServer.svr_port);
            Socket svr_socket = new Socket("localhost", 8396);
            sendMessage(svr_socket, i);
            readResponse(svr_socket);
            svr_socket.close();
        }
    }
    
    public static void sendMessage(Socket socket, int counter) throws IOException
    {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("Message " + counter);
        out.printf("now: %s%n", LocalDateTime.now());
        // Something to think about: what would happen if END wasn't included here?
        out.println("Some garbage and then the magic string END");
    }
    
    public static void readResponse(Socket socket) throws IOException
    {
        BufferedReader resp_read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String resp_text;
        while((resp_text = resp_read.readLine()) != null)
        {
            System.out.println("** " + resp_text);
        }
    }
}
