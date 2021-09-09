package com.hablapatabla.implingfinder;

import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.AsyncBufferedImage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ImplingFinderImpPanel extends JPanel {
    private static final Dimension ICON_SIZE = new Dimension(32, 32);

    @Inject
    private WorldMapPointManager worldMapPointManager;

    // npc id to ItemID
    private Map<Integer, Integer> thumbnails = new HashMap<Integer, Integer>() {{
        /*put(NpcID.BABY_IMPLING, ItemID.BABY_IMPLING_JAR);
        put(NpcID.YOUNG_IMPLING, ItemID.YOUNG_IMPLING_JAR);
        put(NpcID.GOURMET_IMPLING, ItemID.GOURMET_IMPLING_JAR);
        put(NpcID.EARTH_IMPLING, ItemID.EARTH_IMPLING_JAR);
        put(NpcID.ESSENCE_IMPLING, ItemID.ESSENCE_IMPLING_JAR);*/
        //put(NpcID.ECLECTIC_IMPLING, ItemID.ECLECTIC_IMPLING_JAR);
        //put(NpcID.NATURE_IMPLING, ItemID.NATURE_IMPLING_JAR);
        put(NpcID.MAGPIE_IMPLING, ItemID.MAGPIE_IMPLING_JAR);
        put(NpcID.NINJA_IMPLING, ItemID.NINJA_IMPLING_JAR);
        put(NpcID.CRYSTAL_IMPLING, ItemID.CRYSTAL_IMPLING_JAR);
        put(NpcID.DRAGON_IMPLING, ItemID.DRAGON_IMPLING_JAR);
        put(NpcID.LUCKY_IMPLING, ItemID.LUCKY_IMPLING_JAR);
        put(NpcID.NINJA_IMPLING_1653, ItemID.NINJA_IMPLING_JAR);
        put(NpcID.DRAGON_IMPLING_1654, ItemID.DRAGON_IMPLING_JAR);
        put(NpcID.MAGPIE_IMPLING_1652, ItemID.MAGPIE_IMPLING_JAR);
        put(NpcID.LUCKY_IMPLING_7302, ItemID.LUCKY_IMPLING_JAR);
    }};

    private Logger logger = LoggerFactory.getLogger(ImplingFinderImpPanel.class);
    private List<JPanel> panels = new ArrayList<>();

    protected ImplingFinderPlugin plugin;

    ImplingFinderImpPanel(ItemManager manager, ImplingFinderData data, Integer defaultId, ImplingFinderPlugin plugin) {
        Color background = getBackground();
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);
        setToolTipText(ImplingFinderEnum.findById(data.getNpcid()).getName());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panels.add(this);
        setBorder(new EmptyBorder(7, 0, 0, 0));

        WorldPoint implingWorldPoint = new WorldPoint(data.getXcoord(), data.getYcoord(), data.getPlane());

        MouseAdapter itemPanelMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e) {
                for (JPanel p : panels)
                    matchComponentBackground(p, ColorScheme.DARK_GRAY_HOVER_COLOR);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for (JPanel p : panels)
                    matchComponentBackground(p, ColorScheme.DARKER_GRAY_COLOR);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                plugin.addMapPoints(implingWorldPoint);
            }
        };

        addMouseListener(itemPanelMouseListener);

        final JLabel itemIcon = new JLabel();
        itemIcon.setPreferredSize(ICON_SIZE);
        Integer id = thumbnails.get(data.getNpcid());
        AsyncBufferedImage icon;
        if (id != null)
            manager.getImage(id).addTo(itemIcon);
        else
            manager.getImage(defaultId).addTo(itemIcon);

        JPanel iconPanel = new JPanel();
        panels.add(iconPanel);
        iconPanel.add(itemIcon);
        add(iconPanel, BorderLayout.LINE_START);


        JPanel rightPanel = new JPanel(new GridLayout(3, 1));
        panels.add(rightPanel);
        rightPanel.setBackground(background);

        JLabel itemName = new JLabel();
        itemName.setForeground(Color.WHITE);
        itemName.setMaximumSize(new Dimension(0, 0));        // to limit the label's size for
        itemName.setPreferredSize(new Dimension(0, 0));    // items with longer names
        itemName.setText(ImplingFinderEnum.findById(data.getNpcid()).getName());
        rightPanel.add(itemName);

        JPanel middleTextPanel = new JPanel(new BorderLayout());
        middleTextPanel.setBackground(background);
        panels.add(middleTextPanel);

        JLabel middleLeftTextLabel = new JLabel();
        middleLeftTextLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);

        middleLeftTextLabel.setText("World: " + data.getWorld());
        middleTextPanel.add(middleLeftTextLabel, BorderLayout.WEST);

        JLabel middleRightTextLabel = new JLabel();
        middleRightTextLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        String day;
        long daysBetween = ChronoUnit.DAYS.between(data.getDiscoveredtime(), Instant.now());
        if (daysBetween == 0)
            day = "Today";
        else if (daysBetween == 1)
            day = "Yesterday";
        else
            day = daysBetween + " days ago";

        middleRightTextLabel.setText(day);
        middleTextPanel.add(middleRightTextLabel, BorderLayout.EAST);
        rightPanel.add(middleTextPanel);

        JPanel bottomTextRowPanel = new JPanel(new BorderLayout());
        bottomTextRowPanel.setBackground(background);
        panels.add(bottomTextRowPanel);

        JLabel bottomLeftTextLabel = new JLabel();
        String foundTime = data.getDiscoveredtime().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG));
        bottomLeftTextLabel.setText(foundTime);
        bottomLeftTextLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        bottomTextRowPanel.add(bottomLeftTextLabel, BorderLayout.WEST);

        final int playerRegionId = implingWorldPoint.getRegionID();
        String location;
        if (ImplingFinderRegion.fromRegion(playerRegionId) != null)
            location = ImplingFinderRegion.fromRegion(playerRegionId).getName();
        else
            location = "Unknown";
        location = StringUtils.abbreviate(location, 12);

        JLabel bottomRightTextLabel = new JLabel();
        bottomRightTextLabel.setText(location);
        bottomRightTextLabel.setForeground(ColorScheme.GRAND_EXCHANGE_LIMIT);
        bottomRightTextLabel.setBorder(new CompoundBorder(bottomRightTextLabel.getBorder(), new EmptyBorder(0, 0, 0, 7)));
        bottomTextRowPanel.add(bottomRightTextLabel, BorderLayout.EAST);

        rightPanel.add(bottomTextRowPanel);

        for (JPanel p : panels)
            matchComponentBackground(p, ColorScheme.DARKER_GRAY_COLOR);

        add(rightPanel, BorderLayout.CENTER);
    }

    private void matchComponentBackground(JPanel panel, Color color) {
        panel.setBackground(color);
        for (Component c : panel.getComponents()) {
            c.setBackground(color);
        }
    }
}