package net.bdew.wurm.hwportals;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;

import java.util.HashSet;
import java.util.Set;

public class Hooks {
    private final static Set<Item> portalsToLoad = new HashSet<>();

    public static void addItemLoading(Item item) {
        if (item.getTemplateId() == PortalItems.portalItemId && item.getAuxData() == 1) {
            portalsToLoad.add(item);
        }
    }

    public static void checkLoadedPortals() {
        long start = System.currentTimeMillis();
        HwPortals.logInfo("Checking loaded portals...");
        portalsToLoad.forEach(PortalTracker::addPortal);
        portalsToLoad.clear();
        HwPortals.logInfo(String.format("Checking loaded portals done, took %dms", System.currentTimeMillis() - start));
    }

    public static void disband(Village village) {
        Item portal = PortalTracker.getPortalFor(village);
        if (portal != null) {
            PortalTracker.removePortal(portal);
            portal.setAuxData((byte) 0);
            portal.setName("inactive town portal", true);
        }
    }

    public static void resize(Village village) {
        Item portal = PortalTracker.getPortalFor(village);
        if (portal != null && !village.containsItem(portal)) {
            PortalTracker.removePortal(portal);
            portal.setAuxData((byte) 0);
            portal.setName("inactive town portal", true);
        }
    }
}
