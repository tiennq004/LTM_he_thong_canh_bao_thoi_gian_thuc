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

public class Client extends JFrame {
    private static final int CLIENT_PORT = 9877;
    private static final int SERVER_PORT = 9876;
    
    private DatagramSocket socket;
    private CSVLogger logger;
    private String clientId;
    private int floor;
    private String apartment;
    private boolean registered;
    
    private List<AlertMessage> receivedAlerts;
    
    private JLabel statusLabel;
    private JLabel clockLabel;
    private JLabel floorLabel;
    private JTextArea logArea;
    private DefaultTableModel alertTableModel;
    private JButton registerButton;
    private JTextField floorField;
    private JTextField apartmentField;
    private JPanel mainContainer;
    private CardLayout cardLayout;
    private JTabbedPane mainTabs;
    private JPanel topPanel;
    private JLabel floorStatLabel;
    private JLabel apartmentStatLabel;
    
    public Client() {
        receivedAlerts = new ArrayList<>();
        clientId = "CLIENT-" + System.currentTimeMillis();
        registered = false;
        
        setTitle("Hệ thống Cảnh báo Chung cư - Client");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        createMenuBar();
        createTopPanel();
        createCenterPanels();
        createBottomPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);
        
        cardLayout.show(mainContainer, "REGISTER");
        
