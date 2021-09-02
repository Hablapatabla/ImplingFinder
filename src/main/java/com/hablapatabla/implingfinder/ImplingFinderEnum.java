package com.hablapatabla.implingfinder;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum ImplingFinderEnum {
    BABY(1635, "Baby impling", ImplingFinderConfig::implingFinderBabyImpling),
    YOUNG(1636, "Young impling", ImplingFinderConfig::implingFinderYoungImpling),
    GOURMET(1637, "Gourmet impling", ImplingFinderConfig::implingFinderGourmetImpling),
    EARTH(1638, "Earth impling", ImplingFinderConfig::implingFinderEarthImpling),
    ESSENCE(1639, "Essence impling", ImplingFinderConfig::implingFinderEssenceImpling),
    ECLECTIC(1640, "Eclectic impling", ImplingFinderConfig::implingFinderEclecticImpling),
    NATURE(1641, "Nature impling", ImplingFinderConfig::implingFinderNatureImpling),
    MAGPIE(1642, "Magpie impling", ImplingFinderConfig::implingFinderMagpieImpling),
    NINJA(1643, "Ninja impling", ImplingFinderConfig::implingFinderNinjaImpling),
    CRYSTAL(8741, "Crystal impling", ImplingFinderConfig::implingFinderCrystalImpling),
    DRAGON(1644, "Dragon impling", ImplingFinderConfig::implingFinderDragonImpling),
    LUCKY(7233, "Lucky impling", ImplingFinderConfig::implingFinderLuckyImpling),
    FUNGI(8690, "Ancient fungi", ImplingFinderConfig::implingFinderFungiImpling),
    UNKNOWN(10, "Unknown", c -> false);

    private int npcId;
    private String name;
    private Function<ImplingFinderConfig, Boolean> func;

    private static final Map<Integer, ImplingFinderEnum> map;
    static {
        map = new HashMap<Integer, ImplingFinderEnum>();
        for (ImplingFinderEnum e : ImplingFinderEnum.values())  {
            map.put(e.npcId, e);
        }
    }

    public static ImplingFinderEnum findById(int id) {
        return map.get(id);
    }

    public static ImplingFinderEnum getImplingConfigStatus(int id) {
        for (ImplingFinderEnum imp : values()) {
            if (imp.npcId == id) {
                return imp;
            }
        }
        return ImplingFinderEnum.UNKNOWN;
    }

}
