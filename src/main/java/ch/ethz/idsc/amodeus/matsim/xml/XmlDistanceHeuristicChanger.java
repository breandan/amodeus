package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public enum XmlDistanceHeuristicChanger {
    ;

    public static void of(File simFolder, String heuristicName) throws Exception {
        System.out.println("changing distance heuristic to " + heuristicName);

        File xmlFile = new File(simFolder, "av.xml");

        System.out.println("looking for av.xml file at " + xmlFile.getAbsolutePath());

        try (XmlModifier xmlModifier = new XmlModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();

            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element dispatcher = operator.getChild("dispatcher");

            @SuppressWarnings("unchecked")
            List<Element> children = dispatcher.getChildren();

            for (Element element : children) {
                @SuppressWarnings("unchecked")
                List<Attribute> theAttributes = element.getAttributes();

                if (theAttributes.get(0).getValue().equals("distanceHeuristics"))
                    theAttributes.get(1).setValue(heuristicName);

            }
        }
    }
}