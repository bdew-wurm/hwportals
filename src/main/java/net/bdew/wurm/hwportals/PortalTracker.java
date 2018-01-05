package net.bdew.wurm.hwportals;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.Node;
import com.wurmonline.server.highways.PathToCalculate;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalTracker {
    private final static Map<Integer, Long> villagePortalMap = new HashMap<>();
    private final static Map<Long, Integer> portalVillageMap = new HashMap<>();

    public static Village getVillageFor(Item item) {
        if (item.getZoneId() == -10 || item.getParentId() != -10) return null;
        TilePos pos = item.getTilePos();
        VolaTile tile = Zones.getTileOrNull(pos, item.isOnSurface());
        if (tile != null)
            return tile.getVillage();
        else
            return null;
    }

    public static void addPortal(Item portal) {
        Village village = getVillageFor(portal);
        if (village != null) {
            if (villagePortalMap.containsKey(village.id) && villagePortalMap.get(village.id) != portal.getWurmId()) {
                try {
                    Item oldPortal = Items.getItem(villagePortalMap.get(village.id));
                    HwPortals.logWarning(String.format("Adding caravan station in village %s which already has caravan station %d, disabling", village.getName(), portal.getWurmId()));
                    PortalItems.setPortalActive(oldPortal, true);
                } catch (NoSuchItemException e) {
                    HwPortals.logWarning(String.format("Adding caravan station to village %d that had a non-existing station", portal.getWurmId()));
                }
            }
            HwPortals.logInfo(String.format("Adding caravan station %d in village %s", portal.getWurmId(), village.getName()));
            villagePortalMap.put(village.id, portal.getWurmId());
            portalVillageMap.put(portal.getWurmId(), village.id);
        } else {
            HwPortals.logWarning(String.format("Tried to add caravan station %d not in any village, disabling", portal.getWurmId()));
            portal.setAuxData((byte) 0);
            portal.setName("inactive caravan station");
        }
    }

    public static void removePortal(Item portal) {
        if (portalVillageMap.containsKey(portal.getWurmId())) {
            int villageId = portalVillageMap.remove(portal.getWurmId());
            try {
                Village village = Villages.getVillage(villageId);
                HwPortals.logInfo(String.format("Removing caravan station %d from village %s", portal.getWurmId(), village.getName()));
            } catch (NoSuchVillageException e) {
                HwPortals.logWarning(String.format("Removing caravan station %d unknown village %d", portal.getWurmId(), villageId));
            }
            villagePortalMap.remove(villageId);
        }
    }

    public static Item getPortalFor(Village village) {
        if (villagePortalMap.containsKey(village.id)) {
            long portalId = villagePortalMap.get(village.id);
            try {
                return Items.getItem(portalId);
            } catch (NoSuchItemException e) {
                HwPortals.logWarning(String.format("Item not found when looking for caravan station %d for village %s", portalId, village.getName()));
                return null;
            }
        } else return null;
    }

    private static Stream<Village> getVillageSafe(int id) {
        try {
            return Stream.of(Villages.getVillage(id));
        } catch (NoSuchVillageException e) {
            return Stream.empty();
        }
    }

    private static int compareVillage(Village v1, Village v2) {
        if ((v1.isPermanent || v1.isCapital()) && !(v2.isPermanent || v2.isCapital()))
            return -1;
        if ((v2.isPermanent || v2.isCapital()) && !(v1.isPermanent || v1.isCapital()))
            return 1;
        return v1.getName().compareTo(v2.getName());

    }

    public static List<Village> getTeleportDestinations(Creature performer, Village origin) {
        long start = System.currentTimeMillis();
        List<Node> nodes = Arrays.asList(Routes.getNodesFor(origin));
        Set<Village> villages = portalVillageMap.values().stream().flatMap(PortalTracker::getVillageSafe)
                .filter(v -> (v.id != origin.id) && (getPortalFor(v) != null) && (v.isHighwayFound() || v.isCitizen(performer) || v.getRoleFor(performer).mayPassGates()))
                .filter(v -> nodes.stream().anyMatch(n -> PathToCalculate.isVillageConnected(n.getWurmId(), v)))
                .collect(Collectors.toSet());
        List<Village> res = villages.stream().sorted(PortalTracker::compareVillage).collect(Collectors.toList());
        HwPortals.logInfo(String.format("Finding destinations for %s from %s, took %dms", performer.getName(), origin.getName(), System.currentTimeMillis() - start));
        return res;
    }
}

