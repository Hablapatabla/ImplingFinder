package com.hablapatabla.implingfinder;


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
    MAGPIE_1652(1652, "Magpie impling"),
    NINJA_1653(1653, "Ninja impling"),
    DRAGON_1654(1654, "Dragon impling"),
    LUCKY_7302(7302, "Lucky impling"),
    CRYSTAL_IMPLING_8742(8742, "Crystal impling"),
    CRYSTAL_IMPLING_8743(8743, "Crystal impling"),
    CRYSTAL_IMPLING_8744(8744, "Crystal impling"),
    CRYSTAL_IMPLING_8745(8745, "Crystal impling"),
    CRYSTAL_IMPLING_8746(8746, "Crystal impling"),
    CRYSTAL_IMPLING_8747(8747, "Crystal impling"),
    CRYSTAL_IMPLING_8748(8748, "Crystal impling"),
    CRYSTAL_IMPLING_8749(8749, "Crystal impling"),
    CRYSTAL_IMPLING_8750(8750, "Crystal impling"),
    CRYSTAL_IMPLING_8751(8751, "Crystal impling"),
    CRYSTAL_IMPLING_8752(8762, "Crystal impling"),
    CRYSTAL_IMPLING_8753(8753, "Crystal impling"),
    CRYSTAL_IMPLING_8754(8754, "Crystal impling"),
    CRYSTAL_IMPLING_8755(8755, "Crystal impling"),
    CRYSTAL_IMPLING_8756(8756, "Crystal impling"),
    CRYSTAL_IMPLING_8757(8757, "Crystal impling"),
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

    public static ImplingFinderEnum getIdByShortenedName(String name) {
        for (ImplingFinderEnum imp : values()) {
            if (imp.name.contains(name))
                return imp;
        }
        return null;
    }
}
