package Finance.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class CurrencyConverter {
    /**
     * Fetches the current exchange rate between two currencies using the Frankfurter API
     * @param fromCurrency The currency code to convert from (e.g., "USD")
     * @param toCurrency The currency code to convert to (e.g., "EUR")
     * @return The current exchange rate as a double
     * @throws RuntimeException if the API request fails or the response is invalid
     */
    public static double getExchangeRate(String fromCurrency, String toCurrency) {
        try {
            // Construct the API URL with the specified currencies
            String urlStr = String.format(
                    "https://api.frankfurter.app/latest?from=%s&to=%s",
                    fromCurrency, toCurrency);

            System.out.println("Requesting URL: " + urlStr); // Log the URL being requested for debugging

            // Create URL object and open HTTP connection
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET"); // Set the HTTP request method to GET

            int responseCode = con.getResponseCode(); // Get the HTTP response code

            // Check if the response code indicates success (200)
            if (responseCode != 200) {
                throw new RuntimeException("HTTP error code: " + responseCode); // Throw exception for non-successful responses
            }

            // Set up a reader to process the API response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            // Read the response line by line and build the complete response string
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close(); // Close the input stream

            System.out.println("Response JSON: " + response.toString()); // Log the complete JSON response for debugging

            JSONObject json = new JSONObject(response.toString()); // Parse the JSON response into a JSONObject
            JSONObject rates = json.getJSONObject("rates"); // Extract the "rates" object from the JSON response

            if (!rates.has(toCurrency)) { // Verify the target currency exists in the rates
                throw new RuntimeException("Currency not found in response");
            }

            double rate = rates.getDouble(toCurrency); // Get the specific exchange rate for the target currency

            System.out.println("Exchange rate from " + fromCurrency + " to " + toCurrency + ": " + rate); // Log the retrieved exchange rate for debugging

            return rate; // Return the exchange rate

        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace for debugging purposes

            throw new RuntimeException("Currency conversion failed: " + e.getMessage()); // Wrap any exceptions in a RuntimeException with a descriptive message
        }
    }
}