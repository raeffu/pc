import java.util.Arrays;

public class Prefix2x2MatProduct {
  // each Matrix has the Form:
  //  / a    b \
  //  |        |
  //  \ c    d /

  // global data (shared memory)
  static int[] a = {1, 2, 3, 4, 5, 6, 7, 8};
  static int[] b = {2, 3, 4, 5, 6, 7, 8, 9};
  static int[] c = {3, 4, 5, 6, 7, 8, 9, 10};
  static int[] d = {4, 5, 6, 7, 8, 9, 10, 11};

  // array for the prefix sum of a
  static final int N = a.length;
  static int[] a_ = new int[N];
  static int[] b_ = new int[N];
  static int[] c_ = new int[N];
  static int[] d_ = new int[N];

  static int[] copy(int[] from) {
    return Arrays.copyOf(from, from.length);
  }

  public static void prefixMatProd() {

    // loop on all PRAM-steps
    // h runs from 0 to log(N)-1
    // con --------------------------
    a_ = copy(a); // initialize a_
    b_ = copy(b); // initialize b_
    c_ = copy(c); // initialize c_
    d_ = copy(d); // initialize d_

    for (int pow = 1; pow < N; pow = pow * 2)
    {
      // pow corresponds to 2^h
      //-------con----------
      for (int i = 0; i < N; i++)
      {
        if(i >= pow)
        {
          // in a PRAM machine only processes
          // with i >= 2^h would execute this
          a_[i] = a[i] * a[i - pow] + b[i] * c[i - pow];
          b_[i] = a[i] * b[i - pow] + b[i] * d[i - pow];
          c_[i] = c[i] * a[i - pow] + d[i] * c[i - pow];
          d_[i] = c[i] * b[i - pow] + d[i] * d[i - pow];
        }
      }
      a = copy(a_); // simulate memory write-back
      b = copy(b_); // simulate memory write-back
      c = copy(c_); // simulate memory write-back
      d = copy(d_); // simulate memory write-back
      //----------------
    }
    // output
    System.out.println(a[N - 1] + ", " + b[N - 1] + "\n" + c[N - 1] + ", " + d[N - 1]);

  }

  public static void prod() {
    for (int i = 1; i < N; i++)
    {
      a_[i] = a[i] * a[i - 1] + b[i] * c[i - 1];
      b_[i] = a[i] * b[i - 1] + b[i] * d[i - 1];
      c_[i] = c[i] * a[i - 1] + d[i] * c[i - 1];
      d_[i] = c[i] * b[i - 1] + d[i] * d[i - 1];

      a[i] = a_[i];
      b[i] = b_[i];
      c[i] = c_[i];
      d[i] = d_[i];
    }
    System.out.println(a[N - 1] + ", " + b[N - 1] + "\n" + c[N - 1] + ", " + d[N - 1]);
  }

  public static void main(String[] args) {
    prefixMatProd();
    //		91284793, 133970264
    //		112617077, 165277688
  }

}
