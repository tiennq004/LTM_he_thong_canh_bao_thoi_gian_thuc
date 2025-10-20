package apartment;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CSVLogger {
    private String filename;
    private SimpleDateFormat dateFormat;
    
    public CSVLogger(String filename) {
        this.filename = filename;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initializeFile();
    }
    
    private void initializeFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, false))) {
            writer.println("Timestamp,Event Type,Alert Type,Floor,Apartment,Content,Status");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void log(String eventType, String alertType, String floor, 
                                  String apartment, String content, String status) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            String timestamp = dateFormat.format(new Date());
            writer.printf("%s,%s,%s,%s,%s,\"%s\",%s%n", 
                timestamp, eventType, alertType, floor, apartment, 
                content.replace("\"", "\"\""), status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void logAlert(AlertMessage alert, String floor, String status) {
        log("ALERT_SENT", alert.getType().getName(), floor, "N/A", 
            alert.getContent(), status);
    }
    
    public void logClientAction(String action, int floor, String apartment, String details) {
        log(action, "N/A", String.valueOf(floor), apartment, details, "OK");
    }
    
    public void logAcknowledgment(String alertId, int floor, String apartment) {
        log("ACKNOWLEDGMENT", "N/A", String.valueOf(floor), apartment, 
            "Alert ID: " + alertId, "RECEIVED");
    }
}
