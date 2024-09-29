package forensic;
/**
 * This class represents a forensic analysis system that manages DNA data using
 * BSTs.
 * Contains methods to create, read, update, delete, and flag profiles.
 * 
 * CS112 Assignment Creator: Kal Pandit
 */
public class ForensicAnalysis {

    private TreeNode treeRoot; // BST's root
    private String firstUnknownSequence;
    private String secondUnknownSequence;

    public ForensicAnalysis () {
        treeRoot = null;
        firstUnknownSequence = null;
        secondUnknownSequence = null;
    }

    /**
     * Builds a simplified forensic analysis database as a BST and populates unknown sequences.
     * The input file is formatted as follows:
     * 1. one line containing the number of people in the database, say p
     * 2. one line containing first unknown sequence
     * 3. one line containing second unknown sequence
     * 2. for each person (p), this method:
     * - reads the person's name
     * - calls buildSingleProfile to return a single profile.
     * - calls insertPerson on the profile built to insert into BST.
     *      Use the BST insertion algorithm from class to insert.
     * 
     * DO NOT EDIT this method, IMPLEMENT buildSingleProfile and insertPerson.
     * 
     * @param filename the name of the file to read from
     */
    public void buildTree(String filename) {
        // DO NOT EDIT THIS CODE
        StdIn.setFile(filename); // DO NOT remove this line

        // Reads unknown sequences
        String sequence1 = StdIn.readLine();
        firstUnknownSequence = sequence1;
        String sequence2 = StdIn.readLine();
        secondUnknownSequence = sequence2;
        
        int numberOfPeople = Integer.parseInt(StdIn.readLine()); 

        for (int i = 0; i < numberOfPeople; i++) {
            // Reads name, count of STRs
            String fname = StdIn.readString();
            String lname = StdIn.readString();
            String fullName = lname + ", " + fname;
            // Calls buildSingleProfile to create
            Profile profileToAdd = createSingleProfile();
            // Calls insertPerson on that profile: inserts a key-value pair (name, profile)
            insertPerson(fullName, profileToAdd);
        }
    }

    /** 
     * Reads ONE profile from input file and returns a new Profile.
     * Do not add a StdIn.setFile statement, that is done for you in buildTree.
    */
    public Profile createSingleProfile() {
        int numOfSTRs = StdIn.readInt();
        STR [] strs = new STR[numOfSTRs];

        // this loop is populating the STR array by reading through all of the person's STRs & num of occ. 
        for (int i = 0; i < numOfSTRs;i++){
            String str = StdIn.readString();
            int numOfSTROcc = Integer.parseInt(StdIn.readString());
            STR newSTR = new STR(str, numOfSTROcc);
            strs[i] = newSTR;
        }

        Profile newProfile = new Profile (strs);
        return newProfile;
    }

    /**
     * Inserts a node with a new (key, value) pair into
     * the binary search tree rooted at treeRoot.
     * 
     * Names are the keys, Profiles are the values.
     * USE the compareTo method on keys.
     * 
     * @param newProfile the profile to be inserted
     */

     // helper method to make the recursion for insertPerson easier
    private static TreeNode insert(TreeNode node, String name, Profile newProfile){
        // base case
        if (node == null){
            return new TreeNode(name, newProfile, null, null);
        } 
        // recursive step
        int cmp = name.compareTo(node.getName());
        if (cmp < 0){
           node.setLeft(insert(node.getLeft(), name, newProfile));
        } else if (cmp > 0) {
            node.setRight(insert(node.getRight(), name, newProfile));
        }
        return node;
    }

    public void insertPerson(String name, Profile newProfile) {
        treeRoot = insert(treeRoot, name, newProfile);
    }

    /**
     * Finds the number of profiles in the BST whose interest status matches
     * isOfInterest.
     *
     * @param isOfInterest the search mode: whether we are searching for unmarked or
     *                     marked profiles. true if yes, false otherwise
     * @return the number of profiles according to the search mode marked
     */
    

    private int inOrderMarked(TreeNode node, boolean interestMark){
        
        if (node == null){
            return 0;
        }

        int count = 0;
        count += inOrderMarked(node.getLeft(), interestMark);

        if (node.getProfile().getMarkedStatus() == interestMark){
            count++;
        }
        count += inOrderMarked(node.getRight(), interestMark);
        return count;
        }
        
       
    public int getMatchingProfileCount(boolean isOfInterest) {
        TreeNode node = treeRoot;
        return inOrderMarked(node,isOfInterest); // update this line
    }

    

    /**
     * Helper method that counts the # of STR occurrences in a sequence.
     * 
     * @param sequence the sequence to search
     * @param STR      the STR to count occurrences of
     * @return the number of times STR appears in sequence
     */
    private int numberOfOccurrences(String sequence, String STR) {
                
        int repeats = 0;
        if (STR.length() > sequence.length())
            return 0;
        
        int lastOccurrence = sequence.indexOf(STR);
        
        while (lastOccurrence != -1) {
            repeats++;
            // Move start index beyond the last found occurrence
            lastOccurrence = sequence.indexOf(STR, lastOccurrence + STR.length());
        }
        return repeats;
    }

    /**
     * Traverses the BST at treeRoot to mark profiles if:
     * - For each STR in profile STRs: at least half of STR occurrences match (round
     * UP)
     * - If occurrences THROUGHOUT DNA (first + second sequence combined) matches
     * occurrences, add a match
     */
    

