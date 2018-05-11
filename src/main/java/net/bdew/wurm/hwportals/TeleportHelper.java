package net.bdew.wurm.hwportals;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.CreatureBehaviour;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TeleportHelper {
    private static Stream<Player> getPlayerSafe(long id) {
        try {
            return Stream.of(Players.getInstance().getPlayer(id));
        } catch (NoSuchPlayerException e) {
            return Stream.empty();
        }
    }

    private static void broadCastActionAt(String message, Creature performer, Item target, final int tileDist) {
        if (message.length() > 0) {
            final int tilex = target.getTileX();
            final int tiley = target.getTileY();
            for (int x = tilex - tileDist; x <= tilex + tileDist; ++x) {
                for (int y = tiley - tileDist; y <= tiley + tileDist; ++y) {
                    try {
                        final Zone zone = Zones.getZone(x, y, target.isOnSurface());
                        final VolaTile tile = zone.getTileOrNull(x, y);
                        if (tile != null) {
                            tile.broadCastAction(message, performer, false);
                        }
                    } catch (NoSuchZoneException ignored) {
                    }
                }
            }
        }
    }


    public static void doTeleport(Creature performer, Item target) {
        try {
            Vehicle vehicle = null;
            long vehicleId = performer.getVehicle();
            if (vehicleId != -10) {
                vehicle = Vehicles.getVehicleForId(vehicleId);
                if (vehicle != null) {
                    String vehicleName = Vehicle.getVehicleName(vehicle);
                    if (vehicle.pilotId != performer.getWurmId()) {
                        performer.getCommunicator().sendNormalServerMessage(String.format("You disembark the %s and then step into the portal.", vehicleName));
                        Server.getInstance().broadCastAction(String.format("%s disembarks the %s and steps into a portal.", performer.getName(), vehicleName), performer, 5);
                        vehicle = null;
                    } else {
                        performer.getCommunicator().sendNormalServerMessage(String.format("You direct the %s into the portal.", vehicleName));
                        Server.getInstance().broadCastAction(String.format("%s directs %s into a portal.", performer.getName(), vehicleName), performer, 5);
                        Arrays.stream(vehicle.seats)
                                .filter(Seat::isOccupied)
                                .map(Seat::getOccupant)
                                .filter(v -> v != performer.getWurmId())
                                .flatMap(TeleportHelper::getPlayerSafe)
                                .forEach(p -> {
                                    performer.getCommunicator().sendNormalServerMessage(String.format("The %s disappears as it goes into a portal.", vehicleName));
                                    p.disembark(false);
                                });
                    }
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You step into the portal.");
                Server.getInstance().broadCastAction(String.format("%s steps into a portal.", performer.getName()), performer, 5);
            }

            Map<Creature, Item> leadingItems = new HashMap<>();

            Arrays.stream(performer.getFollowers()).forEach(follower -> {
                leadingItems.put(follower, performer.getLeadingItem(follower));
                teleportCreature(follower, target);
            });

            Item dragged = performer.getDraggedItem();

            teleportCreature(performer, target);

            broadCastActionAt(String.format("%s emerges from a portal.", performer.getName()), performer, target, 5);

            if (dragged != null) {
                teleportItem(dragged, target);
                Items.startDragging(performer, dragged);
            }

            leadingItems.forEach((follower, item) -> {
                follower.setLeader(performer);
                performer.addFollower(follower, item);
            });

            if (vehicle != null) {
                if (vehicle.isCreature()) {
                    teleportCreature(Creatures.getInstance().getCreature(vehicleId), target);
                } else {
                    teleportItem(Items.getItem(vehicleId), target);
                }
            }
        } catch (Exception e) {
            HwPortals.logException(String.format("Error while teleporting %s to %d", performer.getName(), target.getWurmId()), e);
            performer.getCommunicator().sendAlertServerMessage("Something went wrong while teleporting, try again later or contact staff.");
        }
    }

    private static void teleportCreature(Creature subject, Item target) {
        float tx = target.getPosX() + Server.rand.nextFloat() - 0.5f;
        float ty = target.getPosY() + Server.rand.nextFloat() - 0.5f;
        float tz = target.getPosZ() + Server.rand.nextFloat() - 0.5f;
        if (subject.isPlayer()) {
            subject.setTeleportPoints(tx, ty, target.isOnSurface() ? 0 : -1, 0);
            if (subject.startTeleporting()) {
                subject.getCommunicator().sendTeleport(false);
            }
        } else {
            CreatureBehaviour.blinkTo(subject, tx, ty, target.isOnSurface() ? 0 : -1, tz, -10L, 0);
        }
    }

    private static void teleportItem(Item subject, Item target) throws NoSuchZoneException {
        Zone originZone = Zones.getZone(subject.getTilePos(), subject.isOnSurface());
        Zone targetZone = Zones.getZone(target.getTilePos(), target.isOnSurface());
        originZone.removeItem(subject);
        float tx = target.getPosX() + Server.rand.nextFloat() - 0.5f;
        float ty = target.getPosY() + Server.rand.nextFloat() - 0.5f;
        float tz = target.getPosZ() + Server.rand.nextFloat() - 0.5f;
        subject.setPosXYZRotation(tx, ty, tz, target.getRotation());
        targetZone.addItem(subject);
    }
}
