package apartment;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlertMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum AlertType {
        FIRE("Cháy nổ", "#FF0000"),
        POWER_WATER("Sự cố điện/nước", "#FFA500"),
        ELEVATOR("Thang máy hỏng", "#FFFF00"),
        EMERGENCY("Khẩn cấp khác", "#FF00FF");
        
        private String name;
        private String color;
        
        AlertType(String name, String color) {
            this.name = name;
            this.color = color;
        }
        
        public String getName() { return name; }
        public String getColor() { return color; }
    }
    
    private String messageId;
    private AlertType type;
    private String content;
    private int[] targetFloors;
    private boolean allFloors;
    private long timestamp;
    private String formattedTime;
    
    public AlertMessage(AlertType type, String content, int[] targetFloors, boolean allFloors) {
        this.messageId = System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        this.type = type;
        this.content = content;
        this.targetFloors = targetFloors;
        this.allFloors = allFloors;
        this.timestamp = System.currentTimeMillis();
        this.formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }
    
    public String getMessageId() { return messageId; }
    public AlertType getType() { return type; }
    public String getContent() { return content; }
    public int[] getTargetFloors() { return targetFloors; }
    public boolean isAllFloors() { return allFloors; }
    public long getTimestamp() { return timestamp; }
    public String getFormattedTime() { return formattedTime; }
    
    public boolean isForFloor(int floor) {
        if (allFloors) return true;
        for (int f : targetFloors) {
            if (f == floor) return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s (Tầng: %s)", 
            formattedTime, type.getName(), content, 
            allFloors ? "Toàn bộ" : java.util.Arrays.toString(targetFloors));
    }
}
