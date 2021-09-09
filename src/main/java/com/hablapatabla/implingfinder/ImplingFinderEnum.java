package com.hablapatabla.implingfinder;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;

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
    MAGPIE(1642, "Magpie"),
    NINJA(1643, "Ninja"),
    CRYSTAL(8741, "Crystal"),
    DRAGON(1644, "Dragon"),
    LUCKY(7233, "Lucky"),
    ANY(-1, "Any");

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

    public static int getIdByShortenedName(String name) {
        for (ImplingFinderEnum imp : values()) {
            if (name.contains(imp.name))
                return imp.npcId;
        }
        return -1;
    }
}
