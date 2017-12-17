package util;

import db.DatabaseHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ViewDataHandler {
    private static final LinkedHashMap<String, Boolean> logEntryColumns = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Boolean> userTotalColumns = new LinkedHashMap<>();
    private static final ObservableList<String> colorComboBoxData = FXCollections.observableArrayList();

    private ViewDataHandler() {
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

    public static void fillComboBox(ComboBox comboBox, String sqlFieldName) {
        new Thread(() -> {
            Timer printTimer = new Timer();
            printTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    boolean run = true;
                    try {
                        StringBuilder queryText = new StringBuilder();
                        queryText.append("SELECT distinct ").append(sqlFieldName).append(" FROM logs order by ").append(sqlFieldName);
                        ResultSet rs = DatabaseHandler.getInstance().searchInDB(queryText.toString());
                        ObservableList<String> comboBoxData = FXCollections.observableArrayList();
                        while (rs.next())   comboBoxData.add(rs.getString(1));
                        if (!comboBoxData.get(0).equals("")) comboBoxData.add(0, "");
                        Platform.runLater(() -> comboBox.setItems(comboBoxData));
                        run = false;
                    } catch (SQLException ignore) {
                        // ignore
                    }
                    if (run) printTimer.schedule(this, 3000);
                }
            }, 50);
        }).start();
    }

    public static void handleStatusCircle(Circle circle) {

        Thread handleStatusThread = new Thread(() -> {
            Timer printTimer = new Timer();
            printTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (DatabaseHandler.getInstance().isConnected()) Platform.runLater(() -> circle.setFill(Color.GREEN));
                    else Platform.runLater(() -> circle.setFill(Color.RED));
                }
            }, 0, 3000);
        });
        handleStatusThread.setDaemon(true);
        handleStatusThread.start();
    }
}
