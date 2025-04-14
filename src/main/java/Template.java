import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Template extends JFrame {

    // Shared screen size
    public static final int SCREEN_WIDTH = 360;
    public static final int SCREEN_HEIGHT = 640;

    private JLabel timeLabel;
    private JLabel batteryLabel;

    public Template() {
        setTitle("Phone Template");
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Top bar (time + battery)
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // Main content area (white background)
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);

        // Bottom bar (iPhone-style home indicator)
        JPanel bottomBar = createIPhoneBottomBar();
        add(bottomBar, BorderLayout.SOUTH);

        // Time updater
        updateTimeEverySecond();

        setVisible(true);
    }

    // Creates the top bar with time and battery
    private JPanel createTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(SCREEN_WIDTH, 30));
        panel.setBackground(Color.DARK_GRAY);

        timeLabel = new JLabel(getCurrentTime());
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        batteryLabel = new JLabel("🔋 100%");
        batteryLabel.setForeground(Color.WHITE);
        batteryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        panel.add(timeLabel, BorderLayout.WEST);
        panel.add(batteryLabel, BorderLayout.EAST);

        return panel;
    }

    // Creates the bottom bar with a centered iPhone-style home indicator
    private JPanel createIPhoneBottomBar() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(SCREEN_WIDTH, 60));
        panel.setBackground(Color.WHITE);

        // Home indicator
        JPanel homeIndicator = new JPanel();
        homeIndicator.setPreferredSize(new Dimension(120, 8));
        homeIndicator.setBackground(Color.LIGHT_GRAY);
        homeIndicator.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeIndicator.setOpaque(true);

        // Rounded effect
        homeIndicator.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));

        // Click behavior
        homeIndicator.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(panel, "Home indicator tapped!");
            }
        });

        // Center the pill in the bottom
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalGlue());
        panel.add(homeIndicator);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // space at the bottom

        return panel;
    }

    // Gets current time in HH:mm format
    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    // Updates time every second
    private void updateTimeEverySecond() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> timeLabel.setText(getCurrentTime()));
            }
        }, 0, 1000);
    }

    public static void main(String[] args) {
        new Template();
    }
}
