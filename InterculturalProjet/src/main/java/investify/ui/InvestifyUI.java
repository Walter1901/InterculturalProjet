package investify.ui; // Defines the package for this UI class

// Imports for AlphaVantage API components for stock data

import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.OutputSize;
// Import for the main application class
import investify.app.Investify;
// Import for the transaction model
import investify.model.Transaction;

// Imports for Java Swing UI components
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
// Import for file operations
import java.io.*;
// Import for data structure
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import shared.RecurringInvestment;

/**
 * InvestifyUI - Main user interface controller for the Investify application.
 * This class manages all UI components and screens including:
 * - Home screen with portfolio overview and charts
 * - Stock search functionality with price lookup
 * - Portfolio view showing current holdings
 * - Recurring investments management
 * - Account settings management
 */
public class InvestifyUI { // Main UI class that manages all interface components
    private final Investify app; // Reference to the main application instance

    // Card layout and panels for switching between screens
    private CardLayout mainCardLayout; // Layout manager to switch between different screens
    private JPanel mainPanel; // Main container panel that holds all screens

    // UI components that need to be accessed between methods
    private JLabel portfolioValueLabel; // Label to display the total portfolio value

    /**
     * Constructs a new InvestifyUI with the provided application reference.
     * Initializes the UI manager with access to the application services.
     *
     * @param app The parent Investify application providing access to services
     */
    public InvestifyUI(Investify app) {
        this.app = app; // Store reference to the main application
    }

    /**
     * Creates the main panel containing all application screens.
     * This method initializes and configures the card layout and adds
     * all screens to the main panel.
     *
     * @return A JPanel containing all application screens in a card layout
     */
    public JPanel createMainPanel() {
        // Initialize main panel with card layout
        mainCardLayout = new CardLayout(); // Create new card layout for switching screens
        mainPanel = new JPanel(mainCardLayout); // Create main panel with card layout

        // Create different screens
        JPanel homeScreen = createHomeScreen(); // Create the home/dashboard screen
        JPanel searchScreen = createSearchScreen(); // Create the stock search screen
        JPanel portfolioScreen = createPortfolioScreen(); // Create the portfolio details screen
        JPanel recurrentScreen = createRecurrentScreen(); // Create the recurring investments screen
        JPanel accountScreen = createAccountScreen(); // Create the account settings screen

        // Add screens to main panel with identifying names
        mainPanel.add(homeScreen, "home"); // Add home screen with "home" identifier
        mainPanel.add(searchScreen, "search"); // Add search screen with "search" identifier
        mainPanel.add(portfolioScreen, "portfolio"); // Add portfolio screen with "portfolio" identifier
        mainPanel.add(recurrentScreen, "recurrent"); // Add recurrent screen with "recurrent" identifier
        mainPanel.add(accountScreen, "account"); // Add account screen with "account" identifier

        // Initialize with home screen
        mainCardLayout.show(mainPanel, "home"); // Display the home screen initially

        return mainPanel; // Return the configured main panel
    }

    /**
     * Creates the home screen with portfolio overview.
     * This screen displays the total portfolio value and a pie chart
     * showing the distribution of investments.
     *
     * @return A JPanel representing the home screen
     */
    private JPanel createHomeScreen() {
        JPanel homeMain = new JPanel(new GridBagLayout()); // Create home screen with GridBagLayout
        homeMain.setName("home"); // Set panel name for identification
        homeMain.setBackground(Investify.backgroundColor); // Set background color from app theme

        // Layout setup
        GridBagConstraints gbc = new GridBagConstraints(); // Create constraints for positioning components
        gbc.gridx = 0; // Set horizontal position to leftmost column
        gbc.gridwidth = 1; // Components span one column
        gbc.fill = GridBagConstraints.NONE; // Components don't resize to fill available space
        gbc.anchor = GridBagConstraints.NORTH; // Anchor components to top of cell
        gbc.insets = new Insets(5, 0, 5, 0); // Add padding (top, left, bottom, right)

        // Title
        JLabel titleLabel = new JLabel("Portfolio"); // Create title label with text
        titleLabel.setForeground(Investify.textColor); // Set text color from app theme
        titleLabel.setFont(new Font("Inter", Font.BOLD, 32)); // Set bold font with size 32
        gbc.gridy = 0; // Set to first row
        homeMain.add(titleLabel, gbc); // Add title to panel with constraints

        // Portfolio value
        portfolioValueLabel = new JLabel("Total value: " + app.getPortfolioManager().getPortfolioValueFormatted()); // Create and set label with formatted value
        portfolioValueLabel.setForeground(Investify.textColor); // Set text color from app theme
        portfolioValueLabel.setFont(new Font("Inter", Font.BOLD, 24)); // Set bold font with size 24
        gbc.gridy = 1; // Move to next row
        gbc.insets = new Insets(5, 0, 15, 0); // Adjust padding with more space at bottom
        homeMain.add(portfolioValueLabel, gbc); // Add portfolio value label to panel

        // Pie chart
        JPanel pieChartPanel = app.getChartService().createPieChartPanel(); // Create pie chart showing portfolio allocation
        gbc.gridy = 2; // Move to third row
        gbc.fill = GridBagConstraints.BOTH; // Allow component to fill available space horizontally and vertically
        gbc.weighty = 1; // Give vertical space weight to this component
        gbc.insets = new Insets(5, 0, 5, 0); // Reset to default padding
        homeMain.add(pieChartPanel, gbc); // Add pie chart panel to main panel

        // Navigation bar
        gbc.gridy = 3; // Move to fourth row
        gbc.fill = GridBagConstraints.HORIZONTAL; // Let component fill available horizontal space
        gbc.weighty = 0; // No vertical weight
        gbc.anchor = GridBagConstraints.SOUTH; // Anchor to bottom of the space
        homeMain.add(createNavBar(mainCardLayout, mainPanel), gbc); // Add navigation bar at bottom

        return homeMain; // Return the configured home screen panel
    }

