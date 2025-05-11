import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;


public class InvestifyApp {

    public static String portfolioValue = "0.00 $";

    public static JPanel createInvestify() {

        // =====API config=====
        Config cfg = Config.builder()
                .key("9RD5X05ALZ59WA9E")
                .timeOut(30)
                .build();

        AlphaVantage.api().init(cfg);
        // =====================

        // =====Main Card Panel=====
        JPanel mainPanel = new JPanel(new CardLayout()); //Creates the card panel
        // =========================

        // =====Home panel=====
        JPanel homeMain = new JPanel(new GridBagLayout());
        homeMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcHome = new GridBagConstraints();
        gbcHome.gridx = 0;
        gbcHome.gridwidth = 1;
        gbcHome.fill = GridBagConstraints.NONE;
        gbcHome.anchor = GridBagConstraints.NORTH;
        gbcHome.insets = new Insets(5, 0, 5, 0);

        JLabel titlePortfolio = new JLabel("Portfolio");
        titlePortfolio.setForeground(phoneUtils.textColor);
        titlePortfolio.setFont(new Font("Inter", Font.BOLD, 32));
        gbcHome.gridy = 0;
        homeMain.add(titlePortfolio, gbcHome);

        // D'abord, calculer la valeur du portfolio en appelant createPieChartPanel()
        // Création d'une version temporaire pour le calcul uniquement
        createPieChartPanel();

        // APRÈS avoir appelé createPieChartPanel(), créer le JLabel avec la valeur mise à jour
        JLabel subtitle1 = new JLabel("Total value: " + portfolioValue);
        subtitle1.setForeground(phoneUtils.textColor);
        subtitle1.setFont(new Font("Inter", Font.BOLD, 24));
        gbcHome.gridy = 1;
        gbcHome.insets = new Insets(5, 0, 15, 0);
        homeMain.add(subtitle1, gbcHome);

        gbcHome.insets = new Insets(5, 0, 5, 0);

        // Création du vrai panel de graphique pour l'affichage
        JPanel pieChartPanel = createPieChartPanel();
        gbcHome.gridy = 2;
        gbcHome.fill = GridBagConstraints.BOTH;
        gbcHome.weighty = 1;
        homeMain.add(pieChartPanel, gbcHome);

        gbcHome.gridy = 3; // Position ajustée pour la navbar
        gbcHome.fill = GridBagConstraints.HORIZONTAL;
        gbcHome.weighty = 0;
        gbcHome.anchor = GridBagConstraints.SOUTH;
        homeMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcHome);
        //=======================


        // =====Search panel=====
        JPanel searchMain = new JPanel(new GridBagLayout());
        searchMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcSearch = new GridBagConstraints();
        gbcSearch.gridx = 0;
        gbcSearch.gridwidth = 1;
        gbcSearch.insets = new Insets(5, 0, 5, 0);
        gbcSearch.fill = GridBagConstraints.NONE;
        gbcSearch.anchor = GridBagConstraints.NORTH;

        JLabel searchLabel = new JLabel("Page Search", SwingConstants.CENTER);
        searchLabel.setForeground(phoneUtils.textColor);
        searchLabel.setFont(new Font("Inter", Font.BOLD, 24));
        gbcSearch.gridy = 0;
        searchMain.add(searchLabel, gbcSearch);

        JLabel subtitleSearch = new JLabel("Search for symbols", SwingConstants.CENTER);
        subtitleSearch.setForeground(phoneUtils.textColor);
        subtitleSearch.setFont(new Font("Inter", Font.PLAIN, 18));
        gbcSearch.gridy = 1; // Position entre le titre et la barre de recherche
        searchMain.add(subtitleSearch, gbcSearch);

        JTextField searchBar = new JTextField();
        searchBar.setPreferredSize(new Dimension(250, 30));
        searchBar.setFont(new Font("Inter", Font.PLAIN, 16));
        searchBar.setBackground(Color.WHITE);
        searchBar.setForeground(Color.BLACK);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        gbcSearch.gridy = 2;
        gbcSearch.insets = new Insets(10, 0, 10, 0);

        searchMain.add(searchBar, gbcSearch);

