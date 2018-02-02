package net.bdew.wurm.hwportals;

import com.wurmonline.server.behaviours.Actions;
import javassist.ClassPool;
import javassist.CtClass;
import net.bdew.wurm.hwportals.actions.PreventPortalActionPerformer;
import net.bdew.wurm.hwportals.actions.TeleportAction;
import net.bdew.wurm.hwportals.actions.TogglePortalAction;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HwPortals implements WurmServerMod, Configurable, PreInitable, Initable, ServerStartedListener, ItemTemplatesCreatedListener {
    private static final Logger logger = Logger.getLogger("HwPortals");

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logWarning(String msg) {
        if (logger != null)
            logger.log(Level.WARNING, msg);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    public static String activeModel, inactiveModel, activeParticle;
    public static float particleZ;

    @Override
    public void configure(Properties properties) {
        activeModel = properties.getProperty("activeModel", "model.structure.portal.7.");
        inactiveModel = properties.getProperty("inactiveModel", activeModel);
        activeParticle = properties.getProperty("activeParticle", null);
        particleZ = Float.parseFloat(properties.getProperty("particleZ", "0"));
    }

    @Override
    public void preInit() {
        try {
            ModActions.init();
            ClassPool classPool = HookManager.getInstance().getClassPool();

            classPool.getCtClass("com.wurmonline.server.zones.Zone").getMethod("addItem", "(Lcom/wurmonline/server/items/Item;ZZZ)V")
                    .insertAfter("if ($4) net.bdew.wurm.hwportals.Hooks.addItemLoading($1);");

            CtClass ctVillage = classPool.getCtClass("com.wurmonline.server.villages.Village");
            ctVillage.getMethod("disband", "(Ljava/lang/String;)V").insertBefore("net.bdew.wurm.hwportals.Hooks.disband(this);");
            ctVillage.getMethod("setNewBounds", "(IIII)V").insertAfter("net.bdew.wurm.hwportals.Hooks.resize(this);");

            if (activeParticle != null && activeParticle.length() > 0) {
                CtClass ctCommunicator = classPool.getCtClass("com.wurmonline.server.creatures.Communicator");
                ctCommunicator.getMethod("sendItem", "(Lcom/wurmonline/server/items/Item;JZ)V")
                        .insertAfter("net.bdew.wurm.hwportals.Hooks.sendItemHook(this, $1);");
                ctCommunicator.getMethod("sendRemoveItem", "(Lcom/wurmonline/server/items/Item;)V")
                        .insertAfter("net.bdew.wurm.hwportals.Hooks.removeItemHook(this, $1);");
            }

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void onServerStarted() {
        ModActions.registerAction(new TogglePortalAction(true));
        ModActions.registerAction(new TogglePortalAction(false));
        ModActions.registerAction(new TeleportAction());
        ModActions.registerActionPerformer(new PreventPortalActionPerformer(Actions.PUSH));
        ModActions.registerActionPerformer(new PreventPortalActionPerformer(Actions.PUSH_GENTLY));
        ModActions.registerActionPerformer(new PreventPortalActionPerformer(Actions.PULL));
        ModActions.registerActionPerformer(new PreventPortalActionPerformer(Actions.PULL_GENTLY));
        ModActions.registerActionPerformer(new PreventPortalActionPerformer(Actions.MOVE_CENTER));
        ModActions.registerActionPerformer(new PreventPortalActionPerformer(Actions.TAKE));
        ModActions.registerActionPerformer(new PreventPortalActionPerformer(Actions.LOAD_CARGO));

        Hooks.checkLoadedPortals();
    }

    @Override
    public void onItemTemplatesCreated() {
        try {
            PortalItems.register();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
