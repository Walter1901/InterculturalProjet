package Address;// Nécessite org.json dans les dépendances Maven

import org.json.JSONObject;
import org.jxmapviewer.viewer.GeoPosition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GeoPositionClass {

    public static GeoPosition Position(String address) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + URLEncoder.encode(address, "UTF-8");
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "JavaApp");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.readLine();
            in.close();
            if (response != null && response.startsWith("[")) {
                org.json.JSONArray arr = new org.json.JSONArray(response);
                if (arr.length() > 0) {
                    JSONObject obj = arr.getJSONObject(0);
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    return new org.jxmapviewer.viewer.GeoPosition(lat, lon);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}