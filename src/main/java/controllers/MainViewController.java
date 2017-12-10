package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import db.DBHandler;
import db.LogEntry;
import tools.ExportHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainViewController implements Initializable {
    @FXML
    private TextField filePathField;
    @FXML
    private Label warningLabel;
    @FXML
    private TextField userNameField;
    @FXML
    private DatePicker datePickerFrom;
    @FXML
    private DatePicker datePickerTo;
    @FXML
    private TableView tableView;
    private final DBHandler dbHandler = DBHandler.getInstance();
    private static LinkedHashMap<String, Boolean> columns;
    private ObservableList<LogEntry> data;
    
    static {
        columns = new LinkedHashMap<>();
        columns.put("Time", true);
        columns.put("User", true);
        columns.put("Pages", true);
        columns.put("Copies", true);
        columns.put("Printer", true);
        columns.put("DocumentName", true);
        columns.put("PaperSize", true);
        columns.put("Grayscale", true);
        columns.put("FileSize", true);
        columns.put("Client", true);
    }

    public void startImport() {
        warningLabel.setText("");
        try {
            String filePath = filePathField.getText();
            dbHandler.readCSV(filePath);
        } catch (NullPointerException e) {
            warningLabel.setText("Please, choose file!");
        } catch (FileNotFoundException e) {
            warningLabel.setText("File not found!");
        } catch (IOException e) {
            warningLabel.setText("IO error");
        } catch (ClassNotFoundException e) {
            warningLabel.setText("File is not correct!");
        }
    }

    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open VSC print log file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("papercut-print-log-*","*.csv"));
        try {
            File file = fileChooser.showOpenDialog(filePathField.getScene().getWindow());
            if (file.exists()) filePathField.setText(file.getPath());
        } catch (Exception e) {
            warningLabel.setText("Please, choose file!");
        }
    }

    public void exportTable() {
        if (tableView.getItems() == null || tableView.getItems().size() == 0) {
            warningLabel.setText("No data to export");
            return;
        }
        /*ObservableList<LogEntry> arrayList = tableView.getItems();*/
        ExportHandler.exportToExcel(tableView);
    }

    public void search() {
        warningLabel.setText("");
        data = FXCollections.observableArrayList();
        clearTable();
        Thread thread = new Thread(() -> {
            Long startTime = System.currentTimeMillis();
            try {
                String query = setUpQuery();
                ResultSet rs = dbHandler.searchInDB(query);
                ResultSetMetaData metaData = rs.getMetaData();
                createColumns(metaData);
                int[] usefulColumns = new int[metaData.getColumnCount()];
                for (int i = 0; i < usefulColumns.length; i++) {
                    usefulColumns[i] = getColumnCount(metaData.getColumnName(i + 1));
                }
                Object[] values = new Object[columns.size()];
                // fill array
                while(rs.next()){
                    for (int i = 0; i < usefulColumns.length; i++) {
                        values[usefulColumns[i]] = rs.getObject(i + 1);
                    }
                    data.add(new LogEntry((Date) values[0], (String) values[1], (int) values[2], (int) values[3], (String) values[4], (String) values[5], (String) values[6], (String) values[7], (String) values[8], (String) values[9]));
                }
                Long time = System.currentTimeMillis() - startTime;
                System.out.println("list done");
                // set array to table
                Platform.runLater(() -> {
                    tableView.setItems(data);
                    tableView.refresh();
                    warningLabel.setText(data.size() + " lines in " + (time / 1000) + " seconds");
                });
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private int getColumnCount(String colName) {
        if (colName.equals("Time")) return 0;
        else if (colName.equals("User")) return 1;
        else if (colName.equals("Pages")) return 2;
        else if (colName.equals("Copies")) return 3;
        else if (colName.equals("Printer")) return 4;
        else if (colName.equals("DocumentName")) return 5;
        else if (colName.equals("PaperSize")) return 6;
        else if (colName.equals("Grayscale")) return 7;
        else if (colName.equals("FileSize")) return 8;
        else if (colName.equals("Client")) return 9;
        return 0; // TODO throw exception
    }

    private void clearTable() { // TODO remove unnecessary lines if they exist
        tableView.getItems().removeAll();
        tableView.getItems().clear();
        tableView.refresh();
        tableView.getColumns().removeAll();
        tableView.getColumns().clear();
        tableView.refresh();
    }

    private void createColumns(ResultSetMetaData metaData) throws SQLException {
        for(int i = 0 ; i < metaData.getColumnCount(); i++){
            String colName = metaData.getColumnName(i + 1);
            if (colName.equals("Time")) {
                TableColumn<LogEntry, Date> column = new TableColumn<>("Time");
                column.setCellValueFactory(new PropertyValueFactory<>("date"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("User")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("User");
                column.setCellValueFactory(new PropertyValueFactory<>("user"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Pages")) {
                TableColumn<LogEntry, Integer> column = new TableColumn<>("Pages");
                column.setCellValueFactory(new PropertyValueFactory<>("pages"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Copies")) {
                TableColumn<LogEntry, Integer> column = new TableColumn<>("Copies");
                column.setCellValueFactory(new PropertyValueFactory<>("copies"));
                Platform.runLater(() -> tableView.getColumns().addAll(column));
            } else if (colName.equals("Printer")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Printer");
                column.setCellValueFactory(new PropertyValueFactory<>("printer"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("DocumentName")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Document Name");
                column.setCellValueFactory(new PropertyValueFactory<>("documentName"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("PaperSize")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Paper Size");
                column.setCellValueFactory(new PropertyValueFactory<>("paperSize"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Grayscale")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Color");
                column.setCellValueFactory(new PropertyValueFactory<>("grayscale"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("FileSize")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("File Size");
                column.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Client")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Client");
                column.setCellValueFactory(new PropertyValueFactory<>("client"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            }
        }
        Platform.runLater(() -> tableView.refresh());
    }

    private String setUpQuery() throws ParseException {
        StringBuffer sb = new StringBuffer();
        int countWhere = 0;
        int countColumns = 0;
        for (Map.Entry<String, Boolean> entry: columns.entrySet()) {
            if (entry.getValue()) {
                if (countColumns == 0) sb.append("SELECT ");
                else sb.append(", ");
                sb.append(entry.getKey());
                countColumns++;
            }
        }
        sb.append(" FROM logs");
        if (userNameField.getText() != null && !userNameField.getText().equals("")) {
            sb.append(" WHERE User = '" + userNameField.getText() + "'");
            countWhere++;
        }
        if (datePickerFrom.getValue() != null) {
            if (countWhere == 0)    sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Time >= '" + getTimeFromText() + "'");
            countWhere++;
        }
        if (datePickerTo.getValue() != null) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Time <= '" + getTimeToText() + "'");
            countWhere++;
        }
        String query = sb.toString();
        System.out.println(query);
        return query;
    }

    private String getTimeFromText() throws ParseException {
        if (datePickerFrom.getValue() == null) return "empty"; // TODO remove after test
        return datePickerFrom.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
    }

    private String getTimeToText() throws ParseException {
        if (datePickerTo.getValue() == null) return "empty"; // TODO remove after test
        return datePickerTo.getValue().format(DateTimeFormatter.ofPattern("yyy-MM-dd")) + " 23:59:59";
    }

    public void startSearch() throws ParseException {
        warningLabel.setText("");
        String stringDateFrom = datePickerFrom.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " 00:00:00";
        String stringDateTo = datePickerTo.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " 23:59:59";
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String userName = userNameField.getText();
        Timestamp dateFrom = new Timestamp(df.parse(stringDateFrom).getTime());
        Timestamp dateTo = new Timestamp(df.parse(stringDateTo).getTime());
        System.out.println(dateFrom + " " + dateTo);
        if (userName == null) {
            warningLabel.setText("Enter username!");
            return;
        }
        try {
            dbHandler.searchByUserInDB(userName, dateFrom, dateTo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void exportToExcel() {
        ExportHandler.exportToExcel(tableView);
    }
}

/*

    private static void readCsvUsingLoad()
    {
        try (Connection connection = DBConnection.getConnection())
        {

            String loadQuery = "LOAD DATA LOCAL INFILE '" + "C:\\upload.csv" + "' INTO TABLE txn_tbl FIELDS TERMINATED BY ','" + " LINES TERMINATED BY '\n' (txn_amount, card_number, terminal_id) ";
            System.out.println(loadQuery);
            Statement stmt = connection.createStatement();
            stmt.execute(loadQuery);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
*/
