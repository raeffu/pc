package pancake;

import java.lang.invoke.SerializedLambda;

/**
 * Created by rlaubscher on 11.11.16.
 */
public class SearchResult {
  Node solutionNode;
  int bound;

  public SearchResult(Node node){
    this.solutionNode = node;
  }

  public SearchResult(int bound){
    this.bound = bound;
  }
}
