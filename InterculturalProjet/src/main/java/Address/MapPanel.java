    package Address;

    import org.jxmapviewer.JXMapViewer;
    import org.jxmapviewer.viewer.*;

    import org.jxmapviewer.viewer.Waypoint;
    import org.jxmapviewer.viewer.DefaultWaypoint;
    import org.jxmapviewer.viewer.WaypointPainter;

    import java.util.Set;
    import java.util.HashSet;

    import javax.swing.*;
    import java.awt.*;


    public class MapPanel extends JPanel {
        private JXMapViewer mapViewer = new JXMapViewer();

        public MapPanel() {
            setLayout(new BorderLayout());


            TileFactoryInfo info = new TileFactoryInfo(
                    1, 17, 17,
                    256, true, true,
                    "https://tile.openstreetmap.org",
                    "x", "y", "z") {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int z = 17 - zoom;
                    return this.baseURL + "/" + z + "/" + x + "/" + y + ".png";
                }
            };
            DefaultTileFactory tileFactory = new DefaultTileFactory(info);
            // Création du composant carte
            mapViewer.setTileFactory(tileFactory);


            // Position initiale (latitude, longitude)
            GeoPosition position = new GeoPosition(46.2044, 6.1432); // Genève par exemple
            mapViewer.setZoom(4);
            mapViewer.setAddressLocation(position);

            add(mapViewer, BorderLayout.CENTER);
        }
        // Dans MapPanel.java
        public JXMapViewer getMapViewer() {
            return mapViewer;
        }

        public void showAddressOnMap(String address) {
            GeoPosition pos = GeoPositionClass.Position(address);
            if (pos != null) {
                // Centrer la carte
                mapViewer.setAddressLocation(pos);
                mapViewer.setZoom(5);

                // Ajouter un marqueur
                Set<Waypoint> waypoints = new HashSet<>();
                waypoints.add(new DefaultWaypoint(pos));
                WaypointPainter<Waypoint> painter = new WaypointPainter<>();
                painter.setWaypoints(waypoints);
                mapViewer.setOverlayPainter(painter);
            }
        }
    }