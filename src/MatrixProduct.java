import java.util.Arrays;

public class MatrixProduct {

  static int[] a = {1, 2, 3, 4, 5, 6, 7, 8};
  static int[] b = {2, 3, 4, 5, 6, 7, 8, 9};
  static int[] c = {3, 4, 5, 6, 7, 8, 9, 10};
  static int[] d = {4, 5, 6, 7, 8, 9, 10, 11};

  static final int N = a.length; // should be a power of 2
  // global data (shared memory)

  static int[] a_ = new int[N];  // aux array to simulate PRAM memory
  static int[] b_ = new int[N];  // aux array to simulate PRAM memory
  static int[] c_ = new int[N];  // aux array to simulate PRAM memory
  static int[] d_ = new int[N];  // aux array to simulate PRAM memory


  static int[] copy(int[] from) {
    return Arrays.copyOf(from, from.length);
  }


  public static void matrixProduct() {
    a_ = copy(a);  // aux array to simulate PRAM memory
    b_ = copy(b);  // aux array to simulate PRAM memory
    c_ = copy(c);  // aux array to simulate PRAM memory
    d_ = copy(d);  // aux array to simulate PRAM memory

    for (int pow = 1; pow < N; pow = pow * 2)
    {
      for (int i = 0; i < N; i++)
      { // loop over all PRAM-processes
        if(i >= pow)
        {
          a_[i] = a[i - pow]*a[i] + b[i - pow]*c[i];
          b_[i] = a[i - pow]*b[i] + b[i - pow]*d[i];
          c_[i] = c[i - pow]*a[i] + d[i - pow]*c[i];
          d_[i] = c[i - pow]*b[i] + d[i - pow]*d[i];
        }
      }
      a = copy(a_);
      b = copy(b_);
      c = copy(c_);
      d = copy(d_);

//      for (int i = 0; i < N; i++)
//      {
//        System.out.println("----- M" + i + " -----");
//        System.out.print("a");
//        System.out.print(i);
//        System.out.print(": ");
//        System.out.println(a[i]);
//        System.out.print("b");
//        System.out.print(i);
//        System.out.print(": ");
//        System.out.println(b[i]);
//        System.out.print("c");
//        System.out.print(i);
//        System.out.print(": ");
//        System.out.println(c[i]);
//        System.out.print("d");
//        System.out.print(i);
//        System.out.print(": ");
//        System.out.println(d[i]);
//        System.out.println("--------------");
//      }
    }
    // output
    System.out.println(a[N - 1] + ", " + b[N - 1] + "\n" + c[N - 1] + ", " + d[N - 1]);
  }

  public static void main(String[] args) {
    matrixProduct();
  }

}
