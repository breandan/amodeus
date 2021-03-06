/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.TensorMap;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Median;
import ch.ethz.matsim.av.passenger.AVRequest;

public class MatsimAmodeusDatabase {

    public static MatsimAmodeusDatabase initialize(Network network, //
            ReferenceFrame referenceFrame) {

        NavigableMap<String, OsmLink> linkMap = new TreeMap<>();

        CoordinateTransformation coords_toWGS84 = referenceFrame.coords_toWGS84();

        for (Link link : network.getLinks().values()) {
            OsmLink osmLink = new OsmLink(link, //
                    coords_toWGS84.transform(link.getFromNode().getCoord()), //
                    coords_toWGS84.transform(link.getToNode().getCoord()) //
            );
            linkMap.put(link.getId().toString(), osmLink);
        }
        // INSTANCE = new MatsimStaticDatabase( //
        // referenceFrame, //
        // linkMap);
        return new MatsimAmodeusDatabase(referenceFrame, linkMap);
    }

    // public static void initializeSingletonInstance( //
    // Network network, //
    // ReferenceFrame referenceFrame) {
    //
    // NavigableMap<String, OsmLink> linkMap = new TreeMap<>();
    //
    // CoordinateTransformation coords_toWGS84 = referenceFrame.coords_toWGS84();
    //
    // for (Link link : network.getLinks().values()) {
    // OsmLink osmLink = new OsmLink(link, //
    // coords_toWGS84.transform(link.getFromNode().getCoord()), //
    // coords_toWGS84.transform(link.getToNode().getCoord()) //
    // );
    // linkMap.put(link.getId().toString(), osmLink);
    // }
    //
    // INSTANCE = new MatsimStaticDatabase( //
    // referenceFrame, //
    // linkMap);
    // }

    // public static MatsimStaticDatabase INSTANCE;

    /** rapid lookup from MATSIM side */
    private final Map<Link, Integer> linkInteger = new HashMap<>();
    public final ReferenceFrame referenceFrame;

    /** rapid lookup from Viewer */
    private final List<OsmLink> list;

    private final IdIntegerDatabase requestIdIntegerDatabase = new IdIntegerDatabase();
    private final IdIntegerDatabase vehicleIdIntegerDatabase = new IdIntegerDatabase();

    private Integer iteration;

    private MatsimAmodeusDatabase( //
            ReferenceFrame referenceFrame, //
            NavigableMap<String, OsmLink> linkMap) {
        this.referenceFrame = referenceFrame;
        list = new ArrayList<>(linkMap.values());
        int index = 0;
        for (OsmLink osmLink : list) {
            linkInteger.put(osmLink.link, index);
            ++index;
        }
    }

    public int getLinkIndex(Link link) {
        return linkInteger.get(link);
    }

    /** @return unmodifiable map that assigns a link to
     *         the corresponding index of the OsmLink in list */
    public Map<Link, Integer> getLinkInteger() {
        return Collections.unmodifiableMap(linkInteger);
    }

    public OsmLink getOsmLink(int index) {
        return list.get(index);
    }

    public Collection<OsmLink> getOsmLinks() {
        return Collections.unmodifiableCollection(list);
    }

    public Coord getCenter() {
        Tensor points = Tensor.of(getOsmLinks().stream() //
                .map(osmLink -> osmLink.getAt(.5)) //
                .map(TensorCoords::toTensor));
        // Tensor mean = Mean.of(points); // <- mean is fast but doesn't produce as good estimate as median
        Tensor median = TensorMap.of(Median::of, Transpose.of(points), 1);
        return TensorCoords.toCoord(median);
    }

    public int getOsmLinksSize() {
        return list.size();
    }

    public int getRequestIndex(AVRequest avRequest) {
        return requestIdIntegerDatabase.getId(avRequest.getId().toString());
    }

    public int getVehicleIndex(RoboTaxi robotaxi) {
        return vehicleIdIntegerDatabase.getId(robotaxi.getId().toString());
    }

    void setIteration(Integer iteration) {
        this.iteration = iteration;
    }

    public int getIteration() {
        return iteration;
    }
}
