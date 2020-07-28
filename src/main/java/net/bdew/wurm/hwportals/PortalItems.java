package net.bdew.wurm.hwportals;

import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.items.ModItems;
import org.gotti.wurmunlimited.modsupport.items.ModelNameProvider;

import java.io.IOException;

public class PortalItems {
    public static int portalItemId;
    public static ItemTemplate portalItem;

    static void register() throws IOException {
        portalItem = new ItemTemplateBuilder("bdew.townportal")
                .name("inactive town portal", "town portals", "A magic device that can send you to other villages.")
                .imageNumber((short) 60)
                .weightGrams(100000)
                .dimensions(100, 100, 100)
                .decayTime(Long.MAX_VALUE)
                .difficulty(60)
                .value(1000)
                .itemTypes(new short[]{
                        ItemTypes.ITEM_TYPE_STONE,
                        ItemTypes.ITEM_TYPE_OUTSIDE_ONLY,
                        ItemTypes.ITEM_TYPE_DECORATION,
                        ItemTypes.ITEM_TYPE_REPAIRABLE,
                        ItemTypes.ITEM_TYPE_USE_GROUND_ONLY,
                        ItemTypes.ITEM_TYPE_TRANSPORTABLE,
                        ItemTypes.ITEM_TYPE_TURNABLE,
                        ItemTypes.ITEM_TYPE_COLORABLE,
                        ItemTypes.ITEM_TYPE_NOT_MISSION
                })
                .material(ItemMaterials.MATERIAL_STONE)
                .modelName(HwPortals.inactiveModel)
                .behaviourType((short) 1)
                .build();


        portalItemId = portalItem.getTemplateId();

        ModItems.addModelNameProvider(portalItemId, new PortalModelProvider());

        CreationEntryCreator.createAdvancedEntry(SkillList.MASONRY, ItemList.mortar, ItemList.stoneBrick, portalItemId, true, false, 0f, true, true, CreationCategories.MAGIC)
                .addRequirement(new CreationRequirement(1, ItemList.stoneBrick, 39, true))
                .addRequirement(new CreationRequirement(2, ItemList.mortar, 39, true))
                .addRequirement(new CreationRequirement(3, ItemList.rubyStar, 1, true))
                .addRequirement(new CreationRequirement(4, ItemList.diamondStar, 1, true))
                .addRequirement(new CreationRequirement(5, ItemList.sapphireStar, 1, true))
                .addRequirement(new CreationRequirement(6, ItemList.emeraldStar, 1, true))
                .addRequirement(new CreationRequirement(7, ItemList.opalBlack, 1, true))
                .addRequirement(new CreationRequirement(8, ItemList.sourceCrystal, 5, true))
                .addRequirement(new CreationRequirement(9, HwPortals.useSerylInRecipe ? ItemList.riftCrystal : ItemList.ruby, 5, true))
                .addRequirement(new CreationRequirement(10, ItemList.ironBar, 20, true))
                .addRequirement(new CreationRequirement(11, HwPortals.useSerylInRecipe ? ItemList.seryllBar : ItemList.goldBar, HwPortals.useSerylInRecipe ? 5 : 10, true))
                .addRequirement(new CreationRequirement(12, ItemList.candle, 3, true));
    }

    public static class PortalModelProvider implements ModelNameProvider {
        @Override
        public String getModelName(Item item) {
            if (item.getAuxData() > 0)
                return HwPortals.activeModel;
            else
                return HwPortals.inactiveModel;
        }
    }

    public static void setPortalActive(Item portal, boolean active) {
        Zone zone = null;
        try {
            zone = Zones.getZone(portal.getTilePos(), portal.isOnSurface());
        } catch (NoSuchZoneException e) {
            HwPortals.logException(String.format("Portal %d in invalid zone", portal.getWurmId()), e);
        }
        if (zone != null) zone.removeItem(portal);
        portal.setIsPlanted(active);
        portal.setName(active ? "town portal" : "inactive town portal", true);
        portal.setAuxData((byte) (active ? 1 : 0));
        if (zone != null) zone.addItem(portal);
    }
}
