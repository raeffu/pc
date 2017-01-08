package pancake.parallel;

import java.io.Serializable;

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
      return solutionNode.toString() + ", next bound: " + this.bound;
    }
    else{
      return "no solution, next bound: " + this.bound;
    }
  }
}