        JLabel searchResults = new JLabel("", SwingConstants.CENTER);
        searchResults.setForeground(phoneUtils.textColor);
        searchResults.setFont(new Font("Inter", Font.PLAIN, 18));
        gbcSearch.gridy = 3;
        gbcSearch.weighty = 1;

        searchMain.add(searchResults, gbcSearch);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(phoneUtils.backgroundColor);
        buttonPanel.setVisible(false);

        searchBar.addActionListener(e -> {
            String symbol = searchBar.getText().trim();
            buttonPanel.setVisible(false); // Cacher les boutons par défaut

            if (!symbol.isEmpty()) {
                try {
                    TimeSeriesResponse response = AlphaVantage.api()
                            .timeSeries()
                            .daily()
                            .forSymbol(symbol)
                            .outputSize(OutputSize.COMPACT)
                            .fetchSync();

                    if (response.getErrorMessage() == null) {
                        response.getStockUnits().stream()
                                .findFirst()
                                .ifPresentOrElse(
                                        unit -> {
                                            searchResults.setText("Last price: " + unit.getClose() + " USD");
                                            buttonPanel.setVisible(true);
                                        },
                                        () -> searchResults.setText("No data available for this symbol.")
                                );
                    } else {
                        searchResults.setText("ERROR : Symbol not found");
                    }
                } catch (Exception ex) {
                    searchResults.setText("Error retrieving data.");
                    ex.printStackTrace();
                }
            } else {
                searchResults.setText("Please enter a valid symbol.");
            }
        });

        JButton buyButton = new JButton("Buy");
        buyButton.setFont(new Font("Inter", Font.BOLD, 16));
        buyButton.setBackground(new Color(50,205,50));
        buyButton.setForeground(phoneUtils.textColor);
        buyButton.setFocusPainted(false);

