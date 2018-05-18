package net.bdew.wurm.hwportals.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import net.bdew.wurm.hwportals.PortalItems;
import net.bdew.wurm.hwportals.PortalTracker;
import org.gotti.wurmunlimited.modsupport.actions.*;

import java.util.Collections;
import java.util.List;

public class TogglePortalAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final ActionEntry actionEntry;
    private final boolean activate;

    public TogglePortalAction(boolean activate) {
        actionEntry = new ActionEntryBuilder((short) ModActions.getNextActionId(), activate ? "Activate" : "Shut Down", activate ? "activating" : "shutting down", new int[]{
                48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                37 /* ACTION_TYPE_NEVER_USE_ACTIVE_ITEM */
        }).range(5).build();
        ModActions.registerAction(actionEntry);
        this.activate = activate;
    }

    @Override
    public short getActionId() {
        return actionEntry.getNumber();
    }


    private boolean canUse(Creature performer, Item target, Boolean sendErrors) {
        if (!performer.isPlayer() || target.getTemplateId() != PortalItems.portalItemId || target.getParentId() != -10L || target.getZoneId() == -10L)
            return false;
        if (target.getAuxData() == 1 && activate) return false;
        if (target.getAuxData() != 1 && !activate) return false;
        VolaTile tile = Zones.getTileOrNull(target.getTilePos(), target.isOnSurface());
        if (tile == null) return false;
        Village village = tile.getVillage();
        if (village == null) {
            if (sendErrors)
                performer.getCommunicator().sendAlertServerMessage("Portals can only be activated in villages.");
            return !sendErrors;
        }
        if (performer.getPower() < 3) {
            VillageRole role = village.getRoleFor(performer);
            if (role == null || !role.mayManageSettings()) {
                if (sendErrors)
                    performer.getCommunicator().sendAlertServerMessage("You need to be the mayor of the village, or have management permissions to manage portals.");
                return !sendErrors;
            }
        }
        if (activate) {
            if (tile.getStructure() != null) {
                if (sendErrors)
                    performer.getCommunicator().sendAlertServerMessage("Portals can't be placed inside buildings or on bridges.");
                return !sendErrors;
            }
            if (PortalTracker.getPortalFor(village) != null) {
                if (sendErrors)
                    performer.getCommunicator().sendAlertServerMessage(String.format("There is already an active portal in %s, shut it down first.", village.getName()));
                return !sendErrors;
            }
            if (target.getCurrentQualityLevel() < 20f) {
                if (sendErrors)
                    performer.getCommunicator().sendAlertServerMessage("This portal is too low quality, using it would be unsafe! Improve it first.");
                return !sendErrors;
            }
            if (!Terraforming.isFlat(target.getTileX(), target.getTileY(), target.isOnSurface(), 10)) {
                if (sendErrors)
                    performer.getCommunicator().sendAlertServerMessage("The portal must be placed on a more level tile.");
                return !sendErrors;
            }

        }
        return true;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        return getBehavioursFor(performer, target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        if (canUse(performer, target, false))
            return Collections.singletonList(actionEntry);
        else
            return null;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    }


    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        if (canUse(performer, target, true)) {
            if (activate) {
                target.setLastOwnerId(performer.getWurmId());
                PortalTracker.addPortal(target);
                PortalItems.setPortalActive(target, true);
                performer.getCommunicator().sendNormalServerMessage("You activate the portal.");
            } else {
                PortalItems.setPortalActive(target, false);
                PortalTracker.removePortal(target);
                performer.getCommunicator().sendNormalServerMessage("You shut the portal down.");
            }
        }
        return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
    }
}
