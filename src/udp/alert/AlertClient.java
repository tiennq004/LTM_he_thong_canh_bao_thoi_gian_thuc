package udp.alert;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class AlertClient extends JFrame {
    private JTable alertTable;
    private DefaultTableModel tableModel;
    private DatagramSocket socket;
    private final String SERVER_IP = "127.0.0.1"; // đổi sang IP thật khi chạy LAN
    private final int SERVER_PORT = 5000;
    private int floorNumber;

    // Map lưu gói PART[x/y] tạm thời để ghép
    private Map<String, StringBuilder[]> pendingParts = new HashMap<>();

    public AlertClient(int floorNumber) {
        this.floorNumber = floorNumber;

        setTitle("Client nhận cảnh báo - Tầng " + floorNumber);
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Bảng hiển thị cảnh báo ---
        tableModel = new DefaultTableModel(new String[]{"Loại", "Mức độ", "Nội dung", "Thời gian"}, 0);
        alertTable = new JTable(tableModel);

        alertTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String level = (String) table.getValueAt(row, 1);
                if ("Thường".equalsIgnoreCase(level)) {
                    c.setBackground(Color.YELLOW);
                } else if ("Trung bình".equalsIgnoreCase(level)) {
                    c.setBackground(Color.ORANGE);
                } else if ("Cao".equalsIgnoreCase(level)) {
                    c.setBackground(Color.RED);
                } else {
                    c.setBackground(Color.WHITE);
                }
                if (isSelected) c.setBackground(c.getBackground().darker());
                return c;
            }
        });

        add(new JScrollPane(alertTable), BorderLayout.CENTER);

        try {
            socket = new DatagramSocket(); // random port
            registerToServer();
            new Thread(this::listen).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerToServer() throws IOException {
        String registerMsg = "REGISTER|" + floorNumber;
        byte[] data = registerMsg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName(SERVER_IP), SERVER_PORT);
        socket.send(packet);
        System.out.println("Tầng " + floorNumber + " đã gửi REGISTER tới server");
    }

    private void listen() {
        byte[] buffer = new byte[8192];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength()).trim();

                // --- Xử lý PART[x/y] ---
                if (msg.startsWith("PART[")) {
                    int idxBracket = msg.indexOf("]|");
                    if (idxBracket != -1) {
                        String partInfo = msg.substring(5, idxBracket); // x/y
                        String rest = msg.substring(idxBracket + 2); // type|level|chunk|time

                        String[] xy = partInfo.split("/");
                        int partNum = Integer.parseInt(xy[0]) - 1;
                        int totalParts = Integer.parseInt(xy[1]);

                        String key = rest.substring(rest.lastIndexOf("|") + 1); // time làm key tạm
                        String[] contentParts = rest.split("\\|", 4);
                        if (contentParts.length == 4) {
                            String type = contentParts[0];
                            String level = contentParts[1];
                            String chunk = contentParts[2];
                            String time = contentParts[3];

                            pendingParts.putIfAbsent(time, new StringBuilder[totalParts]);
                            StringBuilder[] arr = pendingParts.get(time);
                            arr[partNum] = new StringBuilder(chunk);

                            // Kiểm tra đủ gói
                            boolean complete = true;
                            StringBuilder fullMsg = new StringBuilder();
                            for (StringBuilder sb : arr) {
                                if (sb == null) {
                                    complete = false;
                                    break;
                                } else {
                                    fullMsg.append(sb);
                                }
                            }
                            if (complete) {
                                String finalMsg = fullMsg.toString();
                                SwingUtilities.invokeLater(() -> {
                                    tableModel.addRow(new Object[]{type, level, finalMsg, time});
                                    showPopup(type, level, finalMsg);
                                    saveLog(type + "|" + level + "|" + finalMsg + "|" + time);
                                });
                                pendingParts.remove(time);
                            }
                        }
                    }
                } else {
                    // Trường hợp không phải PART
                    String[] parts = msg.split("\\|");
                    if (parts.length == 4) {
                        String type = parts[0];
                        String level = parts[1];
                        String content = parts[2];
                        String time = parts[3];

                        SwingUtilities.invokeLater(() -> {
                            tableModel.addRow(new Object[]{type, level, content, time});
                            showPopup(type, level, content);
                            saveLog(msg);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            tableModel.addRow(new Object[]{"Khác", "Không rõ", msg, ""});
                            showPopup("Khác", "Không rõ", msg);
                            saveLog("Khác|Không rõ|" + msg + "|");
                        });
                    }
                }

                sendAck(packet.getAddress(), packet.getPort());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAck(InetAddress serverAddr, int serverPort) {
        try {
            String ackMsg = "ACK|Floor " + floorNumber;
            byte[] data = ackMsg.getBytes();
            DatagramPacket ackPacket = new DatagramPacket(data, data.length, serverAddr, serverPort);
            socket.send(ackPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPopup(String type, String level, String content) {
        JOptionPane.showMessageDialog(this,
                "⚠ Cảnh báo tại tầng " + floorNumber +
                        "\nLoại: " + type +
                        "\nMức độ: " + level +
                        "\nNội dung: " + content,
                "CẢNH BÁO MỚI - Tầng " + floorNumber,
                JOptionPane.WARNING_MESSAGE);
        playSound(level);
    }

    private void playSound(String level) {
        try {
            String soundFile;
            switch (level.toLowerCase()) {
                case "cao":
                    soundFile = "danger.wav"; break;
                case "trung bình":
                    soundFile = "urgent.wav"; break;
                default:
                    soundFile = "normal.wav"; break;
            }

            File file = new File(soundFile);
            if (!file.exists()) return;

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLog(String alert) {
        try (FileWriter writer = new FileWriter("client_log_floor" + floorNumber + ".csv", true)) {
            writer.write(alert.replace("|", ",") + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(null,
                "Nhập số tầng cho client này:", "Đăng ký tầng", JOptionPane.QUESTION_MESSAGE);
        int floorNum;
        try {
            floorNum = Integer.parseInt(input.trim());
        } catch (Exception e) {
            floorNum = (int) (Math.random() * 100 + 1);
        }

        int finalFloorNum = floorNum;
        SwingUtilities.invokeLater(() -> new AlertClient(finalFloorNum).setVisible(true));
    }
}
