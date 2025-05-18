/**
 * Main class for the phone application.
 * This class extends phoneUtils and serves as the entry point for the application.
 * It creates and displays the home screen of the phone interface.
 */
public class Homescreen extends phoneUtils {
    /**
     * Main method that starts the application.
     * When executed, this method calls createHomescreen() to initialize
     * and display the phone's user interface.
     */
    public static void main(String[] args) {
        JFrame phoneFrame = phoneUtils.createPhoneFrame("The Phone");

        // Add home screen
        JPanel homeScreen = createAppIconsPanel(phoneFrame);
        mainPanel.add(homeScreen, "Home");

        // Add Investify screen
        JPanel investifyScreen = InvestifyApp.createInvestify();
        mainPanel.add(investifyScreen, "Investify");

        // Add Address Book screen
        JPanel addressBookScreen = AddressBook.createAddressBook();
        mainPanel.add(addressBookScreen, "Address Book");

        // Add Picture Gallery screen
        JPanel pictureGalleryScreen = PictureGallery.createPictureGallery();
        mainPanel.add(pictureGalleryScreen, "Picture Gallery");

        // Add Finance Tracker screen
        FinanceTracker financeTracker = new FinanceTracker();
        JPanel financeTrackerScreen = financeTracker.createFinanceTracker();
        mainPanel.add(financeTrackerScreen, "Finance Tracker");


        phoneFrame.add(mainPanel, BorderLayout.CENTER);
        phoneFrame.add(phoneUtils.createTopBar(), BorderLayout.NORTH);
        phoneFrame.add(phoneUtils.createBottomBar(phoneFrame), BorderLayout.SOUTH);
        phoneFrame.setVisible(true);
    }
}
