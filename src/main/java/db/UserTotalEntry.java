package db;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserTotalEntry {

    private String user;
    private int totalPages;
    private String printer;
    private String paperSize;
    private String grayscale;
    private String client;

    public UserTotalEntry(String user, int totalPages, String printer, String paperSize, String grayscale, String client) {
        this.user = user;
        this.totalPages = totalPages;
        this.printer = printer;
        this.paperSize = paperSize;
        this.grayscale = grayscale;
        this.client = client;
    }

    @EntryDataGetter(name = "User")
    public String getUser() {
        return user;
    }

    @EntryDataGetter(name = "Total Pages")
    public int getTotalPages() {
        return totalPages;
    }

    @EntryDataGetter(name = "Printer")
    public String getPrinter() {
        return printer;
    }

    @EntryDataGetter(name = "Color")
    public String getGrayscale() {
        return grayscale;
    }

    @EntryDataGetter(name = "Paper Size")
    public String getPaperSize() {
        return paperSize;
    }

    @EntryDataGetter(name = "Client")
    public String getClient() {
        return client;
    }
}
