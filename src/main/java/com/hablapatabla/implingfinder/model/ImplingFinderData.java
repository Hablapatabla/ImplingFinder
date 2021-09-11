package com.hablapatabla.implingfinder.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@Value
@Builder
public class ImplingFinderData implements Comparable {
    @Getter
    @SerializedName("npcid")
    private int npcid;

    @Getter
    @SerializedName("world")
    private int world;

    @Getter
    @SerializedName("xcoord")
    private int xcoord;

    @Getter
    @SerializedName("ycoord")
    private int ycoord;

    @Getter
    @SerializedName("plane")
    private int plane;

    @Getter
    @SerializedName("discoveredtime")
    private Instant discoveredtime;

    private static Logger logger = LoggerFactory.getLogger(ImplingFinderData.class);

    @Override
    public String toString() {
        return "" + npcid + "  " + world + "  " + xcoord + "  " + ycoord + "  " + plane + "  " + discoveredtime.toString();
    }

    @Override
    public int compareTo(Object o) {
        ImplingFinderData other = (ImplingFinderData) o;
        return this.discoveredtime.compareTo(other.getDiscoveredtime());
    }
}
