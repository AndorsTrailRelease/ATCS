package com.gpl.rpg.atcontentstudio.ui.map;

import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.maps.*;
import tiled.view.MapRenderer;
import tiled.view.OrthogonalRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldMapView extends JComponent implements Scrollable {

    private static final long serialVersionUID = -4111374378777093799L;

    public static final int TILE_SIZE = 32;
    public static final int MIN_ZOOM = 5;
    public static final int MAX_ZOOM = 250;
    public static final int INC_ZOOM = 5;
    public static final float ZOOM_RATIO = 0.01f;

    WorldmapSegment worldmap;
    Project proj;


    public Map<String, Rectangle> mapLocations = new LinkedHashMap<String, Rectangle>();

    public ListSelectionModel selectedSelectionModel = null;
    public ListModel<TMXMap> selectedListModel = null;

    public ListModel<TMXMap> highlightedListModel = null;

    public float zoomLevel = 0.1f;
    int sizeX = 0, sizeY = 0;
    int offsetX = 0, offsetY = 0;


    static final Color selectOutlineColor = new Color(255, 0, 0);
    static final Stroke selectOutlineStroke = new BasicStroke(4f);
    static final Color highlightOutlineColor = Color.CYAN;
    static final Stroke highlightOutlineStroke = new BasicStroke(4f);
    static final Color mapIdLabelOutlineColor = Color.BLACK;
    static final Stroke thinLabelOutlineStroke = new BasicStroke(1.5f);
    static final Stroke labelOutlineStroke = new BasicStroke(3f);

    public WorldMapView(WorldmapSegment worldmap) {
        this.worldmap = worldmap;
        this.proj = worldmap.getProject();
        updateFromModel();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedMap = null;
//				boolean update = false;
                int x = (int) (e.getX() / zoomLevel);
                int y = (int) (e.getY() / zoomLevel);
                for (String s : mapLocations.keySet()) {
                    if (mapLocations.get(s).contains(x, y)) {
                        selectedMap = s;
                        break;
                    }
                }
                if (selectedMap != null) {
                    x = x - mapLocations.get(selectedMap).x;
                    y = y - mapLocations.get(selectedMap).y;
                    //Look for a mapchange there
                    TMXMap map = proj.getMap(selectedMap);

                    boolean mapchangeFound = false;
                    for (MapObjectGroup group : map.groups) {
                        for (MapObject obj : group.mapObjects) {
                            if (obj instanceof MapChange) {
                                if (x >= obj.x && x < obj.x + obj.w && y >= obj.y && y < obj.y + obj.h) {
                                    String mapId = ((MapChange) obj).map != null ? ((MapChange) obj).map.id : ((MapChange) obj).map_id;
                                    mapChangeClicked(e, proj.getMap(selectedMap), proj.getMap(mapId));
                                    mapchangeFound = true;
                                }
                            }
                        }
                    }

                    if (!mapchangeFound) {
                        mapClicked(e, WorldMapView.this.worldmap.getProject().getMap(selectedMap));
                    }
                } else {
                    backgroundClicked(e);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String selectedMap = null;
                int x = (int) (e.getX() / zoomLevel);
                int y = (int) (e.getY() / zoomLevel);
                for (String s : mapLocations.keySet()) {
                    if (mapLocations.get(s).contains(x, y)) {
                        selectedMap = s;
                        break;
                    }
                }
                if (selectedMap != null) {
                    //Reuse x,y to indicate to tile-within-the-map coordinates.
                    x = x - mapLocations.get(selectedMap).x;
                    y = y - mapLocations.get(selectedMap).y;
                    //Look for a mapchange there
                    TMXMap map = proj.getMap(selectedMap);

                    boolean mapchangeFound = false;
                    for (MapObjectGroup group : map.groups) {
                        for (MapObject obj : group.mapObjects) {
                            if (obj instanceof MapChange) {
                                if (x >= obj.x && x < obj.x + obj.w && y >= obj.y && y < obj.y + obj.h) {
                                    String mapId = ((MapChange) obj).map != null ? ((MapChange) obj).map.id : ((MapChange) obj).map_id;
                                    setToolTipText(selectedMap + "->" + mapId);
                                    mapchangeFound = true;
                                }
                            }
                        }
                    }

                    if (!mapchangeFound) {
                        setToolTipText(selectedMap);
                    }
                    ToolTipManager.sharedInstance().registerComponent(WorldMapView.this);
                    ToolTipManager.sharedInstance().setEnabled(true);
                } else {
                    ToolTipManager.sharedInstance().setEnabled(false);
                    ToolTipManager.sharedInstance().unregisterComponent(WorldMapView.this);
                    setToolTipText(null);
                }
            }
        });
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        return event.getPoint();
    }

    private void paintOnGraphics(Graphics2D g2) {
        g2.setPaint(new Color(100, 100, 100));
        g2.fillRect(0, 0, sizeX, sizeY);

        g2.setPaint(selectOutlineColor);
        g2.setStroke(selectOutlineStroke);

        Font areaNameFont = g2.getFont();
        areaNameFont = areaNameFont.deriveFont(70f).deriveFont(Font.BOLD);

        Font mapIdFont = g2.getFont();
        mapIdFont = mapIdFont.deriveFont(50f).deriveFont(Font.BOLD);

        g2.setFont(mapIdFont);
        FontMetrics mifm = g2.getFontMetrics();
        int mapIdLabelHeight = mifm.getHeight();

        for (String s : new HashSet<String>(mapLocations.keySet())) {

            int x = mapLocations.get(s).x;
            int y = mapLocations.get(s).y;

            g2.translate(x, y);


            TMXMap map = proj.getMap(s);
            if (map == null) continue;
            MapRenderer renderer = new OrthogonalRenderer(map.tmxMap);

            // Draw each tile map layer
            for (tiled.core.MapLayer layer : ((TMXMap) map).tmxMap) {
                if (layer instanceof tiled.core.TileLayer && layer.isVisible()) {
                    if (layer.getName().equalsIgnoreCase("walkable")) continue;
                    renderer.paintTileLayer(g2, (tiled.core.TileLayer) layer);
                } else if (layer instanceof tiled.core.ObjectGroup) {
                    paintObjectGroup(g2, map, (tiled.core.ObjectGroup) layer);
                }
            }
            if (map.colorFilter != null) {
                Shape oldClip = g2.getClip();
                g2.setClip(0, 0, map.tmxMap.getWidth() * TILE_SIZE, map.tmxMap.getHeight() * TILE_SIZE);
                MapColorFilters.applyColorfilter(map.colorFilter, g2);
                g2.setClip(oldClip);
            }

            g2.translate(-x, -y);

        }

        if (highlightedListModel != null) {
            outlineFromListModel(g2, highlightedListModel, null, highlightOutlineColor, highlightOutlineStroke, mapIdFont, mapIdLabelHeight);
        }

        if (selectedListModel != null && selectedSelectionModel != null) {
            outlineFromListModel(g2, selectedListModel, selectedSelectionModel, selectOutlineColor, selectOutlineStroke, mapIdFont, mapIdLabelHeight);
        }


        g2.setStroke(labelOutlineStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(areaNameFont);
        FontMetrics fm = g2.getFontMetrics();
        FontRenderContext frc = g2.getFontRenderContext();

        for (String s : worldmap.labels.keySet()) {
            String label = worldmap.labels.get(s).name;
            if (label != null) {
                Rectangle areaCovered = new Rectangle(0, 0, -1, -1);
                for (String map : worldmap.labelledMaps.get(s)) {
                    areaCovered.add(mapLocations.get(map));
                }

                Rectangle2D stringBounds = fm.getStringBounds(label, g2);
                GlyphVector gv = areaNameFont.createGlyphVector(frc, label);
                g2.setColor(Color.WHITE);
                g2.fill(gv.getOutline((int) (areaCovered.getCenterX() - stringBounds.getCenterX()), (int) (areaCovered.getCenterY() - stringBounds.getCenterY())));
                g2.setColor(Color.BLACK);
                g2.draw(gv.getOutline((int) (areaCovered.getCenterX() - stringBounds.getCenterX()), (int) (areaCovered.getCenterY() - stringBounds.getCenterY())));
            }
        }
    }

    private void paintObjectGroup(Graphics2D g2d, TMXMap map, tiled.core.ObjectGroup layer) {
        for (MapObjectGroup group : map.groups) {
            if (group.tmxGroup == layer) {
                for (MapObject object : group.mapObjects) {
                    if (object instanceof MapChange) {
//						Only show mapchange areas pointing to maps not shown in this worldmap
                        if (((MapChange) object).map != null && !mapLocations.containsKey(((MapChange) object).map.id)) {
                            drawObject(object, g2d, new Color(20, 20, 190));
                        }
                    }
                }
                break;
            }
        }
    }

    private void drawObject(MapObject object, Graphics2D g2d, Color color) {
        g2d.setPaint(color);
        g2d.drawRect(object.x + 1, object.y + 1, object.w - 3, object.h - 3);
        g2d.drawRect(object.x + 2, object.y + 2, object.w - 5, object.h - 5);
        g2d.setPaint(color.darker().darker());
        g2d.drawLine(object.x, object.y + object.h - 1, object.x + object.w - 1, object.y + object.h - 1);
        g2d.drawLine(object.x + object.w - 1, object.y, object.x + object.w - 1, object.y + object.h - 1);
        g2d.drawLine(object.x + 3, object.y + 3, object.x + object.w - 4, object.y + 3);
        g2d.drawLine(object.x + 3, object.y + 3, object.x + 3, object.y + object.h - 4);
        g2d.setPaint(color.brighter().brighter().brighter());
        g2d.drawLine(object.x, object.y, object.x + object.w - 1, object.y);
        g2d.drawLine(object.x, object.y, object.x, object.y + object.h - 1);
        g2d.drawLine(object.x + 3, object.y + object.h - 4, object.x + object.w - 4, object.y + object.h - 4);
        g2d.drawLine(object.x + object.w - 4, object.y + 3, object.x + object.w - 4, object.y + object.h - 4);
        Image img = object.getIcon();
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.fillRect(object.x + 2, object.y + 2, img.getWidth(null), img.getHeight(null));
        g2d.drawImage(object.getIcon(), object.x + 2, object.y + 2, null);
    }

    private void outlineFromListModel(Graphics2D g2, ListModel<TMXMap> listModel, ListSelectionModel selectionModel, Color outlineColor, Stroke outlineStroke, Font mapIdFont, int mapIdLabelHeight) {
        for (int i = 0; i < listModel.getSize(); i++) {
            //No selection model ? We want to highlight the whole list.
            if (selectionModel == null || selectionModel.isSelectedIndex(i)) {
                TMXMap map = listModel.getElementAt(i);
                int x = mapLocations.get(map.id).x;
                int y = mapLocations.get(map.id).y;

                g2.translate(x, y);

                GlyphVector gv = mapIdFont.createGlyphVector(g2.getFontRenderContext(), map.id);
                g2.setStroke(outlineStroke);
                g2.setColor(outlineColor);
                g2.drawRect(0, 0, map.tmxMap.getWidth() * TILE_SIZE, map.tmxMap.getHeight() * TILE_SIZE);

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(thinLabelOutlineStroke);
                g2.fill(gv.getOutline(8, 8 + mapIdLabelHeight));
                g2.setColor(mapIdLabelOutlineColor);
                g2.draw(gv.getOutline(8, 8 + mapIdLabelHeight));

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                g2.translate(-x, -y);
            }
        }
    }

    public List<String> getSelectedMapsIDs() {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < selectedListModel.getSize(); i++) {
            if (selectedSelectionModel.isSelectedIndex(i)) {
                TMXMap map = selectedListModel.getElementAt(i);
                result.add(map.id);
            }
        }
        return result;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.scale(zoomLevel, zoomLevel);
//			g2.drawImage(img, 0, 0, null);
            paintOnGraphics(g2);

        } finally {
            g2.dispose();
        }

    }


    public interface MapClickListener {
        public void mapClicked(MouseEvent e, TMXMap m);

        public void mapChangeClicked(MouseEvent e, TMXMap m, TMXMap changeTarget);

        public void backgroundClicked(MouseEvent e);
    }

    private List<MapClickListener> listeners = new CopyOnWriteArrayList<MapClickListener>();

    public void addMapClickListener(MapClickListener l) {
        listeners.add(l);
    }

    public void removeMapClickListener(MapClickListener l) {
        listeners.remove(l);
    }

    private void mapClicked(MouseEvent e, TMXMap m) {
        for (MapClickListener l : listeners) l.mapClicked(e, m);
    }

    private void mapChangeClicked(MouseEvent e, TMXMap m, TMXMap changeTarget) {
        for (MapClickListener l : listeners) l.mapChangeClicked(e, m, changeTarget);
    }

    private void backgroundClicked(MouseEvent e) {
        for (MapClickListener l : listeners) l.backgroundClicked(e);
    }

