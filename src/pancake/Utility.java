package pancake;

import pancake.parallel.Node;

import java.util.Random;

public class Utility {

  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String FLIP = ANSI_RED + "|" + ANSI_RESET;

  public static int[] randomOrder(int n) {
    int[] numbers = new int[n];
    for (int i = 1; i <= n; i++) {
      numbers[i - 1] = i;
    }
    shuffleArray(numbers);
    return numbers;
  }

  public static int[] switchedPairs(int n) {
    int[] numbers = new int[n];
    for (int i = 1; i <= n; i++) {
      numbers[i - 1] = i;
    }
    switchPair(numbers);
    return numbers;
  }

  public static String printSolution(Node node) {
    int steps = node.getDepth();
    String result = "state " + steps + ":\t" + node.toString() + "\n";
    for (int i = 0; i < steps; i++) {
      int[] currentState = node.getState();
      int[] prevState = node.getParent().getState();

      int flip = getFlipPosition(currentState, prevState);
      String prefix = "state " + (steps - (i + 1)) + ":\t[";
      String suffix = "]\n";
      result = prefix + stateToString(prevState, flip) + suffix + result;

      node = node.getParent();
    }
    return result;
  }

  private static int getFlipPosition(int[] current, int[] prev) {
    for (int i = current.length - 1; i >= 0; i--) {
      if(current[i] != prev[i]) {
        return i;
      }
    }
    return -1;
  }

  private static String stateToString(int[] state, int flip) {
    String result = "";
    int n = state.length;
    for (int i = 0; i < n; i++) {
      if(flip == i) {
        if (i == n - 1)
          result += state[i] + FLIP;
        else
          result += state[i] + FLIP + " ";
      }
      else if(i == n - 1) {
        result += state[i];
      }
      else {
        result += state[i] + ", ";
      }
    }
    return result;
  }

  private static void shuffleArray(int[] a) {
    int n = a.length;
    Random random = new Random();
    random.nextInt();
    for (int i = 0; i < n; i++) {
      int change = i + random.nextInt(n - i);
      swap(a, i, change);
    }
  }

  private static void switchPair(int[] a) {
    int n = a.length / 2;
    for (int i = 0; i < n; i++) {
      int change = (i * 2) + 1;
      swap(a, i * 2, change);
    }
  }

  private static void swap(int[] a, int i, int change) {
    int tmp = a[i];
    a[i] = a[change];
    a[change] = tmp;
  }
}
