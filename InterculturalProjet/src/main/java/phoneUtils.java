import Address.AddressBook;
import gallery.PictureGalleryApp;
import gallery.service.TinyPNGService;
import investify.app.Investify;
import phone.HostOS;
import Finance.FinanceTracker;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility class for the phone application interface.
 * Provides methods to create UI components and manage the phone interface.
 */
public class phoneUtils {

    static int SCREEN_WIDTH = 360;
    static int SCREEN_HEIGHT = 640;
    static Color backgroundColor = new Color(28, 28, 30);
    static Color textColor = new Color(255, 255, 255);

    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainPanel = new JPanel(cardLayout);

    /**
     * Creates the main phone frame with fixed dimensions.
     * @param title Title of the phone frame window
     * @return Configured JFrame for the phone application
     */
    public static JFrame createPhoneFrame(String title) {
        JFrame phoneFrame = new JFrame(title);
        phoneFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        phoneFrame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        phoneFrame.setResizable(false);
        phoneFrame.setLayout(new BorderLayout());
        return phoneFrame;
    }

    /**
     * Creates the top status bar with time and battery indicators.
     * Updates the time and battery status every 3 seconds.
     * @return JPanel containing the top bar elements
     */
    public static JPanel createTopBar() {
        // Create main top bar panel
        JPanel topBar = new JPanel();
        topBar.setPreferredSize(new Dimension(SCREEN_WIDTH, 30));
        topBar.setLayout(new BorderLayout());
        topBar.setBackground(backgroundColor);

        // Create time display label (left side)
        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(textColor);
        timeLabel.setFont(new Font("Inter", Font.BOLD, 14));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        topBar.add(timeLabel, BorderLayout.WEST);

        // Create battery status label (right side)
        JLabel batteryStatus = new JLabel();
        batteryStatus.setForeground(textColor);
        batteryStatus.setFont(new Font("Inter", Font.BOLD, 14));
        batteryStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        topBar.add(batteryStatus, BorderLayout.EAST);

        // Set up timer to update time and battery status every 3 seconds
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Update current time
                    String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
                    timeLabel.setText(currentTime);

                    // Update battery percentage
                    int battery = getBatteryPercentage();
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

    /**
     * Retrieves the current battery percentage of the system based on the underlying
     * operating system. For Windows and Linux systems, this method delegates to OS-specific
     * helper methods to obtain the battery percentage. For unsupported operating systems,
     * the method returns -1.
     *
     * @return The battery percentage (0-100) or -1 if not available
     */
    private static int getBatteryPercentage() {
        try {
            if (HostOS.isWindows()) {
                return getWindowsBatteryPercentage();
            } else if (HostOS.isLinux()) {
                return getLinuxBatteryPercentage();
            } else {
                // Unsupported system
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Error retrieving battery level: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Retrieves the current battery percentage from the Windows operating system.
     * This method utilizes the Windows Management Instrumentation Command-line (WMIC)
     * to fetch the estimated charge remaining from the battery.
     *
     * @return An integer representing the battery percentage, or -1 if an error occurs
     *         or no valid battery data is found.
     */
    private static int getWindowsBatteryPercentage() {
        Process process = null;
        BufferedReader reader = null;

        try {
            // Execute Windows command to get battery percentage
            process = Runtime.getRuntime().exec("WMIC Path Win32_Battery Get EstimatedChargeRemaining");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read output and find the battery percentage
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.matches("\\d+")) {
                    return Integer.parseInt(line);
                }
            }

            // Wait for the process to finish
            process.waitFor();

        } catch (Exception e) {
            System.err.println("Error retrieving Windows battery level: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources
            try {
                if (reader != null) reader.close();
                if (process != null) process.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Return -1 if battery percentage couldn't be determined
        return -1;
    }

    /**
     * Retrieves the battery percentage of a Linux system by reading the capacity value
     * from the system file located at "/sys/class/power_supply/BAT0/capacity".
     * The method attempts to read the file, extract the battery percentage as an integer,
     * and return it. If the file does not exist, is unreadable, or does not contain valid
     * data, the method will try alternative battery locations.
     *
     * @return The battery percentage as an integer value (0-100) or -1 if not available
     */
    private static int getLinuxBatteryPercentage() {
        // List of possible paths for battery capacity file
        String[] possiblePaths = {
                "/sys/class/power_supply/BAT0/capacity",
                "/sys/class/power_supply/BAT1/capacity",
                "/sys/class/power_supply/battery/capacity"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line = reader.readLine();
                    if (line != null && line.matches("\\d+")) {
                        return Integer.parseInt(line);
                    }
                } catch (Exception e) {
                    System.err.println("Error reading file " + path + ": " + e.getMessage());
                }
            }
        }

        // If none of the files could be read successfully, try with the acpi command
        try {
            Process process = Runtime.getRuntime().exec("acpi -b");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.contains("%")) {
                    // Typical format: "Battery 0: Discharging, 42%, 01:42:17 remaining"
                    int startIndex = line.indexOf(", ") + 2;
                    int endIndex = line.indexOf("%");
                    if (startIndex > 0 && endIndex > startIndex) {
                        String percentage = line.substring(startIndex, endIndex);
                        return Integer.parseInt(percentage);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Error executing acpi command: " + e.getMessage());
        }

        return -1;  // Return -1 if no method worked
    }
    /**
     * Creates the bottom navigation bar with a home indicator.
     * The home indicator returns to the home screen when clicked.
     *
     * @param parentFrame Parent frame for action references
     * @return JPanel containing the bottom bar with home indicator
     */
    public static JPanel createBottomBar(JFrame parentFrame) {
        // Create main bottom bar panel
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(360, 30));
        panel.setBackground(backgroundColor);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Create home indicator (horizontal bar)
        JPanel homeIndicator = new JPanel();
        homeIndicator.setPreferredSize(new Dimension(120, 5));
        homeIndicator.setBackground(textColor);
        homeIndicator.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeIndicator.setOpaque(true);

        // Add click listener to return to home screen
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

    /**
     * Creates the application icons panel for the home screen.
     * Displays a 2x2 grid of application icons.
     * @param parentFrame Parent frame for action references
     * @return JPanel containing the application icons
     */
    public static JPanel createAppIconsPanel(JFrame parentFrame) {
        // Create panel with 2x2 grid layout for app icons
        JPanel appIconsPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        appIconsPanel.setBackground(backgroundColor);
        appIconsPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 150, 30));

        // Add app buttons for all available applications
        appIconsPanel.add(createAppButton("Address Book", "/homescreenIcons/addressBookIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Picture Gallery", "/homescreenIcons/galleryIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Finance Tracker", "/homescreenIcons/financeTrackerIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Investify", "/homescreenIcons/investifyIcon.png", parentFrame));

        return appIconsPanel;
    }

    /**
     * Creates an application button with icon and name.
     * Each button opens its corresponding application when clicked.
     * @param appName Name of the application
     * @param iconResourcePath Path to the icon resource
     * @param parentFrame Parent frame for action references
     * @return Configured JButton for the application
     */
    public static JButton createAppButton(String appName, String iconResourcePath, JFrame parentFrame) {
        // Create button with app name
        JButton appButton = new JButton(appName);
        appButton.setFont(new Font("Inter", Font.PLAIN, 12));

        try {
            // Load and scale the app icon
            URL imageUrl = phoneUtils.class.getResource(iconResourcePath);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                appButton.setIcon(new ImageIcon(img));
            } else {
                throw new IOException("Image not found: " + iconResourcePath);
            }
        } catch (Exception e) {
            // Use fallback icon if the app icon cannot be loaded
            System.err.println("Erreur lors du chargement de l'icône : " + iconResourcePath);
            ImageIcon fallbackIcon = (ImageIcon) UIManager.getIcon("FileView.computerIcon");
            if (fallbackIcon != null) {
                Image img = fallbackIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                appButton.setIcon(new ImageIcon(img));
            }
        }

        // Configure button appearance
        appButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        appButton.setHorizontalTextPosition(SwingConstants.CENTER);
        appButton.setForeground(textColor);
        appButton.setBackground(backgroundColor);
        appButton.setFocusPainted(false);
        appButton.setBorderPainted(false);

        // Add action listener to open the corresponding app
        appButton.addActionListener(e -> {
            switch (appName) {
                case "Investify":
                    // Remplacer l'utilisation de InvestifyApp par Investify
                    Investify investify = new Investify();
                    boolean investifyApiKeyOk = investify.ensureApiInitialized();
                    if (investifyApiKeyOk) {
                        Homescreen.cardLayout.show(Homescreen.mainPanel, "Investify");
                    }
                    break;
                case "Address Book":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Address Book");
                    break;
                case "Picture Gallery":
                    // Check for API key before showing gallery
                    TinyPNGService tinyPNGService = new TinyPNGService();
                    boolean apiKeyOk = tinyPNGService.showApiKeyDialog(parentFrame);
                    if (apiKeyOk) {
                        Homescreen.cardLayout.show(Homescreen.mainPanel, "Picture Gallery");
                    }
                    // If not ok, do nothing (user cancelled or invalid key)
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

    /**
     * Creates the complete home screen with all applications.
     * Initializes the main frame, adds all application screens,
     * and configures the top and bottom bars.
     */
    public static void createHomescreen(){

        // Create the main phone frame
        JFrame phoneFrame = phoneUtils.createPhoneFrame("The Phone");

        // Add home screen with app icons
        JPanel homeScreen = createAppIconsPanel(phoneFrame);
        mainPanel.add(homeScreen, "Home");

        // Add individual application screens
        // Investify application
        Investify investify = new Investify();
        JPanel investifyScreen = investify.createInvestify();
        mainPanel.add(investifyScreen, "Investify");

        // Address Book application
        JPanel addressBookScreen = AddressBook.createAddressBook();
        mainPanel.add(addressBookScreen, "Address Book");

        // Picture Gallery application
        PictureGalleryApp pictureGallery = new PictureGalleryApp();
        JPanel pictureGalleryScreen = pictureGallery.createPictureGallery();
        mainPanel.add(pictureGalleryScreen, "Picture Gallery");

        // Finance Tracker application
        FinanceTracker financeTracker = new FinanceTracker();
        JPanel financeTrackerScreen = financeTracker.createFinanceTracker();
        mainPanel.add(financeTrackerScreen, "Finance Tracker");

        // Add the main panel and navigation bars to the phone frame
        phoneFrame.add(mainPanel, BorderLayout.CENTER);
        phoneFrame.add(phoneUtils.createTopBar(), BorderLayout.NORTH);
        phoneFrame.add(phoneUtils.createBottomBar(phoneFrame), BorderLayout.SOUTH);

        // Display the phone frame
        phoneFrame.setVisible(true);

    }
}