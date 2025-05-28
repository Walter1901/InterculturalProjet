package Finance.gui.Panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import Finance.model.Goal;
import Finance.data.GoalStorage;

public class SavingGoalsPanel {
    // list of all saving goals
    private final List<Goal> goals;

    // used to load and save goals to storage
    private final GoalStorage storage = new GoalStorage();

    // main panel that holds everything
    private JPanel mainPanel;

    // design constants for colors, fonts, and layout
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color PRIMARY_COLOR = new Color(0, 122, 255);
    private final Font GOAL_FONT = new Font("Inter", Font.BOLD, 16);
    private final Font DETAIL_FONT = new Font("Inter", Font.PLAIN, 14);
    private final Dimension HEADER_SIZE = new Dimension(400, 40);
    private final int MIN_CARD_WIDTH = 250;
    private final int CARD_HEIGHT = 150;

    // scroll pane for goal cards
    private JScrollPane scrollPane;

    public SavingGoalsPanel() {
        this.goals = storage.loadGoals(); // Load goals from storage
        initializeUI();
    }

    public JPanel createSavingGoalsPanel() {
        return mainPanel;
    }

    private void initializeUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        createHeader();
        scrollPane = createGoalsList();
        mainPanel.add(scrollPane);
    }

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

    // builds the list of goals with scroll support
    private JScrollPane createGoalsList() {
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(BACKGROUND_COLOR);

        // Entfernen Sie die feste Breite und lassen Sie die Karten die volle Breite nutzen
        Dimension cardSize = new Dimension(mainPanel.getWidth() - 30, CARD_HEIGHT); // -30 für Scrollbar-Puffer

        if (goals.isEmpty()) {
            JLabel emptyLabel = new JLabel("No saving goals yet.");
            emptyLabel.setFont(DETAIL_FONT);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardsPanel.add(Box.createVerticalGlue());
            cardsPanel.add(emptyLabel);
            cardsPanel.add(Box.createVerticalGlue());
        } else {
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
        scroll.setPreferredSize(new Dimension(mainPanel.getWidth(), 450)); // Nutzt die volle Breite

        return scroll;
    }

    // makes a single goal card UI with all its info
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

        // show goal name at the top
        JLabel nameLabel = new JLabel(goal.getName());
        nameLabel.setFont(GOAL_FONT);
        nameLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(nameLabel);

        // show date range if available
        if (goal.getStartDate() != null && goal.getEndDate() != null) {
            addDateLabel(card, goal);
        }

        // show progress bar
        addProgressBar(card, goal);

        // show amount info
        addAmountLabel(card, goal);

        // button to add money to this goal
        JButton addAmountButton = new JButton("Add Amount");
        addAmountButton.setFont(new Font("Inter", Font.PLAIN, 12));
        addAmountButton.addActionListener(e -> {
            showAddAmountDialog(goal); // open dialog
            refreshGoalsList();        // update the list visually
            storage.saveGoals(goals); // save to file
        });

        // button to delete this goal
        JButton deleteButton = new JButton("🗑");
        deleteButton.setFont(new Font("Inter", Font.PLAIN, 12));
        deleteButton.setForeground(Color.RED);

        // Aktion beim Klick: Ziel entfernen
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

        // put the buttons on the right side
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.X_AXIS));
        buttonWrapper.setBackground(CARD_COLOR);
        buttonWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        buttonWrapper.add(Box.createHorizontalGlue());
        buttonWrapper.add(addAmountButton);
        buttonWrapper.add(Box.createRigidArea(new Dimension(10, 0))); // Abstand
        buttonWrapper.add(deleteButton);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(buttonWrapper);


        return card;
    }

    // adds a label showing the goal's start and end dates
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

    // adds a progress bar based on how much of the goal is reached
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

    // shows the saved amount compared to the target
    private void addAmountLabel(JPanel card, Goal goal) {
        JLabel amountLabel = new JLabel(String.format("CHF %,.0f / %,.0f",
                goal.getCurrentAmount(),
                goal.getTargetAmount()));
        amountLabel.setFont(DETAIL_FONT);
        amountLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        card.add(amountLabel);
    }

    // creates a blue styled "New Goal" button
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

    // opens a dialog to let the user add a new saving goal
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

    // lets user add money to a goal
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

    // refreshes the scrollable list after changes
    private void refreshGoalsList() {
        mainPanel.remove(scrollPane); // remove old scroll pane
        scrollPane = createGoalsList(); // recreate with updated data
        mainPanel.add(scrollPane); // add it again
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}