package pancake.sequential;

import pancake.parallel.Node;

import java.util.Arrays;
import java.util.Stack;

/**
 * Created by rlaubscher on 25.12.16.
 */
public class PancakeIterative {

  public enum Modes {
    solve, count
  }

  static final Modes MODE = Modes.solve;

  static int N = 15;
  static int[] numbers = {2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15};
//  static int[] numbers = {1, 2, 4, 6, 5, 3};
  private static Stack<Node> workStack = new Stack<>();

  public static void main(String[] args) {
    Node root = new Node(numbers, 0, null);
    int[] newState = Arrays.copyOf(root.getState(), root.getState().length + 1);
    newState[newState.length - 1] = root.getState().length + 1;
    root.setState(newState);

    if(MODE == Modes.count) {
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

    System.out.print("Finished after ");
    System.out.print(end - start);
    System.out.println("ms");
    System.out.println("Solutions found: " + count);
  }

  private static int countSolutions (Node root) {
    int count = 0;
    boolean solutionFound = false;
    int solutionBound = 0;
    int nextcb = root.getOptimisticDistanceToSolution();

    while(!solutionFound) {
      int cb = nextcb;
      nextcb = Integer.MAX_VALUE;
      workStack.push(root);
      workStack.peek().nextNodes();
      int depth = 1;

      while(depth > 0){
        if(workStack.peek().getSuccessors().empty()){
          workStack.pop();
          depth--;
        }
        else {
          Node nextChild = workStack.peek().getSuccessors().pop();
          int cost = nextChild.getDepth() + nextChild.getOptimisticDistanceToSolution();
          if (cost <= cb){
            if (nextChild.isSolution()){
              solutionFound = true;
              solutionBound = nextChild.getDepth();
              count++;
            }
            if (!solutionFound || solutionBound > depth) {
              workStack.push(nextChild);
              workStack.peek().nextNodes();
              depth++;
            }
          }
          else {
            nextcb = Math.min(nextcb, cost);
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

    System.out.print("Finished after ");
    System.out.print(end - start);
    System.out.println("ms");
    System.out.println("Flips: " + solution.getDepth());
    System.out.println(solution);
    Node parent = solution.getParent();
    while(parent != null) {
      System.out.println(parent);
      parent = parent.getParent();
    }
  }

  private static Node solve(Node root) {
    boolean solutionFound = false;
    int nextcb = root.getOptimisticDistanceToSolution();

    while(!solutionFound) {
      int cb = nextcb;
      nextcb = Integer.MAX_VALUE;
      workStack.push(root);
      workStack.peek().nextNodes();
      int depth = 1;

      while(depth > 0){
        if(workStack.peek().getSuccessors().empty()){
          workStack.pop();
          depth--;
        }
        else {
          Node nextChild = workStack.peek().getSuccessors().pop();
          int cost = nextChild.getDepth() + nextChild.getOptimisticDistanceToSolution();
          if (cost <= cb){
            if (nextChild.isSolution()){
              solutionFound = true;
              return nextChild;
            }
            workStack.push(nextChild);
            workStack.peek().nextNodes();
            depth++;
          }
          else {
            nextcb = Math.min(nextcb, cost);
          }
        }
      }
    }
    return null;
  }
}
