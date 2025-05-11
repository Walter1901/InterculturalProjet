import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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

    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainPanel = new JPanel(cardLayout);

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
        JPanel topBar = new JPanel();
        topBar.setPreferredSize(new Dimension(SCREEN_WIDTH, 30));
        topBar.setLayout(new BorderLayout());
        topBar.setBackground(backgroundColor);

        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(textColor);
        timeLabel.setFont(new Font("Inter", Font.BOLD, 14));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        topBar.add(timeLabel, BorderLayout.WEST);

        JLabel batteryStatus = new JLabel();
        batteryStatus.setForeground(textColor);
        batteryStatus.setFont(new Font("Inter", Font.BOLD, 14));
        batteryStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        topBar.add(batteryStatus, BorderLayout.EAST);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
                    timeLabel.setText(currentTime);

                    int battery = getWindowsBatteryPercentage();
                    if (battery >= 0) {
                        batteryStatus.setText("🔋 " + battery + "%");
                    } else {
                        batteryStatus.setText("🔋 N/A");
                    }
                });
            }
        }, 0, 3000);

        return topBar;
    }

    private static int getWindowsBatteryPercentage() {
        try {
            Process process = Runtime.getRuntime().exec("WMIC Path Win32_Battery Get EstimatedChargeRemaining");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.matches("\\d+")) {
                    return Integer.parseInt(line);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
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

    // Method to create the app icons panel
    public static JPanel createAppIconsPanel(JFrame parentFrame) {
        JPanel appIconsPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        appIconsPanel.setBackground(backgroundColor);
        appIconsPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 150, 30));

        appIconsPanel.add(createAppButton("Address Book", "/homescreenIcons/addressBookIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Picture Gallery", "/homescreenIcons/galleryIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Finance Tracker", "/homescreenIcons/financeTrackerIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Investify", "/homescreenIcons/investifyIcon.png", parentFrame));

        return appIconsPanel;
    }

    // Method to create app buttons (used by the method "createAppIconsPanel") (Generated by ChatGPT 4-o)
    public static JButton createAppButton(String appName, String iconResourcePath, JFrame parentFrame) {
        JButton appButton = new JButton(appName);
        appButton.setFont(new Font("Inter", Font.PLAIN, 12));

        try {
            URL imageUrl = phoneUtils.class.getResource(iconResourcePath);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                appButton.setIcon(new ImageIcon(img));
            } else {
                throw new IOException("Image not found: " + iconResourcePath);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône : " + iconResourcePath);
            ImageIcon fallbackIcon = (ImageIcon) UIManager.getIcon("FileView.computerIcon");
            if (fallbackIcon != null) {
                Image img = fallbackIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                appButton.setIcon(new ImageIcon(img));
            }
        }

        appButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        appButton.setHorizontalTextPosition(SwingConstants.CENTER);
        appButton.setForeground(textColor);
        appButton.setBackground(backgroundColor);
        appButton.setFocusPainted(false);
        appButton.setBorderPainted(false);

        appButton.addActionListener(e -> {
            switch (appName) {
                case "Investify":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Investify");
                    break;
                case "Address Book":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Address Book");
                    break;
                case "Picture Gallery":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Picture Gallery");
                    break;
                case "Finance Tracker":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Finance Tracker");
                    break;
                default:
                    JOptionPane.showMessageDialog(parentFrame, "Opening " + appName + "...");
                    break;
            }
        });

        return appButton;
    }

    public static void createHomescreen(){

        // Create the main phone frame
        JFrame phoneFrame = phoneUtils.createPhoneFrame("The Phone");

        // Add home screen
        JPanel homeScreen = createAppIconsPanel(phoneFrame);
        mainPanel.add(homeScreen, "Home");

        // Add Investify screen
        JPanel investifyScreen = InvestifyApp.createInvestify();
        mainPanel.add(investifyScreen, "Investify");

        // Add Address Book screen
        JPanel addressBookScreen = AddressBook.createAddressBook();
        mainPanel.add(addressBookScreen, "Address Book");

        // Add Picture Gallery screen
        JPanel pictureGalleryScreen = PictureGallery.createPictureGallery();
        mainPanel.add(pictureGalleryScreen, "Picture Gallery");

        // Add Finance Tracker screen
        JPanel financeTrackerScreen = FinanceTracker.createFinanceTracker();
        mainPanel.add(financeTrackerScreen, "Finance Tracker");

        phoneFrame.add(mainPanel, BorderLayout.CENTER);
        phoneFrame.add(phoneUtils.createTopBar(), BorderLayout.NORTH);
        phoneFrame.add(phoneUtils.createBottomBar(phoneFrame), BorderLayout.SOUTH);
        phoneFrame.setVisible(true);

    }

}