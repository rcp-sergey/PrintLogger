package db;

import java.util.Date;

public class LogEntry {
    private Date date;
    private String user;
    private int pages;
    private int copies;
    private String printer;
    private String documentName;
    private String paperSize;
    private String grayscale;
    private String filesize;
    private String client;

    public LogEntry(Date date, String user, int pages, int copies, String printer, String documentName, String paperSize, String grayscale, String fileSize, String client) {
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    @EntryDataMethod
    public String getUser() {
        return user;
    }

    @EntryDataMethod
    public int getPages() {
        return pages;
    }

    @EntryDataMethod
    public int getCopies() {
        return copies;
    }

    @EntryDataMethod
    public String getPrinter() {
        return printer;
    }

    @EntryDataMethod
    public String getDocumentName() {
        return documentName;
    }

    @EntryDataMethod
    public String getPaperSize() {
        return paperSize;
    }

    public String getGrayscale() {
        return grayscale;
    }

    public String getFileSize() {
        return filesize;
    }

    public String getClient() {
        return client;
    }

    @Override
    public String toString() {
        return date + " " + user + " " + pages + " " + copies + " " + printer + " \"" + documentName + "\" " + paperSize + " " + grayscale + " " + filesize + " " + client;
    }
}