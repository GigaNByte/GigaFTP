import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class GigaFtpHandler implements Runnable {
    private Socket socket;
    private String password;
    private String user;
    private Boolean auth = false;
    private String publicDir = "";
    private String cwd = ""; // curent director
    private int portNumber = 0;
    private String address = "";

    public GigaFtpHandler(Socket socket) {

        this.publicDir = System.getProperty("user.dir") + "\\public\\";
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Connection from " + socket.getInetAddress());
            address = "" + socket.getInetAddress();
            handleConnection(socket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                // print connection closed
                System.out.println("Connection closed");
            }
        }
    }

    private void handleConnection(Socket socket) throws Exception {
        OutputStream out = socket.getOutputStream();
        out.write("Welcome to GigaFTP\n".getBytes());

        // get command from client while true
        Boolean breakflag = false;
        while (true && !breakflag) {

            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            String command = new String(buffer, 0, bytesRead);
            System.out.println("Command: " + command);
            // command switch case
            switch (command) {
                case "USER":
                    // read user name from ftp client and store in user variable
                    out.write("214 Command USER: ".getBytes());
                    user = readRequest(in);

                    out.write("331 User name okay, need password.\n".getBytes());
                    System.out.println("USER" + user);
                    break;
                case "PASS":
                    out.write("214 Command PASS: ".getBytes());
                    password = readRequest(in);
                    // check if user name and password are correct
                    // write response to client
                    if (GigaFtp.checkUser(user, password)) {
                        out.write("230 User logged in, proceed.\n".getBytes());
                        auth = true;
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "LIST":
                    if (auth) {
                        out.write("150 Here comes the directory listing.\n".getBytes());
                        // get all files in public current directory
                        String[] files = GigaFtp.getFiles(publicDir + cwd);

                        // for each file in file array

                        for (String file : files) {
                            // write file to client
                            if (file != null) {
                                String fileString = file + "\n";
                                out.write(fileString.getBytes());
                            }
                        }
                        out.write("226 Directory send OK.\n".getBytes());
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }

                    break;
                case "PWD":
                    if (auth) {
                        // write response to client
                        String response = "257 \"./public" + cwd + "\" is current directory.\n";
                        out.write(response.getBytes());
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "CWD":
                    if (auth) {
                        // read directory from ftp client and store in directory variable
                        out.write("214 Command CWD: ".getBytes());
                        String directory = "/" + readRequest(in);
                        // check if directory exists
                        // replace double slashes with single slash
                        directory = directory.replace("//", "/");

                        File dir = new File(publicDir + directory);
                        if (!dir.exists()) {
                            System.out.println("Directory does not exist: \"./public\"" + directory + "\"\n");
                            out.write("550 Directory does not exist.\n".getBytes());
                        } else {
                            cwd = directory;
                            out.write("250 Directory successfully changed.\n".getBytes());
                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "CDUP":
                    if (auth) {
                        // read directory from ftp client and store in directory variable
                        String info = "214 Command CDUP: \n";

                        if (cwd.equals("")) {
                            info += "550 Directory does not exist.\n";
                            out.write(info.getBytes());
                        } else {
                            // remove last directory from directory variable
                            // check if directory has any slash

                            cwd = cwd.substring(0, cwd.lastIndexOf("/"));
                            // write response to client
                            info += "250 Directory successfully changed.\n";
                            out.write(info.getBytes());
                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "RETR":
                    if (auth) {
                        // read file name from ftp client and store in file variable
                        out.write("214 Command RETR: ".getBytes());
                        String file = readRequest(in);
                        // check if file exists
                        // write response to client
                        File f = new File(publicDir + cwd + "/" + file);
                        // check if file does not exist and is not a directory
                        if (!f.exists() || f.isDirectory()) {
                            out.write("550 File does not exist.\n".getBytes());
                        } else {
                            // switch to binary mode write code
                            String infoRetr = "150 Opening BINARY mode data connection for " + file + "\n";

                            out.write(infoRetr.getBytes());
                            // use openDataSocket to send file
                            Boolean status = openDataSocket(cwd, file);

                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "STOR":

                    if (auth) {
                        // read file name from ftp client and store in file variable
                        out.write("214 Command STOR: ".getBytes());
                        String fileName = readRequest(in);

                        // if ABOR is sent, close data socket and return to command mode
                        if (fileName.equals("ABOR")) {
                            out.write("226 ABOR command successful.\n".getBytes());
                        } else {
                            // check if file exists
                            // write response to client
                            File f1 = new File(publicDir + cwd + "/" + fileName);
                            // check if file is not a directory
                            if (f1.isDirectory()) {
                                out.write("550 File is directory.\n".getBytes());
                            } else {
                                // switch to binary mode write code
                                String infoStor = "150 Opening BINARY mode data connection for " + fileName + "\n";
                                out.write(infoStor.getBytes());
                                // use openDataSocket to send file
                                Boolean status = openInputDataSocket(cwd, fileName);
                            }
                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }

                    break;
                case "DELE":
                    if (auth) {
                        // read file name from ftp client and store in file variable
                        out.write("214 Command DELE: ".getBytes());
                        String fileDel = readRequest(in);
                        // check if file exists
                        // write response to client
                        File fDel = new File(publicDir + cwd + "/" + fileDel);
                        // check if file does not exist and is not a directory
                        if (!fDel.exists() || fDel.isDirectory()) {
                            out.write("550 File does not exist.\n".getBytes());
                        } else {
                            // delete file
                            fDel.delete();
                            out.write("250 File deleted.\n".getBytes());
                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "MKD":
                    if (auth) {
                        // read directory from ftp client and store in directory variable
                        out.write("214 Command MKD: ".getBytes());
                        String directoryMkd = readRequest(in);
                        // check if directory exists
                        // write response to client
                        File dirMkd = new File(publicDir + cwd + "/" + directoryMkd);
                        if (dirMkd.exists()) {
                            out.write("550 Directory already exists.\n".getBytes());
                        } else {
                            dirMkd.mkdir();
                            out.write("250 Directory successfully created.\n".getBytes());
                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "RMD":
                    if (auth) {
                        // remove directory from ftp client and store in directory variable
                        out.write("214 Command RMD: ".getBytes());
                        String directoryRmd = readRequest(in);
                        // check if directory exists
                        // write response to client
                        File dirRmd = new File(publicDir + cwd + "/" + directoryRmd);
                        if (!dirRmd.exists()) {
                            out.write("550 Directory does not exist.\n".getBytes());
                        } else {
                            dirRmd.delete();
                            out.write("250 Directory successfully deleted.\n".getBytes());
                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "PORT":
                    if (auth) {
                        // read port from ftp client and store in port variable
                        out.write("214 Command PORT: ".getBytes());
                        String port = readRequest(in);
                        // validate port
                        if (validatePort(port)) {
                            // cast port to int
                            portNumber = Integer.parseInt(port);
                            // write response to client
                            out.write("200 PORT command successful.\n".getBytes());
                        } else {
                            out.write("500 PORT command failed.\n".getBytes());
                        }
                    } else {
                        out.write("530 Not logged in.\n".getBytes());
                    }
                    break;
                case "QUIT":
                    // write response to client
                    out.write("221 Goodbye.\n".getBytes());
                    // close socket
                    socket.close();
                    // break while loop
                    breakflag = true;
                    break;
                default:
                    out.write("500 Unknown command\n".getBytes());
                    break;
            }
        }
    }

    private Boolean validatePort(String port) {
        // validate port
        // check if all characters are numbers
        for (int i = 0; i < port.length(); i++) {
            if (!Character.isDigit(port.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String readRequest(InputStream inputStream) {
        // read request from ftp client
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        System.out.println("Reading request");
        try {
            bytesRead = inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buffer, 0, bytesRead);
    }

    private String getUserName(Socket socket) {
        // get input stream
        InputStream inputStream = getInputStream(socket);
        // get user name
        String userName = getString(inputStream);
        // return user name
        return userName;
    }

    private InputStream getInputStream(Socket socket) {
        try {
            // get input stream
            InputStream inputStream = socket.getInputStream();
            // return input stream
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return null
        return null;
    }

    // getBytes
    private byte[] getBytes(InputStream inputStream) {
        try {
            // get bytes
            byte[] bytes = new byte[inputStream.available()];
            // read bytes
            inputStream.read(bytes);
            // return bytes
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return null
        return null;
    }

    private String getString(InputStream inputStream) {
        try {
            // get string
            String string = new String(getBytes(inputStream));
            // return string
            return string;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return null
        return null;
    }

    private String getPassword(Socket socket) {
        // get input stream
        InputStream inputStream = getInputStream(socket);
        // get password
        String password = getString(inputStream);
        // return password
        return password;
    }

    private boolean isLoggedIn() {
        return this.user != null && this.password != null;
    }

    // create method that opens the ftp second data socket and awaits for client
    // connection
    private Boolean openInputDataSocket(String directory, String fileName) throws IOException {
        Boolean flag = false;
        try {
            // create new server socket
            ServerSocket dataSocket = new ServerSocket(portNumber);
            // close server socket when program exits
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        dataSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // accept client connection in loop
            OutputStream out = socket.getOutputStream();
            // write response to client to indicate that server is ready to receive data
            String outputResponse = "150 Opening BINARY mode data connection for file upload" + "\n";
            out.write(outputResponse.getBytes());

            out.write("125 Data connection already open; transfer starting.\n".getBytes()); // IDK: it will
                                                                                            // desynchronize with the
                                                                                            // client?
            Socket clientSocket = dataSocket.accept();
            // send 125 code

            // create input stream load file from client

            BufferedInputStream dataIn = new BufferedInputStream(clientSocket.getInputStream());
            // create file output stream
            System.out.println("Saving file to " + cwd + "/" + fileName);
            // output Stream to write to file in current directory
            File file = new File(System.getProperty("user.dir") + "\\public\\" + directory + "\\" + fileName);
            FileOutputStream dataOut = new FileOutputStream(file);
            int bytesRead = 0;
            while ((bytesRead = dataIn.read()) != -1) {
                dataOut.write(bytesRead);
            }

            // close file output stream
            dataOut.close();
            // close client socket
            clientSocket.close();
            // close server socket
            dataSocket.close();

            // check transfer is successful and send code do socket
            if (new File(publicDir + cwd + "/" + fileName).exists()) {
                out.write("226 Transfer complete.\n".getBytes());
                flag = true;
            } else {
                out.write("451 Requested action aborted.\n".getBytes());
                flag = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    // create method that opens the ftp second data socket and awaits for client
    // connection
    private Boolean openDataSocket(String directory, String fileName) throws IOException {
        Boolean flag = false;
        try {

            ServerSocket dataSocket = new ServerSocket(portNumber);

            // close server socket when program exits
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        dataSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            // accept client connection in loop
            OutputStream out = socket.getOutputStream();
            out.write("125 Data connection already open; transfer starting.\n".getBytes()); // IDK: it will
                                                                                            // desynchronize with the
                                                                                            // client?

            Socket clientSocket = dataSocket.accept();
            // send 125 code

            OutputStream outputStream = clientSocket.getOutputStream();
            // create file
            File file = new File(System.getProperty("user.dir") + "\\public\\" + directory + "\\" + fileName);
            // create file input stream

            InputStream fileInputStream = new FileInputStream(file);
            // write file input stream to output stream
            fileInputStream.transferTo(outputStream);

            // close client socket
            clientSocket.close();
            // close server socket
            dataSocket.close();

            // check transfer is successful and send code do socket
            if (file.exists()) {
                out.write("226 Transfer complete.\n".getBytes());
                flag = true;
            } else {
                out.write("550 File does not exist.\n".getBytes());
            }

        } catch (Exception e) {
            String connectionLoss = "Connection with client \n " + address + "has been lost.\n";
            System.out.println(connectionLoss);
        }
        return flag;
    }

}
