package enemyai;

import java.util.Objects;

public class Node implements Comparable<Node> {
    public int x, y;
    public int gCost;
    public int hCost;
    public Node parent;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int fCost() {
        return gCost + hCost;
    }

    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.fCost(), o.fCost());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node other)
            return this.x == other.x && this.y == other.y;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
