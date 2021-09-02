package com.hablapatabla.implingfinder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    /*
        stringified Java ZonedDateTime
        yyyy-mm-ddTHH:mm:ss.ssssss-OO:OO[a-z]
        2021-08-27T20:23:53.893005-07:00[America/Los_Angeles]
        substr [11 19)
     */
    @Getter
    private String discoveredtime;

    private static SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");

    private static Logger logger = LoggerFactory.getLogger(ImplingFinderData.class);

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ImplingFinderData)) return false;
        ImplingFinderData d = (ImplingFinderData) o;
        return (npcid == d.getNpcid())
                && (npcindex == d.getNpcindex())
                && (world == d.getWorld())
                && (xcoord == d.getXcoord())
                && (ycoord == d.getYcoord())
                && (plane == d.getPlane());
    }

    @Override
    public int hashCode() {
        return Objects.hash(npcid, npcindex, world, xcoord, ycoord, plane, discoveredtime);
    }

    @Override
    public String toString() {
        return "" + npcid + "  " + npcindex + "  " + world + "  " + xcoord + "  " + ycoord + "  " + plane + "  " + discoveredtime;
    }

    private Integer parseComparableTime(String time) {
        return Integer.valueOf(time.substring(11, 19).replaceAll(":", ""));
    }

    @Override
    public int compareTo(Object o) {
        ImplingFinderData other = (ImplingFinderData) o;
        String otherTimestamp = other.getDiscoveredtime();
        try {
            Date now = parser.parse(discoveredtime.substring(11, 19));
            Date then = parser.parse(otherTimestamp.substring(11, 19));
            if (now.equals(then))
                return 0;
            else if (now.after(then))
                return -1;
            return 1;
        } catch (ParseException e) {
            logger.debug(e.getMessage());
            Integer myTimeVal = parseComparableTime(discoveredtime);
            Integer otherTimeVal = parseComparableTime(otherTimestamp);
            return otherTimeVal - myTimeVal;
        }

    }
}