        startClient();
        startHeartbeat();
        startClock();
        
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
        topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(52, 152, 219));
        
        JLabel titleLabel = new JLabel("CLIENT - HỆ THỐNG NHẬN CẢNH BÁO");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        infoPanel.setOpaque(false);
        
        clockLabel = new JLabel("", SwingConstants.CENTER);
        clockLabel.setFont(new Font("Arial", Font.BOLD, 14));
        clockLabel.setForeground(Color.WHITE);
        
        floorLabel = new JLabel("Chưa đăng ký", SwingConstants.CENTER);
        floorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        floorLabel.setForeground(Color.WHITE);
        
        infoPanel.add(clockLabel);
        infoPanel.add(floorLabel);
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void createCenterPanels() {
        JPanel registerPanel = createRegistrationPanel();
        
        mainTabs = new JTabbedPane();
        mainTabs.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainTabs.addTab("Dashboard", createDashboardPanel());
        mainTabs.addTab("Cảnh báo", createAlertPanel());
        mainTabs.addTab("Logs", createLogPanel());
        
        mainContainer.add(registerPanel, "REGISTER");
        mainContainer.add(mainTabs, "DASHBOARD");
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        statsPanel.add(createStatCard("Client ID", clientId, new Color(52, 152, 219), null));
        statsPanel.add(createStatCard("Trạng thái", "Đã kết nối", new Color(46, 204, 113), null));
        
        floorStatLabel = new JLabel("---", SwingConstants.CENTER);
        floorStatLabel.setFont(new Font("Arial", Font.BOLD, 24));
        floorStatLabel.setForeground(Color.WHITE);
        statsPanel.add(createStatCard("Tầng", "---", new Color(155, 89, 182), floorStatLabel));
        
        apartmentStatLabel = new JLabel("---", SwingConstants.CENTER);
        apartmentStatLabel.setFont(new Font("Arial", Font.BOLD, 24));
        apartmentStatLabel.setForeground(Color.WHITE);
        statsPanel.add(createStatCard("Căn hộ", "---", new Color(230, 126, 34), apartmentStatLabel));
        
        JLabel welcomeLabel = new JLabel("Chào mừng đến Hệ thống Cảnh báo", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(52, 73, 94));
        welcomeLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        panel.add(welcomeLabel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String label, String value, Color color, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel labelText = new JLabel(label, SwingConstants.CENTER);
        labelText.setFont(new Font("Arial", Font.BOLD, 14));
        labelText.setForeground(Color.WHITE);
        
        JLabel valueText = (valueLabel != null) ? valueLabel : new JLabel(value, SwingConstants.CENTER);
        if (valueLabel == null) {
            valueText.setFont(new Font("Arial", Font.BOLD, 24));
            valueText.setForeground(Color.WHITE);
        }
        
        card.add(labelText, BorderLayout.NORTH);
        card.add(valueText, BorderLayout.CENTER);
        
        return card;
    }
    
    private void updateDashboardInfo() {
        if (floorStatLabel != null) {
            floorStatLabel.setText(String.valueOf(floor));
        }
        if (apartmentStatLabel != null) {
            apartmentStatLabel.setText(apartment);
        }
    }
    
    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("ĐĂNG KÝ THÔNG TIN CƯ DÂN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 152, 219));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel idLabel = new JLabel("Client ID:");
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(idLabel, gbc);
        
        gbc.gridx = 1;
        JTextField idField = new JTextField(clientId, 20);
        idField.setEditable(false);
        idField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel floorLabelForm = new JLabel("Tầng (1-40):");
        floorLabelForm.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(floorLabelForm, gbc);
        
        gbc.gridx = 1;
        floorField = new JTextField(20);
        floorField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(floorField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel apartmentLabelForm = new JLabel("Căn hộ:");
        apartmentLabelForm.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(apartmentLabelForm, gbc);
        
        gbc.gridx = 1;
        apartmentField = new JTextField(20);
        apartmentField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(apartmentField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        registerButton = new JButton("Đăng ký");
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(200, 40));
        registerButton.addActionListener(e -> registerClient());
        panel.add(registerButton, gbc);
        
        gbc.gridy = 5;
        statusLabel = new JLabel("Vui lòng nhập thông tin và đăng ký", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        panel.add(statusLabel, gbc);
        
        return panel;
    }
    
    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("DANH SÁCH CẢNH BÁO");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        String[] columns = {"Thời gian", "Loại", "Nội dung", "Tầng"};
        alertTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable alertTable = new JTable(alertTableModel);
        alertTable.setRowHeight(30);
        alertTable.setFont(new Font("Arial", Font.PLAIN, 12));
        alertTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        alertTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected && row < receivedAlerts.size()) {
                    AlertMessage alert = receivedAlerts.get(receivedAlerts.size() - 1 - row);
                    c.setBackground(Color.decode(alert.getType().getColor()));
                    c.setForeground(Color.BLACK);
                } else if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(alertTable);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("SYSTEM LOGS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JLabel infoLabel = new JLabel("Client đang lắng nghe trên port: " + CLIENT_PORT);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        bottomPanel.add(infoLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void startClient() {
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            
            InetSocketAddress address = new InetSocketAddress(CLIENT_PORT);
            socket.bind(address);
            
            logger = new CSVLogger("client_" + clientId + "_log.csv");
            
            appendLog("Client khởi động thành công trên port " + CLIENT_PORT);
            appendLog("Client ID: " + clientId);
            appendLog("Sẵn sàng nhận cảnh báo broadcast từ server");
            
            new Thread(() -> {
                while (true) {
                    try {
                        byte[] buffer = new byte[65536];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        
                        handleIncomingAlert(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể khởi động client: " + e.getMessage());
        }
    }
    
    private void registerClient() {
        try {
            String floorText = floorField.getText().trim();
            String apartmentText = apartmentField.getText().trim();
            
            if (floorText.isEmpty() || apartmentText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            
            floor = Integer.parseInt(floorText);
            
            if (floor < 1 || floor > 40) {
                JOptionPane.showMessageDialog(this, "Tầng phải từ 1 đến 40!");
                return;
            }
            
            apartment = apartmentText;
            
            String message = "REGISTER|" + clientId + "|" + floor + "|" + apartment;
            byte[] data = message.getBytes();
            
            InetAddress serverAddr = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, SERVER_PORT);
            socket.send(packet);
            
            registered = true;
            floorField.setEnabled(false);
            apartmentField.setEnabled(false);
            registerButton.setEnabled(false);
            
            statusLabel.setText("Đã đăng ký thành công!");
            statusLabel.setForeground(new Color(46, 204, 113));
            floorLabel.setText("Tầng " + floor + " - Căn hộ " + apartment);
            
            appendLog("Đã gửi yêu cầu đăng ký đến server");
            logger.logClientAction("REGISTER", floor, apartment, "Registration sent");
            
            updateDashboardInfo();
            cardLayout.show(mainContainer, "DASHBOARD");
            
            JOptionPane.showMessageDialog(this, 
                "Đăng ký thành công!\nBạn đã được chuyển đến Dashboard",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tầng phải là số nguyên!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đăng ký: " + e.getMessage());
        }
    }
    
    private void handleIncomingAlert(DatagramPacket packet) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
            ObjectInputStream ois = new ObjectInputStream(bis);
            AlertMessage alert = (AlertMessage) ois.readObject();
            
            if (!registered || !alert.isForFloor(floor)) {
                return;
            }
            
            receivedAlerts.add(alert);
            
            appendLog("Nhận cảnh báo: " + alert.getType().getName() + " - " + alert.getContent());
            logger.logClientAction("ALERT_RECEIVED", floor, apartment, 
                alert.getType().getName() + ": " + alert.getContent());
            
            showAlertPopup(alert);
            updateAlertTable();
            sendAcknowledgment(alert.getMessageId());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAlertPopup(AlertMessage alert) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(this, "CẢNH BÁO KHẨN CẤP", true);
            dialog.setSize(500, 300);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));
            
            JPanel topPanel = new JPanel();
            topPanel.setBackground(Color.decode(alert.getType().getColor()));
            topPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel typeLabel = new JLabel(alert.getType().getName().toUpperCase());
            typeLabel.setFont(new Font("Arial", Font.BOLD, 24));
            typeLabel.setForeground(Color.BLACK);
            topPanel.add(typeLabel);
            
            JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
            centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel timeLabel = new JLabel("Thời gian: " + alert.getFormattedTime());
            timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
            
            JTextArea contentArea = new JTextArea(alert.getContent());
            contentArea.setFont(new Font("Arial", Font.PLAIN, 16));
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setOpaque(false);
            
            JScrollPane contentScroll = new JScrollPane(contentArea);
            contentScroll.setBorder(null);
            
            centerPanel.add(timeLabel, BorderLayout.NORTH);
            centerPanel.add(contentScroll, BorderLayout.CENTER);
            
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JButton okButton = new JButton("Đã hiểu");
            okButton.setFont(new Font("Arial", Font.BOLD, 14));
            okButton.setPreferredSize(new Dimension(120, 40));
            okButton.setBackground(new Color(52, 152, 219));
            okButton.setForeground(Color.WHITE);
            okButton.setFocusPainted(false);
            okButton.addActionListener(e -> dialog.dispose());
            bottomPanel.add(okButton);
            
            dialog.add(topPanel, BorderLayout.NORTH);
            dialog.add(centerPanel, BorderLayout.CENTER);
            dialog.add(bottomPanel, BorderLayout.SOUTH);
            
            Toolkit.getDefaultToolkit().beep();
            
            dialog.setVisible(true);
        });
    }
    
    private void sendAcknowledgment(String alertId) {
        try {
            String message = "ACK|" + alertId + "|" + clientId;
            byte[] data = message.getBytes();
            
            InetAddress serverAddr = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, SERVER_PORT);
            socket.send(packet);
            
            appendLog("Đã gửi xác nhận cho cảnh báo: " + alertId);
            logger.logClientAction("ACK_SENT", floor, apartment, "Alert ID: " + alertId);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateAlertTable() {
        SwingUtilities.invokeLater(() -> {
            alertTableModel.setRowCount(0);
            for (int i = receivedAlerts.size() - 1; i >= 0; i--) {
                AlertMessage alert = receivedAlerts.get(i);
                String floorInfo = alert.isAllFloors() ? "Toàn bộ" : Arrays.toString(alert.getTargetFloors());
                
                alertTableModel.addRow(new Object[]{
                    alert.getFormattedTime(),
                    alert.getType().getName(),
                    alert.getContent(),
                    floorInfo
                });
            }
        });
    }
    
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void startHeartbeat() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (registered) {
                    try {
                        String message = "HEARTBEAT|" + clientId;
                        byte[] data = message.getBytes();
                        
                        InetAddress serverAddr = InetAddress.getByName("localhost");
                        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, SERVER_PORT);
                        socket.send(packet);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 5000, 5000);
    }
    
    private void startClock() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    String time = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").format(new Date());
                    clockLabel.setText(time);
                });
            }
        }, 0, 1000);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Hệ thống Cảnh báo Thời gian Thực\n" +
            "Dự án Lập trình Mạng\n" +
            "Phiên bản 1.0\n\n" +
            "Client nhận cảnh báo từ server qua UDP\n" +
            "Client ID: " + clientId,
            "Giới thiệu",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new Client());
    }
}
