package bearmaps.proj2ab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Priority queue where objects have a priority that is provided
 * extrinsically, i.e. are are supplied as an argument during insertion
 * and can be changed using the changePriority method.
 */
public interface ExtrinsicMinPQ<T> {
    /* Inserts an item with the given priority value. */
    void add(T item, double priority);
    /* Returns true if the PQ contains the given item. */
    boolean contains(T item);
    /* Returns the minimum item. */
    T getSmallest();
    /* Removes and returns the minimum item. */
    T removeSmallest();
    /* Changes the priority of the given item. Behavior undefined if the item doesn't exist. */
    void changePriority(T item, double priority);
    /* Returns the number of items in the PQ. */
    int size();

    class KDTree {
        private List<Point> points;
        private Node root;


        public static void main(String[] args) {

            ArrayList<Point> points = new ArrayList<Point>();
            points.add(new Point(0, 0));
            points.add(new Point(-2, 1));
            points.add(new Point(5, 6));
            points.add(new Point(4, 2));
            points.add(new Point(8, 3));
            Random rand = new Random();
            for (int i = 0; i < 10000000; i++) {
                points.add(new Point(rand.nextInt(1000), rand.nextInt(1000)));

            }
            KDTree tree = new KDTree(points);
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1200; i++) {
                tree.nearest(rand.nextInt(1000), rand.nextInt(1000));
            }

            long end = System.currentTimeMillis();
            System.out.println("Total time used " + (end - start)/1000.0 + " seconds");
        }


        public KDTree(List<Point> points) {
            this.points = points;
            root = new Node(points.get(0), Orientation.X);

            buildTreeFromPoints();
        }

        public boolean isWorthIt(Node n, Point goal, double  bestDistance) {
            //if orientation is X, find the absolute value of goal.x - n.x
            //vice versa, compare the distance with best distance, if still further return false

            Orientation orientation = n.getOrientation();
            double currentDistance;
            if (orientation == Orientation.X) {
                currentDistance = Math.abs(goal.getX() - n.getPoint().getX());

            } else {
                currentDistance = Math.abs(goal.getY() - n.getPoint().getY());
            }
            if (currentDistance < bestDistance) {
                return true;

            }
            return false;

        }
        public Node nearest(Node n, Point goal, Node best) {
            if (n == null) {
                return best;
            }
            if (Point.distance(n.getPoint(), goal) < Point.distance(best.getPoint(), goal)) {
                best = n;
            }
            Node goalNode = new Node(goal);
            Node goodSide = null;
            Node badSide = null;
            if (n.compareTo(goalNode) > 0) {
                goodSide = n.getLeftChild();
                badSide = n.getRightChild();
            } else {
                goodSide = n.getRightChild();
                badSide = n.getLeftChild();
            }
            best = nearest(goodSide, goal, best);
            //
            double bestDistance = Point.distance(best.getPoint(), goal);
            if (isWorthIt(n, goal, bestDistance)) {
                best = nearest(badSide, goal, best);
            }

            return best;




        }


        public Point nearest(double x, double y) {
            Point goal = new Point(x, y);
            Node bestNode = nearest(root, goal, root);
            return bestNode.getPoint();




        }

        public void buildTreeFromPoints() {
            for(int i =1; i<points.size(); i++){
                insert(points.get(i));
            }
        }



        //need insertion method
    //    public void insert(Point point) {
    //        Node newNode = new Node(point);
    //        Node current = root;
    //        Node temp = null;
    //        while (current != null) {
    //
    //            //if current has bigger value, new node goes to the left
    //            if (current.compareTo(newNode) > 0) {
    //
    //                temp = current;
    //                current = current.getLeftChild();
    //
    //
    //            } else {
    //                temp = current;
    //                current = current.getRightChild();
    //
    //            }
    //            if (current == null) {
    //                Orientation orientation;
    //                if (temp.getOrientation() == Orientation.X) {
    //                    orientation = Orientation.Y;
    //                } else {
    //
    //                    orientation = Orientation.X;
    //                }
    //
    //
    //                if (temp.compareTo(newNode) > 0) {
    //                    temp.setLeftChild(newNode);
    //
    //                } else {
    //                    temp.setRightChild(newNode);
    //
    //                }
    //                newNode.setOrientation(orientation);
    //
    //
    //
    //            }
    //
    //
    //
    //        }
    //    }


        public void insert(Point point) {
            insert(root, point, 0);
        }

        public Node insert(Node node, Point point, int depth) {
            if (node == null) {
                Orientation orientation;
                if (depth % 2 == 0) {
                    orientation = Orientation.X;
                } else {
                    orientation = Orientation.Y;
                }

                return new Node(point, orientation );
            }
            //node to insert
            Node insert = new Node(point);
            if (node.compareTo(insert) > 0) {
                node.setLeftChild(insert(node.getLeftChild(), point, depth + 1));
            } else {
                node.setRightChild(insert(node.getRightChild(), point, depth + 1));
            }

            return node;


        }






        private class Node{
            private Point point;
            private Orientation orientation;
            private Node leftChild;
            private Node rightChild;
            private int depth;


            private void setOrientation() {

                if (depth % 2 == 0) {
                    orientation = Orientation.X;
                } else {
                    orientation = Orientation.Y;
                }
            }


            public void setOrientation(Orientation orientation) {
                this.orientation = orientation;
            }

            public void setLeftChild(Node leftChild) {
                this.leftChild = leftChild;
            }

            public void setRightChild(Node rightChild) {
                this.rightChild = rightChild;

            }

            public Orientation getOrientation() {
                return orientation;
            }


            public Point getPoint() {
                return this.point;
            }

            //need to be able to be compared
            public Node(Point point, Orientation orientation) {
                this.point = point;
                this.orientation = orientation;


            }

            public Node(Point point, Orientation orientation, int depth) {
                this.point = point;
                this.orientation = orientation;
                this.depth = depth;


            }

            public Node(Point point) {
                this.point = point;
            }

            public Node getLeftChild() {
                return this.leftChild;
            }
            public Node getRightChild() {
                return this.rightChild;
            }

            //if orientation is on x, compare x co orinates, left or right
            //See if this statement is less than 0, when using it
            public double compareTo(Node another)  {

                switch (orientation) {
                    case X:
                        double thisX = this.getPoint().getX();
                        double anotherX = another.getPoint().getX();
                        return thisX - anotherX;




                    default:
                        double thisY = this.getPoint().getY();
                        double anotherY = another.getPoint().getY();
                        return thisY - anotherY;





                }

            }









        }



    }
}