    /**
     * Creates the search screen for finding and trading stocks.
     * This screen includes a search field to look up stock symbols,
     * displays current price data, and provides buy/sell options.
     *
     * @return A JPanel representing the search screen
     */
    private JPanel createSearchScreen() {
        JPanel searchMain = new JPanel(new GridBagLayout()); // Create search screen with GridBagLayout
        searchMain.setBackground(Investify.backgroundColor); // Set background color from app theme

        GridBagConstraints gbc = new GridBagConstraints(); // Create constraints for positioning components
        gbc.gridx = 0; // Set horizontal position to leftmost column
        gbc.gridwidth = 1; // Components span one column
        gbc.insets = new Insets(5, 0, 5, 0); // Add padding (top, left, bottom, right)
        gbc.fill = GridBagConstraints.NONE; // Components don't resize to fill available space
        gbc.anchor = GridBagConstraints.NORTH; // Anchor components to top of cell

        JLabel searchLabel = new JLabel("Page Search", SwingConstants.CENTER); // Create title label with centered text
        searchLabel.setForeground(Investify.textColor); // Set text color from app theme
        searchLabel.setFont(new Font("Inter", Font.BOLD, 24)); // Set bold font with size 24
        gbc.gridy = 0; // Set to first row
        searchMain.add(searchLabel, gbc); // Add title to panel with constraints

        JLabel subtitleSearch = new JLabel("Search for symbols", SwingConstants.CENTER); // Create subtitle with centered text
        subtitleSearch.setForeground(Investify.textColor); // Set text color from app theme
        subtitleSearch.setFont(new Font("Inter", Font.PLAIN, 18)); // Set regular font with size 18
        gbc.gridy = 1; // Move to second row
        searchMain.add(subtitleSearch, gbc); // Add subtitle to panel

        JTextField searchBar = new JTextField(); // Create search input field
        searchBar.setPreferredSize(new Dimension(250, 30)); // Set preferred size for search bar
        searchBar.setFont(new Font("Inter", Font.PLAIN, 16)); // Set font for input text
        searchBar.setBackground(Color.WHITE); // Set white background for search field
        searchBar.setForeground(Color.BLACK); // Set black text for better readability
        searchBar.setBorder(BorderFactory.createCompoundBorder( // Create compound border with
                BorderFactory.createLineBorder(Color.GRAY, 1), // Gray outer border 1px wide and
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Inner padding for text
        ));

        gbc.gridy = 2; // Move to third row
        gbc.insets = new Insets(10, 0, 10, 0); // Add more vertical padding
        searchMain.add(searchBar, gbc); // Add search bar to panel

        JLabel searchResults = new JLabel("", SwingConstants.CENTER); // Create empty label for search results
        searchResults.setForeground(Investify.textColor); // Set text color from app theme
        searchResults.setFont(new Font("Inter", Font.PLAIN, 18)); // Set regular font with size 18
        gbc.gridy = 3; // Move to fourth row
        gbc.weighty = 1; // Give vertical weight to this component
        searchMain.add(searchResults, gbc); // Add results label to panel

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Create panel for buttons with centered flow layout
        buttonPanel.setBackground(Investify.backgroundColor); // Set background color from app theme
        buttonPanel.setVisible(false); // Hide initially until search is performed

        // Set up search action
        searchBar.addActionListener(e -> { // Add action listener for when user presses Enter
            String symbol = searchBar.getText().trim(); // Get and clean search text
            buttonPanel.setVisible(false); // Hide button panel initially

            if (!symbol.isEmpty()) { // Check if search is not empty
                try {
                    // Request stock data from API
                    TimeSeriesResponse response = AlphaVantage.api() // Get API instance
                            .timeSeries() // Access time series data
                            .daily() // Use daily price data
                            .forSymbol(symbol) // Set the stock symbol to search
                            .outputSize(OutputSize.COMPACT) // Use compact output (last 100 data points)
                            .fetchSync(); // Execute request synchronously

                    if (response.getErrorMessage() == null) { // Check if response has no error
                        response.getStockUnits().stream() // Stream the stock units data
                                .findFirst() // Get the most recent data point
                                .ifPresentOrElse( // Process if exists, otherwise show error
                                        unit -> { // For successful data retrieval
                                            searchResults.setText("Last price: " + unit.getClose() + " USD"); // Display closing price
                                            buttonPanel.setVisible(true); // Show buy/sell buttons
                                        },
                                        () -> searchResults.setText("No data available for this symbol.") // Show message if no data points
                                );
                    } else {
                        searchResults.setText("ERROR: Symbol not found"); // Show error if API returned an error message
                    }
                } catch (Exception ex) { // Catch any exceptions during API call
                    searchResults.setText("Error retrieving data."); // Display generic error message
                    ex.printStackTrace(); // Print stack trace for debugging
                }
            } else {
                searchResults.setText("Please enter a valid symbol."); // Show validation message for empty input
            }
        });

        // Create Buy button
        JButton buyButton = new JButton("Buy"); // Create button with Buy label
        buyButton.setFont(new Font("Inter", Font.BOLD, 16)); // Set bold font for emphasis
        buyButton.setBackground(new Color(50, 205, 50)); // Set green background color
        buyButton.setForeground(Investify.textColor); // Set text color from app theme
        buyButton.setFocusPainted(false); // Remove focus highlight

        // Create Sell button
        JButton sellButton = new JButton("Sell"); // Create button with Sell label
        sellButton.setFont(new Font("Inter", Font.BOLD, 16)); // Set bold font for emphasis
        sellButton.setBackground(new Color(255, 62, 65)); // Set red background color
        sellButton.setForeground(Investify.textColor); // Set text color from app theme
        sellButton.setFocusPainted(false); // Remove focus highlight

        buttonPanel.add(buyButton); // Add buy button to button panel
        buttonPanel.add(sellButton); // Add sell button to button panel

        gbc.gridy = 4; // Move to fifth row
        gbc.weighty = 0; // Reset vertical weight
        searchMain.add(buttonPanel, gbc); // Add button panel to main panel

        // Set up button actions
        buyButton.addActionListener(e -> { // Add click handler for buy button
            String symbol = searchBar.getText().trim(); // Get current symbol from search field
            double price = 0.0; // Initialize price variable

            String resultText = searchResults.getText(); // Get current results text
            if (resultText.startsWith("Last price:")) { // Check if results contain price info
                try {
                    String priceText = resultText.substring(12, resultText.indexOf(" USD")); // Extract price text
                    price = Double.parseDouble(priceText); // Parse price as double
                } catch (Exception ex) {
                    // Default to 0 if parsing fails
                }
            }

            app.getTransactionService().openTransactionDialog("Buy", symbol, price, searchMain); // Open buy transaction dialog
        });

        sellButton.addActionListener(e -> { // Add click handler for sell button
            String symbol = searchBar.getText().trim(); // Get current symbol from search field
            double price = 0.0; // Initialize price variable

            String resultText = searchResults.getText(); // Get current results text
            if (resultText.startsWith("Last price:")) { // Check if results contain price info
                try {
                    String priceText = resultText.substring(12, resultText.indexOf(" USD")); // Extract price text
                    price = Double.parseDouble(priceText); // Parse price as double
                } catch (Exception ex) {
                    // Default to 0 if parsing fails
                }
            }

            app.getTransactionService().openTransactionDialog("Sell", symbol, price, searchMain); // Open sell transaction dialog
        });

        // Navigation bar
        gbc.gridy = 5; // Move to sixth row
        gbc.fill = GridBagConstraints.HORIZONTAL; // Let component fill available horizontal space
        gbc.weighty = 0; // No vertical weight
        gbc.anchor = GridBagConstraints.SOUTH; // Anchor to bottom of the space
        searchMain.add(createNavBar(mainCardLayout, mainPanel), gbc); // Add navigation bar at bottom

        return searchMain; // Return the configured search screen panel
    }

