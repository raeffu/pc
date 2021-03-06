package pancake.parallel;

import java.io.Serializable;
import java.util.Stack;

public class WorkSet implements Serializable {
  private Stack<Node> stack;
  private int bound;

  public WorkSet(Stack<Node> stack, int bound) {
    this.stack = stack;
    this.bound = bound;
  }

  public int getBound() {
    return bound;
  }

  public Stack<Node> getStack() {
    return stack;
  }
}
