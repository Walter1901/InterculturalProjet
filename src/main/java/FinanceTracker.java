import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class FinanceTracker {

    private static JFrame financeApp;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            financeApp = new JFrame("Finance Tracker");
            financeApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            financeApp.setSize(600, 400);
            financeApp.setLocationRelativeTo(null);

            // Create card layout for smooth transitions
            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);

            // Create both panels
            JPanel mainPanel = createFinanceTracker();
            JPanel expensesPanel = createExpensesPage();

            // Add both panels to card layout with unique names
            cardPanel.add(mainPanel, "main");
            cardPanel.add(expensesPanel, "expenses");

            financeApp.add(cardPanel);
            financeApp.setVisible(true);
        });
    }

    public static JPanel createFinanceTracker() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 250));

        // Header with greeting
        JLabel helloLabel = new JLabel("Hello User", SwingConstants.LEFT);
        helloLabel.setFont(new Font("Inter", Font.PLAIN, 18));
        helloLabel.setForeground(new Color(50, 50, 50));
        helloLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
        mainPanel.add(helloLabel, BorderLayout.NORTH);

        // Logo area
        try {
            ImageIcon logoIcon = new ImageIcon("src/main/resources/finance/logo.png");
            Image image = logoIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(image));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
            mainPanel.add(logoLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Logo not found", SwingConstants.CENTER);
            mainPanel.add(errorLabel, BorderLayout.CENTER);
        }

        // Menu section
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(245, 245, 250));
        menuPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Clickable menu items - now properly showing expenses page
        menuPanel.add(createClickableMenuItem("My expenses", " $", () -> {
            cardLayout.show(cardPanel, "expenses");
            financeApp.revalidate();
            financeApp.repaint();
        }));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createClickableMenuItem("Balance overview", "📈", () -> {}));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createClickableMenuItem("Saving goals", "🎯", () -> {}));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createClickableMenuItem("Investment", "📄", () -> {}));

        // Bottom navigation
        JPanel bottomNav = new JPanel(new GridLayout(1, 3));
        bottomNav.setBackground(new Color(230, 230, 250));
        bottomNav.setBorder(new EmptyBorder(5, 5, 5, 5));
        bottomNav.add(createNavButton("Home", "🏠"));
        bottomNav.add(createNavButton("Stock", "📈"));
        bottomNav.add(createNavButton("Account", "👤"));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(245, 245, 250));
        southPanel.add(menuPanel, BorderLayout.CENTER);
        southPanel.add(bottomNav, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private static JPanel createClickableMenuItem(String text, String iconText, Runnable action) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icon
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Inter", Font.PLAIN, 24));
        iconLabel.setForeground(new Color(0, 122, 255));

        // Text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        textLabel.setForeground(new Color(30, 30, 30));

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPanel.add(textLabel);

        itemPanel.add(contentPanel, BorderLayout.WEST);
        itemPanel.add(new JLabel("⋮"), BorderLayout.EAST);

        // Mouse listener with hover effects
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(new Color(230, 240, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(Color.WHITE);
            }
        });

        return itemPanel;
    }

    private static JPanel createExpensesPage() {
        JPanel expensesPanel = new JPanel(new BorderLayout());
        expensesPanel.setBackground(new Color(245, 245, 250));
        expensesPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("My Expenses", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        expensesPanel.add(titleLabel, BorderLayout.NORTH);

        // Expenses list
        JTextArea expensesArea = new JTextArea();
        expensesArea.setFont(new Font("Inter", Font.PLAIN, 14));
        expensesArea.setText(String.join("\n",
                "1. Rent: $1200",
                "2. Groceries: $150",
                "3. Utilities: $100",
                "4. Entertainment: $50"
        ));
        expensesArea.setEditable(false);
        expensesArea.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(expensesArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        expensesPanel.add(scrollPane, BorderLayout.CENTER);

        // Back button - now properly goes back to main panel
        JButton backButton = new JButton("Back to Home");
        backButton.setFont(new Font("Inter", Font.PLAIN, 14));
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(new Color(50, 50, 50));
        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "main");
            financeApp.revalidate();
            financeApp.repaint();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 250));
        buttonPanel.add(backButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        expensesPanel.add(buttonPanel, BorderLayout.SOUTH);

        return expensesPanel;
    }

    private static JPanel createNavButton(String text, String icon) {
        JPanel navButton = new JPanel();
        navButton.setLayout(new BoxLayout(navButton, BoxLayout.Y_AXIS));
        navButton.setBackground(new Color(230, 230, 250));
        navButton.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Inter", Font.PLAIN, 20));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        navButton.add(iconLabel);
        navButton.add(Box.createRigidArea(new Dimension(0, 5)));
        navButton.add(textLabel);

        return navButton;
    }
}