package bearmaps.proj2c;

import bearmaps.hw4.WeightedEdge;
import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;


import bearmaps.proj2ab.KDTree;
import bearmaps.proj2ab.Point;

import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 * @author Alan Yao, Josh Hug, Zhuoyuan Cao
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

   private static final String OSM_DB_PATH = "../library-sp19/data/proj2c_xml/berkeley-2019.osm.xml";

    /*
     * KDTree takes in an arraylist of points, so it will include all the points
     * Nearest takes in x and y coordinates of a point
     *
     * */

    private Map<Point, Node> pointNodeMap;
    private ArrayList<Point> points;
    private KDTree kdTree;
    private MyTrieSet trieSet;
    private Map<String, String> cleanNameToName;

    private Map<String, List<Node>> cleanNameToNodes;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        List<Node> nodes = this.getNodes();

        points = new ArrayList<>();
        pointNodeMap = new HashMap<>();
        trieSet = new MyTrieSet();
        cleanNameToName = new HashMap<>();
                cleanNameToNodes = new HashMap<>();


        for (Node node : nodes) {
            ArrayList<WeightedEdge<Long>> neighbors = (ArrayList<WeightedEdge<Long>>) neighbors(node.id());

            if (!neighbors.isEmpty()) {

                double lon = node.lon();
                double lat = node.lat();
                Point point = new Point(lon, lat);
                points.add(point);
                pointNodeMap.put(point, node);

            }
            if (node.name() != null) {
                String name = node.name();

            String cleanName = cleanString(name);
              System.out.println(cleanName);
            trieSet.add(cleanName);

//            cleanNameToName.put(cleanName, node.name());

                if (!cleanNameToNodes.containsKey(cleanName)) {

                    cleanNameToNodes.put(cleanName, new ArrayList<Node>());

                }
            ArrayList<Node> nodeList = (ArrayList<Node>) cleanNameToNodes.get(cleanName);
            nodeList.add(node);
            cleanNameToNodes.put(cleanName, nodeList);
            }

        }

        kdTree = new KDTree(points);
    }

    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */

    /*
     *
     *
     * */
    public long closest(double lon, double lat) {

        Point nearestPoint = kdTree.nearest(lon, lat);
        Node nearestNode = pointNodeMap.get(nearestPoint);
        long id = nearestNode.id();

        return id;
    }


    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        prefix = cleanString(prefix);
        List<String> resultClean = (ArrayList<String>) trieSet.keysWithPrefix(prefix);
        List<String> result = new ArrayList<>();

        for (String name : resultClean) {
            result.add(cleanNameToName.get(name));

        }
        for (String name : resultClean) {
            System.out.println(name);

        }

        return resultClean;

    }

    public static void main(String[] args) {
        AugmentedStreetMapGraph graph = new AugmentedStreetMapGraph(OSM_DB_PATH);
        graph.getLocationsByPrefix("Berk");
        graph.getLocations("berkeley social club");

    }


    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        //clean the name
        String cleanLocationName = cleanString(locationName);
        //A list of node information
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        System.out.println(cleanNameToNodes);
        for(Node n : cleanNameToNodes.get(cleanLocationName)) {
            HashMap<String, Object> nodeInfoMap = new HashMap<>();
            nodeInfoMap.put("lat", n.lat());
            nodeInfoMap.put("lon", n.lon());
            nodeInfoMap.put("name", n.name());
            nodeInfoMap.put("id", n.id());
            result.add(nodeInfoMap);
        }


        return result;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
