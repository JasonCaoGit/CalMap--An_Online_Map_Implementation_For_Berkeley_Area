package bearmaps.hw4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
/*
* @author Zhuoyuan Cao
*
* */
public class ArrayHeapMinPQ<T>  {



    private Heap PQ;
    private int size;
    private Map<T, Integer> map;




    public ArrayHeapMinPQ() {
        PQ = new Heap();
        size = PQ.getSize();
        map = new HashMap<>();

    }



    public boolean contains(T item) {
        return map.containsKey(item);
    }

    public Heap getHeap() {
        return PQ;
    }

    public void add(T item, double priority) throws IllegalArgumentException {
        if(map.containsKey(item)) {
            throw new IllegalArgumentException("item already exists");
        }

        Node newNode = new Node(item, priority);
        PQ.add(newNode);
        map = PQ.getMap();
        size += 1;


    }

    public Map<T, Integer> getMap() {
        return map;
    }

    public T getSmallest() throws NoSuchElementException {
        if(PQ.getSize() == 0) {
            throw new NoSuchElementException();
        }
        Node n = PQ.getSmallest();
        return (T) n.getItem();
    }

    public T removeSmallest() throws NoSuchElementException {
        if(PQ.getSize() == 0) {
            throw new NoSuchElementException();
        }
        Node n = PQ.removeSmallest();
        T item = (T) n.getItem();
        map = PQ.getMap();
        size -= 1;
        return item;

    }

    public int size() {
        return PQ.getSize();
    }

    public void changePriority(T item, double priority) throws NoSuchElementException {
        if(!contains(item)) {
            throw new NoSuchElementException();
        }
        ArrayList<Node> heap = PQ.getHeap();
        int index = map.get(item);
        //set the priority of the item to a new one, index unchanged
        heap.set(index, new Node(item, priority));
        PQ.swim(index);
        PQ.sink(index);

        map = PQ.getMap();

    }



















    public static void main(String[] args) {



            ArrayHeapMinPQ pq = new ArrayHeapMinPQ();
            pq.add(1, 42);

pq.add(2, 35);
pq.add(3, 50);
pq.add(4, 20);
pq.add(5, 45);
pq.add(6, 25);
pq.add(7, 30);
pq.add(8, 55);
pq.add(9, 40);
pq.add(10, 15);
    for (int i = 0; i <= pq.size; i++) {

        Node n = (Node) pq.getHeap().getHeap().get(i);
        System.out.println(n.getPriority());
    }
        System.out.println(pq.getMap());
        System.out.println(pq.contains(1));
        pq.changePriority(1, 1);

        for (int i = 0; i <= pq.size; i++) {

        Node n = (Node) pq.getHeap().getHeap().get(i);
        System.out.println(n.getPriority());
    }
        System.out.println(pq.getMap());









//            Heap h = new Heap();
//Node<Integer> n1 = new Node<>
//Node<Integer> n2 = new Node<>(2, 23);
//Node<Integer> n3 = new Node<>(3, 25);
//Node<Integer> n4 = new Node<>(4, 17);
//Node<Integer> n5 = new Node<>(5, 36);
//Node<Integer> n6 = new Node<>(6, 50);
//Node<Integer> n7 = new Node<>(7, 11);
//
//        h.add(n1);
//        h.add(n2);
//        h.add(n3);
//        h.add(n4);
//        h.removeSmallest();
//
//
//
//
//
//    for (int i = 0; i <= h.getSize(); i++) {
//
//        Node n = (Node) h.getHeap().get(i);
//        System.out.println(n.getPriority());
//    }
//        System.out.println(h.getMap());
//
//








        }
    //min heap
    private static class Heap<T> {
        private ArrayList<Node> heap;
        private int size;
        private HashMap<T, Integer> map;




        public Map<T,Integer> getMap() {
            return map;
        }




        public int getSize() {
            return size;
        }


        //remove the smallest at 1st index
        //put the element of the last index to 1st index
        //sink the item
        public Node removeSmallest() {
            //swap the first and last one
            swap(1, size);
            map.remove((T) heap.get(size).getItem());
            Node ret = heap.get(1);
            heap.remove(size);
            size -= 1;
      ;
            sink(1);
            return ret;
        }

