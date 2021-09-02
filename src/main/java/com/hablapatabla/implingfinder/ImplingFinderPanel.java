package com.hablapatabla.implingfinder;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ImplingFinderPanel extends PluginPanel {
    ArrayList<ImplingFinderImpPanel> impList = new ArrayList<>();
    JPanel impListPanel = new JPanel();

    private static String[] TargetableImplings = {"Eclectic", "Magpie", "Ninja", "Dragon"};
    private Logger logger = LoggerFactory.getLogger(ImplingFinderPanel.class);
    private JScrollPane scrollPane;
    private GridBagConstraints c = new GridBagConstraints();

    protected ImplingFinderPlugin plugin;

    final static int MAX_ACTOR_VIEW_RANGE = 15;

    @Getter
    @Setter
    private boolean clearRequested = false;

    @Getter
    @Setter
    private boolean fetchRequested = false;

    @Getter
    @Setter
    private boolean fetchTargetedRequested = false;

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    protected ImplingFinderPanel(ImplingFinderPlugin plugin) {
        this.plugin = plugin;
        implingFinderPanelHelper(plugin);
    }

    private void implingFinderPanelHelper(ImplingFinderPlugin plugin) {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        titlePanel.setLayout(new BorderLayout());

        JLabel title = new JLabel();
        title.setText("Impling tracker");
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.EAST);


        JButton refreshButton = new JButton("Clear");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearRequested = true;
            }
        });
        refreshButton.setForeground(Color.WHITE);
        titlePanel.add(refreshButton, BorderLayout.WEST);

        JButton fetchButton = new JButton("Fetch Implings");
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchRequested = true;
            }
        });
        fetchButton.setForeground(Color.WHITE);
        titlePanel.add(fetchButton, BorderLayout.SOUTH);

        topContainer.add(titlePanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        impListPanel.setLayout(new GridBagLayout());
        impListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        impListPanel.setBorder(new EmptyBorder(10, 10 ,10 ,10));
        //c.insets = new Insets(1, 1, 1, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.PAGE_START;


        scrollPane = new JScrollPane(impListPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void populateNpcs(ArrayList<ImplingFinderData> npcs) {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        if (container == null)
            return;
        logger.debug("POPULATE NPCS");
        c.gridy = 0;
        c.weighty = 0;

        for (ImplingFinderImpPanel p : impList) {
            impListPanel.remove(p);
        }
        impList.clear();

        ArrayList<Item> inventory = new ArrayList<Item>();
        for (Item item : container.getItems()) {
            //if (item.getQuantity() > 0) Uncomment to get rid of weird dwarf
            inventory.add(item);
        }

        ArrayList<ImplingFinderImpPanel> implings = new ArrayList<ImplingFinderImpPanel>();
        int defaultThumbnailId = inventory.get(0).getId();
        for (ImplingFinderData npc : npcs) {
            ImplingFinderImpPanel imp = new ImplingFinderImpPanel(itemManager, npc, defaultThumbnailId, plugin);
            implings.add(imp);
        }

        for (ImplingFinderImpPanel p : implings) {
            impListPanel.add(p, c);
            c.gridy += 1;
            impList.add(p);
        }

        repaint();
        revalidate();
    }

    /*
    public void populate() {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        if (container == null)
            return;
        c.gridy = 0;
        c.weighty = 0;

        for (ImplingFinderImpPanel p : impList) {
            impListPanel.remove(p);
        }
        impList.clear();

        ArrayList<ImplingFinderImpPanel> implings = new ArrayList<ImplingFinderImpPanel>();
        Set<Item> inventory = new HashSet<Item>();
        for (Item item : container.getItems()) {
            //if (item.getQuantity() > 0) Uncomment to get rid of weird dwarf
                inventory.add(item);
        }

        int world = client.getWorld();
        for (Item uniqueItem : inventory) {
            AsyncBufferedImage icon = itemManager.getImage(uniqueItem.getId());
            String name = client.getItemDefinition(uniqueItem.getId()).getName();
            ImplingFinderImpPanel imp = new ImplingFinderImpPanel(icon, name, world, uniqueItem.getId());
            implings.add(imp);
        }

        for (ImplingFinderImpPanel p : implings) {
            impListPanel.add(p, c);
            c.gridy += 1;
            impList.add(p);
        }

        repaint();
        revalidate();
    }*/

}
