package pancake.sequential;

import pancake.Utility;
import pancake.parallel.Node;
import pancake.parallel.SearchResult;

import java.util.List;

/**
 * Created by Raphael Laubscher (laubr2)
 *
 * MODE: only SOLVE
 * N: length of pankcake stack
 * numbers:  select between 3 options
 *           - enter array manually
 *           - generate random order -> Utility.randomOrder(N)
 *           - generate pair switched order -> Utility.switchedPairs(N)
 *
 */
public class PancakeRecursive {

  static int N = 14;
//  static int[] numbers = {7, 18, 14, 1, 4, 26, 25, 22, 13, 2, 23, 3, 6, 19, 12, 5, 21, 27, 9, 10, 15, 30, 17, 11, 28, 24, 8, 20, 29, 16};
//  static int[] numbers = {2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15};
//  static int[] numbers = Utility.randomOrder(N);
  static int[] numbers = Utility.switchedPairs(N);

  static Node root = new Node(numbers, 0, null);

  public static Node solve(Node root) {
    Node solutionNode = null;
    int bound = root.getOptimisticDistanceToSolution();
    int maxBound = 2 * N;
    while (solutionNode == null) {
      SearchResult r = search(root, bound);
      if (r.solutionNode != null) {
        solutionNode = r.solutionNode;
      }
      if (r.bound >= maxBound) {
        return null;
      }
      bound = r.bound;
    }
    return solutionNode;
  }

  static SearchResult search(Node node, int bound) {
    int f = node.getDepth() + node.getOptimisticDistanceToSolution();
    if (f > bound) {
      return new SearchResult(f);
    }
    if (node.isSolution()) {
      return new SearchResult(node);
    }
    int min = Integer.MAX_VALUE;
    List<Node> successors = node.nextNodes();
    for (Node succ : successors) {
      SearchResult r = search(succ, bound);
      if (r.solutionNode != null) {
        return r;
      }
      if (r.bound < min) {
        min = r.bound;
      }
    }
    return new SearchResult(min);
  }

  public static void main(String[] args) {
    root.addPlate();
    long start = System.currentTimeMillis();
    Node solution = solve(root);
    long end = System.currentTimeMillis();

    System.out.format("Time: %dms\n\n", end - start);
    System.out.format("Flips: %d\n\n", solution.getDepth());
    System.out.println(Utility.printSolution(solution));
  }

}
