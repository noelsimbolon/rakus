package Models;

import Enums.*;
import Rakus.Struct.*;
import Rakus.Vars;

import java.util.*;

public class GameObject {
    public UUID id;
    public Integer size;
    public Integer speed;
    public Integer currentHeading;
    public Position position;
    public ObjectTypes gameObjectType;
    public EnumSet<Effects> activeEffects;
    public Integer torpedoCharge;

    public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType, Integer effects, Integer torpedoCharge) {
        this.id = id;
        this.size = size;
        this.speed = speed;
        this.currentHeading = currentHeading;
        this.position = position;
        this.gameObjectType = gameObjectType;
        this.activeEffects = Effects.fromFlags(effects);
        this.torpedoCharge = torpedoCharge;
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

    public static GameObject FromStateList(UUID id, List<Integer> stateList)
    {
        Position position = new Position(stateList.get(4), stateList.get(5));
        Integer effects = stateList.size() == Vars.PLAYER_ATTRIB_COUNT ? stateList.get(6) : 0;
        Integer torpedoCharge = stateList.size() == Vars.PLAYER_ATTRIB_COUNT ? stateList.get(7) : 0;
        return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), effects, torpedoCharge);
    }

    @Override
    public String toString() {
        var str = new StringBuilder();
        str.append(String.format("type: %s, pos: %d %d\n", gameObjectType.name(), position.getX(), position.getY()));
        str.append(String.format("mov: %d -> %d, size: %d", speed, currentHeading, size));
        if (gameObjectType == ObjectTypes.PLAYER){
            str.append(String.format("\ntorpedo: %d\n", torpedoCharge));
            str.append("state:");
            if (activeEffects.contains(Effects.AFTERBURNER)) str.append(" afterburn");
            if (activeEffects.contains(Effects.ASTEROIDFIELD)) str.append(" asteroid");
            if (activeEffects.contains(Effects.GASCLOUD)) str.append(" gascloud");
        }
        str.append('\n');

        return str.toString();
    }
}
