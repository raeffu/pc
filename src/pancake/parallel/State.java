package pancake.parallel;

import java.util.Stack;

/**
 * Created by rlaubscher on 03.12.16.
 */
public class State {

  private Node node;
  private Stack<Node> children;
  private int bound;

  public State(Node node, Stack<Node> children, int bound) {
    this.node = node;
    this.children = children;
    this.bound = bound;
  }

  public Node getNode() {
    return node;
  }

  public Stack<Node> getChildren() {
    return children;
  }

  public int getBound() {
    return bound;
  }
}
