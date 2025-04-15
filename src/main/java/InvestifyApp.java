import javax.swing.*;
import java.awt.*;

public class InvestifyApp {

    public static JPanel createInvestify() {

        JPanel investifyApp = new JPanel();
        investifyApp.setBackground(phoneUtils.backgroundColor);
        investifyApp.setLayout(new GridLayout(3,1));

        JPanel investifyNavBar = new JPanel();
        investifyNavBar.setBackground(phoneUtils.backgroundColor);
        investifyNavBar.setLayout(new GridLayout(1, 5));
        investifyNavBar.setForeground(phoneUtils.textColor);

        JLabel homeLabel = new JLabel("Home");
        homeLabel.setForeground(phoneUtils.textColor);
        homeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        investifyNavBar.add(homeLabel, BorderLayout.SOUTH);

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setForeground(phoneUtils.textColor);
        searchLabel.setHorizontalAlignment(SwingConstants.CENTER);
        investifyNavBar.add(searchLabel, BorderLayout.SOUTH);

        JLabel portfolioLabel = new JLabel("Portfolio");
        portfolioLabel.setForeground(phoneUtils.textColor);
        portfolioLabel.setHorizontalAlignment(SwingConstants.CENTER);
        investifyNavBar.add(portfolioLabel, BorderLayout.SOUTH);

        JLabel recurrentLabel = new JLabel("Recurrent");
        recurrentLabel.setForeground(phoneUtils.textColor);
        recurrentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        investifyNavBar.add(recurrentLabel, BorderLayout.SOUTH);

        JLabel accountLabel = new JLabel("Account");
        accountLabel.setForeground(phoneUtils.textColor);
        accountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        investifyNavBar.add(accountLabel, BorderLayout.SOUTH);

        investifyApp.add(new JLabel());
        investifyApp.add(new JLabel());
        investifyApp.add(investifyNavBar);






        return investifyApp;
    }



}
