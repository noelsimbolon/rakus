package Rakus.Struct;

import java.util.*;

public enum Effects {
    AFTERBURNER, ASTEROIDFIELD, GASCLOUD, SUPERFOOD, SHIELD;

    public static final EnumSet<Effects> ALL = EnumSet.allOf(Effects.class);

    public static EnumSet<Effects> fromFlags(int flags) {
        var set = EnumSet.allOf(Effects.class);
        if ((flags & 0b00001) == 0) set.remove(AFTERBURNER);
        if ((flags & 0b00010) == 0) set.remove(ASTEROIDFIELD);
        if ((flags & 0b00100) == 0) set.remove(GASCLOUD);
        if ((flags & 0b01000) == 0) set.remove(SUPERFOOD);
        if ((flags & 0b10000) == 0) set.remove(SHIELD);

        return set;
    }
}
