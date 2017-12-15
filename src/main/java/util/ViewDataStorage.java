package util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;

public class ViewDataStorage{
    private static final LinkedHashMap<String, Boolean> logEntryColumns = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Boolean> userTotalColumns = new LinkedHashMap<>();
    /*private static final ObservableList<String> formatComboBoxData = FXCollections.observableArrayList();*/
    private static final ObservableList<String> colorComboBoxData = FXCollections.observableArrayList();

    private ViewDataStorage() {
    }

    static  {
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

        userTotalColumns.put("User", true);
        userTotalColumns.put("Total Pages", true);
        userTotalColumns.put("Printer", true);
        userTotalColumns.put("PaperSize", true);
        userTotalColumns.put("Grayscale", true);
        userTotalColumns.put("Client", true);

        colorComboBoxData.add("");
        colorComboBoxData.add("GRAYSCALE");
        colorComboBoxData.add("NOT GRAYSCALE");
    }

    public static LinkedHashMap<String, Boolean> getLogEntryColumns() {
        return logEntryColumns;
    }

    public static LinkedHashMap<String, Boolean> getUserTotalColumns() {
        return userTotalColumns;
    }

    public static ObservableList<String> getColorComboBoxData() {
        return colorComboBoxData;
    }
}
