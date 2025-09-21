package udp.alert;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class AlertServer extends JFrame {
    private JTextArea logArea;
    private JComboBox<String> typeBox;
    private JComboBox<String> levelBox;
    private JTextField messageField;
    private JTextField timerField;
    private JCheckBox repeatCheck;
    private JTable clientTable, historyTable;
    private DefaultTableModel clientModel, historyModel;

    private DatagramSocket socket;
    private final int SERVER_PORT = 5000;
    private final List<ClientInfo> clients = new ArrayList<>();

    private ScheduledExecutorService scheduler;

    public AlertServer() throws SocketException {
        setTitle("🚨 Hệ thống cảnh báo thời gian thực - The Vesta");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Panel nhập thông tin ===
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        typeBox = new JComboBox<>(new String[]{"Cháy nổ", "Mất điện", "Bảo dưỡng điều hòa", "Kẹt xe", "Thang máy"});
        levelBox = new JComboBox<>(new String[]{"Thường", "Trung bình", "Cao"});
        messageField = new JTextField();
        timerField = new JTextField("");
        repeatCheck = new JCheckBox("⏳ Lặp lại cảnh báo");

        inputPanel.add(new JLabel("Loại cảnh báo:"));
        inputPanel.add(typeBox);
        inputPanel.add(new JLabel("Mức độ:"));
        inputPanel.add(levelBox);
        inputPanel.add(new JLabel("Nội dung:"));
        inputPanel.add(messageField);
        inputPanel.add(new JLabel("Hẹn giờ (giây):"));
        inputPanel.add(timerField);
        inputPanel.add(new JLabel("Tùy chọn:"));
        inputPanel.add(repeatCheck);

        JButton sendBtn = new JButton("🚀 Gửi ngay");
        sendBtn.addActionListener(this::sendAlertNow);
        JButton timerBtn = new JButton("⏱ Hẹn giờ gửi");
        timerBtn.addActionListener(this::scheduleAlert);
        JButton stopBtn = new JButton("⛔ Dừng gửi");
        stopBtn.addActionListener(e -> stopSchedule());

        inputPanel.add(sendBtn);
        inputPanel.add(timerBtn);
        inputPanel.add(stopBtn);

        // === Log ===
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        // === Danh sách client ===
        clientModel = new DefaultTableModel(new String[]{"IP", "Port", "Tầng", "ACK"}, 0);
        clientTable = new JTable(clientModel);

        // === Lịch sử cảnh báo ===
        historyModel = new DefaultTableModel(new String[]{"Thời gian", "Loại", "Mức độ", "Nội dung", "Gửi đến"}, 0);
        historyTable = new JTable(historyModel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📜 Log", new JScrollPane(logArea));
        tabs.addTab("👥 Client", new JScrollPane(clientTable));
        tabs.addTab("🕒 Lịch sử", new JScrollPane(historyTable));

        add(inputPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // Khởi tạo UDP socket
        socket = new DatagramSocket(SERVER_PORT);

        // Thread lắng nghe client
        new Thread(this::listenForClients).start();

        log("✅ Server đã khởi động trên cổng " + SERVER_PORT, Color.BLUE);
    }

    private void listenForClients() {
        while (true) {
            try {
                byte[] buffer = new byte[8192]; // buffer lớn cho gói dài
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength()).trim();

                if (msg.startsWith("REGISTER")) {
                    handleRegister(packet, msg);
                } else if (msg.startsWith("ACK")) {
                    handleAck(packet);
                } else {
                    log("⚠ Dữ liệu lạ từ " + packet.getAddress() + ":" + packet.getPort() + " -> " + msg, Color.ORANGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRegister(DatagramPacket packet, String msg) {
        String[] parts = msg.split("\\|");
        String ip = packet.getAddress().getHostAddress();
        int port = packet.getPort();
        String zone = (parts.length > 1) ? ("Tầng " + parts[1]) : "Chung";

        boolean exists = clients.stream().anyMatch(c -> c.ip.equals(ip) && c.port == port && c.zone.equals(zone));
        if (!exists) {
            clients.add(new ClientInfo(ip, port, zone));
            clientModel.addRow(new Object[]{ip, port, zone, "Chưa nhận"});
            log("👤 Client đăng ký: " + ip + ":" + port + " (" + zone + ")", Color.BLUE);
        }
    }

    private void handleAck(DatagramPacket packet) {
        String ip = packet.getAddress().getHostAddress();
        int port = packet.getPort();
        updateAck(ip, port);
        log("✅ ACK từ " + ip + ":" + port, Color.GREEN);
    }

    private void updateAck(String ip, int port) {
        for (int i = 0; i < clientModel.getRowCount(); i++) {
            if (clientModel.getValueAt(i, 0).equals(ip) && (int) clientModel.getValueAt(i, 1) == port) {
                clientModel.setValueAt("Đã nhận", i, 3);
            }
        }
    }

    // === Gửi cảnh báo ngay ===
    private void sendAlertNow(ActionEvent e) {
        sendAlert();
    }

    private void scheduleAlert(ActionEvent e) {
        try {
            int seconds = Integer.parseInt(timerField.getText().trim());
            boolean repeat = repeatCheck.isSelected();

            if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
            scheduler = Executors.newSingleThreadScheduledExecutor();

            if (repeat) {
                scheduler.scheduleAtFixedRate(this::sendAlert, seconds, seconds, TimeUnit.SECONDS);
                log("▶ Bắt đầu gửi cảnh báo định kỳ mỗi " + seconds + " giây.", Color.MAGENTA);
            } else {
                scheduler.schedule(this::sendAlert, seconds, TimeUnit.SECONDS);
                log("🕒 Hẹn giờ gửi cảnh báo sau " + seconds + " giây.", Color.MAGENTA);
            }
        } catch (Exception ex) {
            log("⚠ Sai định dạng thời gian!", Color.RED);
        }
    }

    private void stopSchedule() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            log("⛔ Dừng hẹn giờ gửi cảnh báo", Color.BLACK);
        } else {
            log("⚠ Không có lịch hẹn nào đang chạy", Color.GRAY);
        }
    }

    private void sendAlert() {
        try {
            String type = (String) typeBox.getSelectedItem();
            String level = (String) levelBox.getSelectedItem();
            String msg = messageField.getText().trim();
            if (msg.isEmpty()) {
                log("⚠ Vui lòng nhập nội dung cảnh báo!", Color.RED);
                return;
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // Chia đoạn văn dài thành nhiều gói PART[x/y]
            List<String> packets = splitMessage(type, level, msg, timestamp);

            int total = clients.size();
            int sent = 0;

            for (ClientInfo c : clients) {
                for (String part : packets) {
                    byte[] data = part.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length,
                            InetAddress.getByName(c.ip), c.port);
                    socket.send(packet);
                }
                sent++;
            }

            log("📢 Đã gửi cảnh báo đến " + sent + "/" + total + " client. Nội dung: " + msg,
                    getLevelColor(level));
            saveLog(timestamp, type, level, msg, sent + "/" + total);
            playSound(level);

        } catch (Exception ex) {
            log("❌ Lỗi gửi cảnh báo: " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        }
    }

    private List<String> splitMessage(String type, String level, String msg, String time) {
        List<String> parts = new ArrayList<>();
        int chunkSize = 8000; // UDP an toàn < 8KB
        int totalParts = (int) Math.ceil((double) msg.length() / chunkSize);

        for (int i = 0; i < totalParts; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, msg.length());
            String chunk = msg.substring(start, end);
            parts.add("PART[" + (i + 1) + "/" + totalParts + "]|" + type + "|" + level + "|" + chunk + "|" + time);
        }
        return parts;
    }

    private void log(String text, Color color) {
        logArea.setForeground(color);
        logArea.append(text + "\n");
    }

    private void saveLog(String time, String type, String level, String msg, String target) {
        historyModel.addRow(new Object[]{time, type, level, msg, target});
        try (FileWriter writer = new FileWriter("server_log.csv", true)) {
            writer.write(time + "," + type + "," + level + "," + msg + "," + target + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playSound(String level) {
        String fileName = switch (level) {
            case "Trung bình" -> "alert_orange.wav";
            case "Cao" -> "alert_red.wav";
            default -> "alert_yellow.wav";
        };
        try {
            File f = new File(fileName);
            if (!f.exists()) {
                log("⚠ Không tìm thấy file âm thanh: " + fileName, Color.GRAY);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            log("❌ Lỗi phát âm thanh: " + e.getMessage(), Color.RED);
        }
    }

    private Color getLevelColor(String level) {
        return switch (level) {
            case "Thường" -> Color.ORANGE;
            case "Trung bình" -> Color.RED;
            case "Cao" -> Color.MAGENTA;
            default -> Color.BLACK;
        };
    }

    static class ClientInfo {
        String ip;
        int port;
        String zone;

        ClientInfo(String ip, int port, String zone) {
            this.ip = ip;
            this.port = port;
            this.zone = zone;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new AlertServer().setVisible(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        });
    }
}
