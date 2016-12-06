package pancake;

/**
 * Created by rlaubscher on 03.12.16.
 */
public class Result {

  private int slave;
  private SearchResult searchResult;

  public Result(int slave, SearchResult searchResult) {
    this.slave = slave;
    this.searchResult = searchResult;
  }

  public int getSlave() {
    return slave;
  }

  public SearchResult getSearchResult() {
    return searchResult;
  }
}
