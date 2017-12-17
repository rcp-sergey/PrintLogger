package db;

import com.opencsv.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class DatabaseHandler {
    public static final String PATH_TO_PROPERTIES = "config.properties";
    private static String connectionURL;
    private static final DatabaseHandler DATABASE_HANDLER = new DatabaseHandler();
    private static Connection connection;
    private Statement stmt;
    /*SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");*/

    private DatabaseHandler() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getConnection() throws SQLException {
        importProperties();
        connection = DriverManager.getConnection(connectionURL);
        stmt = connection.createStatement();
        stmt.execute("SET NAMES 'utf8mb4'");
        createTable();
    }

    public boolean isConnected() {
        try {
            /*if (connection != null && !connection.isClosed()) {*/
            searchInDB("SELECT 1");
            /*System.out.println("connected")*/;
            return true;
        } catch (SQLException e) {
            try {
                /*System.out.println("reconnection");*/
                getConnection();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        /*System.out.println("not connected");*/
        return false;
    }

    public static DatabaseHandler getInstance() {
        return DATABASE_HANDLER;
    }

    public void readCSV(String filePath) throws IOException, ClassNotFoundException  {
        try {
            System.out.println("import...");
            final RFC4180Parser csvParser = new RFC4180ParserBuilder().withSeparator(',').withQuoteChar('"').build();
            final CSVReader csvReader = new CSVReaderBuilder (new InputStreamReader(new FileInputStream(filePath), "cp1251")).withSkipLines(2).withCSVParser(csvParser).build();
            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length());
            int lastLineIndex = getLastLineIndex(fileName);
            System.out.println(fileName);

            // read all lines from cvs to ArrayList
            ArrayList<String[]> csvList = (ArrayList<String[]>) csvReader.readAll();

            // check lines that read whether they exist in file or already been imported
            System.out.println(csvList.size());
            if (csvList.size() < 3) {
                System.out.println("File is empty");
                return;
            }
            if (csvList.size() == lastLineIndex) {
                System.out.println("File was already completely imported");
                return;
            }
            int startLine;
            if (lastLineIndex == 0) {
                startLine = 0;
                System.out.println("Lines to add: " + (csvList.size() - startLine));
            } else {
                startLine = lastLineIndex;
                System.out.println("File will be imported from " + startLine + " line. Lines to add: " + (csvList.size() - lastLineIndex));
            }
            // end of check

            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement("INSERT INTO logs (Source, Line, Time, User, Pages, Copies, Printer, DocumentName, Client, PaperSize, Language, Duplex, Grayscale, FileSize) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            int partNum = 0;
            String[] data = null;
            for (int i = startLine; i < csvList.size(); i++) {
                data = csvList.get(i);
                String docName = data[5];
                try {
                    ps.setString(1, fileName);
                    ps.setInt(2, i + 1);
                    ps.setString(3, data[0]); // Time
                    ps.setString(4, data[1]); // User
                    ps.setInt(5, Integer.parseInt(data[2])); // Pages
                    ps.setInt(6, Integer.parseInt(data[3])); // Copies
                    ps.setString(7, data[4]); // Printer
                    ps.setString(8, docName); // DocumentName
                    ps.setString(9, data[6]); // Client
                    ps.setString(10, data[7]); // PaperSize
                    ps.setString(11, data[8]); // Language
                /*ps.setString(10, data[9]); // Height
                ps.setString(11, data[10]); // Width*/
                    ps.setString(12, data[11]); // Duplex
                    ps.setString(13, data[12]); // Grayscale
                    ps.setString(14, data[13]); // FileSize
                    ps.addBatch();
                    if (i % 100_000 == 1) {
                        ps.executeBatch();
                        System.out.println("Import part " + (++partNum));
                    }
                } catch (Exception e) {
                    System.out.println(docName);
                    e.printStackTrace();
                }
            }
            ps.executeBatch();
            connection.setAutoCommit(true);
            System.out.println("Import completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet searchInDB(String query) throws SQLException {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (NullPointerException e) {
            throw new SQLException();
        }
    }


    public int getLastLineIndex(String file) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT Line FROM logs WHERE Source = ? AND Line = (SELECT MAX(Line) FROM logs)");
        ps.setString(1, file);
        ResultSet rs = ps.executeQuery();
        int line = 0;
        if ( rs.next() ) line = rs.getInt(1);
        return line;
    }

    public void createTable() throws SQLException {
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS logs (id INTEGER PRIMARY KEY AUTO_INCREMENT, Source TEXT NOT NULL, Line INTEGER NOT NULL, Time DATETIME, User TEXT, Pages INTEGER , Copies INTEGER, Printer TEXT, DocumentName LONGTEXT , Client TEXT, PaperSize TEXT, Language TEXT, Duplex TEXT, Grayscale TEXT, FileSize TEXT) CHARACTER SET utf8mb4");
    }

    public void importProperties() {
        try {
            File propertiesFile = new File(PATH_TO_PROPERTIES);
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
                PrintWriter writer = new PrintWriter(new FileWriter(propertiesFile));
                writer.println("server = localhost");
                writer.println("port = 3306");
                writer.println("base = printlogdb");
                writer.println("login = logclient");
                writer.println("password = Qwer1234");
                writer.flush();
            }
            FileInputStream fis = new FileInputStream(propertiesFile);
            Properties properties = new Properties();
            properties.load(fis);
            String server = properties.getProperty("server");
            String port = properties.getProperty("port");
            String base = properties.getProperty("base");
            String login = properties.getProperty("login");
            String password = properties.getProperty("password");
            StringBuilder urlBuilder = new StringBuilder();
            connectionURL = urlBuilder.append("jdbc:mysql://").append(server).append(":").append(port).append("/").append(base).append("?").append("user").append("=").append(login).append("&").append("password").append("=").append(password).toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}







/*    private void setCSVLinesRead(String fileName, int lines) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT Lines FROM importHistory WHERE Name = " + fileName);
        if (rs.next()) {
            int currentLines = rs.getInt(1);
            if (currentLines == lines) System.out.println("Value is actual");
            else if (currentLines > lines) System.out.println("Value is wrong");
            else {
                int changeCount = stmt.executeUpdate("UPDATE Lines")
            }
        }
        PreparedStatement ps = conn.prepareStatement("INSERT INTO importHistory (Name, Lines) VALUES (?, ?)");
        ps.setString(1, fileName);
        ps.setInt(2, lines);
        ps.executeBatch();
    }*/