package db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry implements Serializable {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private LocalDateTime date;
    private String user;
    private int pages;
    private int copies;
    private String printer;
    private String documentName;
    private String paperSize;
    private String grayscale;
    private String filesize;
    private String client;

    public LogEntry(Timestamp date, String user, int pages, int copies, String printer, String documentName, String paperSize, String grayscale, String fileSize, String client) {
        this.date = date.toLocalDateTime();
        this.user = user;
        this.pages = pages;
        this.copies = copies;
        this.printer = printer;
        this.documentName = documentName;
        this.paperSize = paperSize;
        this.grayscale = grayscale;
        this.filesize = fileSize;
        this.client = client;
    }

    @EntryDataGetter(name = "Time")
    public String getDate() {
        return date.format(DATE_TIME_FORMAT);
    }

    @EntryDataGetter(name = "User")
    public String getUser() {
        return user;
    }

    @EntryDataGetter(name = "Pages")
    public int getPages() {
        return pages;
    }

    @EntryDataGetter(name = "Copies")
    public int getCopies() {
        return copies;
    }

    @EntryDataGetter(name = "Printer")
    public String getPrinter() {
        return printer;
    }

    @EntryDataGetter(name = "Document Name")
    public String getDocumentName() {
        return documentName;
    }

    @EntryDataGetter(name = "Paper Size")
    public String getPaperSize() {
        return paperSize;
    }

    @EntryDataGetter(name = "Color")
    public String getGrayscale() {
        return grayscale;
    }

    @EntryDataGetter(name = "File Size")
    public String getFileSize() {
        return filesize;
    }

    @EntryDataGetter(name = "Client")
    public String getClient() {
        return client;
    }

    @Override
    public String toString() {
        return date + " " + user + " " + pages + " " + copies + " " + printer + " \"" + documentName + "\" " + paperSize + " " + grayscale + " " + filesize + " " + client;
    }
}