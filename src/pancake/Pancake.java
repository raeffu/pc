package pancake;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by rlaubscher on 10.11.16.
 */
public class Pancake {
  static int[] numbers = {8, 5, 3, 7, 2, 9, 4, 10, 1, 6};
  static int flipCount = 0;

  // n: n elements are left to sort
  // dir: sorting direction. 1 = ASC, 0 = DESC
  public static void pancakeSort(int n, int dir) {
    if(n == 0)
      return;

    int[] minMax = minmax(n); // [min, max]
    int highest = minMax[dir];
    int lowest = minMax[1 - dir];
    boolean flipped = false; // true if sorting direction changed

    // highest already at right position
    if(highest == n - 1) {
      n--;
    }
    // highest at front
    else if(highest == 0) {
      flip(n - 1);
      n--;
    }
    // lowest is at end, change sorting direction
    else if(lowest == n - 1) {
      dir = 1 - dir;
      n--;
      flipped = true;
    }
    // flip at position of highest
    else {
      flip(highest);
    }
    pancakeSort(n, dir);

    // flip back where we changed sorting order
    if(flipped) {
      flip(n);
    }
  }

  public static void flip(int k) {
    for (int i = 0; i < (k + 1) / 2; ++i) {
      int tmp = numbers[i];
      numbers[i] = numbers[k - i];
      numbers[k - i] = tmp;
    }
    flipCount++;
    System.out.println("flip(0.." + k + "): " + Arrays.toString(numbers));
  }

  // returns [min, max]
  public static int[] minmax(int n) {
    int min = numbers[0];
    int max = numbers[0];
    int posMin = 0, posMax = 0;

    for (int i = 1; i < n; ++i) {
      if(numbers[i] < min) {
        min = numbers[i];
        posMin = i;
      }
      else if(numbers[i] > max) {
        max = numbers[i];
        posMax = i;
      }
    }
    return new int[] {posMin, posMax};
  }
  public static void main(String[] args) {
    shuffleArray(numbers);
    System.out.println("initial configuration:");
    System.out.println(Arrays.toString(numbers) + "\n");

    pancakeSort(numbers.length, 1);

    System.out.format("\nSorted after %d flips\n", flipCount);
    System.out.println(Arrays.toString(numbers));
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
