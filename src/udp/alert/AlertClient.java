package udp.alert;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class AlertClient extends JFrame {
    private static final String SERVER_IP = "127.0.0.1"; // Địa chỉ server
    private static final int SERVER_PORT = 5555;         // Cổng server

    private DefaultTableModel tableModel;

    public AlertClient() {
        setTitle("📡 Hệ thống cảnh báo - Client");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"Thời gian", "Mức độ", "ID", "Nội dung"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        // Đổi màu theo mức độ cảnh báo
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String severity = (String) table.getValueAt(row, 1);
                if ("CRITICAL".equalsIgnoreCase(severity)) {
                    c.setBackground(Color.RED);
                    c.setForeground(Color.WHITE);
                } else if ("WARNING".equalsIgnoreCase(severity)) {
                    c.setBackground(Color.YELLOW);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.GREEN);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        new Thread(this::startClient).start();
    }

    private void startClient() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            // Gửi đăng ký
            byte[] regData = "REGISTER".getBytes(StandardCharsets.UTF_8);
            DatagramPacket regPacket = new DatagramPacket(regData, regData.length, serverAddr, SERVER_PORT);
            socket.send(regPacket);
            System.out.println("📨 Đã gửi đăng ký tới server");

            byte[] buffer = new byte[2048];

            // Lắng nghe cảnh báo
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String alert = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).trim();
                String[] parts = alert.split("\\|", 5);

                if (parts.length == 5) {
                    String id = parts[0];
                    String seq = parts[1];
                    String ts = parts[2];
                    String severity = parts[3];
                    String message = parts[4];

                    String timeStr;
                    try {
                        timeStr = Instant.ofEpochMilli(Long.parseLong(ts)).toString();
                    } catch (Exception e) {
                        timeStr = ts;
                    }

                    // In ra console để dễ debug
                    System.out.printf("📥 Nhận: [%s] %s - %s(seq=%s): %s%n",
                            timeStr, severity, id, seq, message);

                    String finalTimeStr = timeStr;
                    SwingUtilities.invokeLater(() -> {
                        tableModel.addRow(new Object[]{finalTimeStr, severity, id + "(seq=" + seq + ")", message});
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi Client: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AlertClient().setVisible(true));
    }
}
