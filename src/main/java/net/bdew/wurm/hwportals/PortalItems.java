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
        portalItem = new ItemTemplateBuilder("Caravan Station")
                .name("Caravan station", "caravan stations", "A caravan station where you can catch a ride to other villages.")
                .imageNumber((short) 60)
                .weightGrams(100000)
                .dimensions(100, 100, 100)
                .decayTime(Long.MAX_VALUE)
                .difficulty(70)
                .value(1000)
                .itemTypes(new short[]{
                        ItemTypes.ITEM_TYPE_OUTSIDE_ONLY,
                        ItemTypes.ITEM_TYPE_DECORATION,
                        ItemTypes.ITEM_TYPE_REPAIRABLE,
                        ItemTypes.ITEM_TYPE_WOOD,
                        ItemTypes.ITEM_TYPE_INDESTRUCTIBLE,
                        ItemTypes.ITEM_TYPE_USE_GROUND_ONLY,
                        ItemTypes.ITEM_TYPE_TRANSPORTABLE
                })
                .material(ItemMaterials.MATERIAL_WOOD_CEDAR)
                .modelName("model.structure.war.supplydepot.2.")
                .behaviourType((short) 1)
                .build();

        portalItemId = portalItem.getTemplateId();

        CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY_FINE, ItemList.nailsIronLarge, ItemList.plank, portalItemId, true, false, 0f, true, true, CreationCategories.ANIMAL_EQUIPMENT)
                .addRequirement(new CreationRequirement(1, ItemList.plank, 50, true))
                .addRequirement(new CreationRequirement(2, ItemList.nailsIronSmall, 50, true))
                .addRequirement(new CreationRequirement(3, ItemList.nailsIronLarge, 50, true))
                .addRequirement(new CreationRequirement(4, ItemList.shaft, 20, true))
                .addRequirement(new CreationRequirement(5, ItemList.stoneBrick, 25, true))
                .addRequirement(new CreationRequirement(6, ItemList.log, 5, true))
                .addRequirement(new CreationRequirement(7, ItemList.satchel, 5, true))
                .addRequirement(new CreationRequirement(8, ItemList.tentMilitary, 1, true))
                .addRequirement(new CreationRequirement(9, ItemList.rock, 25, true))
                .addRequirement(new CreationRequirement(10, ItemList.rope, 50, true))
                .addRequirement(new CreationRequirement(11, ItemList.dirtPile, 20, true))
                .addRequirement(new CreationRequirement(12, ItemList.bulkContainer, 1, true))
                .addRequirement(new CreationRequirement(13, ItemList.wheelAxleSmall, 1, true));
    }

    public static void setPortalActive(Item portal, boolean active) {
        portal.setName(active ? "Caravan Station" : "Closed Caravan Station", true);
        portal.setAuxData((byte) (active ? 1 : 0));
    }
}
