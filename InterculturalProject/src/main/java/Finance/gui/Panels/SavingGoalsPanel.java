package Finance.gui.Panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import Finance.model.Goal;
import Finance.data.GoalStorage;

// Panel class for displaying and managing saving goals
public class SavingGoalsPanel {
    // list of all saving goals
    private final List<Goal> goals;

    // used to load and save goals to storage
    private GoalStorage storage = new GoalStorage();

    // main panel that holds everything
    private JPanel mainPanel;

    // design constants for colors, fonts, and layout
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color PRIMARY_COLOR = new Color(0, 122, 255);
    private final Font GOAL_FONT = new Font("Inter", Font.BOLD, 16);
    private final Font DETAIL_FONT = new Font("Inter", Font.PLAIN, 14);
    private final int MIN_CARD_WIDTH = 200;
    private final int CARD_HEIGHT = 150;

    // scroll pane for goal cards
    private JScrollPane scrollPane;

    // Constructor initializes the panel and loads saved goals
    public SavingGoalsPanel() {
        this.storage = new GoalStorage();
        List<Goal> loadedGoals = storage.loadGoals();
        this.goals = new ArrayList<>(loadedGoals != null ? loadedGoals : new ArrayList<>());
        initializeUI();
        refreshGoalsList(); // displays all goals
    }

    // Returns the main panel containing all UI components
    public JPanel createSavingGoalsPanel() {
        // ensures saved data is loaded and UI is up to date
        refreshGoalsList();
        return mainPanel;
    }

    // Initializes the user interface components
    private void initializeUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create header
        createHeader();

        // Create initial scroll pane
        scrollPane = createGoalsList();
        mainPanel.add(scrollPane);
    }

    // Refreshes the list of goals in the UI
    private void refreshGoalsList() {
        mainPanel.removeAll();
        createHeader();
        scrollPane = createGoalsList();
        mainPanel.add(scrollPane);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Creates the header panel with title and add button
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("My Saving Goals");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));

        JButton addButton = createStyledButton();
        addButton.addActionListener(e -> showAddGoalDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);
        mainPanel.add(headerPanel);
    }

    // Creates a scrollable list of goal cards
    private JScrollPane createGoalsList() {
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(BACKGROUND_COLOR);

        Dimension cardSize = new Dimension(300, CARD_HEIGHT); // Fixed width as minimum

        // Show empty state if no goals exist
        if (goals.isEmpty()) {
            JLabel emptyLabel = new JLabel("No saving goals yet.");
            emptyLabel.setFont(DETAIL_FONT);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardsPanel.add(Box.createVerticalGlue());
            cardsPanel.add(emptyLabel);
            cardsPanel.add(Box.createVerticalGlue());
        } else {
            // Create a card for each goal
            for (Goal goal : goals) {
                JPanel card = createGoalCard(goal, cardSize);
                cardsPanel.add(card);
                cardsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(mainPanel.getWidth(), 450));

        return scroll;
    }

    // Creates a single goal card UI component
    private JPanel createGoalCard(Goal goal, Dimension size) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 15, 10, 15)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setPreferredSize(size);
        card.setMaximumSize(size);
        card.setMinimumSize(new Dimension(MIN_CARD_WIDTH, CARD_HEIGHT));

        // Goal name label
        JLabel nameLabel = new JLabel(goal.getName());
        nameLabel.setFont(GOAL_FONT);
        nameLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(nameLabel);

        // Add date range if available
        if (goal.getStartDate() != null && goal.getEndDate() != null) {
            addDateLabel(card, goal);
        }

        // Add progress bar
        addProgressBar(card, goal);

        // Add amount information
        addAmountLabel(card, goal);

        // Button to add money to this goal
        JButton addAmountButton = new JButton("Add Amount");
        addAmountButton.setFont(new Font("Inter", Font.PLAIN, 12));
        addAmountButton.addActionListener(e -> {
            showAddAmountDialog(goal);
            refreshGoalsList();
            storage.saveGoals(goals);
        });

        // Delete button setup
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Inter", Font.BOLD, 12));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69).darker()),
                new EmptyBorder(4, 10, 4, 10)));
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Action when clicked: remove goal
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Do you really want to delete this goal?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                goals.remove(goal);
                storage.saveGoals(goals);
                refreshGoalsList();
            }
        });

        // Button container panel
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.X_AXIS));
        buttonWrapper.setBackground(CARD_COLOR);
        buttonWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        buttonWrapper.add(Box.createHorizontalGlue());
        buttonWrapper.add(addAmountButton);
        buttonWrapper.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonWrapper.add(deleteButton);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(buttonWrapper);

        return card;
    }

    // Adds a date range label to the goal card
    private void addDateLabel(JPanel card, Goal goal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String dateRange = formatter.format(goal.getStartDate()) + " - " +
                formatter.format(goal.getEndDate());
        JLabel dateLabel = new JLabel(dateRange);
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(dateLabel);
    }

    // Adds a progress bar showing goal completion percentage
    private void addProgressBar(JPanel card, Goal goal) {
        int percentage = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBackground(CARD_COLOR);
        progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel percentageLabel = new JLabel(percentage + "%");
        percentageLabel.setFont(new Font("Inter", Font.BOLD, 14));
        percentageLabel.setForeground(PRIMARY_COLOR);
        percentageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressPanel.add(percentageLabel);

        JPanel barWrapper = new JPanel();
        barWrapper.setBackground(CARD_COLOR);
        barWrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(percentage);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setPreferredSize(new Dimension(400, 10));
        progressBar.setBorder(BorderFactory.createEmptyBorder());

        barWrapper.add(progressBar);
        progressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        progressPanel.add(barWrapper);

        card.add(progressPanel);
    }

    // Adds a label showing current vs target amount
    private void addAmountLabel(JPanel card, Goal goal) {
        JLabel amountLabel = new JLabel(String.format("CHF %,.0f / %,.0f",
                goal.getCurrentAmount(),
                goal.getTargetAmount()));
        amountLabel.setFont(DETAIL_FONT);
        amountLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        card.add(amountLabel);
    }

    // Creates a styled button for adding new goals
    private JButton createStyledButton() {
        JButton button = new JButton("New Goal");
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 15, 8, 15)));
        button.setFocusPainted(false);
        return button;
    }

    // Shows dialog for adding a new saving goal
    private void showAddGoalDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        JTextField nameField = new JTextField();
        JTextField targetField = new JTextField();

        panel.add(new JLabel("Goal Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Target Amount (CHF):"));
        panel.add(targetField);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Add New Goal", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                double target = Double.parseDouble(targetField.getText());
                goals.add(new Goal(name, null, null, 0, target));
                storage.saveGoals(goals);
                refreshGoalsList(); // update UI after adding
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Please enter a valid number for target amount",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Shows dialog for adding money to a goal
    private void showAddAmountDialog(Goal goal) {
        String input = JOptionPane.showInputDialog(
                null,
                "Enter amount to add (CHF):",
                "Add Amount",
                JOptionPane.PLAIN_MESSAGE);

        if (input != null) {
            try {
                double amount = Double.parseDouble(input);
                if (amount < 0) throw new NumberFormatException();

                goal.addAmount(amount);
                storage.saveGoals(goals);
                refreshGoalsList(); // redraw the cards
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Please enter a valid positive number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}