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
    private JPanel implingListContainer = new JPanel(cardLayout);
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
    private boolean fetchTargetedRequested = false;


    protected List<ImplingFinderButton> buttonList = new ArrayList<ImplingFinderButton>();

    @Getter
    private boolean splashRequested = false;

    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    protected ImplingFinderPanel(ImplingFinderPlugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        implingFinderPanelHelper(plugin);
    }

    private void implingFinderPanelHelper(ImplingFinderPlugin plugin) {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.PAGE_START;

        /*
         * Mockup of overall Panel
         *
         * {
         *     [(clear)        (Impling Finder)]  - clearButton, title
         *  {
         *     [(Recent)   (Magpie)  (Ninja)
         *     (Crystal)  (Dragon)  (Lucky)   ]  - implingSelections (2x3 GridLayout)
         *     [           (Fetch)            ]  - fetchButton
         *  } - fetchPanel
         * } - topContainer
         *
         * {
         *        [ IMPLING LIST ]
         * } - container
         *
         */

        /*
         * Subpanel for all buttons and info at top of the overall Impling Finder panel
         */
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BorderLayout());
        topContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                                                            ColorScheme.DARKER_GRAY_HOVER_COLOR));

        /*
         * Subpanel for title and clear button in top row of topContainer panel
         */
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        titlePanel.setLayout(new BorderLayout());

        JLabel title = new JLabel();
        title.setText("Impling Finder");
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.EAST);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearRequested = true;
            }
        });
        clearButton.setForeground(Color.WHITE);
        clearButton.setRequestFocusEnabled(false);
        titlePanel.add(clearButton, BorderLayout.WEST);

        topContainer.add(titlePanel, BorderLayout.NORTH);

        /*
         * Lower fetch panel, contains impling buttons and fetch button
         */

        JPanel fetchPanel = new JPanel();
        fetchPanel.setLayout(new BorderLayout());
        fetchPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JPanel implingSelections = new JPanel(new GridLayout(2, 3));
        implingSelections.setBorder(new EmptyBorder(0, -1, 3, -1));
        populateButtonList();
        for (JButton p : buttonList)
            implingSelections.add(p);
        fetchPanel.add(implingSelections, BorderLayout.NORTH);

        JButton fetchButton = new JButton("Fetch");
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchRequested = true;
            }
        });
        fetchButton.setForeground(Color.WHITE);
        fetchButton.setRequestFocusEnabled(false);
        fetchPanel.add(fetchButton, BorderLayout.SOUTH);

        topContainer.add(fetchPanel, BorderLayout.SOUTH);

        this.add(topContainer, BorderLayout.NORTH);

        /*
         * End topContainer, add impling list
         */

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
                " This plugin watches for implings around you, and uploads the locations of implings of" +
                        " Magpie quality or higher to an external server. Try clicking on a found impling to see its" +
                        " precise location on your worldmap. Make sure to go to the config and check 'Splash Seen' so" +
                        " that you don't see this splash page again. Uploads are only done when implings are actually" +
                        " found, so this plugin has virtually 0 network usage. Please raise an issue in the github repo" +
                        " if you find one, or would like to request a feature! This plugin crowdsources data, so the" +
                        " more people using it, the better. Tell your friends to install!");
        splashWrapper.add(splashPanel, BorderLayout.NORTH);

        JButton getStartedButton = new JButton("Let's get started");
        getStartedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                continuePastSplash();
            }
        });
        splashWrapper.add(getStartedButton, BorderLayout.SOUTH);

        implingListContainer.add(splashWrapper, SPLASH_PANEL);
        implingListContainer.add(impsWrapper, RESULTS_PANEL);
        implingListContainer.add(errorWrapper, ERROR_PANEL);

        cardLayout.show(implingListContainer, SPLASH_PANEL);

        this.add(implingListContainer, BorderLayout.CENTER);
    }

    public void notifyButtonSelected(String s) {
        for (ImplingFinderButton ib : buttonList) {
            if ((s.equals("Recent") && !ib.getName().equals("Recent")) ||
                    (!s.equals("Recent") && ib.getName().equals("Recent")))
                ib.setSelected(false);
            ib.paint(ib.getGraphics());
        }
    }

    public List<Integer> getSelectedButtons() {
        List<Integer> l = new ArrayList<>();
        for (ImplingFinderButton ib : buttonList) {
            if (ib.isSelected())
                l.add(ImplingFinderEnum.getIdByNameFuzzy(ib.getName()));
        }
        return l;
    }

    private void populateButtonList() {
        buttonList.clear();
        for (String s : TargetableImplings) {
            Image i;
            int id = ImplingFinderEnum.getIdByNameFuzzy(s);
            if (id == ImplingFinderPlugin.RECENT_IMPLINGS_ID)
                i = itemManager.getImage(ItemID.TEAK_CLOCK);
            else {
                int itemid = ImplingFinderImpPanel.getItemIdFromNpcId(id);
                i = itemManager.getImage(itemid);
            }
            ImplingFinderButton b = new ImplingFinderButton(i, s);

            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    b.setSelected(!b.isSelected());
                    notifyButtonSelected(b.getName());
                }
            });

            // Always start with Recent selected
            if (id == ImplingFinderPlugin.RECENT_IMPLINGS_ID)
                b.setSelected(true);
            buttonList.add(b);
        }
    }

    public void showSplash() {
        cardLayout.show(implingListContainer, SPLASH_PANEL);
    }

    public void continuePastSplash() {
        cardLayout.show(implingListContainer, ERROR_PANEL);
    }

    public void populateNpcs(List<ImplingFinderData> npcs) {
        c.gridy = 0;
        c.weighty = 0;

        impListPanel.removeAll();

        if (npcs.size() == 0) {
            cardLayout.show(implingListContainer, ERROR_PANEL);
            return;
        }
        cardLayout.show(implingListContainer, RESULTS_PANEL);

        List<JPanel> implings = new ArrayList<>();
        int defaultThumbnailId = ItemID.BABY_MOLERAT;
        for (ImplingFinderData npc : npcs) {
            JPanel marginWrapper = new JPanel(new BorderLayout());
            marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
            // Magic numbers yikes
            marginWrapper.setBorder(new EmptyBorder(5, 0 ,0 ,-4));

            ImplingFinderImpPanel imp = new ImplingFinderImpPanel(itemManager, npc, defaultThumbnailId, plugin);
            imp.setAutoscrolls(true);
            imp.setBorder(new EmptyBorder(3, 0 ,0 ,7));
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
