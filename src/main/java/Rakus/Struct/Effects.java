package Rakus.Struct;

import java.util.*;

public enum Effects {
    AFTERBURNER, ASTEROIDFIELD, GASCLOUD, SUPERFOOD, SHIELD;

    public static final EnumSet<Effects> ALL = EnumSet.allOf(Effects.class);

    public static EnumSet<Effects> fromFlags(int flags) {
        var set = EnumSet.noneOf(Effects.class);
        if ((flags & 0x00001) > 0) set.add(AFTERBURNER);
        if ((flags & 0x00010) > 0) set.add(ASTEROIDFIELD);
        if ((flags & 0x00100) > 0) set.add(GASCLOUD);
        if ((flags & 0x01000) > 0) set.add(SUPERFOOD);
        if ((flags & 0x10000) > 0) set.add(SHIELD);

        return set;
    }
}
