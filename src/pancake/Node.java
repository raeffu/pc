package pancake;

import com.sun.tools.javac.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Created by rlaubscher on 11.11.16.
 */
public class Node {
  private int[] state;
  private int depth;
  private String steps;
  private Node parent;
  private int gap;
//  private int flipCount;

  public Node(int[] state, int depth, Node parent, String steps){
    this.state = state;
    this.depth = depth;
    this.parent = parent;
    this.gap = -1;
    if (parent != null) {
      this.steps = steps + "\n" + Arrays.toString(parent.getState());
    }
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
    Stack<Node> successors = new Stack();
    for(int i=1; i < state.length; i++){
      int[] child = flip(state, i);
      successors.push(new Node(child, this.depth+1, this, this.steps));
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

  public String getSteps() {
    return steps;
  }

  public String toString() {
    return Arrays.toString(this.state);
  }
  //  public int getFlipCount() {
//    return flipCount;
//  }
}
