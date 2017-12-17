package controllers;

import db.UserTotalEntry;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import db.DatabaseHandler;
import db.LogEntry;
import util.ViewDataHandler;
import util.ExportHandler;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
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
    private ComboBox formatComboBox;
    @FXML
    private ComboBox colorComboBox;
    @FXML
    private MenuItem exportToExcelItem;
    @FXML
    private CheckBox totalCheckBox;
    @FXML
    private ComboBox pcComboBox;
    @FXML
    private TextField pagesFromTextField;
    @FXML
    private TextField pagesToTextField;
    @FXML
    private TextField documentTextField;
    @FXML
    private Circle statusCircle;
    private final DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
    private ObservableList data;

    public void startImport() {
        warningLabel.setText("");
        try {
            String filePath = filePathField.getText();
            databaseHandler.readCSV(filePath);
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

    public void copySelectedToClipboard() {
        ObservableList<TablePosition> posList = tableView.getSelectionModel().getSelectedCells();
        StringBuilder clipboardString = new StringBuilder();
        int oldRow = -1;
        for (TablePosition p : posList) {
            int r = p.getRow();
            int c = p.getColumn();
            TableColumn tableColumn = (TableColumn) tableView.getColumns().get(c);
            ObservableValue observableValue = tableColumn.getCellObservableValue(r);
            String cellText = observableValue.getValue().toString();
            if (oldRow == r)
                clipboardString.append('\t');
            else if (oldRow != -1)
                clipboardString.append('\n');
            /*if (!cellText.equals("")) {
                clipboardString.append(cellText);
                clipboardString.append(" | ");
            }*/
            clipboardString.append(cellText);
            oldRow = r;
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
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
                String query = constructQuery();

                // execute query and get ResultSet and it's MetaData
                ResultSet rs = databaseHandler.searchInDB(query);
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
                Object[] values = null;
                if (totalCheckBox != null && totalCheckBox.isSelected()) {
                    values = new Object[ViewDataHandler.getUserTotalColumns().size()];
                    while(rs.next()){
                        for (int i = 0; i < usefulColumns.length; i++) {
                            values[usefulColumns[i]] = rs.getObject(i + 1);
                        }
                        data.add(new UserTotalEntry((String) values[0], ((BigDecimal) values[1]).intValueExact(), (String) values[2], (String) values[3], (String) values[4], (String) values[5]));
                    }
                } else {
                    values = new Object[ViewDataHandler.getLogEntryColumns().size()];
                    while(rs.next()){
                        for (int i = 0; i < usefulColumns.length; i++) {
                            values[usefulColumns[i]] = rs.getObject(i + 1);
                        }
                        data.add(new LogEntry((Timestamp) values[0], (String) values[1], (int) values[2], (int) values[3], (String) values[4], (String) values[5], (String) values[6], (String) values[7], (String) values[8], (String) values[9]));
                    }
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
                Platform.runLater(()-> warningLabel.setText("SQL state: " + e.getSQLState()));
                System.out.println(e);
            } finally {
                Platform.runLater(() -> lockUI(false));
            }
        });
        thread.start();
    }

    private int getColumnCount(String columnName) {

        if (totalCheckBox != null && totalCheckBox.isSelected()) {
            if (columnName.equals("User")) return 0;
            else if (columnName.equals("Total Pages")) return 1;
            else if (columnName.equals("Printer")) return 2;
            else if (columnName.equals("PaperSize")) return 3;
            else if (columnName.equals("Grayscale")) return 4;
            else if (columnName.equals("Client")) return 5;
        } else {
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
        }
        return 0;
    }

    private void clearTable() { // TODO remove unnecessary lines if they exist AND boolean arg to ask permission
        tableView.getItems().removeAll();
        tableView.getItems().clear();
        tableView.refresh();
        tableView.getColumns().removeAll();
        tableView.getColumns().clear();
        tableView.refresh();
    }

    public void clearFields() {
        userNameField.clear();
        datePickerFrom.getEditor().clear();
        datePickerTo.getEditor().clear();
        printerComboBox.getEditor().clear();
        colorComboBox.getEditor().clear();
        formatComboBox.getEditor().clear();
        pagesFromTextField.clear();
        pagesToTextField.clear();
        pcComboBox.getEditor().clear();
        documentTextField.clear();
        totalCheckBox.setSelected(false);
    }

    public void setUserTotalMode() {
    }

    private void createColumns(ArrayList<String> columnNames) throws SQLException {
        for(int i = 0 ; i < columnNames.size(); i++){
            String colName = columnNames.get(i);
            if (colName.equals("Time")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Time");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.08));
                column.setCellValueFactory(new PropertyValueFactory<>("date"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("User")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("User");
                column.setCellValueFactory(new PropertyValueFactory<>("user"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Pages")) {
                TableColumn<LogEntry, Integer> column = new TableColumn<>("Pages");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));
                column.maxWidthProperty().bind(tableView.widthProperty().multiply(0.10));
                column.setCellValueFactory(new PropertyValueFactory<>("pages"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Copies")) {
                TableColumn<LogEntry, Integer> column = new TableColumn<>("Copies");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));
                column.maxWidthProperty().bind(tableView.widthProperty().multiply(0.10));
                column.setCellValueFactory(new PropertyValueFactory<>("copies"));
                Platform.runLater(() -> tableView.getColumns().addAll(column));
            } else if (colName.equals("Printer")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Printer");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
                column.setCellValueFactory(new PropertyValueFactory<>("printer"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("DocumentName") || colName.equals("Document Name") ) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Document Name");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
                column.setCellValueFactory(new PropertyValueFactory<>("documentName"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("PaperSize") || colName.equals("Paper Size")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Paper Size");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));
                column.maxWidthProperty().bind(tableView.widthProperty().multiply(0.10));
                column.setCellValueFactory(new PropertyValueFactory<>("paperSize"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Grayscale") || colName.equals("Color")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Color");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.08));
                column.setCellValueFactory(new PropertyValueFactory<>("grayscale"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("FileSize") || colName.equals("File Size")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("File Size");
                column.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("PC")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("PC");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
                column.setCellValueFactory(new PropertyValueFactory<>("client"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            } else if (colName.equals("Total Pages")) {
                TableColumn<LogEntry, String> column = new TableColumn<>("Total Pages");
                column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
                column.setCellValueFactory(new PropertyValueFactory<>("totalPages"));
                Platform.runLater(() -> tableView.getColumns().add(column));
            }
        }
        Platform.runLater(() -> tableView.refresh());
    }

    private String constructQuery() throws ParseException {
        StringBuffer sb = new StringBuffer();
        LinkedHashMap<String, Boolean> columns = null;
        int countWhere = 0;
        int countColumns = 0;
        //
        if (totalCheckBox != null && totalCheckBox.isSelected()) {
            columns = ViewDataHandler.getUserTotalColumns();
            String queryField;
            for (Map.Entry<String, Boolean> entry: columns.entrySet()) {
                if (entry.getValue()) {
                    queryField = entry.getKey();
                    if (queryField.equals("Printer") && printerComboBox.getEditor().getText().equals(""))   continue;
                    else if (queryField.equals("PaperSize") && formatComboBox.getEditor().getText().equals("")) continue;
                    else if (queryField.equals("Grayscale") && colorComboBox.getEditor().getText().equals("")) continue;
                    else if (queryField.equals("Client") && colorComboBox.getEditor().getText().equals("")) continue;
                    if (countColumns == 0) sb.append("SELECT ");
                    else sb.append(", ");
                    if (queryField.equals("Total Pages")) queryField = "SUM(Pages * Copies) as 'Total Pages'";
                    sb.append(queryField);
                    countColumns++;
                }
            }
        } else {
            columns = ViewDataHandler.getLogEntryColumns();
            String queryField;
            for (Map.Entry<String, Boolean> entry: columns.entrySet()) {
                if (entry.getValue()) {
                    if (countColumns == 0) sb.append("SELECT ");
                    else sb.append(", ");
                    queryField = entry.getKey();
                    sb.append(queryField);
                    countColumns++;
                }
            }
        }
        sb.append(" FROM logs");
        if (userNameField.getText() != null && !userNameField.getText().equals("")) {
            sb.append(" WHERE User LIKE '").append(userNameField.getText()).append("'");
            countWhere++;
        }
        if (datePickerFrom.getValue() != null) {
            if (countWhere == 0)    sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Time >= '").append(getTimeFromText()).append("'");
            countWhere++;
        }
        if (datePickerTo.getValue() != null) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Time <= '").append(getTimeToText()).append("'");
            countWhere++;
        }
        if (!pagesFromTextField.getText().equals("")) {
            if (countWhere == 0)    sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Pages >= '").append(pagesFromTextField.getText()).append("'");
            countWhere++;
        }
        if (!pagesToTextField.getText().equals("")) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Pages <= '").append(pagesToTextField.getText()).append("'");
            countWhere++;
        }
        if (printerComboBox != null && !printerComboBox.getEditor().getText().equals("")) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Printer LIKE '%").append(printerComboBox.getEditor().getText()).append("%'");
            countWhere++;
        }
        if (formatComboBox != null && !formatComboBox.getEditor().getText().equals("")) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" PaperSize = '").append(formatComboBox.getEditor().getText()).append("'");
            countWhere++;
        }
        if (colorComboBox != null && !colorComboBox.getEditor().getText().equals("")) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Grayscale = '").append(colorComboBox.getEditor().getText()).append("'");
            countWhere++;
        }
        if (pcComboBox != null && !pcComboBox.getEditor().getText().equals("")) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" Client LIKE '").append(pcComboBox.getEditor().getText()).append("'");
            countWhere++;
        }
        if (documentTextField != null && !documentTextField.getText().equals("")) {
            if (countWhere == 0) sb.append(" WHERE");
            else sb.append(" AND");
            sb.append(" DocumentName LIKE '").append(documentTextField.getText()).append("'");
            countWhere++;
        }
        if (totalCheckBox.isSelected()) {
            if (pagesToTextField.getText().equals("")) {
                if (countWhere == 0) sb.append(" WHERE");
                else sb.append(" AND");
                sb.append(" Pages <= '" + 5000 + "'");
            }
            sb.append(" GROUP BY User order by SUM(Pages * Copies) desc");
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
        ViewDataHandler.handleStatusCircle(statusCircle);
        colorComboBox.setItems(ViewDataHandler.getColorComboBoxData());
        ViewDataHandler.fillComboBox(printerComboBox, "Printer");
        ViewDataHandler.fillComboBox(formatComboBox, "PaperSize");
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