package bearmaps.proj2ab;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DefaultKDTree {
    private Node root;

    private static class Node {
        Point point;
        Node left, right;

        Node(Point point) {
            this.point = point;
        }
    }

    public DefaultKDTree(List<Point> points) {
        root = buildTree(points, 0);
    }

    private Node buildTree(List<Point> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        int axis = depth % 2;
        Comparator<Point> comparator = (axis == 0) ? Comparator.comparingDouble(Point::getX) : Comparator.comparingDouble(Point::getY);

        Collections.sort(points, comparator);
        int medianIndex = points.size() / 2;
        Node node = new Node(points.get(medianIndex));

        node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);

        return node;
    }

    public Point nearest(double x, double y) {
        Point target = new Point(x, y);
        return nearest(root, target, root.point, 0);
    }

    private Point nearest(Node node, Point target, Point best, int depth) {
        if (node == null) {
            return best;
        }

        double bestDistance = Point.distance(best, target);
        double currentDistance = Point.distance(node.point, target);

        if (currentDistance < bestDistance) {
            best = node.point;
        }

        int axis = depth % 2;
        Node goodSide, badSide;
        if ((axis == 0 && target.getX() < node.point.getX()) || (axis == 1 && target.getY() < node.point.getY())) {
            goodSide = node.left;
            badSide = node.right;
        } else {
            goodSide = node.right;
            badSide = node.left;
        }

        best = nearest(goodSide, target, best, depth + 1);

        double axisDistance = (axis == 0) ? Math.abs(target.getX() - node.point.getX()) : Math.abs(target.getY() - node.point.getY());
        if (axisDistance < Point.distance(best, target)) {
            best = nearest(badSide, target, best, depth + 1);
        }

        return best;
    }

    public static void main(String[] args) {
        List<Point> points = new ArrayList<>();
        points.add(new Point(2, 3));
        points.add(new Point(5, 4));
        points.add(new Point(9, 6));
        points.add(new Point(4, 7));
        points.add(new Point(8, 1));
        points.add(new Point(7, 2));

        DefaultKDTree kdTree = new DefaultKDTree(points);

        Point nearest = kdTree.nearest(6, 5);
        System.out.println("Nearest point to (6, 5): " + nearest.getX() + ", " + nearest.getY());
    }
}

class DefaultPoint {
    private double x, y;

    public DefaultPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public static double distance(Point p1, Point p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}