    /**
     * Creates the portfolio screen showing detailed holdings.
     * This screen displays a list of all stocks in the user's portfolio
     * with layout and navigation components.
     *
     * @return A JPanel representing the portfolio screen
     */
    private JPanel createPortfolioScreen() {
        // Create the main panel of the portfolio with BorderLayout
        JPanel portfolioMain = new JPanel(new BorderLayout()); // Create portfolio screen with border layout
        portfolioMain.setName("portfolio"); // Set panel name for identification
        portfolioMain.setBackground(Investify.backgroundColor); // Set background color from app theme

        // Create and add the title for the portfolio screen
        JLabel portfolioLabel = new JLabel("Your Portfolio", SwingConstants.CENTER); // Create centered title
        portfolioLabel.setForeground(Investify.textColor); // Set text color from app theme
        portfolioLabel.setFont(new Font("Inter", Font.BOLD, 24)); // Set bold font with size 24

        // Create a header panel for the portfolio title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Create header panel with centered flow layout
        headerPanel.setBackground(Investify.backgroundColor); // Set background color from app theme
        headerPanel.add(portfolioLabel); // Add title to header panel

        // Add components to the portfolio screen
        portfolioMain.add(headerPanel, BorderLayout.NORTH); // Add header at the top
        portfolioMain.add(createPortfolioContent(), BorderLayout.CENTER); // Add portfolio content in the center
        portfolioMain.add(createNavBar(mainCardLayout, mainPanel), BorderLayout.SOUTH); // Add navigation at the bottom

        return portfolioMain; // Return the configured portfolio screen
    }

    /**
     * Creates the content panel for the portfolio screen.
     * This method reads transaction data from storage and displays
     * the current holdings with quantities and values.
     *
     * @return A JPanel containing the portfolio content
     */
    private JPanel createPortfolioContent() {
        // Create the main content panel
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Investify.backgroundColor);

        // Get the path to the transaction data file
        String filePath = System.getProperty("user.home") + "/investifyData.json";
        File file = new File(filePath);

