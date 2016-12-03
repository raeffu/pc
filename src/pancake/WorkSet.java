package pancake;

import java.io.Serializable;

/**
 * Created by rlaubscher on 22.11.16.
 */
public class WorkSet implements Serializable {
  private Node node;
  private int bound;

  public WorkSet(Node node, int bound) {
    this.node = node;
    this.bound = bound;
  }

  public int getBound() {
    return bound;
  }

  public Node getNode() {
    return node;
  }
}
