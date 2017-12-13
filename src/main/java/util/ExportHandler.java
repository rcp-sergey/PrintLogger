package util;

import db.EntryDataGetter;
import db.UserTotalEntry;
import javafx.collections.ObservableList;
import db.LogEntry;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ExportHandler {

    public static void exportToExcel (TableView tableView) {
        FileOutputStream fos = null;
        try {
            File excelFile = handleSaveFile(tableView);
            fos = new FileOutputStream(excelFile);

            // create XSSF book
            XSSFWorkbook excelBook = new XSSFWorkbook();
            XSSFCellStyle style = excelBook.createCellStyle();
            style.setWrapText(true);
            XSSFSheet sheet = excelBook.createSheet("PL sheet 1");

            // get column names from tableView
            ArrayList<String> columnNames = getColumnNames(tableView.getColumns());

            // get getters for each column using reflection
            Class entryClass = null;
            if (tableView.getItems().get(0) instanceof LogEntry) {
                entryClass = LogEntry.class;
            } else if (tableView.getItems().get(0) instanceof UserTotalEntry) {
                entryClass = UserTotalEntry.class;
            }
            Method[] allLogEntryMethods = entryClass.getMethods();
            Method[] sortedGetters = new Method[columnNames.size()];

            int rowNum = 2;
            XSSFRow row = sheet.createRow(rowNum);

            for (int i = 0; i < columnNames.size(); i++) {
                row.createCell(i + 1).setCellValue(columnNames.get(i));
                for (Method m: allLogEntryMethods) {
                    if (m.getAnnotation(EntryDataGetter.class) != null && m.getAnnotation(EntryDataGetter.class).name().equals(columnNames.get(i))) sortedGetters[i] = m;
                }
            }
            rowNum++;

            ObservableList<LogEntry> tableArray = tableView.getItems();
            try {
                for (int i = 0; i < tableArray.size(); i++) {
                    row = sheet.createRow(i + rowNum);
                    for (int j = 0; j < sortedGetters.length; j++) {
                        row.createCell(j + 1).setCellValue((sortedGetters[j].invoke(tableArray.get(i))).toString());

                    }
                }
                excelBook.write(fos);
                excelBook.close();
                System.out.println("file saved");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Please, choose file");
        } finally {
            if (fos != null) try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ArrayList<String> getColumnNames(ObservableList<TableColumn> tableColumns) {
        ArrayList<String> columnNames = new ArrayList<>();
        for (int i = 0; i < tableColumns.size(); i++) columnNames.add(tableColumns.get(i).getText());
        return columnNames;
    }

    public static void serializeTable(TableView tableView) { // TODO auto create folder if it doesn't exist AND ask filename
        ObjectOutputStream oos = null;
        try {
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy_HHmmss");
            File file = new File("C:\\PrintLoggerFiles\\SavedStates\\Table_" + formatForDateNow.format(new Date()) + ".ser");
            oos = new ObjectOutputStream(new FileOutputStream(file));
            HashMap<String, ArrayList> dataToSave = new HashMap<>();
            ArrayList<String> columnNames = getColumnNames(tableView.getColumns());
            dataToSave.put("tableColumns", columnNames);
            dataToSave.put("tableItems", new ArrayList(tableView.getItems()));
            oos.writeObject(dataToSave);
        } catch (IOException e) {e.printStackTrace();
        } finally {
            if (oos != null) try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static HashMap<String, ArrayList> loadSerializedTable(Window stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Saved Table");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Table state files (*.ser)", "*.ser");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("C:\\PrintLoggerFiles\\SavedStates\\"));
        File file = fileChooser.showOpenDialog(stage);
        if (file !=null) {
            ObjectInputStream oos = null;
            try {
                oos = new ObjectInputStream(new FileInputStream(file));
                HashMap<String, ArrayList> data = (HashMap<String, ArrayList>) oos.readObject();
                return data;
            } catch (FileNotFoundException e) {
                System.out.println("File not found");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (oos != null) try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static File handleSaveFile(TableView tableView) {
        FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle("Save File");//Заголовок диалога
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("C:\\PrintLoggerFiles"));
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy");
        fileChooser.setInitialFileName("PrintLoggerTable_" + formatForDateNow.format(new Date()));
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());//Указываем текущую сцену CodeNote.mainStage
        if (file != null) {
            //Save
            System.out.println("Процесс открытия файла");
        }
        return file;
    }

}
