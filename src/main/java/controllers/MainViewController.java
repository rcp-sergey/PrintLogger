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

import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
    @FXML
    private Button searchButton;
    @FXML
    private ComboBox printerComboBox;
    @FXML
    private MenuItem exportToExcelItem;
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
        warningLabel.setText("searching in database...");
        // lock UI to prevent harmful actions during process
        lockUI(true);
        data = FXCollections.observableArrayList();
        clearTable();

        // new thread to prevent UI freezes
        Thread thread = new Thread(() -> {
            Long startTime = System.currentTimeMillis();
            try {
                // generate SQL query string depending on search parameters
                String query = setUpQuery();

                // execute query and get ResultSet and it's MetaData
                ResultSet rs = dbHandler.searchInDB(query);
                ResultSetMetaData metaData = rs.getMetaData();

                // create list with column names from ResultSet and array with order numbers of parameters for LogEntry constructor
                ArrayList<String> columnNames = new ArrayList<>();
                int[] usefulColumns = new int[metaData.getColumnCount()];
                for (int i = 0; i < usefulColumns.length; i++) {
                    columnNames.add(metaData.getColumnName(i + 1));
                    usefulColumns[i] = getColumnCount(metaData.getColumnName(i + 1));
                }

                // create columns by their names
                createColumns(columnNames);

                // create template array for LogEntry constructor and start to fill ObservableList. Non-used parameters are nulls.
                Object[] values = new Object[columns.size()];
                while(rs.next()){
                    for (int i = 0; i < usefulColumns.length; i++) {
                        values[usefulColumns[i]] = rs.getObject(i + 1);
                    }
                    data.add(new LogEntry((Timestamp) values[0], (String) values[1], (int) values[2], (int) values[3], (String) values[4], (String) values[5], (String) values[6], (String) values[7], (String) values[8], (String) values[9]));
                }
                Long time = System.currentTimeMillis() - startTime;
                System.out.println("list done");
                // set ObservableList to TableView
                Platform.runLater(() -> {
                    tableView.setItems(data);
                    tableView.refresh();
                    warningLabel.setText(data.size() + " lines in " + (time / 1000) + " seconds");
                    // unlock UI
                    lockUI(false);
                });
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private int getColumnCount(String columnName) {
        if (columnName.equals("Time")) return 0;
        else if (columnName.equals("User")) return 1;
        else if (columnName.equals("Pages")) return 2;
        else if (columnName.equals("Copies")) return 3;
        else if (columnName.equals("Printer")) return 4;
        else if (columnName.equals("DocumentName")) return 5;
        else if (columnName.equals("PaperSize")) return 6;
        else if (columnName.equals("Grayscale")) return 7;
        else if (columnName.equals("FileSize")) return 8;
        else if (columnName.equals("Client")) return 9;
        return 0; // TODO throw exception
    }

    private void clearTable() { // TODO remove unnecessary lines if they exist AND boolean arg to ask permission
        tableView.getItems().removeAll();
        tableView.getItems().clear();
        tableView.refresh();
        tableView.getColumns().removeAll();
        tableView.getColumns().clear();
        tableView.refresh();
    }

    private void createColumns(ArrayList<String> columnNames) throws SQLException {
        for(int i = 0 ; i < columnNames.size(); i++){
            String colName = columnNames.get(i);
            if (colName.equals("Time")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Time");
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
            } else if (colName.equals("DocumentName") || colName.equals("Document Name") ) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Document Name");
                column.setCellValueFactory(new PropertyValueFactory<>("documentName"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("PaperSize") || colName.equals("Paper Size")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Paper Size");
                column.setCellValueFactory(new PropertyValueFactory<>("paperSize"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Grayscale")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Color");
                column.setCellValueFactory(new PropertyValueFactory<>("grayscale"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("FileSize") && colName.equals("FileSize")) {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        new Thread(() -> {
            Timer printTimer = new Timer();
            printTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        boolean run = true;
                        try {
                            ResultSet rs = DBHandler.getInstance().searchInDB("SELECT distinct Printer FROM logs order by Printer");
                            ObservableList<String> comboBoxData = FXCollections.observableArrayList();
                            while (rs.next()) comboBoxData.add(rs.getString(1));
                            Platform.runLater(() -> printerComboBox.setItems(comboBoxData));
                            run = false;
                        } catch (SQLException e) {
                            // ignore
                        }
                        if (run) printTimer.schedule(this, 5000);
                    }
                }, 300);
        }).start();
    }

    public void exportToExcel() {
        if (tableView.getItems().size() > 65530) {
            warningLabel.setText("Too much results for export. Max size is 65500 lines");
            return;
        }
        ExportHandler.exportToExcel(tableView);
    }

    public void saveTableState() {
        if (tableView != null) ExportHandler.serializeTable(tableView);
    }

    private void lockUI(boolean choice) {
        if (choice) {
            exportToExcelItem.setDisable(true);
            searchButton.setDisable(true);
        } else if (!choice) {
            exportToExcelItem.setDisable(false);
            searchButton.setDisable(false);
        }
    }

    public void loadSavedTableState() {
        try {
            HashMap<String, ArrayList> data = ExportHandler.loadSerializedTable(tableView.getScene().getWindow());
            clearTable();
            createColumns(data.get("tableColumns"));
            tableView.refresh();
            tableView.setItems(FXCollections.observableArrayList(data.get("tableItems")));
            tableView.refresh();
        } catch (Exception e) {
            warningLabel.setText("File was not loaded correctly");
        }
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
