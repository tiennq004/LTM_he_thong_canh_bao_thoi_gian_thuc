package apartment;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Server extends JFrame {
    private static final int SERVER_PORT = 9876;
    private static final int CLIENT_PORT = 9877;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    
    private DatagramSocket socket;
    private CSVLogger logger;
    private Map<String, ClientInfo> clients;
    private List<AlertMessage> alertHistory;
    private Map<String, Set<String>> acknowledgments;
    
    private JPanel buildingMapPanel;
    private JTextArea logArea;
    private DefaultTableModel clientTableModel;
    private DefaultTableModel alertTableModel;
    private JLabel onlineCountLabel;
    private JLabel totalAlertsLabel;
    
    private Map<Integer, JPanel> floorPanels;

    // --- Các biến mới để quản lý timer / dừng cảnh báo ---
    private JButton stopAllAlertsButton;                         // nút dừng tất cả cảnh báo
    private final List<Timer> activeAlertTimers = Collections.synchronizedList(new ArrayList<>()); // danh sách timer đang chạy
    
    public Server() {
        clients = new HashMap<>();
        alertHistory = new ArrayList<>();
        acknowledgments = new HashMap<>();
        floorPanels = new HashMap<>();
        logger = new CSVLogger("server_log.csv");
        
        setTitle("Hệ thống Cảnh báo Chung cư - Server");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        createMenuBar();
        createTopPanel();
        createCenterPanel();
        createBottomPanel();
        
        startServer();
        startHeartbeatChecker();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Thoát");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("Giới thiệu");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(41, 128, 185));
        
        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftSection.setOpaque(false);
        ClockPanel clockPanel = new ClockPanel();
        leftSection.add(clockPanel);
        
        JPanel centerSection = new JPanel(new BorderLayout());
        centerSection.setOpaque(false);
        
        JLabel titleLabel = new JLabel("HỆ THỐNG CẢNH BÁO THỜI GIAN THỰC");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        onlineCountLabel = new JLabel("Clients: 0", SwingConstants.CENTER);
        onlineCountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        onlineCountLabel.setForeground(Color.WHITE);
        
        totalAlertsLabel = new JLabel("Cảnh báo: 0", SwingConstants.CENTER);
        totalAlertsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalAlertsLabel.setForeground(Color.WHITE);
        
        statsPanel.add(onlineCountLabel);
        statsPanel.add(totalAlertsLabel);
        
        centerSection.add(titleLabel, BorderLayout.NORTH);
        centerSection.add(statsPanel, BorderLayout.CENTER);
        
        topPanel.add(leftSection, BorderLayout.WEST);
        topPanel.add(centerSection, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        
        centerPanel.add(splitPane);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        JLabel mapLabel = new JLabel("Bản đồ Tòa nhà (40 tầng)");
        mapLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        buildingMapPanel = new JPanel(new GridLayout(8, 5, 5, 5));
        buildingMapPanel.setBackground(Color.WHITE);
        
        for (int i = 40; i >= 1; i--) {
            JPanel floorPanel = createFloorPanel(i);
            floorPanels.put(i, floorPanel);
            buildingMapPanel.add(floorPanel);
        }
        
        JScrollPane mapScrollPane = new JScrollPane(buildingMapPanel);
        mapScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        leftPanel.add(mapLabel, BorderLayout.NORTH);
        leftPanel.add(mapScrollPane, BorderLayout.CENTER);
        
        return leftPanel;
    }
    
    private JPanel createFloorPanel(int floor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(46, 204, 113));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        
        JLabel label = new JLabel("Tầng " + floor, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Color.WHITE);
        
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Gửi Cảnh báo", createAlertSendPanel());
        tabbedPane.addTab("Danh sách Client", createClientListPanel());
        tabbedPane.addTab("Lịch sử Cảnh báo", createAlertHistoryPanel());
        tabbedPane.addTab("Logs", createLogPanel());
        
        rightPanel.add(tabbedPane);
        
        return rightPanel;
    }
    
    private JPanel createAlertSendPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Loại cảnh báo:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<AlertMessage.AlertType> typeCombo = new JComboBox<>(AlertMessage.AlertType.values());
        typeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AlertMessage.AlertType) {
                    setText(((AlertMessage.AlertType) value).getName());
                }
                return this;
            }
        });
        formPanel.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nội dung:"), gbc);
        
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(3, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        formPanel.add(contentScroll, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Chọn tầng:"), gbc);
        
        gbc.gridx = 1;
        JPanel floorSelectionPanel = new JPanel(new BorderLayout(5, 5));
        JCheckBox allFloorsCheck = new JCheckBox("Toàn bộ tòa nhà");
        allFloorsCheck.setSelected(true);
        
        JPanel floorCheckPanel = new JPanel(new GridLayout(8, 5, 5, 5));
        List<JCheckBox> floorChecks = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            JCheckBox cb = new JCheckBox("T" + i);
            cb.setEnabled(false);
            floorChecks.add(cb);
            floorCheckPanel.add(cb);
        }
        
        allFloorsCheck.addActionListener(e -> {
            boolean enabled = !allFloorsCheck.isSelected();
            for (JCheckBox cb : floorChecks) {
                cb.setEnabled(enabled);
            }
        });
        
        JScrollPane floorScroll = new JScrollPane(floorCheckPanel);
        floorScroll.setPreferredSize(new Dimension(300, 120));
        
        floorSelectionPanel.add(allFloorsCheck, BorderLayout.NORTH);
        floorSelectionPanel.add(floorScroll, BorderLayout.CENTER);
        formPanel.add(floorSelectionPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JCheckBox scheduleCheck = new JCheckBox("Hẹn giờ gửi");
        formPanel.add(scheduleCheck, gbc);
        
        gbc.gridy = 4; gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel delayLabel = new JLabel("Sau (giây):");
        delayLabel.setEnabled(false);
        formPanel.add(delayLabel, gbc);
        
        gbc.gridx = 1;
        JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(10, 1, 3600, 1));
        delaySpinner.setEnabled(false);
        formPanel.add(delaySpinner, gbc);
        
        scheduleCheck.addActionListener(e -> {
            boolean enabled = scheduleCheck.isSelected();
            delayLabel.setEnabled(enabled);
            delaySpinner.setEnabled(enabled);
        });
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JCheckBox repeatCheck = new JCheckBox("Lặp lại cảnh báo");
        formPanel.add(repeatCheck, gbc);
        
        gbc.gridy = 6; gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel intervalLabel = new JLabel("Mỗi (giây):");
        intervalLabel.setEnabled(false);
        formPanel.add(intervalLabel, gbc);
        
        gbc.gridx = 1;
        JSpinner intervalSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 3600, 5));
        intervalSpinner.setEnabled(false);
        formPanel.add(intervalSpinner, gbc);
        
        repeatCheck.addActionListener(e -> {
            boolean enabled = repeatCheck.isSelected();
            intervalLabel.setEnabled(enabled);
            intervalSpinner.setEnabled(enabled);
        });
        
        // --- Nút Gửi và nút Dừng (thêm stopAllAlertsButton ở đây) ---
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        JButton sendButton = new JButton("Gửi Cảnh báo");
        sendButton.setFont(new Font("Arial", Font.BOLD, 16));
        sendButton.setBackground(new Color(231, 76, 60));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        formPanel.add(sendButton, gbc);
        
        gbc.gridx = 1; gbc.gridy = 7;
        stopAllAlertsButton = new JButton("Dừng tất cả cảnh báo");
        stopAllAlertsButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopAllAlertsButton.setBackground(new Color(149, 165, 166));
        stopAllAlertsButton.setForeground(Color.WHITE);
        stopAllAlertsButton.setFocusPainted(false);
        stopAllAlertsButton.setEnabled(false); // mặc định tắt
        formPanel.add(stopAllAlertsButton, gbc);
        
        // Hành vi khi nhấn nút Dừng tất cả cảnh báo
        stopAllAlertsButton.addActionListener(e -> {
            cancelAllActiveTimers();
            appendLog("Người quản trị đã dừng tất cả lịch cảnh báo.");
        });
        
        // Hành vi khi nhấn nút Gửi
        sendButton.addActionListener(e -> {
            AlertMessage.AlertType type = (AlertMessage.AlertType) typeCombo.getSelectedItem();
            String content = contentArea.getText().trim();
            
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung cảnh báo!");
                return;
            }
            
            boolean allFloors = allFloorsCheck.isSelected();
            int[] targetFloors = null;
            
            if (!allFloors) {
                List<Integer> selected = new ArrayList<>();
                for (int i = 0; i < floorChecks.size(); i++) {
                    if (floorChecks.get(i).isSelected()) {
                        selected.add(i + 1);
                    }
                }
                
                if (selected.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một tầng!");
                    return;
                }
                
                targetFloors = selected.stream().mapToInt(Integer::intValue).toArray();
            }
            
            boolean isScheduled = scheduleCheck.isSelected();
            boolean isRepeating = repeatCheck.isSelected();
            int delay = (Integer) delaySpinner.getValue();
            int interval = (Integer) intervalSpinner.getValue();
            
            if (!isScheduled && !isRepeating) {
                sendAlert(type, content, targetFloors, allFloors);
                contentArea.setText("");
                appendLog("Cảnh báo đã được gửi ngay (không hiển thị hộp thoại).");
            } else {
                scheduleAlert(type, content, targetFloors, allFloors, isScheduled, delay, isRepeating, interval);
                contentArea.setText("");
                
                String message = "Đã lên lịch cảnh báo ";
                if (isScheduled) {
                    message += "(gửi sau: " + delay + "s) ";
                }
                if (isRepeating) {
                    message += "(lặp mỗi: " + interval + "s) ";
                }
                message += (allFloors ? "cho toàn bộ tòa nhà." : "cho tầng được chọn.");
                
                // Không sử dụng JOptionPane chặn; chỉ ghi log và kích hoạt nút Dừng nếu cần
                appendLog(message);
                
                if (isRepeating) {
                    stopAllAlertsButton.setEnabled(true);
                }
            }
        });
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * scheduleAlert: tạo Timer/TimerTask và lưu Timer vào activeAlertTimers nếu cần.
     * Nếu là lặp (repeat==true) thì sẽ scheduleAtFixedRate và giữ timer để có thể dừng.
     * Nếu chỉ là delayed 1 lần thì vẫn schedule 1 lần (cũng lưu timer nhưng sẽ tự hết).
     */
    private void scheduleAlert(AlertMessage.AlertType type, String content, int[] targetFloors, 
                               boolean allFloors, boolean delayed, int delaySeconds, 
                               boolean repeat, int intervalSeconds) {
        Timer timer = new Timer();
        
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sendAlert(type, content, targetFloors, allFloors);
            }
        };
        
        long delayMs = delayed ? delaySeconds * 1000L : 0;
        
        if (repeat) {
            long intervalMs = intervalSeconds * 1000L;
            timer.scheduleAtFixedRate(task, delayMs, intervalMs);
            activeAlertTimers.add(timer); // lưu lại để có thể dừng
            stopAllAlertsButton.setEnabled(true);
            
            String floorInfo = allFloors ? "Toàn bộ" : Arrays.toString(targetFloors);
            appendLog("Đã lên lịch lặp lại: " + type.getName() + " - Sau " + delaySeconds + 
                     "s, lặp mỗi " + intervalSeconds + "s (Tầng: " + floorInfo + ")");
        } else {
            timer.schedule(task, delayMs);
            // Lưu timer tạm thời để nếu Admin nhấn Dừng ngay thì cancel được
            activeAlertTimers.add(timer);
            
            String floorInfo = allFloors ? "Toàn bộ" : Arrays.toString(targetFloors);
            appendLog("Đã lên lịch: " + type.getName() + " - Sau " + delaySeconds + 
                     " giây (Tầng: " + floorInfo + ")");
            // Lưu timer, nhưng sau thực thi một lần, timer sẽ hết; ta sẽ xóa nó trong cancelAllActiveTimers khi cần
        }
    }
    
    private void cancelAllActiveTimers() {
        synchronized (activeAlertTimers) {
            for (Timer t : activeAlertTimers) {
                try {
                    t.cancel();
                } catch (Exception ex) {
                    // ignore
                }
            }
            activeAlertTimers.clear();
        }
        // Vô hiệu nút dừng sau khi đã cancel tất cả
        SwingUtilities.invokeLater(() -> stopAllAlertsButton.setEnabled(false));
        appendLog("Tất cả lịch cảnh báo đã bị huỷ.");
    }
    
    private JPanel createClientListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"Client ID", "Tầng", "Căn hộ", "Địa chỉ", "Trạng thái"};
        clientTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable clientTable = new JTable(clientTableModel);
        clientTable.setRowHeight(25);
        clientTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(clientTable);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAlertHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"Thời gian", "Loại", "Nội dung", "Tầng", "Đã nhận"};
        alertTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable alertTable = new JTable(alertTableModel);
        alertTable.setRowHeight(25);
        alertTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        alertTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected && row < alertHistory.size()) {
                    AlertMessage alert = alertHistory.get(alertHistory.size() - 1 - row);
                    c.setBackground(Color.decode(alert.getType().getColor()));
                    c.setForeground(Color.BLACK);
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(alertTable);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JLabel statusLabel = new JLabel("Server đang chạy trên port: " + SERVER_PORT);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        bottomPanel.add(statusLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void startServer() {
        try {
            socket = new DatagramSocket(SERVER_PORT);
            socket.setBroadcast(true);
            
            appendLog("Server khởi động thành công trên port " + SERVER_PORT);
            
            new Thread(() -> {
                while (true) {
                    try {
                        byte[] buffer = new byte[65536];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        
                        handleIncomingPacket(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể khởi động server: " + e.getMessage());
        }
    }
    
    private void handleIncomingPacket(DatagramPacket packet) {
        try {
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] parts = message.split("\\|");
            
            if (parts[0].equals("REGISTER")) {
                String clientId = parts[1];
                int floor = Integer.parseInt(parts[2]);
                String apartment = parts[3];
                
                ClientInfo client = new ClientInfo(clientId, floor, apartment, 
                    packet.getAddress(), packet.getPort());
                clients.put(clientId, client);
                
                appendLog("Client đăng ký: " + clientId + " - Tầng " + floor + " - Căn hộ " + apartment);
                logger.logClientAction("CLIENT_REGISTER", floor, apartment, "Registered successfully");
                
                updateClientTable();
                updateOnlineCount();
                
            } else if (parts[0].equals("ACK")) {
                String alertId = parts[1];
                String clientId = parts[2];
                
                if (clients.containsKey(clientId)) {
                    ClientInfo client = clients.get(clientId);
                    client.setLastSeen(System.currentTimeMillis());
                    client.setAcknowledged(true);
                    
                    acknowledgments.computeIfAbsent(alertId, k -> new HashSet<>()).add(clientId);
                    
                    appendLog("Nhận ACK từ " + clientId + " cho cảnh báo " + alertId);
                    logger.logAcknowledgment(alertId, client.getFloor(), client.getApartment());
                    
                    updateAlertTable();
                }
                
            } else if (parts[0].equals("HEARTBEAT")) {
                String clientId = parts[1];
                if (clients.containsKey(clientId)) {
                    clients.get(clientId).setLastSeen(System.currentTimeMillis());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendAlert(AlertMessage.AlertType type, String content, int[] targetFloors, boolean allFloors) {
        try {
            AlertMessage alert = new AlertMessage(type, content, targetFloors, allFloors);
            alertHistory.add(alert);
            acknowledgments.put(alert.getMessageId(), new HashSet<>());
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(alert);
            oos.flush();
            byte[] data = bos.toByteArray();
            
            InetAddress broadcastAddr = InetAddress.getByName(BROADCAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddr, CLIENT_PORT);
            socket.send(packet);
            
            String floorInfo = allFloors ? "Toàn bộ" : Arrays.toString(targetFloors);
            appendLog("Đã gửi cảnh báo: " + type.getName() + " - " + content + " (Tầng: " + floorInfo + ")");
            logger.logAlert(alert, floorInfo, "SENT");
            
            updateBuildingMap(alert);
            updateAlertTable();
            updateTotalAlerts();
            
            // Không hiển thị hộp thoại chặn; chỉ ghi log
            // JOptionPane.showMessageDialog(this, "Cảnh báo đã được gửi thành công!");
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi gửi cảnh báo: " + e.getMessage());
        }
    }
    
    private void updateBuildingMap(AlertMessage alert) {
        SwingUtilities.invokeLater(() -> {
            for (int floor = 1; floor <= 40; floor++) {
                JPanel panel = floorPanels.get(floor);
                if (alert.isForFloor(floor)) {
                    panel.setBackground(Color.decode(alert.getType().getColor()));
                    
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                panel.setBackground(new Color(46, 204, 113));
                            });
                        }
                    }, 5000);
                }
            }
        });
    }
    
    private void updateClientTable() {
        SwingUtilities.invokeLater(() -> {
            clientTableModel.setRowCount(0);
            for (ClientInfo client : clients.values()) {
                clientTableModel.addRow(new Object[]{
                    client.getClientId(),
                    client.getFloor(),
                    client.getApartment(),
                    client.getAddress().getHostAddress() + ":" + client.getPort(),
                    client.isOnline() ? "Online" : "Offline"
                });
            }
        });
    }
    
    private void updateAlertTable() {
        SwingUtilities.invokeLater(() -> {
            alertTableModel.setRowCount(0);
            for (int i = alertHistory.size() - 1; i >= 0; i--) {
                AlertMessage alert = alertHistory.get(i);
                int ackCount = acknowledgments.getOrDefault(alert.getMessageId(), new HashSet<>()).size();
                int totalClients = (int) clients.values().stream()
                    .filter(c -> alert.isForFloor(c.getFloor())).count();
                
                String floorInfo = alert.isAllFloors() ? "Toàn bộ" : Arrays.toString(alert.getTargetFloors());
                String ackInfo = ackCount + "/" + totalClients;
                
                alertTableModel.addRow(new Object[]{
                    alert.getFormattedTime(),
                    alert.getType().getName(),
                    alert.getContent(),
                    floorInfo,
                    ackInfo
                });
            }
        });
    }
    
    private void updateOnlineCount() {
        SwingUtilities.invokeLater(() -> {
            long onlineCount = clients.values().stream().filter(ClientInfo::isOnline).count();
            onlineCountLabel.setText("Clients: " + onlineCount + "/" + clients.size());
        });
    }
    
    private void updateTotalAlerts() {
        SwingUtilities.invokeLater(() -> {
            totalAlertsLabel.setText("Cảnh báo: " + alertHistory.size());
        });
    }
    
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void startHeartbeatChecker() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                boolean anyChange = false;

                for (ClientInfo client : clients.values()) {
                    // Nếu hơn 10 giây không gửi heartbeat thì coi như offline
                    if (now - client.getLastSeen() > 10000) {
                        if (client.isOnline()) {
                            client.setOnline(false);
                            appendLog("Client " + client.getClientId() + " (Tầng " + client.getFloor() +
                                    ", Căn " + client.getApartment() + ") đã offline (mất kết nối).");
                            anyChange = true;
                        }
                    } else {
                        if (!client.isOnline()) {
                            client.setOnline(true);
                            anyChange = true;
                        }
                    }
                }

                if (anyChange) {
                    updateClientTable();
                    updateOnlineCount();
                }
            }
        }, 5000, 5000);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Hệ thống Cảnh báo Thời gian Thực\n" +
            "Dự án Lập trình Mạng\n" +
            "Phiên bản 1.0\n\n" +
            "Server quản lý và gửi cảnh báo đến các client qua UDP",
            "Giới thiệu",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new Server());
    }
}
