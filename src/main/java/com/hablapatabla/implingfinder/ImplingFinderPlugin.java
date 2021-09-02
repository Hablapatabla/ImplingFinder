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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    private ArrayList<ImplingFinderData> implingsToUpload = new ArrayList<>();

    @Setter(AccessLevel.PACKAGE)
    private ArrayList<ImplingFinderData> remotelyFetchedNpcs = new ArrayList<>();

    private ArrayList<ImplingFinderData> previousHighlightedNpcs = new ArrayList<>();

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
    private static final int FUNGI = 8690;
    public static final int BABY_IMPLING = 1635;
    public static final int YOUNG_IMPLING = 1636;
    public static final int GOURMET_IMPLING = 1637;
    public static final int EARTH_IMPLING = 1638;
    public static final int ESSENCE_IMPLING = 1639;
    public static final int ECLECTIC_IMPLING = 1640;
    public static final int NATURE_IMPLING = 1641;
    public static final int MAGPIE_IMPLING = 1642;
    public static final int NINJA_IMPLING = 1643;
    public static final int CRYSTAL_IMPLING = 8741;
    public static final int DRAGON_IMPLING = 1644;
    public static final int LUCKY_IMPLING = 7233;

    private List<Object> serializedData = new ArrayList<>();
    @Override
    protected void startUp() throws Exception
    {
        logger = LoggerFactory.getLogger(ImplingFinderPlugin.class);
        //overlayManager.add(overlay);
        implingCapacity = config.implingFinderMaxImplings();
        loadPluginPanel();
    }

    @Override
    protected void shutDown() throws Exception
    {
        //overlayManager.remove(overlay);
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
            logger.debug("Impling capacity: " + Integer.toString(implingCapacity) + " , size: " + Integer.toString(workingImplings.size()));
            logger.debug("Removing from highlighted, onnpcspawned");
            workingImplings.remove(workingImplings.size()-1);
        }
        ImplingFinderData imp = makeImp(npc);
        workingImplings.add(0, imp);
        implingsToUpload.add(imp);
    }

    private boolean npcAlreadyInList(int index) {
        return workingImplings.stream().anyMatch(npc -> npc.getNpcindex() == index);
    }

