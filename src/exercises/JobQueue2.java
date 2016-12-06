package exercises;

import java.util.Random;

public class JobQueue2 {
  static Random rand = new Random();
  static int N = 8;
  static double[] r = new double[N]; // Zeit zwischen job i-1 und i
  static double[] s = new double[N]; // Service-Zeit von Job i
  static double[] an = new double[N];
  static double[] ab = new double[N];
  static double[] a = new double[N];
  static double[] b = new double[N];
  static double[] c = new double[N];
  static double[] aHilfs = new double[N];
  static double[] bHilfs = new double[N];
  static double[] cHilfs = new double[N];
  // a[] links oben
  // b[] rechts oben
  // c[] rechts unten

  static void copy(double[] from, double[] to) {
    // con --------------------------
    for (int i = 0; i < from.length; i++)
      to[i] = from[i];
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    rand.setSeed(123456);
    // generiere exponentialverteilte Zeitintervalle
    // mit Mittelwert 1
    for (int i = 0; i < N; i++)
    {
      r[i] = -Math.log(rand.nextDouble());
      s[i] = -Math.log(rand.nextDouble());
    }

    // Zuerst sequentielle Variante zur Ueberprüfung
    an[0] = r[0];
    ab[0] = r[0] + s[0];

    System.out.println(0 + ", an: " + an[0] + ", ab: " + ab[0]);
    for (int i = 1; i < N; i++)
    {
      an[i] = an[i - 1] + r[i];
      if(an[i] > ab[i - 1])
        ab[i] = an[i] + s[i];
      else
        ab[i] = ab[i - 1] + s[i];
      System.out.println(i + ", an: " + an[i] + ", ab: " + ab[i]);
    }

    // und jetzt die parallelisierbare  (PRAM-Variante)
    // paralleles for (zur initialisierung der Matrizen)----------
    for (int i = 0; i < N; i++)
    {
      a[i] = s[i];
      b[i] = s[i] + r[i];
      c[i] = r[i];
    }
    //-------------------------------------------------------------
    int zweiHochH = 1;  // 2^h
    while (zweiHochH <= N)
    {
      copy(a, aHilfs);
      copy(b, bHilfs);
      copy(c, cHilfs);
      // con -------------------------------------------------------
      for (int i = zweiHochH; i < N; i++)
      {
        //.................................................................
        aHilfs[i] = a[i] + a[i-zweiHochH];
        bHilfs[i] = Math.max(a[i] + b[i-zweiHochH], b[i] + c[i-zweiHochH]);
        cHilfs[i] = c[i] + c[i-zweiHochH];
      }
      copy(aHilfs, a);
      copy(bHilfs, b);
      copy(cHilfs, c);

      // -----------------------------------------------------------------------
      zweiHochH = zweiHochH * 2; // nächste Runde
      // -----------------------------------------------------------------------
    }

    System.out.println("-----------");
    // nun austesten sollte dasselbe ergeben wie oben
    for (int i = 0; i < N; i++){
      System.out.println(i + ", an: " + c[i] + ", ab: " + Math.max(a[i], b[i]));
    }
  }

}

