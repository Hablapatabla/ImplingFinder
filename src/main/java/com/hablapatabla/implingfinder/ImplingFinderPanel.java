package com.hablapatabla.implingfinder;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
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
    private static final String RESULTS_PANEL = "RESULTS_PANEL";
    private static final String ERROR_PANEL = "ERROR_PANEL";
    private static String[] TargetableImplings = {"Any", "Eclectic", "Magpie", "Ninja", "Crystal", "Dragon", "Lucky"};

    private GridBagConstraints c = new GridBagConstraints();
    private final CardLayout cardLayout = new CardLayout();

    private JPanel impListPanel = new JPanel();
    private JPanel container = new JPanel(cardLayout);
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();


    private Logger logger = LoggerFactory.getLogger(ImplingFinderPanel.class);
    protected ImplingFinderPlugin plugin;

    @Getter
    @Setter
    private boolean clearRequested = false;

    @Getter
    @Setter
    private boolean fetchRequested = false;

    @Getter
    @Setter
    private Integer requestedId = -1;

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

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.PAGE_START;

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(new EmptyBorder(10, 10, 10, 0));
        titlePanel.setLayout(new BorderLayout());

        JLabel title = new JLabel();
        title.setText("Impling Finder");
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

        JPanel fetchPanel = new JPanel();
        fetchPanel.setLayout(new BorderLayout());
        fetchPanel.setBorder(new EmptyBorder(0, 10, 5, 0));

        JComboBox cbox = new JComboBox(TargetableImplings);
        cbox.setSelectedIndex(0);
        cbox.setForeground(Color.WHITE);
        fetchPanel.add(cbox, BorderLayout.WEST);

        JButton fetchButton = new JButton("Fetch");
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchRequested = true;
                String requestedImpling = (String)cbox.getSelectedItem();
                Integer id = ImplingFinderEnum.getIdByShortenedName(requestedImpling).getNpcId();
                if (id != null)
                    requestedId = id;
                else
                    requestedId = -1;
            }
        });
        fetchButton.setForeground(Color.WHITE);
        fetchPanel.add(fetchButton, BorderLayout.EAST);

        topContainer.add(titlePanel, BorderLayout.NORTH);
        topContainer.add(fetchPanel, BorderLayout.SOUTH);
        topContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARKER_GRAY_HOVER_COLOR));


        this.add(topContainer, BorderLayout.NORTH);

        impListPanel.setLayout(new GridBagLayout());
        impListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel impsWrapper = new JPanel(new BorderLayout());
        impsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        impsWrapper.add(impListPanel, BorderLayout.NORTH);

        JPanel errorWrapper = new JPanel(new BorderLayout());
        errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        errorPanel.setContent("Nothing to display    :(",
                "Either I have no implings to show you, or an error has occurred.");
        errorWrapper.add(errorPanel, BorderLayout.NORTH);

        container.add(impsWrapper, RESULTS_PANEL);
        container.add(errorWrapper, ERROR_PANEL);

        cardLayout.show(container, ERROR_PANEL);

        this.add(container, BorderLayout.CENTER);
    }

    public void populateNpcs(ArrayList<ImplingFinderData> npcs) {
        c.gridy = 0;
        c.weighty = 0;

        impListPanel.removeAll();

        if (npcs.size() == 0) {
            cardLayout.show(container, ERROR_PANEL);
            return;
        }
        cardLayout.show(container, RESULTS_PANEL);

        ArrayList<JPanel> implings = new ArrayList<>();
        int defaultThumbnailId = ItemID.BABY_MOLERAT;
        for (ImplingFinderData npc : npcs) {
            JPanel marginWrapper = new JPanel(new BorderLayout());
            marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
            marginWrapper.setBorder(new EmptyBorder(5, 0 ,0 ,0));

            ImplingFinderImpPanel imp = new ImplingFinderImpPanel(itemManager, npc, defaultThumbnailId, plugin);
            imp.setAutoscrolls(true);
            imp.setBorder(new EmptyBorder(3, 0 ,0 ,0));
            marginWrapper.add(imp);

            implings.add(marginWrapper);
        }

        for (JPanel p : implings) {
            impListPanel.add(p, c);
            c.gridy += 1;
        }

        repaint();
        revalidate();
    }
}
