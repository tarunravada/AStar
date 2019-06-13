import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

public class AStar {

    /**
     * Comparator for openList to sort by f
     * 
     * @author Tarun Ravada
     *
     */
    private static class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node arg0, Node arg1)
        {
            return Integer.compare(arg0.getF(), arg1.getF());
        }

    }

    private static int diagonalCost;
    private static int hvCost;
    private static PriorityQueue<Node> openList;
    private static HashSet<Node> closedList;
    private static HashSet<Node> path;
    private static Node env[][];
    private static Node start, goal;

    /**
     * Initializes program variables and the environment
     * 
     * @param dcost diagonal cost
     * @param hvcost hv cost
     * @param size size of board
     * @param blockChance % chance of tile being block
     */
    public static void initEnv(int dcost, int hvcost, int size, int blockChance)
    {

        openList = new PriorityQueue<Node>(new NodeComparator());
        closedList = new HashSet<Node>();
        path = new HashSet<Node>();

        start = null;
        goal = null;

        diagonalCost = dcost;
        hvCost = hvcost;
        env = new Node[size][size];

        initBlocks(blockChance);

    }

    /**
     * Resets the program variables Called when user wants to set new start and goal
     */
    public static void resetEnv()
    {
        start = null;
        goal = null;
        openList.clear();
        closedList.clear();
        path.clear();

        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                env[i][j].setParent(null);
                env[i][j].setG(0);
                env[i][j].setH(0);
                env[i][j].setF();
            }
        }

    }

    /**
     * Initializes non traversable tiles
     * 
     * @param prob % probability each tile is not traversable
     */
    public static void initBlocks(int prob)
    {
        Random rand = new Random();
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                if (rand.nextInt(prob) == 1)
                    env[i][j] = new Node(i, j, 1);
                else
                    env[i][j] = new Node(i, j, 0);
            }
        }
    }

    /**
     * Prints all the environment to console
     */
    public static void printEnv()
    {
        for (int i = 0; i < env.length; i++)
            System.out.print("__");
        System.out.println();
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env.length; j++) {
                if (env[i][j].getType() == 1) // check if traversible
                    System.out.print("|X");
                else if (start != null && env[i][j].equals(start)) // check if start
                    System.out.print("|S");
                else if (goal != null && env[i][j].equals(goal)) // check if goal
                    System.out.print("|G");
                else if (path.contains(env[i][j])) // is part of path
                    System.out.print("|o");
                else // else empty
                    System.out.print("|_");
            }

            System.out.println("|");
        }
        System.out.println();
    }

    /**
     * adds tiles that are part of the path to a list
     */
    public static void getPath()
    {
        Node n = env[goal.getRow()][goal.getCol()].getParent();
        while (!n.equals(start)) {
            path.add(n);
            n = n.getParent();
        }
    }

    /**
     * Finds the path to goal from start tile
     * 
     * @return return true if path is found, false if not
     */
    public static boolean findPath()
    {
        Node current;

        // init node before adding to openList
        initNode(start.getRow(), start.getCol(), 0);
        openList.add(env[start.getRow()][start.getCol()]);

        // algorithm
        while (!openList.isEmpty()) {
            current = openList.poll();
            closedList.add(current);

            if (current.equals(goal)) {
                getPath();
                return true;
            }

            getNeighbors(current);
        }

        return false;

    }

    /**
     * Finds and validates the neighbors of given node Adds valid nodes to openList
     * 
     * @param n current node
     */
    public static void getNeighbors(Node n)
    {
        for (int i = n.getRow() - 1; i <= n.getRow() + 1; i++) {
            for (int j = n.getCol() - 1; j <= n.getCol() + 1; j++) {

                if (isValidNeighbor(i, j, n)) { // neighbor valid
                    if (!openList.contains(env[i][j])) { // openList doesn't contain

                        if (Math.abs(n.getRow() - i) == Math.abs(n.getCol() - j)) // check diagonal
                            initNode(i, j, diagonalCost + n.getG());
                        else
                            initNode(i, i, hvCost + n.getG());
                        env[i][j].setParent(n);
                        openList.add(env[i][j]);
                    }
                    else { // already exists in openList, check for better g cost
                        if (Math.abs(n.getRow() - i) == Math.abs(n.getCol() - j)) {
                            if (diagonalCost + n.getG() <= env[i][j].getG()) { // if new cost less
                                openList.remove(env[i][j]);
                                initNode(i, j, diagonalCost + n.getG());
                                env[i][j].setParent(n);
                                openList.add(env[i][j]);
                            }
                        }
                        else {
                            if (hvCost + n.getG() < env[i][j].getG()) {
                                openList.remove(env[i][j]);
                                initNode(i, j, hvCost + n.getG());
                                env[i][j].setParent(n);
                                openList.add(env[i][j]);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Check if node with given coordinates is a valid neighbor of current node
     * 
     * @param nr row number
     * @param nc column number
     * @param current current node
     * @return true if node is valid, false if not
     */
    public static boolean isValidNeighbor(int nr, int nc, Node current)
    {

        if (nr < 0 || nc < 0 || nc >= env.length || nr >= env.length) // out of bounds
            return false;
        else if (current.equals(env[nr][nc])) // is current node
            return false;
        else if (env[nr][nc].getType() == 1) // not traversable
            return false;
        else if (closedList.contains(env[nr][nc]))
            return false;
        else
            return true;

    }

    /**
     * Sets h, g, and f of node with given coordinates
     * 
     * @param r row
     * @param c column
     * @param g g value
     */
    public static void initNode(int r, int c, int g)
    {
        env[r][c].setH(Math.abs(goal.getCol() - c) + Math.abs(goal.getRow() - r));
        env[r][c].setG(g);
        env[r][c].setF();
        // env[r][c].setParent(n);
        // openList.add(env[r][c]);
    }

    /**
     * Sets Start tile and Goal tile Prompts user for coordinates of start and goal
     * tile
     */
    public static void setGoal()
    {

        Scanner console = new Scanner(System.in);
        int startX, startY, goalX, goalY;

        System.out.println("Enter the row and column of the start tile");
        System.out.print("Row (1 - " + env.length + "): ");
        startX = console.nextInt();
        System.out.print("Col (1 - " + env.length + "): ");
        startY = console.nextInt();
        start = env[startX - 1][startY - 1];

        System.out.println("Enter the row and column of the goal tile");
        System.out.print("Row (1 - " + env.length + "): ");
        goalX = console.nextInt();
        System.out.print("Col (1 - " + env.length + "): ");
        goalY = console.nextInt();
        goal = env[goalX - 1][goalY - 1];

    }

    /**
     * Calls required methods to run the algorithm
     * 
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        boolean cont = true;
        Scanner console = new Scanner(System.in);

        // init env with hv cost 10, diag cost 14, boards size 15x15, and 10%
        // probability tile will be a block
        initEnv(14, 10, 15, 10);

        while (cont) {
            System.out.println("-------------------------------------------");
            System.out.println("X - Blocked, S - Origin, G - Goal, o - Path");
            System.out.println("Please provide valid input");
            printEnv();
            setGoal();
            printEnv();

            if (findPath()) {
                System.out.println("Path Found!");
                printEnv();
            }
            else
                System.out.println("Path not found!");

            System.out.print("Do you want to replay?(y/n): ");

            if (console.next().equals("y"))
                resetEnv();
            else
                cont = false;
            System.out.println("\n");
        }

    }

}
