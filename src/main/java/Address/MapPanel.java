package Address;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import java.util.Set;

public class MapPanel extends JPanel {
    private JXMapViewer mapViewer = new JXMapViewer();
    private Point lastPoint;

    public MapPanel() {
        setLayout(new BorderLayout());

        // Création des boutons de zoom
        JPanel zoomPanel = new JPanel(new GridLayout(1, 2));
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("-");

        zoomIn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() - 1));
        zoomOut.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() + 1));

        zoomPanel.add(zoomIn);
        zoomPanel.add(zoomOut);
        add(zoomPanel, BorderLayout.NORTH);

        // Configuration de la carte
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
        mapViewer.setTileFactory(tileFactory);

        // Position initiale (latitude, longitude)
        GeoPosition position = new GeoPosition(46.2044, 6.1432); // Genève par exemple
        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(position);

        // Ajout des contrôles de navigation
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
            }
        });

        mapViewer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Point currentPoint = e.getPoint();
                    int deltaX = currentPoint.x - lastPoint.x;
                    int deltaY = currentPoint.y - lastPoint.y;

                    GeoPosition currentCenter = mapViewer.getAddressLocation();
                    double zoom = mapViewer.getZoom();

                    // Calcul des déplacements en latitude/longitude
                    double latDelta = deltaY * -0.0001 * zoom;
                    double lonDelta = deltaX * -0.0001 * zoom;

                    // Création de la nouvelle position
                    GeoPosition newCenter = new GeoPosition(
                            currentCenter.getLatitude() - latDelta,
                            currentCenter.getLongitude() + lonDelta
                    );

                    mapViewer.setAddressLocation(newCenter);
                    lastPoint = currentPoint;
                }
            }
        });

        // Ajout du zoom avec la molette
        mapViewer.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            if (rotation < 0) {
                mapViewer.setZoom(mapViewer.getZoom() - 1);
            } else if (rotation > 0) {
                mapViewer.setZoom(mapViewer.getZoom() + 1);
            }
        });

        add(mapViewer, BorderLayout.CENTER);
    }

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