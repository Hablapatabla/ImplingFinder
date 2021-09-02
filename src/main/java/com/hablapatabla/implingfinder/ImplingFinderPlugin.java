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
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@PluginDescriptor(
        name = "implingfinder"
        //description = "A plugin to crowdsource impling locations",
        //tags = {"config", "menu", "impling"},
        //loadWhenOutdated = true,
        //enabledByDefault = true
)

public class ImplingFinderPlugin extends Plugin {
    @Inject
    private OverlayManager overlayManager;

    //@Inject
    //private net.runelite.client.plugins.implingfinder.ImplingFinderOverlay overlay;

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

    /*@Inject
    private TimestampPlugin timestampPlugin;*/

    @Getter(AccessLevel.PACKAGE)
    private NavigationButton button = null;

    @Getter(AccessLevel.PACKAGE)
    private ArrayList<ImplingFinderData> workingImplings = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private ArrayList<ImplingFinderData> implingsToUpload = new ArrayList<>();

    @Setter(AccessLevel.PACKAGE)
    private ArrayList<ImplingFinderData> remotelyFetchedImplings = new ArrayList<>();

    private ArrayList<ImplingFinderData> previousHighlightedImplings = new ArrayList<>();

    @Provides
    ImplingFinderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ImplingFinderConfig.class);
    }

    private Logger logger;
    private BufferedImage icon;
    private BufferedImage mapArrow;
    private static int implingCapacity;

    private boolean mapPointSet = false;
    private SimpleDateFormat formatter;

    protected static final String CONFIG_GROUP = "implingfinder";
    private static final int NPC_UPLOAD_TIME = 20;
    private static final int PANEL_REFRESH_TIME = 2;
    private static final int NPC_SEARCH_REFRESH_TIME = 1;
    private static final int API_REQUEST_TIME = 10;

    private static final int MAX_ACTOR_VIEW_RANGE = 75;


    private List<Object> serializedData = new ArrayList<>();
    @Override
    protected void startUp() throws Exception {
        logger = LoggerFactory.getLogger(ImplingFinderPlugin.class);
        implingCapacity = config.implingFinderMaxImplings();
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
            logger.error("ERROR LOADING PANEL CLASS", e);
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
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(CONFIG_GROUP))
            return;
        switch (event.getKey()) {
            case "maxImplings":
                implingCapacity = config.implingFinderMaxImplings();
                break;
            default:
                updatePanels();
                break;
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        final NPC npc = npcSpawned.getNpc();
        final String npcName = npc.getName();

        if (npcName == null)
            return;

        if (!isInViewRange(npc.getWorldLocation(), client.getLocalPlayer().getWorldLocation()))
            return;

        if (!npcAllowedInConfig(npc))
            return;

        if (npcAlreadyInList(npc.getIndex()))
            return;

        if (workingImplings.size() >= implingCapacity) {
            //logger.debug("Impling capacity: " + Integer.toString(implingCapacity) + " , size: " + Integer.toString(workingImplings.size()));
            //logger.debug("Removing from highlighted, onnpcspawned");
            workingImplings.remove(workingImplings.size()-1);
        }
        ImplingFinderData imp = makeImp(npc);
        workingImplings.add(0, imp);
        implingsToUpload.add(imp);
    }

    private boolean npcAlreadyInList(int index) {
        return workingImplings.stream().anyMatch(npc -> npc.getNpcindex() == index);
    }

    private static boolean isInViewRange(WorldPoint wp1, WorldPoint wp2)
    {
        int distance = wp1.distanceTo(wp2);
        return distance < MAX_ACTOR_VIEW_RANGE;
    }

    private void pruneNpcByIndex(int index) {
        workingImplings.removeIf(npc -> npc.getNpcindex() == index);
    }

    private boolean npcAllowedInConfig(NPC npc) {
        return ImplingFinderEnum.getImplingConfigStatus(npc.getId()).getFunc().apply(config);
    }
    private boolean npcAllowedInConfig(int id) {
        return ImplingFinderEnum.getImplingConfigStatus(id).getFunc().apply(config);
    }

    private ImplingFinderData makeImp(NPC n) {
        //logger.error("MAKING IMP   " + n.getIndex() + "   " +n.getId());
        int world = client.getWorld();
        WorldArea area = n.getWorldArea();
        WorldPoint point = area.toWorldPoint();
        ImplingFinderData datum = new ImplingFinderData(n.getId(), n.getIndex(), world, point.getX(), point.getY(), point.getPlane(), ZonedDateTime.now().toString());
        return datum;
    }

    public BufferedImage getClueScrollImage()
    {
        return itemManager.getImage(ItemID.CLUE_SCROLL_MASTER);
    }

    public BufferedImage getMapArrow() {
        if (mapArrow != null) {
            return mapArrow;
        }

        mapArrow = ImageUtil.loadImageResource(getClass(), "/util/clue_arrow.png");
        return mapArrow;
    }

    public void addMapPoints(WorldPoint... points) {
        WorldPoint p = client.getLocalPlayer().getWorldLocation();
        //System.out.println("Local WP: " + p.toString());
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

    private void interpolateRemoteNpcs() {
        for (ImplingFinderData imp : remotelyFetchedImplings) {
            if (!workingImplings.contains(imp)) {
                if (!npcAllowedInConfig(imp.getNpcid()))
                    continue;
                if (workingImplings.size() >= implingCapacity)
                    workingImplings.remove(workingImplings.size()-1);
                workingImplings.add(imp);
            }
        }
    }


    @Schedule(
            period = NPC_UPLOAD_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    public void uploadFoundImplings() {
        if (implingsToUpload.size() == 0)
            return;

        webManager.postImplings();
    }

    public void clearImplingsToUpload() {
        implingsToUpload.clear();
    }


    @Schedule(
            period = NPC_SEARCH_REFRESH_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    public void checkPanelRequest() {
        if (panel.isClearRequested()) {
            this.workingImplings.clear();
            SwingUtilities.invokeLater(() -> panel.populateNpcs(workingImplings));
            panel.setClearRequested(false);
            this.remotelyFetchedImplings.clear();
            updatePanels();
        }

        if (panel.isFetchRequested()) {
            //logger.debug("FETCH REQUESTED, REMOTE SIZE " + remotelyFetchedNpcs.size() + "  working size " + workingImplings.size() + " previous size " + previousHighlightedNpcs.size());
            webManager.getData();
            panel.setFetchRequested(false);
        }
    }

    @Schedule(
            period = PANEL_REFRESH_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    public void updatePanels()
    {
        interpolateRemoteNpcs();
        if (!workingImplings.equals(previousHighlightedImplings)) {
            SwingUtilities.invokeLater(() -> panel.populateNpcs(workingImplings));
            previousHighlightedImplings = null;
            previousHighlightedImplings = new ArrayList<ImplingFinderData>(workingImplings);
        }
    }
}
