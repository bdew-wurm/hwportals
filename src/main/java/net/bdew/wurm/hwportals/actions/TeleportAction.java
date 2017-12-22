package net.bdew.wurm.hwportals.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import net.bdew.wurm.hwportals.PortalItems;
import net.bdew.wurm.hwportals.PortalTracker;
import net.bdew.wurm.hwportals.questions.TeleportQuestion;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modsupport.actions.*;

import java.util.Collections;
import java.util.List;

public class TeleportAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final ActionEntry actionEntry;

    public TeleportAction() {
        actionEntry = ActionEntry.createEntry((short) ModActions.getNextActionId(), "Teleport", "thinking with portals", new int[]{
                48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                37 /* ACTION_TYPE_NEVER_USE_ACTIVE_ITEM */
        });
        try {
            ReflectionUtil.setPrivateField(actionEntry, ReflectionUtil.getField(ActionEntry.class, "maxRange"), 4);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        ModActions.registerAction(actionEntry);
    }

    @Override
    public short getActionId() {
        return actionEntry.getNumber();
    }


    private boolean canUse(Creature performer, Item target) {
        return performer.isPlayer() && target.getTemplateId() == PortalItems.portalItemId && target.getAuxData() == 1 && target.getParentId() == -10L && target.getZoneId() != -10L;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        return getBehavioursFor(performer, target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        if (canUse(performer, target))
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
        if (!canUse(performer, target)) {
            performer.getCommunicator().sendAlertServerMessage("You are not allowed to do that.");
            return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
        }

        if (performer.getVisionArea() == null || !performer.getVisionArea().isInitialized()) {
            performer.getCommunicator().sendAlertServerMessage("You have not fully manifested in the world yet, please wait a bit.");
            return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
        }

        Village village = PortalTracker.getVillageFor(target);
        if (village == null) {
            performer.getCommunicator().sendAlertServerMessage("The portal needs to be placed in a village to use.");
            return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
        }

        List<Village> validTargets = PortalTracker.getTeleportDestinations(performer, village);
        if (validTargets.isEmpty()) {
            performer.getCommunicator().sendAlertServerMessage("There are no valid destinations for the portal.");
            return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
        }

        TeleportQuestion.send(performer, validTargets);
        return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
    }
}