        JButton sellButton = new JButton("Sell");
        sellButton.setFont(new Font("Inter", Font.BOLD, 16));
        sellButton.setBackground(new Color(255, 62, 65));
        sellButton.setForeground(phoneUtils.textColor);
        sellButton.setFocusPainted(false);

        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);

        gbcSearch.gridy = 4; // Position sous le label des résultats
        gbcSearch.weighty = 0;
        searchMain.add(buttonPanel, gbcSearch);

        buyButton.addActionListener(e -> openTransactionDialog("Buy", searchMain));
        sellButton.addActionListener(e -> openTransactionDialog("Sell", searchMain));

        gbcSearch.gridy = 5;
        gbcSearch.fill = GridBagConstraints.HORIZONTAL;
        gbcSearch.weighty = 0;
        gbcSearch.anchor = GridBagConstraints.SOUTH;
        searchMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcSearch);
        //=======================


        // =====Portfolio Panel=====
        JPanel portfolioMain = new JPanel(new BorderLayout());
        portfolioMain.setName("portfolio");
        portfolioMain.setBackground(phoneUtils.backgroundColor);

        JLabel portfolioLabel = new JLabel("Your Portfolio", SwingConstants.CENTER);
        portfolioLabel.setForeground(phoneUtils.textColor);
        portfolioLabel.setFont(new Font("Inter", Font.BOLD, 24));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(phoneUtils.backgroundColor);
        headerPanel.add(portfolioLabel);

        portfolioMain.add(headerPanel, BorderLayout.NORTH);
        portfolioMain.add(createPortfolioContent(), BorderLayout.CENTER);
        portfolioMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), BorderLayout.SOUTH);
        // =========================

        // =====Recurrent Panel=====
        JPanel recurrentMain = new JPanel(new GridBagLayout());
        recurrentMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcRecurrent = new GridBagConstraints();
        gbcRecurrent.gridx = 0;
        gbcRecurrent.gridy = 0;
        gbcRecurrent.fill = GridBagConstraints.NONE;
        gbcRecurrent.anchor = GridBagConstraints.NORTH;
        JLabel recurrentLabel = new JLabel("Recurrent Investments", SwingConstants.CENTER);
        recurrentLabel.setForeground(phoneUtils.textColor);
        recurrentLabel.setFont(new Font("Inter", Font.BOLD, 24));
        recurrentMain.add(recurrentLabel, gbcRecurrent);

        gbcRecurrent.weighty = 1;
        gbcRecurrent.gridy = 1;
        JLabel recurrentInfo = new JLabel("Recurrent investments info...");
        recurrentInfo.setForeground(phoneUtils.textColor);
        recurrentInfo.setFont(new Font("Inter", Font.PLAIN, 18));
        recurrentMain.add(recurrentInfo, gbcRecurrent);

        gbcRecurrent.gridy = 2;
        gbcRecurrent.fill = GridBagConstraints.HORIZONTAL;
        gbcRecurrent.weighty = 0;
        gbcRecurrent.anchor = GridBagConstraints.SOUTH;
        recurrentMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcRecurrent);
        //=======================

        // =====Account Panel=====
        JPanel accountMain = new JPanel(new GridBagLayout());
        accountMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcAccount = new GridBagConstraints();
        gbcAccount.gridx = 0;
        gbcAccount.gridy = 0;
        gbcAccount.fill = GridBagConstraints.NONE;
        gbcAccount.anchor = GridBagConstraints.NORTH;
        JLabel accountLabel = new JLabel("Account Settings", SwingConstants.CENTER);
        accountLabel.setForeground(phoneUtils.textColor);
        accountLabel.setFont(new Font("Inter", Font.BOLD, 24));
        accountMain.add(accountLabel, gbcAccount);

        gbcAccount.weighty = 1;
        gbcAccount.gridy = 1;
        JLabel accountInfo = new JLabel("Account settings info...");
        accountInfo.setForeground(phoneUtils.textColor);
        accountInfo.setFont(new Font("Inter", Font.PLAIN, 18));
        accountMain.add(accountInfo, gbcAccount);

        gbcAccount.gridy = 2;
        gbcAccount.fill = GridBagConstraints.HORIZONTAL;
        gbcAccount.weighty = 0;
        gbcAccount.anchor = GridBagConstraints.SOUTH;
        accountMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcAccount);
        //=======================



        // Adding the panels to the card layout
        mainPanel.add(homeMain, "home");
        mainPanel.add(searchMain, "search");
        mainPanel.add(portfolioMain, "portfolio");
        mainPanel.add(recurrentMain, "recurrent");
        mainPanel.add(accountMain, "account");

        return mainPanel;
    }

    private static JButton createNavButton(String resourcePath) {
        java.net.URL iconURL = InvestifyApp.class.getResource(resourcePath);
        if (iconURL == null) {
            System.err.println("Icon not found: " + resourcePath);
            return new JButton(); // fallback button
        }
        ImageIcon originalIcon = new ImageIcon(iconURL);
        Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JButton button = new JButton(scaledIcon);
        button.setBackground(phoneUtils.backgroundColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(5, 7, 5, 7));
        return button;
    }


    private static JPanel createNavBar(CardLayout layout, JPanel parentPanel) {
        JPanel navBar = new JPanel(new GridLayout(1, 5));
        navBar.setBackground(phoneUtils.backgroundColor);

        JButton homeIcon = createNavButton("/investify/icons/homeIcon.png");
        JButton searchIcon = createNavButton("/investify/icons/searchIcon.png");
        JButton portfolioIcon = createNavButton("/investify/icons/portfolioIcon.png");
        JButton recurrentIcon = createNavButton("/investify/icons/recurrentIcon.png");
        JButton accountIcon = createNavButton("/investify/icons/accountIcon.png");

        navBar.add(homeIcon);
        navBar.add(searchIcon);
        navBar.add(portfolioIcon);
        navBar.add(recurrentIcon);
        navBar.add(accountIcon);

        homeIcon.addActionListener(e -> layout.show(parentPanel, "home"));
        searchIcon.addActionListener(e -> layout.show(parentPanel, "search"));
        portfolioIcon.addActionListener(e -> {
            // Mettre à jour le contenu du portfolio existant
            Component[] components = parentPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getName() != null && panel.getName().equals("portfolio")) {
                        // Remplacer uniquement le contenu central
                        panel.removeAll();

                        JLabel portfolioLabel = new JLabel("Your Portfolio", SwingConstants.CENTER);
                        portfolioLabel.setForeground(phoneUtils.textColor);
                        portfolioLabel.setFont(new Font("Inter", Font.BOLD, 24));

                        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        headerPanel.setBackground(phoneUtils.backgroundColor);
                        headerPanel.add(portfolioLabel);

                        panel.setLayout(new BorderLayout());
                        panel.add(headerPanel, BorderLayout.NORTH);
                        panel.add(createPortfolioContent(), BorderLayout.CENTER);
                        panel.add(createNavBar(layout, parentPanel), BorderLayout.SOUTH);
                        panel.revalidate();
                        panel.repaint();
                    }
                }
            }

            layout.show(parentPanel, "portfolio");
        });
        recurrentIcon.addActionListener(e -> layout.show(parentPanel, "recurrent"));
        accountIcon.addActionListener(e -> layout.show(parentPanel, "account"));

        return navBar;
    }

    private static JPanel createPortfolioContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(phoneUtils.backgroundColor);

        // Lecture du fichier JSON
        String filePath = System.getProperty("user.home") + "/investifyData.json";
        File file = new File(filePath);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Transaction[] transactions = gson.fromJson(reader, Transaction[].class);

                if (transactions != null && transactions.length > 0) {
                    // Regrouper les actions par symbole
                    Map<String, Integer> holdings = new HashMap<>();
                    Map<String, Double> totalValues = new HashMap<>();

                    for (Transaction transaction : transactions) {
                        String symbol = transaction.getSymbol();
                        int quantity = transaction.getQuantity();
                        double price = transaction.getPrice();
                        boolean isBuy = transaction.getAction().equalsIgnoreCase("Buy");

                        // Mise à jour des quantités
                        holdings.put(symbol, holdings.getOrDefault(symbol, 0) +
                                (isBuy ? quantity : -quantity));

                        // Mise à jour des valeurs totales
                        double transactionValue = quantity * price;
                        totalValues.put(symbol, totalValues.getOrDefault(symbol, 0.0) +
                                (isBuy ? transactionValue : -transactionValue));
                    }

                    // Filtrer les actions avec une quantité positive
                    holdings.entrySet().removeIf(entry -> entry.getValue() <= 0);

                    if (!holdings.isEmpty()) {
                        // Utiliser un BoxLayout vertical pour un meilleur affichage
                        JPanel holdingsPanel = new JPanel();
                        holdingsPanel.setLayout(new BoxLayout(holdingsPanel, BoxLayout.Y_AXIS));
                        holdingsPanel.setBackground(phoneUtils.backgroundColor);

                        for (String symbol : holdings.keySet()) {
                            int quantity = holdings.get(symbol);
                            double totalValue = totalValues.get(symbol);

                            // Création d'un panneau pour chaque action avec une bordure pour séparation visuelle
                            JPanel stockPanel = new JPanel(new BorderLayout());
                            stockPanel.setBackground(phoneUtils.backgroundColor);
                            stockPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));
                            stockPanel.setPreferredSize(new Dimension(300, 60));
                            stockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                            // Symbole de l'action (côté gauche)
                            JLabel symbolLabel = new JLabel(symbol);
                            symbolLabel.setForeground(phoneUtils.textColor);
                            symbolLabel.setFont(new Font("Inter", Font.BOLD, 20));
                            symbolLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

                            // Panneau pour les informations (côté droit)
                            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
                            infoPanel.setBackground(phoneUtils.backgroundColor);

                            // Quantité d'actions
                            JLabel quantityLabel = new JLabel(quantity + " shares");
                            quantityLabel.setForeground(phoneUtils.textColor);
                            quantityLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                            quantityLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                            // Valeur totale
                            JLabel valueLabel = new JLabel(String.format("%.2f $", totalValue));
                            valueLabel.setForeground(phoneUtils.textColor);
                            valueLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                            infoPanel.add(quantityLabel);
                            infoPanel.add(valueLabel);
                            infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

                            stockPanel.add(symbolLabel, BorderLayout.WEST);
                            stockPanel.add(infoPanel, BorderLayout.EAST);

                            holdingsPanel.add(stockPanel);
                            holdingsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacement entre actions
                        }

                        // Ajout d'un JScrollPane pour permettre de défiler si beaucoup d'actions
                        JScrollPane scrollPane = new JScrollPane(holdingsPanel);
                        scrollPane.setBorder(null);
                        scrollPane.getViewport().setBackground(phoneUtils.backgroundColor);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        content.add(scrollPane, BorderLayout.CENTER);
                    } else {
                        JLabel noHoldingsLabel = new JLabel("No shares found", SwingConstants.CENTER);
                        noHoldingsLabel.setForeground(phoneUtils.textColor);
                        noHoldingsLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                        content.add(noHoldingsLabel, BorderLayout.CENTER);
                    }
                } else {
                    JLabel noDataLabel = new JLabel("No transactions found.", SwingConstants.CENTER);
                    noDataLabel.setForeground(phoneUtils.textColor);
                    noDataLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                    content.add(noDataLabel, BorderLayout.CENTER);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JLabel errorLabel = new JLabel("Error loading data.", SwingConstants.CENTER);
                errorLabel.setForeground(phoneUtils.textColor);
                errorLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                content.add(errorLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel noFileLabel = new JLabel("Data file not found.", SwingConstants.CENTER);
            noFileLabel.setForeground(phoneUtils.textColor);
            noFileLabel.setFont(new Font("Inter", Font.PLAIN, 18));
            content.add(noFileLabel, BorderLayout.CENTER);
        }

        return content;
    }

    private static void openTransactionDialog(String action, JPanel parentPanel) {
        // Récupérer le symbole actuel depuis le champ de recherche
        JTextField searchBar = null;
        double currentPrice = 0.0;
        String symbol = "";

        // Trouver le champ de recherche et le résultat du prix
        for (Component comp : parentPanel.getComponents()) {
            if (comp instanceof JTextField) {
                searchBar = (JTextField) comp;
                symbol = searchBar.getText().trim().toUpperCase();
            }
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text != null && text.startsWith("Last price:")) {
                    try {
                        // Extraire le prix du texte "Last price: XX.XX USD"
                        String priceText = text.substring(12, text.indexOf(" USD"));
                        currentPrice = Double.parseDouble(priceText);
                    } catch (Exception e) {
                        // En cas d'erreur, utiliser une valeur par défaut
                        currentPrice = 0.0;
                    }
                }
            }
        }

        // Si le symbole ou le prix n'a pas été trouvé, afficher un message d'erreur
        if (symbol.isEmpty() || currentPrice == 0.0) {
            JOptionPane.showMessageDialog(parentPanel, "Please search for a valid symbol first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Agrandir la boîte de dialogue
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), action + " Stocks", true);
        dialog.setSize(350, 280);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Afficher le symbole et le prix actuels
        JLabel symbolLabel = new JLabel("Symbol: " + symbol);
        symbolLabel.setFont(new Font("Inter", Font.BOLD, 16));
        dialog.add(symbolLabel, gbc);

        gbc.gridy = 1;
        JLabel priceLabel = new JLabel(String.format("Price: %.2f $", currentPrice));
        priceLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        dialog.add(priceLabel, gbc);

        gbc.gridy = 2;
        JLabel label = new JLabel("Quantity to " + action.toLowerCase() + ":");
        label.setFont(new Font("Inter", Font.PLAIN, 16));
        dialog.add(label, gbc);

        // Champ de texte agrandi
        JTextField quantityField = new JTextField(20);
        quantityField.setFont(new Font("Inter", Font.PLAIN, 18));
        quantityField.setPreferredSize(new Dimension(250, 40));
        gbc.gridy = 3;
        gbc.ipady = 10; // Hauteur interne supplémentaire
        dialog.add(quantityField, gbc);

        // Bouton de confirmation
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Inter", Font.BOLD, 16));
        confirmButton.setPreferredSize(new Dimension(150, 40));
        gbc.gridy = 4;
        gbc.ipady = 5;
        gbc.insets = new Insets(20, 10, 10, 10); // Plus d'espace au-dessus du bouton
        dialog.add(confirmButton, gbc);

        // Utiliser le symbole et le prix récupérés
        final String finalSymbol = symbol;
        final double finalPrice = currentPrice;

        confirmButton.addActionListener(ev -> {
            String quantityText = quantityField.getText().trim();
            try {
                int quantity = Integer.parseInt(quantityText);
                if (quantity > 0) {
                    Transaction transaction = new Transaction(action, finalSymbol, quantity, finalPrice);

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String filePath = System.getProperty("user.home") + "/investifyData.json";
                    File file = new File(filePath);

                    List<Transaction> transactions = new ArrayList<>();
                    if (file.exists()) {
                        try (FileReader reader = new FileReader(file)) {
                            Transaction[] existing = gson.fromJson(reader, Transaction[].class);
                            if (existing != null) transactions.addAll(Arrays.asList(existing));
                        }
                    } else {
                        file.createNewFile(); // Crée le fichier s'il n'existe pas
                    }

                    transactions.add(transaction);
                    try (FileWriter writer = new FileWriter(file)) {
                        gson.toJson(transactions, writer);
                    }

                    JOptionPane.showMessageDialog(dialog, "Transaction recorded successfully!");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error while recording the transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private static JPanel createPieChartPanel() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(phoneUtils.backgroundColor);
        chartPanel.setPreferredSize(new Dimension(300, 200));

        // Récupérer les données du portfolio
        String filePath = System.getProperty("user.home") + "/investifyData.json";
        File file = new File(filePath);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Transaction[] transactions = gson.fromJson(reader, Transaction[].class);

                if (transactions != null && transactions.length > 0) {
                    // Calculer la répartition des actions
                    Map<String, Double> holdings = new HashMap<>();

                    for (Transaction transaction : transactions) {
                        String symbol = transaction.getSymbol();
                        int quantity = transaction.getQuantity();
                        double price = transaction.getPrice();
                        boolean isBuy = transaction.getAction().equalsIgnoreCase("Buy");

                        double transactionValue = quantity * price;

                        if (isBuy) {
                            holdings.put(symbol, holdings.getOrDefault(symbol, 0.0) + transactionValue);
                        } else {
                            holdings.put(symbol, holdings.getOrDefault(symbol, 0.0) - transactionValue);
                        }
                    }

                    // Filtrer les actions avec une valeur positive
                    holdings.entrySet().removeIf(entry -> entry.getValue() <= 0);

                    if (!holdings.isEmpty()) {
                        // Créer le dataset pour le graphique
                        DefaultPieDataset dataset = new DefaultPieDataset();

                        for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                            dataset.setValue(entry.getKey(), entry.getValue());
                        }

                        // Créer le graphique
                        JFreeChart chart = ChartFactory.createPieChart(
                                null, // Pas de titre, nous avons déjà un titre au-dessus
                                dataset,
                                true, // Afficher la légende
                                false, // Pas d'info-bulles
                                false // Pas d'URLs
                        );

                        // Personnaliser l'apparence du graphique
                        chart.setBackgroundPaint(phoneUtils.backgroundColor);

                        PiePlot plot = (PiePlot) chart.getPlot();
                        plot.setBackgroundPaint(phoneUtils.backgroundColor);
                        plot.setOutlinePaint(null);
                        plot.setLabelOutlinePaint(null);
                        plot.setLabelShadowPaint(null);
                        plot.setLabelBackgroundPaint(null);
                        plot.setLabelFont(new Font("Inter", Font.PLAIN, 12));
                        plot.setLabelPaint(phoneUtils.textColor);

                        // Légende
                        LegendTitle legend = chart.getLegend();
                        legend.setBackgroundPaint(phoneUtils.backgroundColor);
                        legend.setItemFont(new Font("Inter", Font.PLAIN, 12));
                        legend.setItemPaint(phoneUtils.textColor);

                        // Ajouter le graphique au panel
                        ChartPanel chartComponent = new ChartPanel(chart);
                        chartComponent.setPreferredSize(new Dimension(280, 180));
                        chartComponent.setBackground(phoneUtils.backgroundColor);
                        chartPanel.add(chartComponent, BorderLayout.CENTER);

                        // Calculer la valeur totale du portfolio
                        double totalValue = holdings.values().stream().mapToDouble(Double::doubleValue).sum();
                        portfolioValue = String.format("%.2f $", totalValue);

                        return chartPanel;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Si aucune donnée disponible, afficher un panneau vide
        JLabel noDataLabel = new JLabel("No portfolio data available", SwingConstants.CENTER);
        noDataLabel.setForeground(phoneUtils.textColor);
        noDataLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        chartPanel.add(noDataLabel, BorderLayout.CENTER);

        portfolioValue = "0.00 $";
        return chartPanel;
    }

}