    private void inOrderInterestCheck(TreeNode node){
       
        if (node == null){
            return;
        }

        inOrderInterestCheck(node.getLeft());
        nodeMarkCheck(node);
        inOrderInterestCheck(node.getRight());

    }

    private void nodeMarkCheck (TreeNode node){
    int halfOccurence = (int) Math.ceil(node.getProfile().getStrs().length /2.0);
    STR [] profileSTR = node.getProfile().getStrs();
    int matchCount = 0;

        // checks each STR in the node's profile and checks if it matches (if the numOfOccurances of said STR in the node's profile equals the numOfOccurences of said STR from both sequences)
        for (int i = 0; i < profileSTR.length;i++){
        int numOfSTROcc = numberOfOccurrences(firstUnknownSequence,profileSTR[i].getStrString()) + numberOfOccurrences(secondUnknownSequence, profileSTR[i].getStrString());
            
            // increments counter if they're equal
            if (profileSTR[i].getOccurrences() == numOfSTROcc){
            matchCount++;
            }
        }
        // change marked status if matchCount is at least half the number of STRs in the node's profile
        if (matchCount >= halfOccurence){
            node.getProfile().setInterestStatus(true);
        }
        return;
    }   

    public void flagProfilesOfInterest() {
        TreeNode node = treeRoot;
        inOrderInterestCheck(node);
    }

    /**
     * Uses a level-order traversal to populate an array of unmarked Strings representing unmarked people's names.
     * - USE the getMatchingProfileCount method to get the resulting array length.
     * - USE the provided Queue class to investigate a node and enqueue its
     * neighbors.
     * 
     * @return the array of unmarked people
     */

    
    public String[] getUnmarkedPeople() {
        int numOfUnmarked = getMatchingProfileCount(false);
        String [] unmarkedProfiles = new String[numOfUnmarked];
        Queue<TreeNode> q = new Queue<>();

        q.enqueue(treeRoot);
        int index = 0;

        while (!q.isEmpty()){

            TreeNode temp = q.dequeue();

            if (temp.getProfile().getMarkedStatus() == false){
                unmarkedProfiles[index++] = temp.getName();
            }

            if (temp.getLeft() != null){
                q.enqueue(temp.getLeft());
            }
            if (temp.getRight() != null){
                q.enqueue(temp.getRight());
            }

        }

        return unmarkedProfiles;
    }

    /**
     * Removes a SINGLE node from the BST rooted at treeRoot, given a full name (Last, First)
     * This is similar to the BST delete we have seen in class.
     * 
     * If a profile containing fullName doesn't exist, do nothing.
     * You may assume that all names are distinct.
     * 
     * @param fullName the full name of the person to delete
     */
    
    // helper method to help with successor find
    private TreeNode min(TreeNode node){
        if (node.getLeft() == null){
            return node;
        } else {
            return min(node.getLeft());
        }   
    }

    private TreeNode deleteMin(TreeNode node){
        if (node.getLeft() == null) return node.getRight();
        node.setLeft(deleteMin(node.getLeft()));
        return node;
    }

    private TreeNode delete (TreeNode node, String name){
        if (node == null){
            return null;
        }

        int cmp = name.compareTo(node.getName());
        if (cmp < 0){
            node.setLeft(delete(node.getLeft(),name));
        } else if (cmp > 0){
            node.setRight(delete(node.getRight(),name));
        } else {
            if (node.getRight() == null) return node.getLeft();
            if (node.getLeft() == null) return node.getRight();

            TreeNode temp = node;
            node = min(temp.getRight());
            node.setRight(deleteMin(temp.getRight()));
            node.setLeft(temp.getLeft());
        }
        return node;
    }

    public void removePerson(String fullName) {
        treeRoot = delete(treeRoot,fullName);
    }

    /**
     * Clean up the tree by using previously written methods to remove unmarked
     * profiles.
     * Requires the use of getUnmarkedPeople and removePerson.
     */
    public void cleanupTree() {
        String [] unmarkedPeople = getUnmarkedPeople();

        for (int i = 0; i < unmarkedPeople.length; i++){
            removePerson(unmarkedPeople[i]);
        }
    }

    /**
     * Gets the root of the binary search tree.
     *
     * @return The root of the binary search tree.
     */
    public TreeNode getTreeRoot() {
        return treeRoot;
    }

    /**
     * Sets the root of the binary search tree.
     *
     * @param newRoot The new root of the binary search tree.
     */
    public void setTreeRoot(TreeNode newRoot) {
        treeRoot = newRoot;
    }

    /**
     * Gets the first unknown sequence.
     * 
     * @return the first unknown sequence.
     */
    public String getFirstUnknownSequence() {
        return firstUnknownSequence;
    }

    /**
     * Sets the first unknown sequence.
     * 
     * @param newFirst the value to set.
     */
    public void setFirstUnknownSequence(String newFirst) {
        firstUnknownSequence = newFirst;
    }

    /**
     * Gets the second unknown sequence.
     * 
     * @return the second unknown sequence.
     */
    public String getSecondUnknownSequence() {
        return secondUnknownSequence;
    }

    /**
     * Sets the second unknown sequence.
     * 
     * @param newSecond the value to set.
     */
    public void setSecondUnknownSequence(String newSecond) {
        secondUnknownSequence = newSecond;
    }

}
