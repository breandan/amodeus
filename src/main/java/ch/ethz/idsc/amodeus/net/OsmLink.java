/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

public class OsmLink {
    /** the factor SCALE is used to offset vehicles from the street
     * so that the direction of the vehicles can be destinguished.
     * the factor SCALE does not work universally all over the world
     * since it scales degrees in WGS84 */
    public static double SCALE = 5e-5;

    /** link may be used in post processing for lookup with MATSim database */
    public final Link link;
    /** coords[0] is the location of the from node coords[1] is the location of
     * the to node */
    private final Coord[] coords = new Coord[2]; // WGS84
    private final double dx;
    private final double dy;
    private final double length;
    private double nx = 0;
    private double ny = 0;

    OsmLink(Link link, Coord from, Coord to) {
        this.link = link;
        coords[0] = from;
        coords[1] = to;
        nx = coords[0].getX();
        ny = coords[0].getY();
        dx = coords[1].getX() - coords[0].getX();
        dy = coords[1].getY() - coords[0].getY();
        length = Math.hypot(dx, dy);
        if (0 < length) {
            nx += dy / length * SCALE;
            ny -= dx / length * SCALE;
        }
    }

    public Coord getAt(double lambda) {
        return new Coord( //
                nx + lambda * dx, //
                ny + lambda * dy);
    }

    public Coord getCoordFrom() {
        return coords[0];
    }

    public Coord getCoordTo() {
        return coords[1];
    }

    public double getLength() {
        return link.getLength();
    }
}
