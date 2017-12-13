package tools;

import javafx.collections.ObservableList;

import java.util.LinkedHashMap;

public class ColumnHandler{
    private static LinkedHashMap<String, Boolean> logEntryColumns;
    private static LinkedHashMap<String, Boolean> userTotalColumns;
    private static ObservableList<String> formatComboBoxData;
    private static ObservableList<String> colorComboBoxData;

    private ColumnHandler() {
    }

    static  {
        logEntryColumns = new LinkedHashMap<>();
        logEntryColumns.put("Time", true);
        logEntryColumns.put("User", true);
        logEntryColumns.put("Pages", true);
        logEntryColumns.put("Copies", true);
        logEntryColumns.put("Printer", true);
        logEntryColumns.put("DocumentName", true);
        logEntryColumns.put("PaperSize", true);
        logEntryColumns.put("Grayscale", true);
        logEntryColumns.put("FileSize", true);
        logEntryColumns.put("Client", true);

        userTotalColumns = new LinkedHashMap<>();
        userTotalColumns.put("User", true);
        userTotalColumns.put("Total Pages", true);
        userTotalColumns.put("Printer", true);
        userTotalColumns.put("PaperSize", true);
        userTotalColumns.put("Grayscale", true);
        userTotalColumns.put("Client", true);
    }

    public static LinkedHashMap<String, Boolean> getLogEntryColumns() {
        return logEntryColumns;
    }

    public static LinkedHashMap<String, Boolean> getUserTotalColumns() {
        return userTotalColumns;
    }
}
