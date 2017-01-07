package pancake.parallel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Stack;

/**
 * Created by rlaubscher on 11.11.16.
 */
public class Node implements Serializable {
  private int[] state;
  private int depth;
  private Node parent;
  private int gap;
  private Stack<Node> successors = new Stack();
//  private int flipCount;

  public Node(int[] state, int depth, Node parent){
    this.state = state;
    this.depth = depth;
    this.parent = parent;
    this.gap = -1;
    this.getOptimisticDistanceToSolution();
//    this.flipCount = flipCount;
  }

  public int getDepth(){
    return this.depth;
  }

  public Node getParent(){
    return this.parent;
  }

  public int getOptimisticDistanceToSolution(){
    if(this.gap < 0) { // gap not set
      this.gap = 0;
      int prev = state[0];

      for (int i = 1; i < state.length; i++) {
        if(Math.abs(state[i] - prev) != 1) {
          this.gap++;
        }
        prev = state[i];
      }
    }
    return this.gap;
  }

  public boolean isSolution(){
    int prev = state[0];

    for (int i = 1; i < state.length; i++){
      if(state[i] - 1 != prev){
        return false;
      }
      prev = state[i];
    }
    return true;
  }

  public Stack<Node> nextNodes(){
    if (this.successors.empty()) {
      for (int i = 1; i < state.length; i++) {
        int[] child = flip(state, i);
        successors.push(new Node(child, this.depth + 1, this));
      }
      successors.sort((o1, o2) -> {
        int gap1 = o1.getOptimisticDistanceToSolution();
        int gap2 = o2.getOptimisticDistanceToSolution();
        return gap2 - gap1;
      });
    }
    return successors;
  }

  private int[] flip(int[] a, int k) {
    int[] pancake = Arrays.copyOf(a, a.length);
    for (int i = 0; i < (k + 1) / 2; ++i) {
      int tmp = pancake[i];
      pancake[i] = pancake[k - i];
      pancake[k - i] = tmp;
    }
//    this.flipCount++;
    return pancake;
  }

  public int[] getState() {
    return state;
  }

  public void setState(int[] state) { this.state = state;}

  public String toString() {
    return Arrays.toString(this.state);
  }
  //  public int getFlipCount() {
//    return flipCount;
//  }
  public Stack<Node> getSuccessors() {
    return this.successors;
  }

  @Override public boolean equals(Object obj) {
    if(obj instanceof Node) {
      return Arrays.equals(this.state, ((Node) obj).getState());
    }
    else {
      return super.equals(obj);
    }
  }
}