        if (file.exists()) {
            try {
                // Read transactions from file
                Transaction[] transactions = app.getTransactionService().readTransactionsFromFile(file);

                if (transactions != null && transactions.length > 0) {
                    // Group stocks by symbol
                    Map<String, Integer> holdings = app.getPortfolioManager().getHoldings();
                    Map<String, Double> totalValues = app.getPortfolioManager().getTotalValues();

                    if (!holdings.isEmpty()) {
                        // Create a vertical panel to display stocks
                        JPanel holdingsPanel = new JPanel();
                        holdingsPanel.setLayout(new BoxLayout(holdingsPanel, BoxLayout.Y_AXIS));
                        holdingsPanel.setBackground(Investify.backgroundColor);

                        for (String symbol : holdings.keySet()) {
                            int quantity = holdings.get(symbol);
                            double totalValue = totalValues.get(symbol);

                            // Create a panel for each stock with a border for visual separation
                            JPanel stockPanel = new JPanel(new BorderLayout());
                            stockPanel.setBackground(Investify.backgroundColor);
                            stockPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));
                            stockPanel.setPreferredSize(new Dimension(300, 60));
                            stockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                            // Stock symbol (left side)
                            JLabel symbolLabel = new JLabel(symbol);
                            symbolLabel.setForeground(Investify.textColor);
                            symbolLabel.setFont(new Font("Inter", Font.BOLD, 20));
                            symbolLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

                            // Panel for stock information (right side)
                            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
                            infoPanel.setBackground(Investify.backgroundColor);

                            // Number of shares
                            JLabel quantityLabel = new JLabel(quantity + " shares");
                            quantityLabel.setForeground(Investify.textColor);
                            quantityLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                            quantityLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                            // Total value
                            JLabel valueLabel = new JLabel(app.getCurrencyService().formatCurrency(totalValue));
                            valueLabel.setForeground(Investify.textColor);
                            valueLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                            // Add components to panels
                            infoPanel.add(quantityLabel);
                            infoPanel.add(valueLabel);
                            infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

                            stockPanel.add(symbolLabel, BorderLayout.WEST);
                            stockPanel.add(infoPanel, BorderLayout.EAST);

                            holdingsPanel.add(stockPanel);
                            holdingsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                        }

                        // Add scrolling functionality for many stocks
                        JScrollPane scrollPane = new JScrollPane(holdingsPanel);
                        scrollPane.setBorder(null);
                        scrollPane.getViewport().setBackground(Investify.backgroundColor);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        content.add(scrollPane, BorderLayout.CENTER);
                    } else {
                        // Display a message if there are no active holdings
                        JLabel noHoldingsLabel = new JLabel("No shares found", SwingConstants.CENTER);
                        noHoldingsLabel.setForeground(Investify.textColor);
                        noHoldingsLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                        content.add(noHoldingsLabel, BorderLayout.CENTER);
                    }
                } else {
                    // Display a message if no transactions exist
                    JLabel noDataLabel = new JLabel("No transactions found.", SwingConstants.CENTER);
                    noDataLabel.setForeground(Investify.textColor);
                    noDataLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                    content.add(noDataLabel, BorderLayout.CENTER);
                }
            } catch (Exception e) {
                // Handle errors when loading data
                e.printStackTrace();
                JLabel errorLabel = new JLabel("Error loading data.", SwingConstants.CENTER);
                errorLabel.setForeground(Investify.textColor);
                errorLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                content.add(errorLabel, BorderLayout.CENTER);
            }
        } else {
            // Display a message if the data file doesn't exist
            JLabel noFileLabel = new JLabel("Data file not found.", SwingConstants.CENTER);
            noFileLabel.setForeground(Investify.textColor);
            noFileLabel.setFont(new Font("Inter", Font.PLAIN, 18));
            content.add(noFileLabel, BorderLayout.CENTER);
        }

        return content;
    }

    /**
     * Creates the recurrent investments screen.
     * This screen allows users to set up automatic recurring investments
     * (placeholder for future functionality).
     *
     * @return A JPanel representing the recurrent investments screen
     */
    private JPanel createRecurrentScreen() {
        // Create main panel with GridBagLayout for flexible component positioning
        JPanel recurrentMain = new JPanel(new GridBagLayout());
        recurrentMain.setBackground(Investify.backgroundColor);
        recurrentMain.setName("recurrent");

        // Configure layout constraints for component placement
        GridBagConstraints gbcRecurrent = new GridBagConstraints();
        gbcRecurrent.gridx = 0;
        gbcRecurrent.gridy = 0;
        gbcRecurrent.fill = GridBagConstraints.NONE;
        gbcRecurrent.anchor = GridBagConstraints.NORTH;
        gbcRecurrent.insets = new Insets(10, 0, 10, 0);

        // Create and add the screen title
        JLabel recurrentLabel = new JLabel("Recurrent Investments", SwingConstants.CENTER);
        recurrentLabel.setForeground(Investify.textColor);
        recurrentLabel.setFont(new Font("Inter", Font.BOLD, 24));
        recurrentMain.add(recurrentLabel, gbcRecurrent);

        // Create a "Refresh" button to reload investment data
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setForeground(Investify.textColor);
        refreshButton.setBackground(new Color(60, 60, 65));
        refreshButton.setFont(new Font("Inter", Font.BOLD, 14));
        refreshButton.addActionListener(e -> {
            // Force reload from file system by clearing any cached data
            System.out.println("Refreshing recurring investments from shared file");

            // Find and replace the current panel with a completely fresh one
            int index = -1;
            for (int i = 0; i < mainPanel.getComponentCount(); i++) {
                Component c = mainPanel.getComponent(i);
                if (c instanceof JPanel && ((JPanel) c).getName() != null
                        && ((JPanel) c).getName().equals("recurrent")) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                mainPanel.remove(index);
                JPanel freshScreen = createRecurrentScreen();
                mainPanel.add(freshScreen, "recurrent", index);
                mainCardLayout.show(mainPanel, "recurrent");

                // Explicitly request repainting
                mainPanel.revalidate();
                mainPanel.repaint();
                System.out.println("Recurring investments view refreshed");
            }
        });

        // Add the refresh button to a container panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Investify.backgroundColor);
        buttonPanel.add(refreshButton);

        // Add the button panel to the main screen
        gbcRecurrent.gridy = 1;
        gbcRecurrent.weighty = 0;
        recurrentMain.add(buttonPanel, gbcRecurrent);

        // Execute recurring investments and display status
        int processedCount = executeRecurringInvestments();

        // Add execution status display
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBackground(Investify.backgroundColor);

        String statusMessage = processedCount > 0
                ? processedCount + " investment(s) executed today"
                : "No investments due today";

        JLabel statusLabel = new JLabel(statusMessage);
        statusLabel.setForeground(processedCount > 0 ? new Color(0, 200, 83) : Investify.textColor);
        statusLabel.setFont(new Font("Inter", Font.BOLD, 14));
        statusPanel.add(statusLabel);

        // Add status panel to its own grid position
        gbcRecurrent.gridy = 2;
        recurrentMain.add(statusPanel, gbcRecurrent);

        // Create a panel to hold all investment cards
        JPanel investmentsPanel = new JPanel();
        investmentsPanel.setLayout(new BoxLayout(investmentsPanel, BoxLayout.Y_AXIS));
        investmentsPanel.setBackground(Investify.backgroundColor);

        // Import recurring investments data from Finance Tracker application
        List<RecurringInvestment> importedInvestments = importRecurringInvestments();

        if (!importedInvestments.isEmpty()) {
            // Create and add cards for each imported investment
            for (RecurringInvestment inv : importedInvestments) {
                JPanel investmentCard = createInvestmentCard(inv);
                investmentsPanel.add(investmentCard);
                investmentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } else {
            // Display a message when no investments are found
            JLabel noInvestmentsLabel = new JLabel("No recurring investments found", SwingConstants.CENTER);
            noInvestmentsLabel.setForeground(Investify.textColor);
            noInvestmentsLabel.setFont(new Font("Inter", Font.ITALIC, 16));
            noInvestmentsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            investmentsPanel.add(noInvestmentsLabel);
        }

        // Add scrolling capability for many investment cards
        JScrollPane scrollPane = new JScrollPane(investmentsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Investify.backgroundColor);

        // Add the scrollable investments panel to the next grid position
        // to prevent overlapping with the status message
        gbcRecurrent.gridy = 3;  // Increment grid position to avoid overlap
        gbcRecurrent.fill = GridBagConstraints.BOTH;
        gbcRecurrent.weighty = 1;
        recurrentMain.add(scrollPane, gbcRecurrent);

        // Adjust navigation bar position to come after the scroll pane
        gbcRecurrent.gridy = 4;  // Move navigation bar to the last position
        gbcRecurrent.fill = GridBagConstraints.HORIZONTAL;
        gbcRecurrent.weighty = 0;
        gbcRecurrent.anchor = GridBagConstraints.SOUTH;
        gbcRecurrent.insets = new Insets(5, 0, 5, 0);
        recurrentMain.add(createNavBar(mainCardLayout, mainPanel), gbcRecurrent);

        return recurrentMain;
    }

    // Helper method to create an investment card
    private JPanel createInvestmentCard(RecurringInvestment investment) {
        // Create a card panel with vertical layout for investment details
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(45, 45, 50));
        card.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 75), 1));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Create header panel for investment symbol and name
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 45, 50));

        // Add the stock symbol on the left in green
        JLabel symbolLabel = new JLabel(investment.getSymbol());
        symbolLabel.setForeground(new Color(0, 200, 83));
        symbolLabel.setFont(new Font("Inter", Font.BOLD, 18));
        symbolLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        // Add the investment name on the right
        JLabel nameLabel = new JLabel(investment.getName());
        nameLabel.setForeground(Investify.textColor);
        nameLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        // Add symbol and name to the header panel
        headerPanel.add(symbolLabel, BorderLayout.WEST);
        headerPanel.add(nameLabel, BorderLayout.EAST);
        card.add(headerPanel);

        // Create panel for investment amount and frequency
        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setBackground(new Color(45, 45, 50));

        // Add the formatted investment amount on the left
        String formattedAmount = app.getCurrencyService().formatCurrency(investment.getAmount());
        JLabel amountLabel = new JLabel(formattedAmount);
        amountLabel.setForeground(Investify.textColor);
        amountLabel.setFont(new Font("Inter", Font.BOLD, 16));
        amountLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        // Add the investment frequency on the right
        JLabel frequencyLabel = new JLabel(investment.getFrequency());
        frequencyLabel.setForeground(new Color(150, 150, 150));
        frequencyLabel.setFont(new Font("Inter", Font.ITALIC, 14));
        frequencyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        frequencyLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        // Add amount and frequency to the amount panel
        amountPanel.add(amountLabel, BorderLayout.WEST);
        amountPanel.add(frequencyLabel, BorderLayout.EAST);
        card.add(amountPanel);

        // Create panel for the investment start date
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setBackground(new Color(45, 45, 50));

        // Add the formatted start date
        JLabel startDateLabel = new JLabel("Start date: " +
                investment.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        startDateLabel.setForeground(new Color(150, 150, 150));
        startDateLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        startDateLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Add start date to the date panel
        datePanel.add(startDateLabel, BorderLayout.WEST);
        card.add(datePanel);

        return card;
    }

    // Add these methods directly to InvestifyUI.java

    /**
     * Executes all pending recurring investments that should run today.
     * Prevents duplicate executions on the same day by tracking execution history.
     *
     * @return Number of investments executed
     */
    private int executeRecurringInvestments() {
        int executedCount = 0;
        List<RecurringInvestment> investments = importRecurringInvestments();
        LocalDate today = LocalDate.now();

        // Load or create execution history file
        Map<String, LocalDate> executionHistory = new HashMap<>();
        String historyPath = Paths.get(System.getProperty("user.home"), "investifyExecutionHistory.dat").toString();
        File historyFile = new File(historyPath);

        // Load existing execution history if available
        if (historyFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(historyFile))) {
                executionHistory = (Map<String, LocalDate>) in.readObject();
                System.out.println("Loaded execution history with " + executionHistory.size() + " entries");
            } catch (Exception e) {
                System.err.println("Error loading execution history: " + e.getMessage());
            }
        }

        for (RecurringInvestment investment : investments) {
            // Create unique identifier for this investment
            String investmentKey = investment.getSymbol() + "-" + investment.getName();

            // Check if investment should execute today based on schedule
            if (shouldExecuteToday(investment, today)) {
                // Check if it has already executed today
                LocalDate lastExecution = executionHistory.get(investmentKey);

                if (lastExecution == null || !lastExecution.equals(today)) {
                    // Not yet executed today - proceed with execution
                    executeSingleInvestment(investment);
                    executedCount++;

                    // Mark as executed today
                    executionHistory.put(investmentKey, today);
                    System.out.println("Executed and marked: " + investmentKey);
                } else {
                    // Already executed today - skip
                    System.out.println("Skipping already executed: " + investmentKey);
                }
            }
        }

        // Save updated execution history
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(historyFile))) {
            out.writeObject(executionHistory);
            System.out.println("Saved execution history with " + executionHistory.size() + " entries");
        } catch (Exception e) {
            System.err.println("Error saving execution history: " + e.getMessage());
        }

        // Refresh UI if needed
        if (executedCount > 0) {
            updateHomeScreen();
            updatePortfolioScreen();
        }

        return executedCount;
    }

    /**
     * Determines if a recurring investment should execute today based on its schedule.
     *
     * @param investment The recurring investment to check
     * @param today      Today's date
     * @return true if the investment should execute today
     */
    private boolean shouldExecuteToday(RecurringInvestment investment, LocalDate today) {
        LocalDate startDate = investment.getStartDate();
        if (startDate.isAfter(today)) return false;

        long daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, today);

        switch (investment.getFrequency().toUpperCase()) {
            case "DAILY":
                return true;
            case "WEEKLY":
                return daysSinceStart % 7 == 0;
            case "MONTHLY":
                return startDate.getDayOfMonth() == today.getDayOfMonth();
            case "YEARLY":
                return startDate.getDayOfYear() == today.getDayOfYear();
            default:
                return false;
        }
    }

    /**
     * Executes a single recurring investment by creating a buy transaction
     * using real-time market data from AlphaVantage API.
     *
     * @param investment The investment to execute
     */
    private void executeSingleInvestment(RecurringInvestment investment) {
        // First ensure API is properly initialized
        if (!app.ensureApiInitialized()) {
            System.err.println("Cannot execute investment: API not initialized");
            return;
        }

        String symbol = investment.getSymbol();

        try {
            // Request current price data from the API
            final double[] currentPrice = {0.0};
            final boolean[] dataReceived = {false};

            // Use AlphaVantage API to get latest quote
            AlphaVantage.api()
                    .timeSeries()
                    .intraday()
                    .forSymbol(symbol)
                    .outputSize(OutputSize.COMPACT)
                    .onSuccess(response -> {
                        // Extract latest price from the response
                        TimeSeriesResponse data = (TimeSeriesResponse) response;
                        if (data.getStockUnits() != null && !data.getStockUnits().isEmpty()) {
                            currentPrice[0] = data.getStockUnits().get(0).getClose();
                            dataReceived[0] = true;
                        }
                    })
                    .onFailure(e -> System.err.println("API Error: " + e.getMessage()))
                    .fetch();

            // Wait briefly for API response (simple approach)
            Thread.sleep(2000);

            // If we couldn't get data, use a fallback price
            if (!dataReceived[0] || currentPrice[0] <= 0) {
                System.out.println("Using fallback price for " + symbol);
                currentPrice[0] = 100.0; // Fallback price
            }

            // Calculate quantity to buy based on investment amount and price
            int quantity = (int) (investment.getAmount() / currentPrice[0]);

            if (quantity > 0) {
                // Execute transaction with real price data
                app.getTransactionService().saveTransaction(
                        "Buy",
                        investment.getSymbol(),
                        quantity,
                        currentPrice[0]
                );

                System.out.println("Executed recurring investment: " +
                        quantity + " shares of " + symbol +
                        " at " + currentPrice[0]);
            } else {
                System.out.println("Investment amount too small to purchase any shares");
            }
        } catch (Exception e) {
            System.err.println("Error executing recurring investment: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Creates the account settings screen.
     * This screen provides options for changing application settings
     * such as currency preference.
     *
     * @return A JPanel representing the account settings screen
     */
    /**
     * Creates the account settings screen.
     * This screen provides options for changing application settings
     * such as currency preference.
     *
     * @return A JPanel representing the account settings screen
     */
    private JPanel createAccountScreen() {
        // Create the main account settings panel with GridBagLayout
        JPanel accountMain = new JPanel(new GridBagLayout()); // Create account screen with grid bag layout
        accountMain.setBackground(Investify.backgroundColor); // Set background color from app theme

        // Configure layout constraints for the account settings screen
        GridBagConstraints gbcAccount = new GridBagConstraints(); // Create constraints for positioning components
        gbcAccount.gridx = 0; // Set horizontal position to leftmost column
        gbcAccount.gridy = 0; // Set vertical position to top row
        gbcAccount.fill = GridBagConstraints.NONE; // Components don't resize to fill available space
        gbcAccount.anchor = GridBagConstraints.NORTH; // Anchor components to top of cell

        // Create and add the account settings screen title
        JLabel accountLabel = new JLabel("Account Settings", SwingConstants.CENTER); // Create centered title
        accountLabel.setForeground(Investify.textColor); // Set text color from app theme
        accountLabel.setFont(new Font("Inter", Font.BOLD, 24)); // Set bold font with size 24
        accountMain.add(accountLabel, gbcAccount); // Add title to panel with constraints

        // Create a panel for currency selection options
        JPanel currencyPanel = new JPanel(); // Create panel for currency options
        currencyPanel.setBackground(Investify.backgroundColor); // Set background color from app theme
        currencyPanel.setBorder(BorderFactory.createTitledBorder( // Create titled border with
                BorderFactory.createLineBorder(Investify.textColor), // Border color matching text color
                "Currency", // Title text
                TitledBorder.CENTER, // Center-aligned title
                TitledBorder.TOP, // Title at the top of the border
                new Font("Inter", Font.BOLD, 16), // Bold font for the title
                Investify.textColor // Title color matching text color
        ));

        // Create radio buttons for currency selection
        ButtonGroup currencyGroup = new ButtonGroup(); // Create button group to ensure only one selection
        JRadioButton usdButton = new JRadioButton("USD ($)"); // Create button for USD currency
        usdButton.setFont(new Font("Inter", Font.PLAIN, 16)); // Set font for button text
        usdButton.setForeground(Investify.textColor); // Set text color from app theme
        usdButton.setBackground(Investify.backgroundColor); // Set background color from app theme
        usdButton.setSelected(app.getCurrencyService().getCurrentCurrency().equals("USD")); // Set selected based on current setting

        JRadioButton chfButton = new JRadioButton("CHF (Fr.)"); // Create button for Swiss Franc currency
        chfButton.setFont(new Font("Inter", Font.PLAIN, 16)); // Set font for button text
        chfButton.setForeground(Investify.textColor); // Set text color from app theme
        chfButton.setBackground(Investify.backgroundColor); // Set background color from app theme
        chfButton.setSelected(app.getCurrencyService().getCurrentCurrency().equals("CHF")); // Set selected based on current setting

        // Group radio buttons and add them to the currency panel
        currencyGroup.add(usdButton); // Add USD button to group (ensures mutual exclusivity)
        currencyGroup.add(chfButton); // Add CHF button to group
        currencyPanel.add(usdButton); // Add USD button to panel
        currencyPanel.add(chfButton); // Add CHF button to panel

        // For the USD button
        usdButton.addActionListener(e -> { // Add click handler for USD button
            if (usdButton.isSelected() && !app.getCurrencyService().getCurrentCurrency().equals("USD")) { // Check if this is a change
                app.getCurrencyService().setCurrency("USD");  // Update the currency setting
                JOptionPane.showMessageDialog(accountMain, // Show confirmation dialog
                        "Currency changed to USD", // Message text
                        "Currency Updated", // Dialog title
                        JOptionPane.INFORMATION_MESSAGE); // Information message type
            }
        });

        // For the CHF button
        chfButton.addActionListener(e -> { // Add click handler for CHF button
            if (chfButton.isSelected() && !app.getCurrencyService().getCurrentCurrency().equals("CHF")) { // Check if this is a change
                app.getCurrencyService().setCurrency("CHF");  // Update the currency setting
                JOptionPane.showMessageDialog(accountMain, // Show confirmation dialog
                        "Currency changed to CHF", // Message text
                        "Currency Updated", // Dialog title
                        JOptionPane.INFORMATION_MESSAGE); // Information message type
            }
        });

        // Add the currency panel to the account settings screen
        gbcAccount.gridy = 1; // Move to second row
        gbcAccount.weighty = 0; // No vertical weight
        gbcAccount.insets = new Insets(20, 10, 20, 10); // Add padding around the panel
        accountMain.add(currencyPanel, gbcAccount); // Add currency panel to main panel

        // Add instructions about currency changes
        JLabel currencyInfo = new JLabel("<html>The currency change will be applied<br>when navigating between screens.</html>", SwingConstants.CENTER); // Create centered multi-line label
        currencyInfo.setForeground(Investify.textColor); // Set text color from app theme
        currencyInfo.setFont(new Font("Inter", Font.ITALIC, 14)); // Set italic font for note text
        gbcAccount.gridy = 2; // Move to third row
        gbcAccount.weighty = 1; // Give vertical weight to this component
        accountMain.add(currencyInfo, gbcAccount); // Add info label to panel

        // Add navigation bar to the account settings screen
        gbcAccount.gridy = 3; // Move to fourth row
        gbcAccount.fill = GridBagConstraints.HORIZONTAL; // Let component fill available horizontal space
        gbcAccount.weighty = 0; // No vertical weight
        gbcAccount.anchor = GridBagConstraints.SOUTH; // Anchor to bottom of the space
        gbcAccount.insets = new Insets(5, 0, 5, 0); // Reset to default padding
        accountMain.add(createNavBar(mainCardLayout, mainPanel), gbcAccount); // Add navigation bar at bottom

        return accountMain; // Return the configured account screen panel
    }

    /**
     * Creates the navigation bar with icon buttons for screen switching.
     * This method builds the bottom navigation bar that appears on all screens
     * and handles the navigation events between different screens.
     *
     * @param layout      The CardLayout used to switch between screens
     * @param parentPanel The parent panel containing all screens
     * @return A JPanel containing the navigation buttons
     */
    private JPanel createNavBar(CardLayout layout, JPanel parentPanel) {
        JPanel navBar = new JPanel(new GridLayout(1, 5)); // Create navigation bar with 5-column grid layout
        navBar.setBackground(Investify.backgroundColor); // Set background color from app theme

        // Create buttons
        JButton homeIcon = createNavButton("/investify/icons/homeIcon.png"); // Create button with home icon
        JButton searchIcon = createNavButton("/investify/icons/searchIcon.png"); // Create button with search icon
        JButton portfolioIcon = createNavButton("/investify/icons/portfolioIcon.png"); // Create button with portfolio icon
        JButton recurrentIcon = createNavButton("/investify/icons/recurrentIcon.png"); // Create button with recurrent icon
        JButton accountIcon = createNavButton("/investify/icons/accountIcon.png"); // Create button with account icon

        // Add to nav bar
        navBar.add(homeIcon); // Add home button to navbar
        navBar.add(searchIcon); // Add search button to navbar
        navBar.add(portfolioIcon); // Add portfolio button to navbar
        navBar.add(recurrentIcon); // Add recurrent button to navbar
        navBar.add(accountIcon); // Add account button to navbar

        // Set up actions
        homeIcon.addActionListener(e -> { // Add click handler for home button
            updateHomeScreen(); // Update home screen data before showing
            layout.show(parentPanel, "home"); // Switch to home screen
        });

        searchIcon.addActionListener(e -> layout.show(parentPanel, "search")); // Switch to search screen when clicked

        portfolioIcon.addActionListener(e -> { // Add click handler for portfolio button
            updatePortfolioScreen(); // Update portfolio screen data before showing
            layout.show(parentPanel, "portfolio"); // Switch to portfolio screen
        });

        recurrentIcon.addActionListener(e -> layout.show(parentPanel, "recurrent")); // Switch to recurrent screen when clicked

        accountIcon.addActionListener(e -> layout.show(parentPanel, "account")); // Switch to account screen when clicked

        return navBar; // Return the configured navigation bar
    }

    /**
     * Updates the home screen content with current data.
     * This method refreshes the portfolio value and pie chart
     * when returning to the home screen.
     */
    private void updateHomeScreen() {
        // Update the portfolio value label
        portfolioValueLabel.setText("Total value: " + app.getPortfolioManager().getPortfolioValueFormatted()); // Update portfolio value label with latest data

        // Find and update the chart panel
        for (Component comp : mainPanel.getComponents()) { // Loop through all components in main panel
            if (comp instanceof JPanel && "home".equals(((JPanel) comp).getName())) { // Find home panel by name
                JPanel homePanel = (JPanel) comp; // Cast to JPanel

                // Find and replace the chart panel (usually at position 2)
                Component[] components = homePanel.getComponents(); // Get all components in home panel
                for (int i = 0; i < components.length; i++) { // Loop through all components
                    if (components[i] instanceof JPanel && i == 2) { // Find chart panel at index 2
                        // Remove the old chart
                        homePanel.remove(components[i]); // Remove old chart panel

                        // Create a new chart panel
                        JPanel newChartPanel = app.getChartService().createPieChartPanel(); // Create new chart panel with latest data

                        // Reuse the same layout constraints
                        GridBagConstraints gbc = new GridBagConstraints(); // Create position constraints
                        gbc.gridx = 0; // Same horizontal position
                        gbc.gridy = 2; // Same vertical position
                        gbc.fill = GridBagConstraints.BOTH; // Same fill behavior
                        gbc.weighty = 1; // Same vertical weight

                        homePanel.add(newChartPanel, gbc, i); // Add new chart panel at same position
                        break; // Exit the loop after replacement
                    }
                }

                // Refresh the display
                homePanel.revalidate(); // Recalculate layout
                homePanel.repaint(); // Redraw the panel
                break; // Exit loop after finding and updating
            }
        }
    }

    /**
     * Updates the portfolio screen with current data.
     * This method rebuilds the portfolio content to reflect
     * any changes in holdings or currency settings.
     */
    private void updatePortfolioScreen() {
        // Get the portfolio panel
        Component[] components = mainPanel.getComponents(); // Get all screens in main panel
        for (Component comp : components) { // Loop through all components
            if (comp instanceof JPanel) { // Check if component is a panel
                JPanel panel = (JPanel) comp; // Cast to JPanel
                if (panel.getName() != null && panel.getName().equals("portfolio")) { // Find portfolio panel by name
                    // Remove current content
                    panel.removeAll(); // Clear all components from panel

                    // Recreate content with updated values and currency
                    JLabel portfolioLabel = new JLabel("Your Portfolio", SwingConstants.CENTER); // Recreate title
                    portfolioLabel.setForeground(Investify.textColor); // Set text color from app theme
                    portfolioLabel.setFont(new Font("Inter", Font.BOLD, 24)); // Set bold font with size 24

                    JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Recreate header panel
                    headerPanel.setBackground(Investify.backgroundColor); // Set background color from app theme
                    headerPanel.add(portfolioLabel); // Add title to header

                    // Reapply layout
                    panel.setLayout(new BorderLayout()); // Reset to border layout
                    panel.add(headerPanel, BorderLayout.NORTH); // Add header at top
                    panel.add(createPortfolioContent(), BorderLayout.CENTER); // Add fresh content in center
                    panel.add(createNavBar(mainCardLayout, mainPanel), BorderLayout.SOUTH); // Add navigation at bottom

                    // Force visual update
                    panel.revalidate(); // Recalculate layout
                    panel.repaint(); // Redraw the panel
                    break; // Exit loop after updating
                }
            }
        }
    }

    /**
     * Creates a navigation button with an icon.
     * This utility method loads, scales and configures
     * an icon button for the navigation bar.
     *
     * @param resourcePath The path to the icon resource
     * @return A JButton configured with the specified icon
     */
    private JButton createNavButton(String resourcePath) {
        java.net.URL iconURL = getClass().getResource(resourcePath); // Get icon resource URL
        if (iconURL == null) { // Check if icon resource was found
            System.err.println("Icon not found: " + resourcePath); // Log error if icon not found
            return new JButton(); // Return empty button as fallback
        }

        ImageIcon originalIcon = new ImageIcon(iconURL); // Create icon from resource URL
        Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH); // Scale icon to 48x48 pixels
        ImageIcon scaledIcon = new ImageIcon(scaledImage); // Create new icon with scaled image

        JButton button = new JButton(scaledIcon); // Create button with icon
        button.setBackground(Investify.backgroundColor); // Set background color from app theme
        button.setFocusPainted(false); // Remove focus highlight
        button.setBorderPainted(false); // Remove button border
        button.setMargin(new Insets(5, 7, 5, 7)); // Add padding around icon
        return button; // Return the configured button
    }

    // I had issues with the shared investments between the two apps, so I asked claude 3.7 to generate the following lines
    // Method for sharing the RecurringInvestment class between both applications
    public static class RecurringInvestmentAdapter {
        // Converts a shared RecurringInvestment to the format needed by Investify
        public static RecurringInvestment fromShared(shared.RecurringInvestment sharedInvestment) {
            // Create a new RecurringInvestment with all fields from shared object
            return new RecurringInvestment(
                    sharedInvestment.getName(),
                    sharedInvestment.getAmount(),
                    sharedInvestment.getFrequency(),
                    sharedInvestment.getStartDate(),
                    sharedInvestment.getSymbol()
            );
        }
    }


    /**
     * Imports recurring investments from Finance Tracker with improved cache management.
     * This implementation ensures that deleted investments are properly synchronized.
     *
     * @return List of current recurring investments from shared file
     */
    private List<RecurringInvestment> importRecurringInvestments() {
        // Initialize empty result list
        List<RecurringInvestment> result = new ArrayList<>();

        // Build path to the shared data file in user's home directory
        String sharedFilePath = Paths.get(System.getProperty("user.home"),
                "recurringInvestments.dat").toString();

        // Try-with-resources to ensure proper stream closing
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(sharedFilePath))) {
            // Read list of objects from the file
            List<Object> importedObjects = (List<Object>) in.readObject();
            System.out.println("Read " + importedObjects.size() + " objects from file");

            // Convert each compatible object to RecurringInvestment
            for (Object obj : importedObjects) {
                if (obj instanceof shared.RecurringInvestment) {
                    shared.RecurringInvestment sharedInv = (shared.RecurringInvestment) obj;
                    result.add(RecurringInvestmentAdapter.fromShared(sharedInv));
                    System.out.println("Imported: " + sharedInv.getName());
                } else {
                    System.out.println("Unknown object type: " + obj.getClass().getName());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Shared file not found: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Class compatibility issue: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

}