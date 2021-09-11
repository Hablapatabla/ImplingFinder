package com.hablapatabla.implingfinder.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ImplingFinderEnum {
    /*BABY(1635, "Baby impling"),
    YOUNG(1636, "Young impling"),
    GOURMET(1637, "Gourmet impling"),
    EARTH(1638, "Earth impling"),
    ESSENCE(1639, "Essence impling"),*/
    //ECLECTIC(1640, "Eclectic impling"),
    //NATURE(1641, "Nature impling"),
    MAGPIE(1642, "Magpie impling"),
    NINJA(1643, "Ninja impling"),
    CRYSTAL(8741, "Crystal impling"),
    DRAGON(1644, "Dragon impling"),
    LUCKY(7233, "Lucky impling"),
    RECENT(-1, "Recent");

    private int npcId;
    private String name;

    private static final Map<Integer, ImplingFinderEnum> map;
    static {
        map = new HashMap<>();
        for (ImplingFinderEnum e : ImplingFinderEnum.values())  {
            map.put(e.npcId, e);
        }
    }

    public static ImplingFinderEnum findById(int id) {
        return map.get(id);
    }

    public static int getIdByNameFuzzy(String name) {
        for (ImplingFinderEnum imp : values()) {
            if (imp.name.contains(name))
                return imp.npcId;
        }
        return -1;
    }

    public static int getIdByNameStrict(String name) {
        for (ImplingFinderEnum imp : values()) {
            if (name.contains(imp.name))
                return imp.npcId;
        }
        return -1;
    }
}
