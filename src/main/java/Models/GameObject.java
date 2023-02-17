package Models;

import Enums.ObjectTypes;
import Rakus.Struct.Effects;
import Rakus.Vars;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class GameObject {
    public UUID id;
    public Integer size;
    public Integer speed;
    public Integer currentHeading;
    public Position position;
    public ObjectTypes gameObjectType;
    public EnumSet<Effects> activeEffects;
    public Integer torpedoCharge;
    public Boolean hasSupernova;
    public Integer teleporterCharge;
    public Integer shieldCharge;

    public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType, Integer effects, Integer torpedoCharge, Boolean hasSupernova, Integer teleporterCharge, Integer shieldCharge) {
        this.id = id;
        this.size = size;
        this.speed = speed;
        this.currentHeading = currentHeading;
        this.position = position;
        this.gameObjectType = gameObjectType;
        this.activeEffects = Effects.fromFlags(effects);
        this.torpedoCharge = torpedoCharge;
        this.hasSupernova = hasSupernova;
        this.teleporterCharge = teleporterCharge;
        this.shieldCharge = shieldCharge;
    }

    public static GameObject FromStateList(UUID id, List<Integer> stateList) {
        Position position = new Position(stateList.get(4), stateList.get(5));
        Integer
                effects = 0,
                torpedoCharge = 0,
                teleporterCharge = 0,
                shieldCharge = 0;
        boolean hasSupernova = false;
        if (stateList.size() == Vars.PLAYER_ATTRIB_COUNT) {
            effects = stateList.get(6);
            torpedoCharge = stateList.get(7);
            hasSupernova = stateList.get(8) != 0;
            teleporterCharge = stateList.get(9);
            shieldCharge = stateList.get(10);
        }

        return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), effects, torpedoCharge, hasSupernova, teleporterCharge, shieldCharge);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public ObjectTypes getGameObjectType() {
        return gameObjectType;
    }

    public void setGameObjectType(ObjectTypes gameObjectType) {
        this.gameObjectType = gameObjectType;
    }

    public EnumSet<Effects> getActiveEffects() {
        return this.activeEffects;
    }

    public int getTorpedoCharge() {
        return this.torpedoCharge != null ? this.torpedoCharge : 0;
    }

    public boolean hasSupernova() {
        return this.hasSupernova != null ? this.hasSupernova : false;
    }

    public int getTeleporterCharge() {
        return this.teleporterCharge != null ? this.teleporterCharge : 0;
    }

    public int getShieldCharge() {
        return this.shieldCharge != null ? this.shieldCharge : 0;
    }

    @Override
    public String toString() {
        var str = new StringBuilder();
        str.append(String.format("type: %s, pos: %d %d\n", gameObjectType.name(), position.getX(), position.getY()));
        str.append(String.format("mov: %d -> %d, size: %d", speed, currentHeading, size));
        if (gameObjectType == ObjectTypes.PLAYER) {
            str.append("\narmaments:\n");
            str.append(String.format("  torpedo: %d, teleport: %d, shield: %d", torpedoCharge, teleporterCharge, shieldCharge));
            if (hasSupernova) str.append(", supernova");
            str.append("\neffects:");
            if (!activeEffects.isEmpty()) {
                str.append("\n ");
                if (activeEffects.contains(Effects.AFTERBURNER)) str.append(" afterburn");
                if (activeEffects.contains(Effects.ASTEROIDFIELD)) str.append(" asteroid");
                if (activeEffects.contains(Effects.GASCLOUD)) str.append(" gascloud");
                if (activeEffects.contains(Effects.SUPERFOOD)) str.append(" superfood");
                if (activeEffects.contains(Effects.SHIELD)) str.append(" shield");
            }
        }
        str.append('\n');

        return str.toString();
    }
}
