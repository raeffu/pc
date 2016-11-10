package exercises;

import java.util.Arrays;

public class PrefixSum {

  // global data (shared memory)
  static int[] a = {1, 2, 3, 4, 5, 6, 7, 8};

  static final int N = a.length; // should be a power of 2
  static int[] a_ = new int[N];  // aux array to simulate PRAM memory

  static int[] copy(int[] from) {
    return Arrays.copyOf(from, N);
  }

  /**
   * @param args
   */
  public static void prefixSum() {
    // h runs from 0 to log2(N)-1
    // loop on all PRAM-steps
    a_ = copy(a); // initialize auxiliary memory
    for (int pow = 1; pow < N; pow = pow * 2)
    {
      // pow corresponds to 2^h

      // con --------------------------
      for (int i = 0; i < N; i++)
      { // loop over all PRAM-processes
        if(i >= pow)
        {
          // in a PRAM machine only processes
          // with i >= 2^h would execute this
          a_[i] = a[i] + a[i - pow];
        }
      }
      // in a PRAM program i would be the processor number
      // and we would have the statement
      // if (i<2^h) a[i]=a[i]-a[i-2^h]
      //------------------------------------------------

      a = copy(a_); // simulate memory write-back
    }
    // output
    for (int i = 0; i < N; i++)
      System.out.println(a[i]);
  }

  public static void main(String[] args) {
    prefixSum();
  }

}
