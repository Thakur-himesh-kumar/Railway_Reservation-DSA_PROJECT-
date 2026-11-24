import java.util.*;

/**
 * Stations are vertices and routes are weighted (distance in km), undirected edges.
 * Stored as an adjacency list: Map<station, List<Edge>>.
 */
public class RailwayGraph {

    public static class Edge {
        final String to;
        final int distance;
        Edge(String to, int distance) { this.to = to; this.distance = distance; }
    }

    /** Result of a shortest-path search. */
    public static class PathResult {
        final List<String> path;
        final int distance;
        PathResult(List<String> path, int distance) { this.path = path; this.distance = distance; }
    }

    private final Map<String, List<Edge>> adjacency = new TreeMap<>();

    public void addStation(String station) {
        adjacency.putIfAbsent(station, new ArrayList<>());
    }

    public void addRoute(String a, String b, int distance) {
        addStation(a);
        addStation(b);
        adjacency.get(a).add(new Edge(b, distance));
        adjacency.get(b).add(new Edge(a, distance));
    }

    public boolean hasStation(String s) { return adjacency.containsKey(s); }

    public Set<String> getStations() { return adjacency.keySet(); }

    public List<Edge> getNeighbours(String s) {
        return adjacency.getOrDefault(s, Collections.emptyList());
    }

    /** Dijkstra's algorithm: shortest route (by km) between two stations, or null if none. */
    public PathResult shortestPath(String from, String to) {
        if (!hasStation(from) || !hasStation(to)) return null;
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Object[]> pq = new PriorityQueue<>(Comparator.comparingInt(o -> (Integer) o[1]));
        dist.put(from, 0);
        pq.add(new Object[]{from, 0});
        while (!pq.isEmpty()) {
            Object[] cur = pq.poll();
            String u = (String) cur[0];
            int d = (Integer) cur[1];
            if (d > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;
            if (u.equals(to)) break;
            for (Edge e : adjacency.get(u)) {
                int nd = d + e.distance;
                if (nd < dist.getOrDefault(e.to, Integer.MAX_VALUE)) {
                    dist.put(e.to, nd);
                    prev.put(e.to, u);
                    pq.add(new Object[]{e.to, nd});
                }
            }
        }
        if (!dist.containsKey(to)) return null;
        LinkedList<String> path = new LinkedList<>();
        for (String at = to; at != null; at = prev.get(at)) path.addFirst(at);
        return new PathResult(path, dist.get(to));
    }

    /** Text view of the whole network. */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<Edge>> en : adjacency.entrySet()) {
            sb.append(String.format("  %-12s -> ", en.getKey()));
            List<String> parts = new ArrayList<>();
            for (Edge e : en.getValue()) parts.add(e.to + " (" + e.distance + " km)");
            sb.append(String.join(", ", parts)).append("\n");
        }
        return sb.toString();
    }
}
