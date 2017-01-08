package pancake.parallel.count;

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
