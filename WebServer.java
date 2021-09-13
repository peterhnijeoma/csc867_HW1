// ################################################################
// Description: Basic webserver - demonstrate the HTTP protocol.
// runs at port specified in configuration file.
//
// Author: Peter Ijeoma and Khushboo Gandhi
//
// usage: java WebServer
// #################################################################

import java.net.*;
import java.io.*;
import java.util.*;
//import java.util.HashMap;

public class WebServer
{
     //public static final int svr_port = 8100;
    //  public static Hashtable config_values = new Hashtable();
    //  public static Hashtable mime_types = new Hashtable();
    //  public static Hashtable file_allow = new Hashtable();
     public static HashMap<String, String> config_values = new HashMap<String, String>();
     public static HashMap<String, String> mime_types = new HashMap<String, String>();
     public static HashMap<String, String> file_allow = new HashMap<String, String>();
     public static FileWriter fw_log;
     
     public static String cgi_directory;
     public static String logstr;

     // request line variables
     static String request_line, req_method, req_resource, req_version;
     static String requested_file, query_string;
     // request header variables
     static String request_header, req_header, header_value;
     // message body string
     static String req_msg_body;
     // hold request headers
     static HashMap<String, String> req_headers = new HashMap<String, String>();

     // response headers
     static String response_line = "HTTP/1.1 ";
     static String content_type = "Content-type: ";
     static String content_length = "Content-length: ";
     static String server_header = "Server: Peter Ijeoma and Khushboo Gandhi";
     static String date_header = "Date: ";

    // web server document root directory
    static File webRoot;

     public static void main(String[] args) throws IOException
     {
        try
        {
           // read configuration files and store in memory
           readConfiguration();

           // open web server root directory
           webRoot = new File(config_values.get("DocumentRoot"));

           // open the log file for logging
           fw_log = new FileWriter(config_values.get("LogFile").toString(), true);
           //String filename = config_values.get("LogFile").toString();
           //System.out.println(filename);
           //fw_log = new FileWriter("myserverlog.txt", true);
           //fw_log = new FileWriter(filename, true);
         
           // open specified server port port for listening
           ServerSocket server_socket = new ServerSocket(Integer.parseInt(config_values.get("Listen").toString()));

           // log server start - date and time
           logstr = "WebServer started: listening on port: " + config_values.get("Listen") + " on " + new Date();
           logAction(logstr);

           // listen for client requests
           Socket client_socket = null;
           while(true)
           {
               client_socket = server_socket.accept();
               logstr = "Connected to a client at client socket : " + client_socket;
               logAction(logstr);

               processClient(client_socket);

               //outputRequest(client_socket);
               //sendResponseold(client_socket);

               // close connection after responding
               logstr = "Cpnnection to client socket (" + client_socket + ") is now closed";
               client_socket.close();
               logAction(logstr);
           }
           //fw_log.close();
        }
        catch (IOException e)
        {
            System.out.println("Server error : " + e.getMessage());
        }
     }

     public static void processClient(Socket client_sock) throws IOException
     {
        parseRequest(client_sock);

        sendResponse(client_sock);
     }

