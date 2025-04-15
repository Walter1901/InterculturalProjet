import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class phoneUtils {

    static int SCREEN_WIDTH = 360;
    static int SCREEN_HEIGHT = 640;
    static Color backgroundColor = new Color(28, 28, 30);
    static Color textColor = new Color(255, 255, 255);

    // Method to create the main phone frame
    public static JFrame createPhoneFrame(String title) {
        JFrame phoneFrame = new JFrame(title);
        phoneFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        phoneFrame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        phoneFrame.setResizable(false);
        phoneFrame.setLayout(new BorderLayout());
        return phoneFrame;
    }

    // Method to create the top bar (time and battery)
    public static JPanel createTopBar() {
        JPanel timeAndDatePanel = new JPanel();
        timeAndDatePanel.setPreferredSize(new Dimension(SCREEN_WIDTH, 30));
        timeAndDatePanel.setLayout(new BorderLayout());
        timeAndDatePanel.setBackground(backgroundColor);

        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(textColor);
        timeLabel.setFont(new Font("Inter", Font.BOLD, 14));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        timeAndDatePanel.add(timeLabel, BorderLayout.WEST);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
                    timeLabel.setText(currentTime);
                });
            }
        }, 0, 3000);

        JLabel batteryStatus = new JLabel("🔋 100%");
        batteryStatus.setForeground(textColor);
        batteryStatus.setFont(new Font("Inter", Font.BOLD, 14));
        batteryStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        timeAndDatePanel.add(batteryStatus, BorderLayout.EAST);

        return timeAndDatePanel;
    }

    // Method to create the bottom bar
    public static JPanel createBottomBar(JFrame parentFrame) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(360, 30));
        panel.setBackground(backgroundColor);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JPanel homeIndicator = new JPanel();
        homeIndicator.setPreferredSize(new Dimension(120, 5));
        homeIndicator.setBackground(textColor);
        homeIndicator.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeIndicator.setOpaque(true);

        homeIndicator.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        homeIndicator.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Homescreen.cardLayout.show(Homescreen.mainPanel, "Home");
            }
        });

        panel.add(homeIndicator);
        return panel;
    }


}