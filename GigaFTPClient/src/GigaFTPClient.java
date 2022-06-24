
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

//create class
public class GigaFTPClient {
    // create main method
    public static void main(String[] args) {
        GigaFTPClient client = new GigaFTPClient(20);
        // create connection
        client.createConnection();
    }

    private int dataPort = 21;
    private String username;
    private int port;
    private String host;
    private String password;
    private String filename;
    private String command;
    private String clientFilename;

    // create constructor to ftp commandline client
    public GigaFTPClient(int port) {
        // create variables

        this.port = port;
        this.username = username;
        this.password = password;
        this.filename = filename;
        this.command = command;
        this.host = "localhost";
    }

    // create method to create connection to ftp server
    public void createConnection() {
        // create variables
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
        Scanner scanner = new Scanner(System.in);
        String host = "";
        // print welcome message

        while (true) {
            System.out.println("Welcome to GigaFTPClient use OPEN command to open connection to GigaFTP server");
            System.out.print("ftp> ");
            String command = scanner.nextLine();
            if (command.equals("EXIT")) {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    // close scanner
                } catch (Exception e) {
                    System.out.println("Error closing socket");
                    // e.printStackTrace();
                }
                scanner.close();
                System.out.println("Thank you for using GigaFTPClient by GigaMorswin");
                break;
            }
            if (command.equals("OPEN")) {
                System.out.println("FTPClient: OPEN command");
                System.out.print("ftp> ");
                host = scanner.nextLine();
                try {
                    // create socket
                    socket = new Socket(host, port);
                    // create output stream
                    out = socket.getOutputStream();
                    // create input stream
                    in = socket.getInputStream();
                    // read response
                    byte[] buffer = new byte[1024];
                    int bytesRead = in.read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    // print response
                    System.out.println(response);
                    // create command line client loop
                    // get response from server
                    Boolean changePort = false;
                    Boolean storeCommand = false;
                    String tempPort = "";
                    String tempCommand = "";
                    while (true) {
                        // print prompt
                        System.out.print("ftp> ");
                        // get command from user
                        command = "";
                        command = scanner.nextLine();

                        if (changePort) {
                            tempPort = command;
                        }
                        if (command.equals("PORT")) {
                            changePort = true;
                        }

                        if (storeCommand) {
                            tempCommand = command;
                        }

                        // write command to server

                        if (storeCommand) {
                            clientFilename = tempCommand;
                            // check if file exists
                            if (!new File(clientFilename).exists()) {
                                System.out.println("FTPClient: file doesnt exist");
                                out.write("ABOR".getBytes());
                                storeCommand = false;
                            } else {
                                out.write(command.getBytes());
                            }
                        } else {
                            out.write(command.getBytes());
                        }
                        // read response
                        bytesRead = in.read(buffer);

                        response = new String(buffer, 0, bytesRead);
                        // get response code from server 3 digits
                        String responseCode = response.substring(0, 3);
                        responseCode = responseCode.trim();
                        System.out.println(response);
                        // boolean to string
                        if (responseCode.equals("200") && changePort == true) {
                            // cast response to int
                            dataPort = Integer.parseInt(tempPort);
                        }
                        // it shouldnt be coded like that but it works bcause I don't know why field
                        // returns null

                        /*
                         * if (responseCode.equals("550")){
                         * System.out.println(response);
                         * }
                         * 
                         */

                        if (command.equals("STOR")) {
                            storeCommand = true;
                        }

                        if (responseCode.equals("150")) {
                            while (!(responseCode.equals("226") || responseCode.equals("426"))) {

                                if (responseCode.equals("125") && response != "") {
                                    // open data connection socket and retrieve and save file from server using file
                                    // input stream
                                    // create socket
                                    if (storeCommand) {
                                        socket = new Socket(host, dataPort);

                                        OutputStream out2 = socket.getOutputStream();
                                        // send file to server

                                        InputStream fileInputStream = new FileInputStream(clientFilename);
                                        fileInputStream.transferTo(out2);

                                        fileInputStream.close();
                                        socket.close();
                                        storeCommand = false;
                                    } else {
                                        Socket dataSocket = new Socket(host, dataPort);
                                        // create input stream
                                        BufferedInputStream dataIn = new BufferedInputStream(
                                                dataSocket.getInputStream());
                                        // create file output stream
                                        System.out.println("Saving file to " + command);
                                        FileOutputStream dataOut = new FileOutputStream(command);
                                        // read data from server
                                        int c = 0;

                                        while ((c = dataIn.read()) != -1) {
                                            dataOut.write(c);
                                        }
                                        // close file output stream
                                        dataOut.close();
                                        // close data socket
                                        dataSocket.close();
                                    }
                                }

                                // read response line by line
                                bytesRead = in.read(buffer);

                                // offset the buffer to get the response line
                                response = new String(buffer, 0, bytesRead);

                                // foreach string with newline
                                for (String line : response.split("\n")) {
                                    // get response code from server 3 digits
                                    if (line.length() > 2) {
                                        responseCode = line.substring(0, 3);
                                    }
                                    responseCode = responseCode.trim();
                                }
                                // print response
                                System.out.print(response);
                            }
                        }

                    }

                } catch (Exception e) {
                    System.out.println(
                            "Connection has been not estabilished using port or server is not available anymore");
                    // e.printStackTrace();
                } finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        // close scanner

                    } catch (Exception e) {
                        System.out.println("Error closing socket");
                        // e.printStackTrace();
                    }
                }

            } else {
                System.out.println("FTPClient: Invalid command");
            }
        }

    }
}
