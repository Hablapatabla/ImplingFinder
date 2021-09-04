package com.hablapatabla.implingfinder;

import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@PluginDescriptor(
        name = "implingfinder",
        description = "A plugin to crowdsource impling locations",
        tags = {"config", "menu", "impling", "finder", "hunter", "group",
                    "fun", "crowdsource", "crowd", "party", "implingfinder", "impling finder",
                    "clue", "clue scroll", "medium clue",},
        loadWhenOutdated = true,
        enabledByDefault = true
)
public class ImplingFinderPlugin extends Plugin {
    @Inject
    private ImplingFinderConfig config;

    @Inject
    private ImplingFinderPanel panel;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private Client client;

    @Inject
    private Gson gson;

    @Inject
    private ItemManager itemManager;

    @Inject
    private WorldMapPointManager worldMapPointManager;

    @Inject
    private ImplingFinderWebManager webManager;

    @Getter(AccessLevel.PACKAGE)
    private NavigationButton button = null;

    @Getter(AccessLevel.PACKAGE)
    private ArrayList<ImplingFinderData> implingsToUpload = new ArrayList<>();

    @Setter(AccessLevel.PACKAGE)
    private ArrayList<ImplingFinderData> remotelyFetchedImplings = new ArrayList<>();


    @Provides
    ImplingFinderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ImplingFinderConfig.class);
    }

    private Logger logger;
    private BufferedImage icon;
    private BufferedImage mapArrow;

    private boolean mapPointSet = false;
    private long lastGetCall = System.currentTimeMillis();

    protected static final String CONFIG_GROUP = "Impling Finder";
    private static final int NPC_UPLOAD_TIME = 20;
    private static final int PANEL_REFRESH_TIME = 1;

    @Override
    protected void startUp() throws Exception {
        logger = LoggerFactory.getLogger(ImplingFinderPlugin.class);
        loadPluginPanel();
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(button);
    }

    private void loadPluginPanel() {
        try {
           icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
        }
        catch (Exception e) {
            logger.error("Couldn't load plugin icon");
            return;
        }
        if (button != null) {
            clientToolbar.removeNavigation(button);
        }

        panel = injector.getInstance(ImplingFinderPanel.class);

        button = NavigationButton.builder().tooltip("Impling Finder").icon(icon).priority(1).panel(panel).build();
        clientToolbar.addNavigation(button);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        final NPC npc = npcSpawned.getNpc();

        if (npc.getName() == null)
            return;

        if (npcAlreadyInList(npc.getIndex()))
            return;

        if (!isImpling(npc.getId()))
            return;

        ImplingFinderData imp = makeImp(npc);
        implingsToUpload.add(imp);
    }

    private boolean npcAlreadyInList(int index) {
        return remotelyFetchedImplings.stream().anyMatch(npc -> npc.getNpcindex() == index) ||
                implingsToUpload.stream().anyMatch(npc -> npc.getNpcindex() == index);
    }

    private boolean isImpling(int id) {
        return ImplingFinderEnum.findById(id) != null;
    }

    private ImplingFinderData makeImp(NPC n) {
        int world = client.getWorld();
        WorldArea area = n.getWorldArea();
        WorldPoint point = area.toWorldPoint();
        return new ImplingFinderData(n.getId(), n.getIndex(), world, point.getX(), point.getY(), point.getPlane(),
                ZonedDateTime.now(ZoneId.of("UTC")).toString());
    }

    public BufferedImage getClueScrollImage() {
        return itemManager.getImage(ItemID.CLUE_SCROLL_MASTER);
    }

    public BufferedImage getMapArrow() {
        if (mapArrow != null)
            return mapArrow;

        mapArrow = ImageUtil.loadImageResource(getClass(), "/util/clue_arrow.png");
        return mapArrow;
    }

    public void addMapPoints(WorldPoint... points) {
        WorldPoint p = client.getLocalPlayer().getWorldLocation();
        if (p == null)
            return;

        if (mapPointSet) {
            mapPointSet = false;
            worldMapPointManager.removeIf(ImplingFinderWorldMapPoint.class::isInstance);
            return;
        }

        mapPointSet = true;
        worldMapPointManager.removeIf(ImplingFinderWorldMapPoint.class::isInstance);
        for (final WorldPoint point : points)
        {
            System.out.println(point.toString());
            worldMapPointManager.add(new ImplingFinderWorldMapPoint(point, this));
        }
    }

    @Schedule(
            period = NPC_UPLOAD_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    public void uploadFoundImplings() {
        // List is cleared by webManager after uploading
        if (implingsToUpload.size() > 0)
            webManager.postImplings();
    }

    @Schedule(
            period = PANEL_REFRESH_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    public void checkPanelRequest() {
        long currTime = System.currentTimeMillis();
        if (panel.isClearRequested()) {
            remotelyFetchedImplings.clear();
            updatePanels();
            panel.setClearRequested(false);
        }

        if (panel.isFetchRequested() && currTime - lastGetCall >= 4000) {
            remotelyFetchedImplings.clear();
            webManager.getData(panel.getRequestedId());
            panel.setFetchRequested(false);
            lastGetCall = System.currentTimeMillis();
        }
    }

    public void updatePanels() {
        Collections.sort(remotelyFetchedImplings);
        SwingUtilities.invokeLater(() -> panel.populateNpcs(remotelyFetchedImplings));
    }
}
