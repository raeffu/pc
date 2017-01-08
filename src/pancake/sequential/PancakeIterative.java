package pancake.sequential;

import pancake.Utility;
import pancake.parallel.Node;

import java.util.Stack;

/**
 * Created by Raphael Laubscher (laubr2)
 *
 * MODE: set mode to SOLVE or COUNT. COUNT counts the optimal solutions.
 * N: length of pankcake stack
 * numbers:  select between 3 options
 *           - enter array manually
 *           - generate random order -> Utility.randomOrder(N)
 *           - generate pair switched order -> Utility.switchedPairs(N)
 *
 */
public class PancakeIterative {

  public enum Modes {
    SOLVE, COUNT
  }

  static final Modes MODE = Modes.SOLVE;

  static int N = 15;
//  static int[] numbers = {2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15};
//  static int[] numbers = {1, 2, 4, 6, 5, 3};
//  static int[] numbers = Utility.randomOrder(N);
  static int[] numbers = Utility.switchedPairs(N);

  private static Stack<Node> workStack = new Stack<>();

  public static void main(String[] args) {
    Node root = new Node(numbers, 0, null);
    root.addPlate();

    if(MODE == Modes.COUNT) {
      count(root);
    }
    else {
      sort(root);
    }
  }

  private static void count(Node root) {
    long start = System.currentTimeMillis();
    int count = countSolutions(root);
    long end = System.currentTimeMillis();

    System.out.format("Time: %dms\n", end - start);
    System.out.format("Initial state: %s\n", root);
    System.out.format("Number of optimal solutions: %d\n", count);
  }

  private static int countSolutions(Node root) {
    int count = 0;
    boolean solutionFound = false;
    int solutionBound = 0;
    int nextBound = root.getOptimisticDistanceToSolution();

    while (!solutionFound) {
      int currentBound = nextBound;
      nextBound = Integer.MAX_VALUE;
      workStack.push(root);
      workStack.peek().nextNodes();
      int depth = 1;

      while (depth > 0) {
        if(workStack.peek().getSuccessors().empty()) {
          workStack.pop();
          depth--;
        }
        else {
          Node nextChild = workStack.peek().getSuccessors().pop();
          int cost = nextChild.getDepth() + nextChild.getOptimisticDistanceToSolution();
          if(cost <= currentBound) {
            if(nextChild.isSolution()) {
              solutionFound = true;
              solutionBound = nextChild.getDepth();
              count++;
            }
            if(!solutionFound || solutionBound > depth) {
              workStack.push(nextChild);
              workStack.peek().nextNodes();
              depth++;
            }
          }
          else {
            nextBound = Math.min(nextBound, cost);
          }
        }
      }
    }
    return count;
  }

  private static void sort(Node root) {
    long start = System.currentTimeMillis();
    Node solution = solve(root);
    long end = System.currentTimeMillis();

    System.out.format("Time: %dms\n\n", end - start);
    System.out.format("Flips: %d\n\n", solution.getDepth());
    String result = Utility.printSolution(solution);
    System.out.print(result);
  }

  private static Node solve(Node root) {
    boolean solutionFound = false;
    int nextBound = root.getOptimisticDistanceToSolution();

    while (!solutionFound) {
      int currentBound = nextBound;
      nextBound = Integer.MAX_VALUE;
      workStack.push(root);
      workStack.peek().nextNodes();
      int depth = 1;

      while (depth > 0) {
        if(workStack.peek().getSuccessors().empty()) {
          workStack.pop();
          depth--;
        }
        else {
          Node nextChild = workStack.peek().getSuccessors().pop();
          int cost = nextChild.getDepth() + nextChild.getOptimisticDistanceToSolution();
          if(cost <= currentBound) {
            if(nextChild.isSolution()) {
              return nextChild;
            }
            workStack.push(nextChild);
            workStack.peek().nextNodes();
            depth++;
          }
          else {
            nextBound = Math.min(nextBound, cost);
          }
        }
      }
    }
    return null;
  }
}
