package com.hablapatabla.implingfinder;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Objects;

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
