import javax.swing.*;
import java.awt.*;

public class FinanceTracker {

    public static JPanel createFinanceTracker() {

        JPanel financeApp = new JPanel();
        financeApp.setBackground(phoneUtils.backgroundColor);
        JLabel label = new JLabel("Finance Tracker App", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.BOLD, 20));
        label.setForeground(phoneUtils.textColor);
        financeApp.add(label, BorderLayout.CENTER);

        return financeApp;
    }



}
