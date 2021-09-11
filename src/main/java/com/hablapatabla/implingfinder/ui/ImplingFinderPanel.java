package com.hablapatabla.implingfinder.ui;

import com.hablapatabla.implingfinder.*;
import com.hablapatabla.implingfinder.model.ImplingFinderData;
import com.hablapatabla.implingfinder.model.ImplingFinderEnum;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ImplingFinderPanel extends PluginPanel {
    private static final String RESULTS_PANEL = "RESULTS_PANEL";
    private static final String ERROR_PANEL = "ERROR_PANEL";
    private static final String SPLASH_PANEL = "SPLASH_PANEL";
    private static String[] TargetableImplings = {"Recent", "Magpie", "Ninja", "Crystal", "Dragon", "Lucky"};

    private GridBagConstraints c = new GridBagConstraints();
    private final CardLayout cardLayout = new CardLayout();

    private JPanel impListPanel = new JPanel();
    private JPanel container = new JPanel(cardLayout);
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();
    private final ImplingFinderSplashPanel splashPanel = new ImplingFinderSplashPanel();


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

    @Getter
    private boolean splashRequested = false;

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
        refreshButton.setRequestFocusEnabled(false);
        titlePanel.add(refreshButton, BorderLayout.WEST);

        JPanel fetchPanel = new JPanel();
        fetchPanel.setLayout(new BorderLayout());
        fetchPanel.setBorder(new EmptyBorder(0, 10, 5, 0));

        JComboBox<String> implingSelectionDropDown = new JComboBox<>(TargetableImplings);

        DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
        listRenderer.setRequestFocusEnabled(false);
        listRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER); // center-aligned items
        listRenderer.setOpaque(false);
        listRenderer.setForeground(ColorScheme.DARK_GRAY_COLOR);

        implingSelectionDropDown.setForeground(Color.WHITE);
        implingSelectionDropDown.setBorder(new CompoundBorder(implingSelectionDropDown.getBorder(), new EmptyBorder(0, 0, 0, 11)));
        implingSelectionDropDown.setRenderer(listRenderer);
        implingSelectionDropDown.setRequestFocusEnabled(false);
        implingSelectionDropDown.setSelectedIndex(0);

        fetchPanel.add(implingSelectionDropDown, BorderLayout.WEST);

        JButton fetchButton = new JButton("Fetch");
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchRequested = true;
                String requestedImpling = (String)implingSelectionDropDown.getSelectedItem();
                requestedId = ImplingFinderEnum.getIdByNameFuzzy(requestedImpling);
            }
        });
        fetchButton.setForeground(Color.WHITE);
        fetchButton.setRequestFocusEnabled(false);
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


        JPanel splashWrapper = new JPanel(new BorderLayout());
        splashWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        splashPanel.setContent("Welcome to Impling Finder!",
                " This plugin watches for implings around you, and uploads the locations of implings of Magpie quality" +
                        " or higher to an external server. Uploads are only done when implings are actually found, so this plugin" +
                        " has virtually 0 network usage. Please raise an issue in the github repo if you find one, or would like to request a feature!" +
                        " This plugin crowdsources data, so the more people using it, the better. Tell your friends to install!" +
                        " Try clicking on a found impling to see its precise location on your worldmap." +
                        "\nMake sure to go to the config and check 'Splash Seen' so that you don't see this splash page again.\n\n");
        splashWrapper.add(splashPanel, BorderLayout.NORTH);

        JButton getStartedButton = new JButton("Let's get started");
        getStartedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                continuePastSplash();
            }
        });
        splashWrapper.add(getStartedButton, BorderLayout.SOUTH);

        container.add(splashWrapper, SPLASH_PANEL);
        container.add(impsWrapper, RESULTS_PANEL);
        container.add(errorWrapper, ERROR_PANEL);

        cardLayout.show(container, ERROR_PANEL);

        this.add(container, BorderLayout.CENTER);
    }

    public void showSplash() {
        cardLayout.show(container, SPLASH_PANEL);
    }

    private void continuePastSplash() {
        cardLayout.show(container, ERROR_PANEL);
    }

    public void populateNpcs(List<ImplingFinderData> npcs) {
        c.gridy = 0;
        c.weighty = 0;

        impListPanel.removeAll();

        if (npcs.size() == 0) {
            cardLayout.show(container, ERROR_PANEL);
            return;
        }
        cardLayout.show(container, RESULTS_PANEL);

        List<JPanel> implings = new ArrayList<>();
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
