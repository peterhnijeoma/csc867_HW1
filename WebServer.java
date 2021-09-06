// ################################################################
// Description: Basic webserver - demonstrate the HTTP protocol. runs at port 8100
//
// Author: Peter Ijeoma and [team mate]
//
// usage: java WebServer
// #################################################################

import java.net.*;
import java.io.*;
import java.util.*;

public class WebServer
{
     //public static final int svr_port = 8100;
     public static Hashtable config_values = new Hashtable();
     public static Hashtable mime_types = new Hashtable();
     public static Hashtable file_allow = new Hashtable();
     public static FileWriter fw_log;
     
     public static String cgi_directory;
     public static String logstr;

     public static void main(String[] args) throws IOException
     {
         /* commented out - these valuses are now read from the config file conf/httpd.conf
         config_values.put ("Listen", 8200);
         config_values.put ("DocumentRoot" , "./");
         config_values.put ("LogFile", "myserver.log");
         config_values.put("DirectoryIndex", "index.html"); 
         */
         
         // reade configuration files and store in memory
         readConfiguration();
         //fw_log = new FileWriter(config_values.get("LogFile").toString(), true);
         String filename = config_values.get("LogFile").toString();
         System.out.println(filename);
         fw_log = new FileWriter("myserverlog.txt", true);
         //fw_log = new FileWriter(filename, true);
         
         //ServerSocket svr_socket = new ServerSocket(svr_port);
         ServerSocket svr_socket = new ServerSocket(Integer.parseInt(config_values.get("Listen").toString()));
         //System.out.println("WebServer started: listening on port: " + config_values.get("Listen"));
         logstr = "WebServer started: listening on port: " + config_values.get("Listen");
         logAction(logstr);
         Socket clnt_socket = null;
         while(true)
         {
             clnt_socket = svr_socket.accept();
             logstr = "Accepted request from a client socket : " + clnt_socket;
             logAction(logstr);
             outputRequest(clnt_socket);
             sendResponse(clnt_socket);
             clnt_socket.close();
             logAction("client socket has been closed");
         }
         //fw_log.close();
     }
     
     protected static void outputRequest(Socket clnt) throws IOException
     {
         String msg_text;
         BufferedReader msg_reader = new BufferedReader(new InputStreamReader(clnt.getInputStream()));
         
         while(true)
         {
             msg_text = msg_reader.readLine();
             System.out.println("> " + msg_text);
             // check for END of message stream
             if (msg_text.contains("END"))
             {
                 break;
                 //logAction("clients requests ended by client");
             }
         }
         outputLineBreak();
     }
     
     protected static void outputLineBreak()
     {
         System.out.println("-------------------------");
     }
     
     protected static void sendResponse(Socket clnt) throws IOException
     {
         PrintWriter out_resp = new PrintWriter(clnt.getOutputStream(), true);
         int gift = (int) Math.ceil(Math.random() * 100);
         String svr_response = "Gee, thanks, this is for you: " + gift;
         out_resp.println(svr_response);
         outputLineBreak();
         System.out.println("I sent: " + svr_response);
         logAction("response sent to client on socket: ");
     }
     
        
    public static void readConfiguration( ) throws IOException
    {
        FileReader file_reader;        
        String file_line, key_str, value_str;
        BufferedReader buf_reader;
        StringTokenizer st_line;
        
        try
        {
            // read configuration file - conf/httpd.cof
            System.out.println("reading conf/httpd.conf into memory");
            file_reader = new FileReader("conf/httpd.conf");
            buf_reader = new BufferedReader(file_reader);
            while((file_line = buf_reader.readLine()) != null)
            {
                if ( file_line.startsWith("#"))
                    continue;
                //System.out.println(file_line);
                st_line = new StringTokenizer(file_line, " ");
                key_str = st_line.nextToken();
                value_str = st_line.nextToken();
                config_values.put (key_str, value_str);
                if ( key_str.equals("ScriptAlias"))
                    cgi_directory = st_line.nextToken();
                
                // for now Alias /ab/ and others are not properly tokenized
                // needs to be done if absolutely required
                //System.out.println("key is: " + key_str + "; value is: " + value_str +";");
            }
            file_reader.close();
            
            // read configuration file - conf/mime.types
            System.out.println("reading mime types into memory");
            file_reader = new FileReader("conf/mime.types");
            buf_reader = new BufferedReader(file_reader);
            value_str = "";
            while((file_line = buf_reader.readLine()) != null)
            {
                if ( file_line.startsWith("#") || file_line.length() == 0)
                    continue;
                
                //System.out.println(file_line);
                st_line = new StringTokenizer(file_line, " ");
                key_str = st_line.nextToken();
                while (st_line.hasMoreTokens())
                {
                    value_str = value_str + " " + st_line.nextToken ();
                }
                mime_types.put (key_str, value_str);
                //System.out.println("key is: " + key_str + "; value is: " + value_str +";");
            }
            file_reader.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e);
        }
    }
    
    
    public static void logAction(String log_str) throws IOException
    {
         PrintWriter log_writer = new PrintWriter(fw_log, true);
         log_writer.println(log_str);
         System.out.println(log_str);
    }
     
}