package net.bdew.wurm.hwportals.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import net.bdew.wurm.hwportals.PortalItems;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;

public class PreventPortalActionPerformer implements ActionPerformer {
    private final short actionId;

    public PreventPortalActionPerformer(short actionId) {
        this.actionId = actionId;
    }

    @Override
    public short getActionId() {
        return actionId;
    }

    private boolean shouldPrevent(Creature performer, Item target) {
        return target.getTemplateId() == PortalItems.portalItemId && target.getAuxData() == 1;
    }


    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    }


    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        if (shouldPrevent(performer, target)) {
            performer.getCommunicator().sendAlertServerMessage(String.format("Shut the portal down before %s it.", action.getActionEntry().getVerbString()));
            return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
        } else {
            return propagate(action, ActionPropagation.ACTION_PERFORMER_PROPAGATION, ActionPropagation.SERVER_PROPAGATION);
        }
    }
}