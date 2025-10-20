package apartment;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Calendar;

public class ClockPanel extends JPanel {
    private static final int SIZE = 120;

    public ClockPanel() {
        setPreferredSize(new Dimension(SIZE, SIZE + 30)); // chừa chỗ dưới cho ngày
        setBackground(new Color(230, 240, 250));
        Timer timer = new Timer(1000, e -> repaint());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 📦 Vẽ hình vuông nền trắng bao quanh đồng hồ
        int boxSize = Math.min(width, height - 30) - 8;
        int boxX = (width - boxSize) / 2;
        int boxY = 5;
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(boxX, boxY, boxSize, boxSize, 16, 16);
        g2d.setColor(new Color(180, 180, 180));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(boxX, boxY, boxSize, boxSize, 16, 16);

        // 🎯 Tâm và bán kính đồng hồ
        int centerX = width / 2;
        int centerY = boxY + boxSize / 2;
        int radius = boxSize / 2 - 10;

        // 🕓 Vẽ viền đồng hồ tròn
        g2d.setColor(new Color(52, 73, 94));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));

        // 🔢 Vẽ số 1–12
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(44, 62, 80));
        for (int i = 1; i <= 12; i++) {
            double angle = Math.toRadians((i * 30) - 90);
            int x = (int) (centerX + Math.cos(angle) * (radius - 12));
            int y = (int) (centerY + Math.sin(angle) * (radius - 12));
            String num = String.valueOf(i);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(num, x - fm.stringWidth(num) / 2, y + fm.getAscent() / 3);
        }

        // 🕒 Lấy thời gian
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // ⚙️ Tính góc
        double secondAngle = Math.toRadians((second * 6) - 90);
        double minuteAngle = Math.toRadians((minute * 6) - 90);
        double hourAngle = Math.toRadians(((hour % 12 + minute / 60.0) * 30) - 90);

        // Kim giây
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1));
        drawHand(g2d, centerX, centerY, secondAngle, radius * 0.9);

        // Kim phút
        g2d.setColor(new Color(41, 128, 185));
        g2d.setStroke(new BasicStroke(1.8f));
        drawHand(g2d, centerX, centerY, minuteAngle, radius * 0.7);

        // Kim giờ
        g2d.setColor(new Color(39, 174, 96));
        g2d.setStroke(new BasicStroke(3));
        drawHand(g2d, centerX, centerY, hourAngle, radius * 0.5);

        // Tâm đồng hồ
        g2d.setColor(Color.BLACK);
        g2d.fill(new Ellipse2D.Double(centerX - 3, centerY - 3, 6, 6));

        // 📅 Ngày tháng dưới khung
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String date = String.format("%02d/%02d/%d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = width / 2 - fm.stringWidth(date) / 2;
        int textY = height - 6;
        g2d.drawString(date, textX, textY);
    }

    private void drawHand(Graphics2D g2d, int x, int y, double angle, double length) {
        int x2 = (int) (x + Math.cos(angle) * length);
        int y2 = (int) (y + Math.sin(angle) * length);
        g2d.drawLine(x, y, x2, y2);
    }
}
