package com.hablapatabla.implingfinder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Objects;

@AllArgsConstructor
public class ImplingFinderData implements Comparable {
    @Getter
    private int npcid;

    @Getter
    private int npcindex;

    @Getter
    private int world;

    @Getter
    private int xcoord;

    @Getter
    private int ycoord;

    @Getter
    private int plane;

    @Getter
    private String discoveredtime;

    private static Logger logger = LoggerFactory.getLogger(ImplingFinderData.class);

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ImplingFinderData)) return false;
        ImplingFinderData d = (ImplingFinderData) o;
        return (discoveredtime.equals(d.getDiscoveredtime())
                && npcid == d.getNpcid()
                && npcindex == d.getNpcindex()
                && world == d.getWorld()
                && xcoord == d.getXcoord()
                && ycoord == d.getYcoord()
                && plane == d.getPlane());
    }

    @Override
    public String toString() {
        return "" + npcid + "  " + npcindex + "  " + world + "  " + xcoord + "  " + ycoord + "  " + plane + "  " + discoveredtime;
    }

    @Override
    public int compareTo(Object o) {
        ImplingFinderData other = (ImplingFinderData) o;
        ZonedDateTime myTime = ZonedDateTime.parse(this.discoveredtime);
        ZonedDateTime otherTime = ZonedDateTime.parse(other.discoveredtime);
        if (myTime.equals(otherTime))
            return 0;
        else if (myTime.isAfter(otherTime))
            return -1;
        return 1;
    }
}
