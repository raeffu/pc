package pancake.parallel.count;

/**
 * Created by rlaubscher on 03.12.16.
 */
public class Result {

  private int slave;
  private CountResult countResult;

  public Result(int slave, CountResult countResult) {
    this.slave = slave;
    this.countResult = countResult;
  }

  public int getSlave() {
    return slave;
  }

  public CountResult getCountResult() {
    return countResult;
  }
}
