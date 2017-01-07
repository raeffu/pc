package pancake.parallel;

import java.io.Serializable;

/**
 * Created by rlaubscher on 11.11.16.
 */
public class SearchResult implements Serializable {
  public Node solutionNode;
  public int bound;

  public SearchResult(Node node){
    this.solutionNode = node;
  }

  public SearchResult(int bound){
    this.bound = bound;
  }

  @Override public String toString() {
    if (solutionNode != null) {
      return solutionNode.toString() + ", bound: " + this.bound;
    }
    else{
      return "no solution, bound: " + this.bound;
    }
  }
}