     protected static void parseRequest(Socket client) throws IOException
     {
        BufferedReader req_reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        StringTokenizer req_token; // for all tokenizations
        String str;  // request messahe body

        try
        {
            // get request line
            request_line = req_reader.readLine();
            //System.out.println("request line is: " + request_line);

            // get components of request line - method, resource and HTTP version
            req_token = new StringTokenizer(request_line, " ");
            req_method = req_token.nextToken().toUpperCase();
            req_resource = req_token.nextToken();
            req_version = req_token.nextToken();

            //System.out.println("requested file is: " + req_resource);

            // get the requested file and query string from the request resource
            //req_token = new StringTokenizer(req_resource, "?");
            //requested_file = req_token.nextToken();
            requested_file = req_resource;
            //query_string = req_token.nextToken();

            // get request header fields
            //request_header = req_reader.readLine();

            //System.out.println("header line : " + request_header);
            while ((request_header = req_reader.readLine()) != null)
            {
                //System.out.println("in loop - header line : " + request_header);

                // if a newline charcter is read, end of header
                if (request_header.length() == 0)
                   break;
                
                req_token = new StringTokenizer(request_header, ":");
                req_header = req_token.nextToken();
                header_value = req_token.nextToken();
                req_headers.put(req_header, header_value);
                //request_header = req_reader.readLine();
                
            }

            //System.out.println("done with headers");

            // get request message body
            
            //str = req_reader.readLine();

            //System.out.println("request message body : " + str);
            while ((str = req_reader.readLine()) != null)
            {
                // message body has ended if a new line
                if (str.length() == 0)
                   break;

                req_msg_body += str;
                //str = req_reader.readLine();
            }

            //System.out.println("done with msg body: " + req_msg_body);
        }
        catch (IOException e)
        {
            System.out.println("parsing error : " + e.getMessage());
        }

        //System.out.println("done parsing request.");
     }
     
     protected static void sendResponse(Socket client) throws IOException
     {
         PrintWriter response_out = new PrintWriter(client.getOutputStream());
         BufferedOutputStream resp_data_out = new BufferedOutputStream(client.getOutputStream());

         byte[] response_data;  //binary data from requested file
         File data_file;        // for reading requested file data

        try
        {
           // response headers
           response_out.println(response_line);
           response_out.println(server_header);
           response_out.println(date_header);

           if (requested_file.endsWith("html"))
              content_type += "text/html";

           response_out.println(content_type);

           if (req_method.equals("GET"))
           {
              // get data from requested file
              data_file = new File(webRoot, requested_file);
              content_length += data_file.length();
              response_out.println(content_length);
              response_data = read_file_data(data_file);
              resp_data_out.write(response_data, 0, (int) data_file.length());
              resp_data_out.flush();
           }
           else
           {
              response_out.println("Not impelemnted yet!!");
           }

           logstr = "response sent to client: " + client;
           logAction(logstr);
        }
        catch (IOException e)
        {
            System.out.println("response error : " + e.getMessage());
        }
     }
     
    public static void readConfiguration( ) throws IOException
    {
        FileReader file_reader;        
        String file_line, key_str, value_str;
        BufferedReader buf_reader;
        StringTokenizer st_line;

        file_reader = new FileReader("conf/httpd.conf");
        buf_reader = new BufferedReader(file_reader);
        
        try
        {
            // read configuration file - conf/httpd.cof
            System.out.println("reading conf/httpd.conf into memory");
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
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e);
        }
        finally
        {
            file_reader.close();
        }

        // read configuration file - conf/mime.types
        System.out.println("reading mime types into memory");
        file_reader = new FileReader("conf/mime.types");
        buf_reader = new BufferedReader(file_reader);
        try
        {
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
            
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e);
        }
        finally
        {
            file_reader.close();
        }
    }
    
    // read requested resource file into a byte array for response
    private static byte[] read_file_data(File res_file) throws IOException
    {
        FileInputStream file_input = new FileInputStream(res_file);
        byte[] file_data = new byte[(int) res_file.length()];

        try
        {
            file_input.read(file_data);
        }
        catch (IOException e)
        {
            System.out.println("resource file read error\n");
        } // close the input file even if an exception occured
        finally
        {
            if (file_input != null)
               file_input.close();
        }

        return file_data;
    }

    public static void logAction(String log_str) throws IOException
    {
         PrintWriter log_writer = new PrintWriter(fw_log, true);
         log_writer.println(log_str);
         System.out.println(log_str);
    }



     // old template functions - will be deleted before release.
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
     
     protected static void sendResponseold(Socket clnt) throws IOException
     {
         PrintWriter out_resp = new PrintWriter(clnt.getOutputStream(), true);
         int gift = (int) Math.ceil(Math.random() * 100);
         String svr_response = "Gee, thanks, this is for you: " + gift;
         out_resp.println(svr_response);
         outputLineBreak();
         System.out.println("I sent: " + svr_response);
         logAction("response sent to client on socket: ");
     }
     
}