        public Node getSmallest() {
            return heap.get(1);
        }



        //compare compare to the children
        // if children are smaller, choose the smallest child
        //swap current and smallest child
        //repeat
        public void sink(int index) {
            if (size == 0) {
                return;
            }
            Node current = heap.get(index);
//            Node leftChild = getLeftChild(index);
//            Node rightChild = getRightChild(index);
            int leftChildIndex = getLeftChildIndex(index);
            int rightChildIndex = getRightChildIndex(index);
            int i= decideWhichChildToSwap(index, leftChildIndex, rightChildIndex);
            if (i == -1) {
                return;


            }
            swap(index, i);
            sink(i);



        }

        public int decideWhichChildToSwap(int index, int leftChildIndex, int rightChildIndex) {

            //if they are all null
            if(leftChildIndex==-1 && rightChildIndex ==-1) {
                return -1;

            }
            //if only 1 child
            Node left = heap.get(leftChildIndex);

            Node current = heap.get(index);
            if (rightChildIndex == -1) {
                //if leftChild is smaller than current one
                if (left.compareTo(current) < 0) {
                    return leftChildIndex;
                } else {
                    return -1;
                }
            }
            Node right = heap.get(rightChildIndex);
            //if two children, decide which is smaller
            if(left.compareTo(current) < 0 || right.compareTo(current) < 0) {
                if (left.compareTo(right) < 0) {
                    return leftChildIndex;
                } else {
                    return rightChildIndex;
                }

            }
            return -1;



        }





        public ArrayList<Node> getHeap() {
            return heap;
        }



        public Heap() {
            heap = new ArrayList<>();
            size = 0;
            heap.add(new Node());
            map = new HashMap<>();


        }


        public void swap(int i, int j) {
            Node temp = heap.get(i);
            heap.set(i, heap.get(j));
            map.put((T) heap.get(j).getItem(), i);
            heap.set(j, temp);
            map.put((T) temp.getItem(), j);

        }

        public void swim(int index) {
            Node parent = getParent(index);
            int parentIndex = getParentIndex(index);
            Node current = heap.get(index);
            if (parent.compareTo(current) > 0) {
                swap(index, parentIndex);
                swim(parentIndex);


            }

        }


        //add to index 1, if size is 0
        //if not 0, add to the end
        // and call swim(node.index)
        public void add(Node node) {
            if (size == 0) {
                heap.add( node);
                map.put((T)node.getItem(), 1);
                size += 1;
                return;
            }
            // add to size +1 position, when size = 0, add to index 1
            heap.add(size + 1, node);
            map.put((T)node.getItem(), size + 1);

            swim(size + 1);









            size += 1;
            return;



        }








        public int getParentIndex(int childIndex) {
            return childIndex / 2;

        }

        public int getLeftChildIndex(int parentIndex) {
            if (2 * parentIndex>size) {
                return -1;
            }
            return 2 * parentIndex;
        }

        public int getRightChildIndex(int parentIndex) {
            if (2 * parentIndex+1 > size) {
                return -1;
            }
            return 2 * parentIndex + 1;
        }

        public Node getParent(int childIndex) {
            return heap.get(childIndex / 2);
        }

        public Node getLeftChild(int childIndex) {
            if (getLeftChildIndex(childIndex) == -1) {
                return null;
            }
            return heap.get(getLeftChildIndex(childIndex));
        }

        public Node getRightChild(int childIndex) {

             if (getLeftChildIndex(childIndex) == -1) {
                return null;
            }
            return heap.get(getRightChildIndex(childIndex));
        }

    }


    private static class Node<T> {
        private T item;
        private double priority;



        public T getItem() {
            return item;
        }

        public Node() {
        }

        public Node(T item, double priority) {
            this.item = item;
            this.priority = priority;
        }


        public double getPriority() {
            return priority;
        }

        public boolean equals(Node other) {
            if (this.getItem().equals(other.getItem())) {
                return true;
            }
            return false;
        }



        //compare based on which has bigger priority
        public double compareTo(Node other) {
            return this.getPriority() - other.getPriority();


        }

    }







}
