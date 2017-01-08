package pancake.parallel.count;

import java.io.Serializable;

public class CountResult implements Serializable {
  public int count;
  public int bound;

  public CountResult(int count, int bound){
    this.bound = bound;
    this.count = count;
  }

  @Override public String toString() {
    return "Count: " + this.count + ", next bound: " + this.bound;
  }
}