/*
    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        final NPC npc = npcDespawned.getNpc();
        logger.debug("npc despawn: " + npc.getIndex());
        //highlightedNpcs.remove(npc);
    }*/

    private static boolean isInViewRange(WorldPoint wp1, WorldPoint wp2)
    {
        int distance = wp1.distanceTo(wp2);
        return distance < MAX_ACTOR_VIEW_RANGE;
    }

    /*private void pruneDistantNpcs() {
        Set<NPC> distantNpcs = new HashSet<>();
        for (ImplingFinderData npc : highlightedNpcs) {
            if (!isInViewRange(npc.getEntity().getWorldLocation(), client.getLocalPlayer().getWorldLocation()))
                distantNpcs.add(npc.getEntity());
        }
        for (NPC npc : distantNpcs) {
            pruneNpcByIndex(npc.getIndex());
        }
    }*/



    private void pruneNpcByIndex(int index) {
        workingImplings.removeIf(npc -> npc.getNpcindex() == index);
    }

    private boolean npcAllowedInConfig(NPC npc) {
        return ImplingFinderEnum.getImplingConfigStatus(npc.getId()).getFunc().apply(config);
    }
    private boolean npcAllowedInConfig(int id) {
        return ImplingFinderEnum.getImplingConfigStatus(id).getFunc().apply(config);
    }

    private Queue<ImplingFinderData> reverse(Queue<ImplingFinderData> q) {
        List<ImplingFinderData> collect = q.stream().collect(Collectors.toList());
        Collections.reverse(collect);
        return new LinkedList<>(collect);
    }
    /*
    @Schedule(
            period = NPC_SEARCH_REFRESH_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    private void fetchNearbyNpcs() {
        List<NPC> npcs = client.getNpcs();
        logger.debug("nothing?");
        for (NPC n : npcs) {
            logger.debug("PRE");
            if (n.getName() != null && npcAllowedInConfig(n) && !npcAlreadyInList(n.getIndex())) {
                if (highlightedNpcs.size() >= implingCapacity) {
                    logger.debug("Removing from highlighted, fetchnearbynpcs");
                    highlightedNpcs.poll();
                }
                logger.debug("POST1");
                highlightedNpcs.add(makeImp(n));
            }
            logger.debug("POST2");
        }
    } */

    String generateTimestamp(int timestamp, ZoneId zoneId) {
        final ZonedDateTime time = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp), zoneId);

        return formatter.format(Date.from(time.toInstant()));
    }

    private ImplingFinderData makeImp(NPC n) {
        logger.error("MAKING IMP   " + n.getIndex() + "   " +n.getId());
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
        if (mapArrow != null)
        {
            return mapArrow;
        }

        mapArrow = ImageUtil.loadImageResource(getClass(), "/util/clue_arrow.png");
        return mapArrow;
    }

    /*
        Zanaris top left: (X: 2563 Y: 4348)
        Zanaris bottom right: (X: 2620 Y: 4291)
     */
    public void addMapPoints(WorldPoint... points) {
        WorldPoint p = client.getLocalPlayer().getWorldLocation();
        System.out.println("Local WP: " + p.toString());
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
        //logger.debug("INTERPOLATE");
        //boolean added = false;
        for (ImplingFinderData imp : remotelyFetchedNpcs) {
            if (!workingImplings.contains(imp)) {
                if (!npcAllowedInConfig(imp.getNpcid()))
                    continue;
                if (workingImplings.size() >= implingCapacity)
                    workingImplings.remove(workingImplings.size()-1);
                //logger.debug("INTERPOLATE ADD");
                workingImplings.add(imp);
                //added = true;
            }
        }
        //if (added)
        //    this.remotelyFetchedNpcs.clear();
    }

   /* private int specificImplingTypeRequested() {

    }*/

    @Schedule(
            period = NPC_UPLOAD_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    public void uploadFoundImplings() {
        if (implingsToUpload.size() == 0)
            return;

        webManager.storeManyImplings(implingsToUpload);
        webManager.postImplings();
        //implingsToUpload.clear();
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
            this.remotelyFetchedNpcs.clear();
            //this.previousHighlightedNpcs.clear();
            updatePanels();
        }

        if (panel.isFetchRequested()) {
            logger.debug("FETCH REQUESTED, REMOTE SIZE " + remotelyFetchedNpcs.size() + "  working size " + workingImplings.size() + " previous size " + previousHighlightedNpcs.size());
            webManager.getData();
            panel.setFetchRequested(false);
            logger.debug("New remoet size: " + remotelyFetchedNpcs.size());
            //interpolateRemoteNpcs();
            //logger.debug("AFTER FETCH REMOT SIZE " + remotelyFetchedNpcs.size() + "  wokring size" + workingImplings.size() + " previous size " + previousHighlightedNpcs.size());
            //updatePanels();
        }
    }

    @Schedule(
            period = PANEL_REFRESH_TIME,
            unit = ChronoUnit.SECONDS,
            asynchronous = true
    )
    public void updatePanels()
    {
        //pruneDistantNpcs();
        /*for (NPC n : highlightedNpcs) {
            logger.debug(n.getWorldLocation() + "  " + n.getWorldArea());
        }*/
        //logger.debug("BEFORE UPDATE, REMOTE SIZE" + Integer.toString(remotelyFetchedNpcs.size()));
        /*for (ImplingFinderData d : remotelyFetchedNpcs) {
            System.out.println(d);
        }*/
        interpolateRemoteNpcs();
        if (!workingImplings.equals(previousHighlightedNpcs)) {
            logger.debug("DURING UPDATE");
            //.debug("updating panels");
            //interpolateRemoteNpcs();
            //Collections.sort(workingImplings);
            /*for (ImplingFinderData d : highlightedNpcs)
                logger.debug(d.toString());*/
            SwingUtilities.invokeLater(() -> panel.populateNpcs(workingImplings));
            previousHighlightedNpcs = null;
            previousHighlightedNpcs = new ArrayList<ImplingFinderData>(workingImplings);
        }
        //logger.debug("Len: " + highlightedNpcs.size());

        //SwingUtilities.invokeLater(() -> panel.populate());
    }

}
