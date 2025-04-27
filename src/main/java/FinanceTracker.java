import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class FinanceTracker {

    private static CardLayout cardLayout = new CardLayout();
    private static JPanel cardPanel = new JPanel(cardLayout);
    private static Map<String, List<String>> monthlyExpenses = new HashMap<>();
    private static JLabel viewAllLabel;
    private static JComboBox<String> monthCombo;
    private static JComboBox<String> yearCombo;
    private static JTextArea expensesArea;

    public static JPanel createFinanceTracker() {
        initializeSampleData();

        JPanel financeApp = new JPanel(new BorderLayout());
        financeApp.setBackground(new Color(245, 245, 250));

        JPanel mainPanel = createMainPanel();
        JPanel expensesPanel = createExpensesPanel();

        cardPanel.add(mainPanel, "main");
        cardPanel.add(expensesPanel, "expenses");

        JPanel bottomNav = createBottomNavigation();
        financeApp.add(cardPanel, BorderLayout.CENTER);
        financeApp.add(bottomNav, BorderLayout.SOUTH);

        return financeApp;
    }

    private static void initializeSampleData() {
        List<String> marchExpenses = new ArrayList<>();
        marchExpenses.add("28.03.2025  Coop, Brig       CHF13.10");
        marchExpenses.add("28.03.2025  SBB Ticket Shop  CHF3.20-");
        marchExpenses.add("24.03.2025  Muller, Brig     CHF3.70-");
        monthlyExpenses.put("March 2025", marchExpenses);

        String[] months = {"January", "February", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String month : months) {
            monthlyExpenses.put(month + " 2025", new ArrayList<>());
        }
    }

    private static JPanel createExpensesPanel() {
        JPanel expensesPanel = new JPanel(new BorderLayout());
        expensesPanel.setBackground(new Color(245, 245, 250));
        expensesPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 250));

        JLabel titleLabel = new JLabel("My expenses", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));

        JPanel datePanel = createDateSelectionPanel();

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(datePanel, BorderLayout.EAST);
        headerPanel.add(titlePanel, BorderLayout.NORTH);

        JButton addButton = createAddExpenseButton();
        headerPanel.add(addButton, BorderLayout.SOUTH);
        expensesPanel.add(headerPanel, BorderLayout.NORTH);

        expensesArea = new JTextArea();
        expensesArea.setFont(new Font("Inter", Font.PLAIN, 14));
        expensesArea.setEditable(false);
        expensesArea.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(expensesArea);
        expensesPanel.add(scrollPane, BorderLayout.CENTER);

        viewAllLabel = createViewAllLabel();
        expensesPanel.add(viewAllLabel, BorderLayout.SOUTH);

        updateExpensesDisplay();

        return expensesPanel;
    }

    private static JPanel createDateSelectionPanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setBackground(new Color(245, 245, 250));

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String[] years = {"2025"};

        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem("March");
        monthCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        yearCombo = new JComboBox<>(years);
        yearCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        monthCombo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        yearCombo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        ActionListener dateChangeListener = e -> updateExpensesDisplay();
        monthCombo.addActionListener(dateChangeListener);
        yearCombo.addActionListener(dateChangeListener);

        datePanel.add(monthCombo);
        datePanel.add(yearCombo);

        return datePanel;
    }

    private static JButton createAddExpenseButton() {
        JButton addButton = new JButton("Add new expense");
        addButton.setBackground(new Color(0, 122, 255));
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        addButton.addActionListener(e -> {
            // Dialog für neue Ausgabe erstellen
            JPanel panel = new JPanel(new GridLayout(3, 2));

            // Aktuelles Datum im Format dd.MM.yyyy
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            JLabel dateLabel = new JLabel("Date:");
            JTextField dateField = new JTextField(currentDate);

            JLabel descLabel = new JLabel("Description:");
            JTextField descField = new JTextField();

            JLabel amountLabel = new JLabel("Amount (CHF):");
            JTextField amountField = new JTextField();

            panel.add(dateLabel);
            panel.add(dateField);
            panel.add(descLabel);
            panel.add(descField);
            panel.add(amountLabel);
            panel.add(amountField);

            int result = JOptionPane.showConfirmDialog(null, panel,
                    "Add New Expense", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String month = (String) monthCombo.getSelectedItem();
                String year = (String) yearCombo.getSelectedItem();
                String key = month + " " + year;

                String newEntry = String.format("%s  %-20s CHF%s",
                        dateField.getText(),
                        descField.getText(),
                        amountField.getText());

                if (!monthlyExpenses.containsKey(key)) {
                    monthlyExpenses.put(key, new ArrayList<>());
                }
                monthlyExpenses.get(key).add(newEntry);
                updateExpensesDisplay();
            }
        });
        return addButton;
    }

    private static JLabel createViewAllLabel() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setForeground(new Color(0, 122, 255));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String month = (String) monthCombo.getSelectedItem();
                String year = (String) yearCombo.getSelectedItem();
                int count = monthlyExpenses.getOrDefault(month + " " + year, new ArrayList<>()).size();
                JOptionPane.showMessageDialog(null, "Showing all " + count +
                        " expenses for " + month + " " + year);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(new Color(0, 80, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(new Color(0, 122, 255));
            }
        });
        return label;
    }

    private static void updateExpensesDisplay() {
        if (viewAllLabel == null || expensesArea == null) {
            return;
        }

        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;

        List<String> currentExpenses = monthlyExpenses.getOrDefault(key, new ArrayList<>());

        StringBuilder sb = new StringBuilder();
        for (String expense : currentExpenses) {
            sb.append(expense).append("\n");
        }
        expensesArea.setText(sb.toString().trim());

        viewAllLabel.setText("View " + currentExpenses.size() + " expenses");
    }

    private static JPanel createBottomNavigation() {
        JPanel navPanel = new JPanel(new GridLayout(1, 3));
        navPanel.setBackground(new Color(230, 230, 250));
        navPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        navPanel.add(createNavButton("Home", "🏠", () -> cardLayout.show(cardPanel, "main")));
        navPanel.add(createNavButton("Stock", "📈", () -> {}));
        navPanel.add(createNavButton("Account", "👤", () -> {}));

        return navPanel;
    }

    private static JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 250));

        // Header
        JLabel helloLabel = new JLabel("Hello User", SwingConstants.LEFT);
        helloLabel.setFont(new Font("Inter", Font.PLAIN, 18));
        helloLabel.setForeground(new Color(50, 50, 50));
        helloLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
        mainPanel.add(helloLabel, BorderLayout.NORTH);

        // Logo
        try {
            ImageIcon logoIcon = new ImageIcon("src/main/resources/finance/logo.png");
            Image image = logoIcon.getImage().getScaledInstance(350, 200, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(image));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
            mainPanel.add(logoLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Logo not found", SwingConstants.CENTER);
            mainPanel.add(errorLabel, BorderLayout.CENTER);
        }

        // Menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(245, 245, 250));
        menuPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        menuPanel.add(createMenuItem("My expenses", " $", () -> cardLayout.show(cardPanel, "expenses")));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createMenuItem("Balance overview", "📈", () -> {}));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createMenuItem("Saving goals", "🎯", () -> {}));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createMenuItem("Investment", "📄", () -> {}));

        mainPanel.add(menuPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private static JPanel createMenuItem(String text, String iconText, Runnable action) {
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

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPanel.add(textLabel);

        itemPanel.add(contentPanel, BorderLayout.WEST);
        itemPanel.add(new JLabel("⋮"), BorderLayout.EAST);

        // Mouse effect
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

    private static JPanel createNavButton(String text, String icon, Runnable action) {
        JPanel navButton = new JPanel();
        navButton.setLayout(new BoxLayout(navButton, BoxLayout.Y_AXIS));
        navButton.setBackground(new Color(230, 230, 250));
        navButton.setBorder(new EmptyBorder(5, 5, 5, 5));
        navButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Inter", Font.PLAIN, 20));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        navButton.add(iconLabel);
        navButton.add(Box.createRigidArea(new Dimension(0, 5)));
        navButton.add(textLabel);

        navButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                navButton.setBackground(new Color(210, 220, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                navButton.setBackground(new Color(230, 230, 250));
            }
        });

        return navButton;
    }
}