//	private boolean paintObjectGroup(Graphics2D g2d, TMXMap map, tiled.core.ObjectGroup layer) {
//    	boolean paintSelected = false;
//    	for (MapObjectGroup group : map.groups) {
//			if (group.tmxGroup == layer) {
//				for (MapObject object : group.mapObjects) {
//					drawObject(object, g2d, new Color(20, 20, 190));
//				}
//				break;
//			}
//		}
//    	return paintSelected;
//	}
//
//	private void drawObject(MapObject object, Graphics2D g2d, Color color) {
//		g2d.setPaint(color);
//		g2d.drawRect(object.x+1, object.y+1, object.w-3, object.h-3);
//		g2d.drawRect(object.x+2, object.y+2, object.w-5, object.h-5);
//		g2d.setPaint(color.darker().darker());
//		g2d.drawLine(object.x, object.y + object.h - 1, object.x + object.w - 1, object.y + object.h - 1);
//		g2d.drawLine(object.x + object.w - 1, object.y, object.x + object.w - 1, object.y + object.h - 1);
//		g2d.drawLine(object.x + 3, object.y + 3, object.x + object.w - 4, object.y + 3);
//		g2d.drawLine(object.x + 3, object.y + 3, object.x + 3, object.y + object.h - 4);
//		g2d.setPaint(color.brighter().brighter().brighter());
//		g2d.drawLine(object.x, object.y, object.x + object.w - 1, object.y);
//		g2d.drawLine(object.x, object.y, object.x, object.y + object.h - 1);
//		g2d.drawLine(object.x + 3, object.y + object.h - 4, object.x + object.w - 4, object.y + object.h - 4);
//		g2d.drawLine(object.x + object.w - 4, object.y + 3, object.x + object.w - 4, object.y + object.h - 4);
//		Image img = object.getIcon();
//		g2d.setColor(new Color(255, 255, 255, 120));
//		g2d.fillRect(object.x + 2, object.y + 2, img.getWidth(null), img.getHeight(null));
//		g2d.drawImage(object.getIcon(), object.x + 2, object.y + 2, null);
//	}

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) (zoomLevel * sizeX), (int) (zoomLevel * sizeY));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return TILE_SIZE;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 4 * TILE_SIZE;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean recomputeSize() {
        sizeX = sizeY = 0;
        boolean originMoved = false;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        for (String s : mapLocations.keySet()) {
            int x = mapLocations.get(s).x;
            int w = proj.getMap(s).tmxMap.getWidth() * TILE_SIZE;
            int y = mapLocations.get(s).y;
            int h = proj.getMap(s).tmxMap.getHeight() * TILE_SIZE;

            sizeX = Math.max(sizeX, x + w);
            sizeY = Math.max(sizeY, y + h);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
        }

        if (minX != 0) {
            for (String s : mapLocations.keySet()) {
                mapLocations.get(s).x -= minX;
            }
            sizeX -= minX;
            offsetX += minX;
            originMoved = true;
        }

        if (minY != 0) {
            for (String s : mapLocations.keySet()) {
                mapLocations.get(s).y -= minY;
            }
            sizeY -= minY;
            offsetY += minY;
            originMoved = true;
        }
        return originMoved;
    }


    public void updateFromModel() {
        mapLocations.clear();
        sizeX = sizeY = 0;
        offsetX = worldmap.segmentX * TILE_SIZE;
        offsetY = worldmap.segmentY * TILE_SIZE;
        for (String s : worldmap.mapLocations.keySet()) {
            if (proj.getMap(s) == null) {
                System.err.println("Warning. Worldmap " + worldmap.id + " references map " + s + " but it doesn't exist in this project");
                continue;
            }
            int x = worldmap.mapLocations.get(s).x * TILE_SIZE;
            int w = proj.getMap(s).tmxMap.getWidth() * TILE_SIZE;
            int y = worldmap.mapLocations.get(s).y * TILE_SIZE;
            int h = proj.getMap(s).tmxMap.getHeight() * TILE_SIZE;

            sizeX = Math.max(sizeX, x + w);
            sizeY = Math.max(sizeY, y + h);

            mapLocations.put(s, new Rectangle(x, y, w, h));
        }
    }

    public void pushToModel() {
        worldmap.segmentX = offsetX / TILE_SIZE;
        worldmap.segmentY = offsetY / TILE_SIZE;
        for (String id : worldmap.mapLocations.keySet()) {
            if (worldmap.getProject().getMap(id) == null) {
                System.err.println("Warning. Worldmap " + worldmap.id + " references map " + id + " but it doesn't exist in this project");
                continue;
            }
            worldmap.getProject().getMap(id).removeBacklink(worldmap);
        }
        worldmap.mapLocations.clear();
        for (String s : mapLocations.keySet()) {
            int x = mapLocations.get(s).x / TILE_SIZE;
            int y = mapLocations.get(s).y / TILE_SIZE;

            worldmap.mapLocations.put(s, new Point(x, y));
        }

        for (String id : worldmap.mapLocations.keySet()) {
            if (worldmap.getProject().getMap(id) == null) {
                System.err.println("Warning. Worldmap " + worldmap.id + " references map " + id + " but it doesn't exist in this project");
                continue;
            }
            worldmap.getProject().getMap(id).addBacklink(worldmap);
        }
    }

}
