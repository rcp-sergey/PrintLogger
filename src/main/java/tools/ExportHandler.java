package tools;

import controllers.MainViewController;
import db.EntryDataMethod;
import javafx.collections.ObservableList;
import db.LogEntry;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ExportHandler {
/*    public static void handleTableViewExport(ObservableList<LogEntry> table) {
        File excelFile = new File("Output.xls");
        try (FileOutputStream fos = new FileOutputStream(excelFile)) {
            HSSFWorkbook excelBook = new HSSFWorkbook();
            HSSFSheet sheet = excelBook.createSheet("PL sheet 1");
            Class logEntryClass = LogEntry.class;
            Method[] methods = logEntryClass.getMethods();
            ArrayList<Method> entryDataMethods = new ArrayList<>();
            for (Method m: methods) {
                if (m.getAnnotation(EntryDataMethod.class) != null) entryDataMethods.add(m);
            }
            try {
                for (int i = 0; i < table.size(); i++) {
                    HSSFRow row = sheet.createRow(i + 1);
                    for (int j = 0; j < entryDataMethods.size(); j++) {
                        row.createCell(j + 1).setCellValue((entryDataMethods.get(j).invoke(table.get(i))).toString());
                    }
                }
                excelBook.write(fos);
                excelBook.close();
                fos.close();
                System.out.println("file saved");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    public static void exportToExcel (TableView table) {
        FileOutputStream fos = null;
        try {
            File excelFile = handleSaveFile(table);
            ObservableList<LogEntry> tableArray = table.getItems();
            fos = new FileOutputStream(excelFile);
            HSSFWorkbook excelBook = new HSSFWorkbook();
            HSSFCellStyle style = excelBook.createCellStyle();
            style.setWrapText(true);
            HSSFSheet sheet = excelBook.createSheet("PL sheet 1");
            Class logEntryClass = LogEntry.class;
            Method[] methods = logEntryClass.getMethods();
            ArrayList<Method> entryDataMethods = new ArrayList<>();
            for (Method m: methods) {
                if (m.getAnnotation(EntryDataMethod.class) != null) entryDataMethods.add(m);
            }
            try {
                for (int i = 0; i < tableArray.size(); i++) {
                    HSSFRow row = sheet.createRow(i + 1);
                    for (int j = 0; j < entryDataMethods.size(); j++) {
                        row.createCell(j + 1).setCellValue((entryDataMethods.get(j).invoke(tableArray.get(i))).toString());

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

    private static File handleSaveFile(TableView table) {
        FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle("Save File");//Заголовок диалога
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Excel files (*.xls)", "*.xls");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("C:\\PrintLoggerFiles"));
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy");
        fileChooser.setInitialFileName("PrintLoggerTable_" + formatForDateNow.format(new Date()));
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());//Указываем текущую сцену CodeNote.mainStage
        if (file != null) {
            //Save
            System.out.println("Процесс открытия файла");
        }
        return file;
    }

}
