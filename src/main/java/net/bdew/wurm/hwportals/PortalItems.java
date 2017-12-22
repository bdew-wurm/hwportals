package net.bdew.wurm.hwportals;

import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.ItemMaterials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

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
                        ItemTypes.ITEM_TYPE_INDESTRUCTIBLE,
                        ItemTypes.ITEM_TYPE_USE_GROUND_ONLY,
                        ItemTypes.ITEM_TYPE_TRANSPORTABLE
                })
                .material(ItemMaterials.MATERIAL_MARBLE)
                .modelName("model.structure.portal.7.")
                .behaviourType((short) 1)
                .build();

        portalItemId = portalItem.getTemplateId();

        CreationEntryCreator.createAdvancedEntry(SkillList.MASONRY, ItemList.mortar, ItemList.marbleBrick, portalItemId, true, false, 0f, true, true, CreationCategories.MAGIC)
                .addRequirement(new CreationRequirement(1, ItemList.marbleBrick, 39, true))
                .addRequirement(new CreationRequirement(2, ItemList.mortar, 39, true))
                .addRequirement(new CreationRequirement(3, ItemList.rubyStar, 1, true))
                .addRequirement(new CreationRequirement(4, ItemList.diamondStar, 1, true))
                .addRequirement(new CreationRequirement(5, ItemList.sapphireStar, 1, true))
                .addRequirement(new CreationRequirement(6, ItemList.emeraldStar, 1, true))
                .addRequirement(new CreationRequirement(7, ItemList.opalBlack, 1, true))
                .addRequirement(new CreationRequirement(8, ItemList.sourceCrystal, 5, true))
                .addRequirement(new CreationRequirement(9, ItemList.riftCrystal, 5, true))
                .addRequirement(new CreationRequirement(10, ItemList.ironBar, 20, true))
                .addRequirement(new CreationRequirement(11, ItemList.goldBar, 10, true));
    }

    public static void setPortalActive(Item portal, boolean active) {
        portal.setName(active ? "town portal" : "inactive town portal", true);
        portal.setAuxData((byte) (active ? 1 : 0));
    }
}
