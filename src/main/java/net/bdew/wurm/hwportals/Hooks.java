package net.bdew.wurm.hwportals;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import com.wurmonline.shared.constants.ItemMaterials;

import java.util.HashSet;
import java.util.Set;

public class Hooks {
    private final static Set<Item> portalsToLoad = new HashSet<>();

    public static void addItemLoading(Item item) {
        if (item.getTemplateId() == PortalItems.portalItemId) {
            if (item.getMaterial() == ItemMaterials.MATERIAL_MARBLE)
                item.setMaterial(ItemMaterials.MATERIAL_STONE);
            if (item.getAuxData() == 1)
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
            PortalItems.setPortalActive(portal, false);
            PortalTracker.removePortal(portal);
        }
    }

    public static void resize(Village village) {
        Item portal = PortalTracker.getPortalFor(village);
        if (portal != null && !village.containsItem(portal)) {
            PortalItems.setPortalActive(portal, false);
            PortalTracker.removePortal(portal);
        }
    }

    public static void sendItemHook(Communicator comm, Item item) {
        if (item.getTemplateId() == PortalItems.portalItemId && item.getAuxData() > 0) {
            comm.sendRemoveEffect(item.getWurmId());
            comm.sendAddEffect(item.getWurmId(), (short) 27, item.getPosX(), item.getPosY(), item.getPosZ() + HwPortals.particleZ, (byte) (item.isOnSurface() ? 0 : -1), HwPortals.activeParticle, Float.MAX_VALUE, 0f);
            comm.sendAttachEffect(item.getWurmId(), (byte) 4, (byte) 255, (byte) 250, (byte) 205, (byte) 120);
        }
    }

    public static void removeItemHook(Communicator comm, Item item) {
        if (item.getTemplateId() == PortalItems.portalItemId && item.getAuxData() > 0) {
            comm.sendRemoveEffect(item.getWurmId());
        }
    }

}
