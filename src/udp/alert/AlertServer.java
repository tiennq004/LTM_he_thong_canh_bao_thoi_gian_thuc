package udp.alert;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AlertServer {
    private static final int PORT = 5555;        // Server lắng nghe
    private static final int INTERVAL_MS = 3000; // Gửi mỗi 3 giây

    private static final Set<ClientInfo> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("🚀 Server chạy trên port " + PORT);

            // Luồng nhận đăng ký từ client
            new Thread(() -> listenForClients(socket)).start();

            AtomicInteger seq = new AtomicInteger(0);
            int idCounter = 1;

            while (true) {
                if (clients.isEmpty()) {
                    System.out.println("⚠ Không có client đăng ký, bỏ qua alert " + idCounter);
                    idCounter++;
                } else {
                    String id = String.format("ALERT-%04d", idCounter++);
                    int s = seq.incrementAndGet();
                    long ts = Instant.now().toEpochMilli();
                    String severity = (s % 5 == 0) ? "CRITICAL" : (s % 3 == 0) ? "WARNING" : "INFO";
                    String message = "Cảnh báo số " + s + " - Thời gian: " + ts;

                    String alert = String.join("|", id, String.valueOf(s), String.valueOf(ts), severity, message);
                    byte[] data = alert.getBytes(StandardCharsets.UTF_8);

                    synchronized (clients) {
                        for (ClientInfo client : clients) {
                            DatagramPacket packet = new DatagramPacket(data, data.length, client.address, client.port);
                            socket.send(packet);
                        }
                    }
                    System.out.println("📢 Đã gửi alert tới " + clients.size() + " client: " + alert);
                }
                Thread.sleep(INTERVAL_MS);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi Server: " + e.getMessage());
        }
    }

    private static void listenForClients(DatagramSocket socket) {
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                if (msg.equalsIgnoreCase("REGISTER")) {
                    ClientInfo client = new ClientInfo(packet.getAddress(), packet.getPort());
                    clients.add(client);
                    System.out.println("✅ Client đăng ký: " + client);
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi nhận đăng ký: " + e.getMessage());
            }
        }
    }

    private static class ClientInfo {
        InetAddress address;
        int port;

        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientInfo)) return false;
            ClientInfo c = (ClientInfo) o;
            return port == c.port && address.equals(c.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, port);
        }

        @Override
        public String toString() {
            return address.getHostAddress() + ":" + port;
        }
    }
}
