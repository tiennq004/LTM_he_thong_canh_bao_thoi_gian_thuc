package apartment;

import java.net.InetAddress;

public class ClientInfo {
    private String clientId;
    private int floor;
    private String apartment;
    private InetAddress address;
    private int port;
    private long lastSeen;
    private boolean acknowledged;
    private boolean online; // ✅ thêm biến trạng thái online

    public ClientInfo(String clientId, int floor, String apartment, InetAddress address, int port) {
        this.clientId = clientId;
        this.floor = floor;
        this.apartment = apartment;
        this.address = address;
        this.port = port;
        this.lastSeen = System.currentTimeMillis();
        this.acknowledged = false;
        this.online = true; // ✅ mặc định khi đăng ký là online
    }

    public String getClientId() { return clientId; }
    public int getFloor() { return floor; }
    public String getApartment() { return apartment; }
    public InetAddress getAddress() { return address; }
    public int getPort() { return port; }
    public long getLastSeen() { return lastSeen; }
    public boolean isAcknowledged() { return acknowledged; }

    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    // ✅ thêm getter & setter cho online
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    @Override
    public String toString() {
        return String.format("Tầng %d - Căn hộ %s [%s]", 
            floor, apartment, online ? "Online" : "Offline");
    }
}
