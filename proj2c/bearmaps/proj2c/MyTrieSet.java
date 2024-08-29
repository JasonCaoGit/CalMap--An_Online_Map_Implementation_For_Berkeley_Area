package bearmaps.proj2c;

import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

public class MyTrieSet implements TrieSet61B {
    private Node root;

    public static void main(String[] args) {
        MyTrieSet trieSet = new MyTrieSet();
        trieSet.add("same");
        trieSet.add("sam");
        trieSet.add("sad");
        trieSet.add("samehere");
        trieSet.add("sand");
        System.out.println(trieSet.contains("sack"));

        System.out.println(trieSet.keysWithPrefix("sa"));


    }

    public MyTrieSet() {
        root = new Node(false);

    }




    private class Node {
        private Hashtable<Character, Node> next;
        private boolean isKey;

        public Node(boolean isKey) {
            this.isKey = isKey;
            next = new Hashtable<>();

        }

    }

    //implements everything except for longestPrefixOf and Add

    /**
     * Clears all items out of Trie
     */
    //clear all children in the next nodes of the root,
    //make sure you do not store nodes in any place other than the next hash table
    public void clear() {
        root.next = new Hashtable<>();

    }

    /**
     * Returns true if the Trie contains KEY, false otherwise
     */
    public boolean contains(String key) {
        boolean isContained =  containsHelper(root, key, 0);
        return isContained;

    }

    /*
    * if after going through all the letters and find out the last letter is in a blue node, return true, if last node is not blue ,return false
    * if in any step, the next map does not contain the character, return false
    *
    *
    * */
    public boolean containsHelper(Node node, String key, int index) {
        if (index == key.length()) {
            if (node.isKey) {
                return true;
            } else {
                return false;
            }
        }

        char c =  key.charAt(index);
        if (node.next.containsKey(c)) {
            return true && containsHelper(node.next.get(c), key, index + 1);
        } else {
            return false;
        }
    }

      public boolean containsPrefix(String key) {
        boolean isContained =  containsPrefixHelper(root, key, 0);
        return isContained;

    }

       public boolean containsPrefixHelper(Node node, String key, int index) {
        if (index == key.length()) {
            return true;
        }

        char c =  key.charAt(index);
        if (node.next.containsKey(c)) {
            return true && containsPrefixHelper(node.next.get(c), key, index + 1);
        } else {
            return false;
        }
    }



    /**
     * Inserts string KEY into Trie
     */
    public void add(String key) {
        //start with the first letter
        addHelper(root, key, 0);

    }

    /*
     *
     * */
    public void addHelper(Node node, String key, int index) {
        if (index == key.length()) {
            node.isKey = true;
            return;
        }

        char c = key.charAt(index);
        //if the node has no child at all
        if(node.next.size()==0) {


            node.next.put(c, new Node(false));
            addHelper(node.next.get(c), key, index + 1);
            return;
        }
        //if it has  children that contains the char
        if (node.next.containsKey(c)) {
            addHelper(node.next.get(c), key, index + 1);
            return;
        } else {
            //create a new node in for the current letter because our set does not have it
            //if we want a new node with character a , set its parent's next to (a, node)

            node.next.put(c, new Node(false));
            addHelper(node.next.get(c), key, index + 1);
            return;



        }
    }

    /**
     * Returns a list of all words that start with PREFIX
     */

    /*
    * Find the node a that contains the last letter of key
    * create a list to store the strings
    * for all characters in a's children
    * call colHelper(prefix + thisCharacter, list, that node containg the character)
    *
    *
    * */
    public List<String> keysWithPrefix(String prefix) {

        ArrayList<String> result = new ArrayList<>();
        if (!containsPrefix(prefix)) {
            return result;
        }
        //find the node a
        Node a = nodeFinder(root, prefix, 0);
        for(char c : a.next.keySet()) {
            colHelper(prefix + c, result, a.next.get(c));
        }


        return result;
    }
    /*
    * If node is blue, add the string to the result
    * for every character in node's childrent, call it self again with string extended
    *
    * */
    public void colHelper(String s, List<String> result, Node node) {
        if(node.isKey) {
            result.add(s);
        }
        for(char c : node.next.keySet()) {
            colHelper(s +c, result, node.next.get(c));
        }
    }






        public Node nodeFinder(Node node, String key, int index) {
        if (index == key.length()) {
            return node;
        }

        char c =  key.charAt(index);
       return  nodeFinder(node.next.get(c), key, index + 1);


    }

    /**
     * Returns the longest prefix of KEY that exists in the Trie
     * Not required for Lab 9. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    public String longestPrefixOf(String key) throws UnsupportedOperationException {

        throw new UnsupportedOperationException();
    }


}
