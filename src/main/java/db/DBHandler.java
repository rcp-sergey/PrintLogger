package db;

import com.opencsv.CSVReader;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DBHandler {
    private static DBHandler dbHandler = new DBHandler();
    private static Connection conn;
    private Statement stmt;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private DBHandler() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/printlogdb?user=logclient&password=Qwer1234");
            stmt = conn.createStatement();
            stmt.execute("SET NAMES 'utf8mb4'");
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DBHandler getInstance() {
        return dbHandler;
    }

    public void readCSV(String filePath) throws IOException, ClassNotFoundException  {
        try {
            System.out.println("import...");
            CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "cp1251"),',');
            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length());
            int lastLineIndex = getLastLineIndex(fileName);
            System.out.println(fileName);
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO logs (Source, Line, Time, User, Pages, Copies, Printer, DocumentName, Client, PaperSize, Language, Duplex, Grayscale, FileSize) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            String[] data = null;
            ArrayList<String[]> csvList = (ArrayList<String[]>) csvReader.readAll();
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
                startLine = 2;
                System.out.println("Lines to add: " + (csvList.size() - startLine));
            } else {
                startLine = lastLineIndex;
                System.out.println("File will be imported from " + startLine + " line. Lines to add: " + (csvList.size() - lastLineIndex));
            }
            int partNum = 0;
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
            conn.setAutoCommit(true);
            System.out.println("Import completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void searchByUserInDB(String user, Timestamp dateFrom, Timestamp dateTo) throws SQLException {
        /*PreparedStatement ps = conn.prepareStatement("SELECT Time, User, DocumentName FROM logs WHERE User = ? AND Time >='2016-04-26 00:00:00' AND Time <'2016-04-27 23:59:59'");*/
        PreparedStatement ps = conn.prepareStatement("SELECT Time, User, DocumentName FROM logs WHERE User = ? AND Time >=? AND Time <=?");
        ps.setString(1, user);
        ps.setTimestamp(2, dateFrom);
        ps.setTimestamp(3, dateTo);
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData meta = rs.getMetaData();
        System.out.println(meta.getColumnTypeName(1));
        ArrayList<String[]> list = new ArrayList<>();
        String[] tmp;
        while (rs.next()) {
            tmp = new String[3];
            tmp[0] = dateFormat.format(rs.getTimestamp(1));
            tmp[1] = rs.getString(2);
            tmp[2] = rs.getString(3);
            list.add(tmp);
        }
        for (String[] element: list) {
            for (String s: element) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }

    public ResultSet searchInDB(String query) throws SQLException {
        return conn.createStatement().executeQuery(query);
    }


    public int getLastLineIndex(String file) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT Line FROM logs WHERE Source = ? AND Line = (SELECT MAX(Line) FROM logs)");
        ps.setString(1, file);
        ResultSet rs = ps.executeQuery();
        int line = 0;
        if ( rs.next() ) line = rs.getInt(1);
        return line;
    }

    public void createTable() throws SQLException {
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS logs (id INTEGER PRIMARY KEY AUTO_INCREMENT, Source TEXT NOT NULL, Line INTEGER NOT NULL, Time DATETIME, User TEXT, Pages INTEGER , Copies INTEGER, Printer TEXT, DocumentName LONGTEXT , Client TEXT, PaperSize TEXT, Language TEXT, Duplex TEXT, Grayscale TEXT, FileSize TEXT) CHARACTER SET utf8mb4");
/*
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS logs (id INTEGER PRIMARY KEY AUTO_INCREMENT, Source TEXT NOT NULL, Line INTEGER NOT NULL, Time DATETIME, User TEXT, Pages INTEGER , Copies INTEGER, Printer TEXT, DocumentName LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_g utf8mb4 COLLATE utf8mb4_general_ci , Client TEXT, PaperSize TEXT, Language TEXT, Duplex TEXT, Grayscale TEXT, FileSize TEXT) CHARACTER SET utf8 COLLATE utf8_general_ci");
*/
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