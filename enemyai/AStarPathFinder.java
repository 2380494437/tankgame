package enemyai;

import java.util.*;

public class AStarPathFinder {
    private final int[][] map;
    private final int width, height;

    public AStarPathFinder(int[][] map) {
        this.map = map;
        this.width = map.length;
        this.height = map[0].length;
    }

    public List<Node> findPath(int startX, int startY, int targetX, int targetY) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<Node> closed = new HashSet<>();

        Node start = new Node(startX, startY);
        Node end = new Node(targetX, targetY);
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();
            closed.add(current);

            if (current.equals(end)) return reconstruct(current);

            for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (map[nx][ny] != 0) continue;

                Node neighbor = new Node(nx, ny);
                if (closed.contains(neighbor)) continue;

                int g = current.gCost + 1;
                if (!open.contains(neighbor) || g < neighbor.gCost) {
                    neighbor.gCost = g;
                    neighbor.hCost = Math.abs(end.x - nx) + Math.abs(end.y - ny);
                    neighbor.parent = current;
                    open.remove(neighbor);
                    open.add(neighbor);
                }
            }
        }
        return new ArrayList<>();
    }

    private List<Node> reconstruct(Node end) {
        List<Node> path = new ArrayList<>();
        for (Node curr = end; curr != null; curr = curr.parent) {
            path.add(curr);
        }
        Collections.reverse(path);
        return path;
    }
}
