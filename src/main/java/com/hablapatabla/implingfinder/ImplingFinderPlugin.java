package com.hablapatabla.implingfinder;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.hablapatabla.implingfinder.model.ImplingFinderData;
import com.hablapatabla.implingfinder.model.ImplingFinderEnum;
import com.hablapatabla.implingfinder.model.ImplingFinderWorldMapPoint;
import com.hablapatabla.implingfinder.ui.ImplingFinderPanel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.WorldService;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@PluginDescriptor(
        name = "Impling Finder",
        description = "A plugin to crowdsource impling locations",
        tags = {"config", "menu", "impling", "finder", "hunter", "group",
                    "fun", "crowdsource", "crowd", "party", "implingfinder", "impling finder",
                    "clue", "clue scroll", "medium clue", "Impling Finder", "Impling", "Finder"},
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

    @Inject
    private WorldService worldService;

    @Inject
    private Notifier notifier;

    @Getter(AccessLevel.PACKAGE)
    private NavigationButton button = null;

    @Getter(AccessLevel.PACKAGE)
    private List<ImplingFinderData> implingsToUpload = new ArrayList<>();

    @Setter(AccessLevel.PACKAGE)
    private List<ImplingFinderData> remotelyFetchedImplings = new ArrayList<>();

    protected static String implingGetAnyEndpoint = "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/implingdev/dev";

    protected static String implingGetIdEndpoint = "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/implingdev/dev/";

    protected static String implingPostEndpoint = "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/implingdev/dev";

    public static final int RECENT_IMPLINGS_ID = -1;

    @Provides
    ImplingFinderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ImplingFinderConfig.class);
    }

    private Logger logger;
    private BufferedImage icon;
    private BufferedImage mapArrow = null;

    private boolean mapPointSet = false;
    private boolean displayingButton = true;
    private boolean wantSpawnNotifications = false;
    private long lastGetCall = System.currentTimeMillis();

    protected static final String CONFIG_GROUP = "Impling Finder";
    private static final int NPC_UPLOAD_TIME = 20;
    private static final int PANEL_REFRESH_TIME = 1;
    private static final int GET_REQUEST_COOLDOWN_TIME = 2000;



    @Override
    protected void startUp() throws Exception {
        logger = LoggerFactory.getLogger(ImplingFinderPlugin.class);
        loadPluginPanel();
        if (!config.beenOpened())
            panel.showSplash();
        else
            panel.continuePastSplash();
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

        button = NavigationButton.builder().tooltip("Impling Finder").icon(icon).priority(6).panel(panel).build();
        if (displayingButton)
            clientToolbar.addNavigation(button);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(CONFIG_GROUP)) {
            switch (event.getKey()) {
                case ImplingFinderConfig.HIDE_BUTTON:
                    if (config.hideButton())
                        clientToolbar.removeNavigation(button);
                    else
                        clientToolbar.addNavigation(button);
                    break;
                case ImplingFinderConfig.POST_ENDPOINT_KEYNAME:
                    implingPostEndpoint = config.implingFinderPostEndpointConfig();
                    break;
                case ImplingFinderConfig.GET_ENDPOINT_KEYNAME:
                    implingGetAnyEndpoint = config.implingFinderGetEndpointConfig();
                    break;
                case ImplingFinderConfig.IMPLING_SPAWN_NOTIFY:
                    wantSpawnNotifications = config.implingSpawnNotify();
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        final NPC npc = npcSpawned.getNpc();
        if (npc.getName() == null)
            return;

        if (!isImpling(npc.getName()))
            return;

        if (wantSpawnNotifications)
            notifier.notify("An impling just spawned!");

        ImplingFinderData imp = makeImp(npc);
        logger.error(imp.toString());
        implingsToUpload.add(imp);
    }

    private boolean isImpling(String name) {
        return ImplingFinderEnum.getIdByNameStrict(name) != RECENT_IMPLINGS_ID;
    }

    private boolean isImpling(int id) {
        return ImplingFinderEnum.findById(id) != null;
    }

    private ImplingFinderData makeImp(NPC n) {
        int world = client.getWorld();
        WorldArea area = n.getWorldArea();
        WorldPoint point = area.toWorldPoint();
        return ImplingFinderData.builder()
                                    .npcid(ImplingFinderEnum.getIdByNameStrict(n.getName()))
                                    .world(world)
                                    .xcoord(point.getX())
                                    .ycoord(point.getY())
                                    .plane(point.getPlane())
                                    .discoveredtime(Instant.now())
                                    .build();
        //logger.error("Making Imp:" + n.getName() + " " + datum.toString());
    }

    public BufferedImage getWorldMapImage() {
        return ImageUtil.loadImageResource(getClass(), "/icon.png");
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
            worldMapPointManager.add(new ImplingFinderWorldMapPoint(point, this));
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

        // 2 second wait between requests
        if (panel.isFetchRequested() && currTime - lastGetCall >= GET_REQUEST_COOLDOWN_TIME) {
            remotelyFetchedImplings.clear();
            remotelyFetchedImplings = webManager.getData(panel.getSelectedButtons());
            panel.setFetchRequested(false);
            lastGetCall = System.currentTimeMillis();
            updatePanels();
        }
    }

    public void updatePanels() {
        Collections.sort(remotelyFetchedImplings, Collections.reverseOrder());
        SwingUtilities.invokeLater(() -> panel.populateNpcs(remotelyFetchedImplings));
    }
}
