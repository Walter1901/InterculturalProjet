import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class phoneUtils {

    protected static final int SCREEN_WIDTH = 360;
    protected static final int SCREEN_HEIGHT = 640;
    protected static final Color backgroundColor = new Color(28, 28, 30);
    protected static final Color textColor = new Color(255, 255, 255);

    // Crée la frame principale
    public static JFrame createPhoneFrame(String title) {
        JFrame phoneFrame = new JFrame(title);
        phoneFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        phoneFrame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        phoneFrame.setResizable(false);
        phoneFrame.setLayout(new BorderLayout());
        return phoneFrame;
    }

    // Bar du haut avec l'heure et la batterie
    public static JPanel createTopBar() {
        JPanel topBar = new JPanel();
        topBar.setPreferredSize(new Dimension(SCREEN_WIDTH, 30));
        topBar.setLayout(new BorderLayout());
        topBar.setBackground(backgroundColor);

        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(textColor);
        timeLabel.setFont(new Font("Inter", Font.BOLD, 14));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        topBar.add(timeLabel, BorderLayout.WEST);

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
        topBar.add(batteryStatus, BorderLayout.EAST);

        return topBar;
    }

    // Bar du bas avec le bouton "Home"
    public static JPanel createBottomBar(Runnable goHomeAction) {
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
                goHomeAction.run();
            }
        });

        panel.add(homeIndicator);
        return panel;
    }
}
