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
    private static final Map<String, Double> monthlyBalances = new HashMap<>();
    private static JLabel viewAllLabel;
    private static JComboBox<String> monthCombo;
    private static JComboBox<String> yearCombo;
    private static JTextArea expensesArea;
    private static JTextArea balanceArea;
    private static JComboBox<String> balanceMonthCombo;
    private static JComboBox<String> balanceYearCombo;


    public static JPanel createFinanceTracker() {

        JPanel financeApp = new JPanel(new BorderLayout());
        financeApp.setBackground(new Color(245, 245, 250));

        JPanel mainPanel = createMainPanel();
        JPanel expensesPanel = createExpensesPanel();

        cardPanel.add(mainPanel, "main");
        cardPanel.add(expensesPanel, "expenses");

        JPanel bottomNav = createBottomNavigation();
        financeApp.add(cardPanel, BorderLayout.CENTER);
        financeApp.add(bottomNav, BorderLayout.SOUTH);

        JPanel balancePanel = createBalancePanel();
        cardPanel.add(balancePanel, "balance");


        return financeApp;
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

        // JTextArea mit Zeilenumbruch aktivieren
        expensesArea = new JTextArea();
        expensesArea.setFont(new Font("Inter", Font.PLAIN, 14));
        expensesArea.setEditable(false);
        expensesArea.setBackground(Color.WHITE);
        expensesArea.setLineWrap(true);  // Zeilenumbruch aktivieren
        expensesArea.setWrapStyleWord(true);  // Wortweise umbrechen

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
        String[] years = {"2024", "2025"};

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
                try {
                    // Datum parsen
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate expenseDate = LocalDate.parse(dateField.getText(), formatter);

                    // Monat und Jahr aus dem Datum extrahieren
                    String month = expenseDate.getMonth().toString();
                    month = month.charAt(0) + month.substring(1).toLowerCase(); // "MARCH" -> "March"
                    String year = String.valueOf(expenseDate.getYear());
                    String key = month + " " + year;

                    // Formatierter Eintrag mit fester Breite
                    String newEntry = String.format("%-12s  %-25s CHF%10.2f",
                            dateField.getText(),
                            descField.getText(),
                            Double.parseDouble(amountField.getText()));

                    if (!monthlyExpenses.containsKey(key)) {
                        monthlyExpenses.put(key, new ArrayList<>());
                    }
                    monthlyExpenses.get(key).add(newEntry);

                    // Dropdowns auf das Datum der neuen Ausgabe setzen
                    monthCombo.setSelectedItem(month);
                    yearCombo.setSelectedItem(year);

                    updateExpensesDisplay();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid input. Please check date format (dd.MM.yyyy) and amount.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
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

    private static JPanel createBalancePanel() {
        JPanel balancePanel = new JPanel(new BorderLayout());
        balancePanel.setBackground(new Color(245, 245, 250));
        balancePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 250));

        JLabel title = new JLabel("Balance overview");
        title.setFont(new Font("Inter", Font.BOLD, 15));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Datumsauswahl
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setBackground(new Color(245, 245, 250));

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String[] years = {"2024", "2025"};

        JComboBox<String> balanceMonthCombo = new JComboBox<>(months);
        JComboBox<String> balanceYearCombo = new JComboBox<>(years);
        balanceMonthCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        balanceYearCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        datePanel.add(balanceMonthCombo);
        datePanel.add(balanceYearCombo);

        // Setze aktuellen Monat und Jahr als Standardauswahl
        LocalDate now = LocalDate.now();
        balanceMonthCombo.setSelectedIndex(now.getMonthValue() - 1); // 0-basiert
        balanceYearCombo.setSelectedItem(String.valueOf(now.getYear()));

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(datePanel, BorderLayout.EAST);

        balancePanel.add(headerPanel, BorderLayout.NORTH);

        balanceArea = new JTextArea();
        balanceArea.setFont(new Font("Inter", Font.PLAIN, 14));
        balanceArea.setEditable(false);
        balanceArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(balanceArea);
        balancePanel.add(scrollPane, BorderLayout.CENTER);

        // Listener für Änderungen am Monat
        balanceMonthCombo.addItemListener(e -> updateBalanceArea(balanceArea, balanceMonthCombo, balanceYearCombo));

        // Listener für Änderungen am Jahr
        balanceYearCombo.addItemListener(e -> updateBalanceArea(balanceArea, balanceMonthCombo, balanceYearCombo));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(245, 245, 250));

        // Button für Ausgaben
        JButton addExpenseButton = new JButton("Add expense");
        addExpenseButton.setBackground(new Color(0, 122, 255));
        addExpenseButton.setForeground(Color.WHITE);
        addExpenseButton.addActionListener(e -> {
            JPanel inputPanel = new JPanel(new GridLayout(2, 2));
            inputPanel.add(new JLabel("Description:"));
            JTextField descField = new JTextField();
            inputPanel.add(descField);
            inputPanel.add(new JLabel("Amount (CHF):"));
            JTextField amountField = new JTextField();
            inputPanel.add(amountField);

            int result = JOptionPane.showConfirmDialog(null, inputPanel,
                    "Add Expense Entry", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String month = (String) balanceMonthCombo.getSelectedItem();
                    String year = (String) balanceYearCombo.getSelectedItem();
                    String key = month + " " + year;

                    String entry = String.format("%-25s %10s", descField.getText(), "CHF" + amountField.getText());

                    if (!monthlyExpenses.containsKey(key)) {
                        monthlyExpenses.put(key, new ArrayList<>());
                    }
                    monthlyExpenses.get(key).add(entry);

                    updateBalanceArea(balanceArea, balanceMonthCombo, balanceYearCombo);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Button für Kontostand
        JButton enterBalanceButton = new JButton("Enter account balance");
        enterBalanceButton.setBackground(new Color(0, 122, 255));
        enterBalanceButton.setForeground(Color.WHITE);
        enterBalanceButton.addActionListener(e -> {
            String month = (String) balanceMonthCombo.getSelectedItem();
            String year = (String) balanceYearCombo.getSelectedItem();
            String key = month + " " + year;

            String input = JOptionPane.showInputDialog(null,
                    "Enter your account balance for " + key + ":", "Account Balance",
                    JOptionPane.PLAIN_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                try {
                    double balance = Double.parseDouble(input.trim());
                    monthlyBalances.put(key, balance);
                    updateBalanceArea(balanceArea, balanceMonthCombo, balanceYearCombo);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid number format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Button zum Aktualisieren
        JButton refreshButton = new JButton("Refresh overview");
        refreshButton.setBackground(new Color(0, 122, 255));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> updateBalanceArea(balanceArea, balanceMonthCombo, balanceYearCombo));

        bottomPanel.add(addExpenseButton);
        bottomPanel.add(enterBalanceButton);
        bottomPanel.add(refreshButton);
        balancePanel.add(bottomPanel, BorderLayout.SOUTH);

        updateBalanceArea(balanceArea, balanceMonthCombo, balanceYearCombo);
        return balancePanel;
    }

    private static void updateBalanceArea(JTextArea balanceArea, JComboBox<String> monthCombo, JComboBox<String> yearCombo) {
        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;

        // Monospaced Font für exakte Ausrichtung
        balanceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Balance Overview for %s%n%n", key));

        // Kontostand
        double balance = monthlyBalances.getOrDefault(key, 0.0);
        sb.append(String.format("%-20s CHF%10.2f%n", "Account Balance:", balance));
        sb.append("\n");

        // Berechne Gesamtausgaben
        List<String> expenses = monthlyExpenses.getOrDefault(key, new ArrayList<>());
        double totalExpenses = 0.0;
        for (String entry : expenses) {
            try {
                // Extrahiere den Betrag aus dem Eintrag
                int chfIndex = entry.indexOf("CHF");
                if (chfIndex >= 0) {
                    String amountStr = entry.substring(chfIndex + 3).trim();
                    amountStr = amountStr.replace(".", "").replace(",", ".");  // Entfernt Tausenderpunkt, ersetzt Komma durch Punkt
                    totalExpenses += Double.parseDouble(amountStr);

                }
            } catch (Exception ex) {
                System.err.println("Error parsing amount from entry: " + entry);
                ex.printStackTrace();
            }
        }

        // Nur das Total anzeigen
        sb.append(String.format("%-20s CHF%10.2f%n", "Total Expenses:", totalExpenses));

        double remaining = balance - totalExpenses;
        sb.append(String.format("%-20s CHF%10.2f%n", "Remaining Balance:", remaining));

        balanceArea.setText(sb.toString());
    }

    private static void updateExpensesDisplay() {
        if (viewAllLabel == null || expensesArea == null) {
            return;
        }

        // Monospaced Font für exakte Ausrichtung
        expensesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;

        List<String> currentExpenses = monthlyExpenses.getOrDefault(key, new ArrayList<>());

        StringBuilder sb = new StringBuilder();
        for (String expense : currentExpenses) {
            // Teile den Eintrag in Datum, Beschreibung und Betrag auf
            String[] parts = expense.split("CHF");
            String dateAndDesc = parts[0].trim();
            String amount = parts.length > 1 ? "CHF" + parts[1].trim() : "CHF0.00";

            // Finde die Position des letzten Leerzeichens, um Datum und Beschreibung zu trennen
            int lastSpace = dateAndDesc.lastIndexOf("  ");
            String date = lastSpace >= 0 ? dateAndDesc.substring(0, lastSpace).trim() : "";
            String description = lastSpace >= 0 ? dateAndDesc.substring(lastSpace).trim() : dateAndDesc;

            // Formatierte Ausgabe mit fester Breite
            sb.append(String.format("%-12s %-25s %10s%n", date, description, amount));
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
        menuPanel.add(createMenuItem("Balance overview", "📈", () -> {
            cardLayout.show(cardPanel, "balance");
            // automatisches Update beim Wechsel
            updateBalanceArea(balanceArea, balanceMonthCombo, balanceYearCombo);
        }));
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