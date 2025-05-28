package investify.service;

import investify.app.Investify;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Service for creating and managing investment visualization charts in the Investify application.
 * This service provides methods to generate graphical representations of portfolio data,
 * making it easier for users to understand their investment distribution and performance.
 * It uses the JFreeChart library to create visually appealing and interactive charts.
 */
public class ChartService { // Main class responsible for creating investment portfolio charts
    private final PortfolioManager portfolioManager; // Reference to manage portfolio data
    private final CurrencyService currencyService; // Reference to handle currency conversions

    /**
     * Constructs a new ChartService with the necessary dependencies.
     *
     * @param portfolioManager The portfolio manager that provides access to investment data
     * @param currencyService The currency service used for currency conversions in charts
     */
    public ChartService(PortfolioManager portfolioManager, CurrencyService currencyService) {
        this.portfolioManager = portfolioManager; // Stores reference to portfolio manager
        this.currencyService = currencyService; // Stores reference to currency service
    }

    /**
     * Creates a pie chart panel that visualizes the distribution of investments in the portfolio.
     * The chart shows the relative value of each stock holding as a proportion of the total portfolio.
     * If no portfolio data is available, a message is displayed instead. Made by IA.
     *
     * @return A JPanel containing either the pie chart or a message indicating no data is available
     */
    public JPanel createPieChartPanel() {
        // Creates the main panel with BorderLayout to hold the chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        // Sets background color to match application theme
        chartPanel.setBackground(Investify.backgroundColor);
        // Sets default size for the chart panel
        chartPanel.setPreferredSize(new Dimension(300, 200));

        // Updates portfolio values before displaying the chart
        portfolioManager.calculatePortfolioValue();

        // Retrieves current holdings data from portfolio manager
        Map<String, Double> holdings = portfolioManager.getTotalValues();

        // Checks if there is portfolio data to display
        if (!holdings.isEmpty()) {
            // Creates dataset for the pie chart
            DefaultPieDataset dataset = new DefaultPieDataset();
            // Populates the dataset with stock symbols and their respective values
            for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }

            // Creates pie chart using JFreeChart factory
            JFreeChart chart = ChartFactory.createPieChart(
                    null, // No title for the chart
                    dataset, // The dataset containing portfolio values
                    true, // Show legend to identify segments
                    false, // No tooltips when hovering
                    false // No URLs for web integration
            );

            // Sets chart background to match application theme
            chart.setBackgroundPaint(Investify.backgroundColor);

            // Gets the plot object to configure pie chart appearance
            PiePlot plot = (PiePlot) chart.getPlot();
            // Sets plot background to match application theme
            plot.setBackgroundPaint(Investify.backgroundColor);
            // Removes outline around the plot
            plot.setOutlinePaint(null);
            // Removes label outlines for cleaner look
            plot.setLabelOutlinePaint(null);
            // Removes label shadows
            plot.setLabelShadowPaint(null);
            // Removes label backgrounds
            plot.setLabelBackgroundPaint(null);
            // Sets font for segment labels
            plot.setLabelFont(new Font("Inter", Font.PLAIN, 12));
            // Sets label text color
            plot.setLabelPaint(Investify.textColor);

            // Configures the chart legend for better readability
            LegendTitle legend = chart.getLegend();
            // Sets legend background to match application theme
            legend.setBackgroundPaint(Investify.backgroundColor);
            // Sets font for legend items
            legend.setItemFont(new Font("Inter", Font.PLAIN, 12));
            // Sets legend text color
            legend.setItemPaint(Investify.textColor);

            // Creates a chart panel component to display the chart
            ChartPanel chartComponent = new ChartPanel(chart);
            // Sets size for the chart component
            chartComponent.setPreferredSize(new Dimension(280, 180));
            // Sets chart component background
            chartComponent.setBackground(Investify.backgroundColor);
            // Adds chart component to the main panel
            chartPanel.add(chartComponent, BorderLayout.CENTER);

            // Returns the complete chart panel
            return chartPanel;
        }

        // Creates and displays a placeholder when no portfolio data exists
        JLabel noDataLabel = new JLabel("No portfolio data available", SwingConstants.CENTER);
        // Sets text color for the placeholder
        noDataLabel.setForeground(Investify.textColor);
        // Sets font for the placeholder
        noDataLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        // Adds the placeholder to the center of the panel
        chartPanel.add(noDataLabel, BorderLayout.CENTER);

        // Returns the panel with the placeholder message
        return chartPanel;
    }
}