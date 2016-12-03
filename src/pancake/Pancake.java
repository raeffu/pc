package pancake;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by rlaubscher on 10.11.16.
 */
public class Pancake {
  static int N = 30;
//  static int[] numbers = new int[N];
  static int[] numbers = {7, 18, 14, 1, 4, 26, 25, 22, 13, 2, 23, 3, 6, 19, 12, 5, 21, 27, 9, 10, 15, 30, 17, 11, 28, 24, 8, 20, 29, 16};
  static Node root = new Node(numbers, 0, null, "");
//  static int flipCount = 0;

  public static Node solve(Node root) {
    Node solutionNode = null;
    int bound = root.getOptimisticDistanceToSolution();
    // 10 ist ein willkürlich gewählter Faktor zur Begrenzung der Suche
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

//  public static void flip(int k) {
//    for (int i = 0; i < (k + 1) / 2; ++i) {
//      int tmp = numbers[i];
//      numbers[i] = numbers[k - i];
//      numbers[k - i] = tmp;
//    }
//    flipCount++;
//    System.out.println("flip(0.." + k + "): " + Arrays.toString(numbers));
//  }

  public static void main(String[] args) {
//    for (int i = 1; i <= N; i++) {
//      numbers[i-1] = i;
//    }
//    System.out.println(Arrays.toString(numbers));
//    shuffleArray(numbers);
//    System.out.println(Arrays.toString(numbers));

    System.out.println("initial configuration:");
    System.out.println(Arrays.toString(numbers) + "\n");

    long start = System.currentTimeMillis();
    Node solution = solve(root);
    long end = System.currentTimeMillis();

    System.out.println(solution.getSteps());
    System.out.format("\nSorted after %d flips\n", solution.getDepth());
    System.out.println(Arrays.toString(solution.getState()));

    System.out.format("Time: %dms", end - start);
  }

  public static void shuffleArray(int[] a) {
    int n = a.length;
    Random random = new Random();
    random.nextInt();
    for (int i = 0; i < n; i++) {
      int change = i + random.nextInt(n - i);
      swap(a, i, change);
    }
  }

  private static void swap(int[] a, int i, int change) {
    int helper = a[i];
    a[i] = a[change];
    a[change] = helper;
  }
}
