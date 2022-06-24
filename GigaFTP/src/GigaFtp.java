import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GigaFtp {
    public static final int PORT = 20;
    public static final int DATA_PORT = 21;
    private static final int MAX_THREADS = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    static final HashMap<String, String> users = new HashMap<>();
    private static HashMap<String, String> passwords = new HashMap<>();

    public GigaFtp() {
        // read all users and passwords from users.config file
        try {
            BufferedReader br = new BufferedReader(new FileReader("users.config"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] userAndPassword = line.split(",");
                addUser(userAndPassword[0], userAndPassword[1]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InputStream openFile(String directory) {
        File file = new File(directory);
        return file.exists() ? GigaFtp.class.getClassLoader().getResourceAsStream(directory) : null;
    }

    public void addUser(String userName, String password) {
        // add user name
        users.put(userName, userName);
        // add password
        passwords.put(userName, password);
    }

    // check user
    public static boolean checkUser(String userName, String password) {
        // check if user name is in user map
        if (users.containsKey(userName)) {
            // check if password is in password map
            if (passwords.containsKey(userName)) {
                // check if password is equal to password map
                if (passwords.get(userName).equals(password)) {
                    // return true
                    return true;
                }
            }
        }
        // return false
        return false;
    }

    // create getFiles method to list all files in public directory in project
    // directory
    public static String[] getFiles(String directory) {
        // create new directory

        // check if directory exists
        File dir = new File(directory);
        System.out.println(directory);

        if (!dir.exists()) {
            System.out.println("Directory does not exist");
            // return empty string array
            return new String[0];
        }

        // LIST ALL FILES IN DIRECTORY
        File[] files = dir.listFiles();

        String[] filenames = new String[files.length];
        // for each file in file array
        int i = 0;
        // loop filenames
        for (File file : files) {
            // add file name to filenames array
            filenames[i] = file.getName();
            i++;
        }
        return filenames;
    }

    // start server
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            // close server socket when program exits
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        serverSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(new GigaFtpHandler(socket));
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GigaFtp gigaFtp = new GigaFtp();
        gigaFtp.start();
    }

}
