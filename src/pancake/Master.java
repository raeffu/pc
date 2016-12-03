package pancake;

import mpi.MPI;

import java.util.*;

/**
 * Created by rlaubscher on 20.11.16.
 * Parallel Pancake sorting
 */
public class Master implements IWorker {
  public static final int RANK = 0;
  public static final int IDLE_TAG = 10;
  public static final int WORK_TAG = 20;
  public static final int KILL_TAG = 66;
  public static final int RESULT_TAG = 30;
  private int[] state;
  private int nSlaves;
  private int[] slaves;
  private int[] no_data = new int[0];
  private Node root;
  // Map { parent => child states }
  // parent = expanded nodes
  // children = unexpanded nodes
  private LinkedHashMap<Node, Stack<Node>> workStack = new LinkedHashMap<>();
  private Stack<Integer> idleWorkers;
  private List<IdleListener> iListeners = new ArrayList<>();
  private List<ResultListener> rListeners = new ArrayList<>();
  private List<SearchResult> results;

  public Master(int[] state, int nSlaves) {
    this.state = state;
    this.nSlaves = nSlaves;
    this.slaves = new int[nSlaves];
    this.root = new Node(state, 0, null, "");
  }

  @Override public void run() {

    // save listener messages from idle workers
    this.idleWorkers = new Stack<>();
    this.results = new ArrayList<>();
    for (int i=0; i<nSlaves; i++) {
      IdleListener idleListener = new IdleListener(i+1, this.idleWorkers);
      idleListener.start();
      iListeners.add(idleListener);
      ResultListener resultListener = new ResultListener(i+1, this.results);
      resultListener.start();
      rListeners.add(resultListener);
      this.slaves[i] = i+1;
    }

    // solve
    System.out.println("initial configuration:");
    System.out.println(Arrays.toString(this.state) + "\n");

    long start = System.currentTimeMillis();
    Node solution = solve(this.root);
    long end = System.currentTimeMillis();

    System.out.println(solution.getSteps());
    System.out.format("\nSorted after %d flips\n", solution.getDepth());
    System.out.println(Arrays.toString(solution.getState()));

    System.out.format("Time: %dms", end - start);

    this.iListeners.forEach(IdleListener::stopListening);
    this.rListeners.forEach(ResultListener::stopListening);

    // stop workers
    for (int slave : this.slaves) {
      MPI.COMM_WORLD.Isend(this.no_data, 0, 0, MPI.INT, slave, Master.KILL_TAG);
    }

    // generate work (own function)
    // save parent and children state in workStack
//    generateWork();

    // pick idle worker and send work
    // ...
//    Set<Map.Entry<Node, Stack<Node>>> it = workStack.entrySet();

//    workStack.forEach((key, value) -> {
//      String str1 = Arrays.toString(key.getState());
//      System.out.println("\nparent:\n" + str1 + "\nchildren: ");
//      System.out.println(value.size());
//      value.forEach(item -> System.out.print(Arrays.toString(item.getState()) + "\n"));
//    });
  }

  private Node solve(Node root) {
    Node solutionNode = null;
    int bound = root.getOptimisticDistanceToSolution();
    int maxBound = 2 * root.getState().length;

    while (solutionNode == null) {
      SearchResult result = search(root, bound);

      if (result.solutionNode != null) {
        solutionNode = result.solutionNode;
      }

      if (result.bound >= maxBound) {
        return null;
      }
      bound = result.bound;
    }

    return solutionNode;
  }

  private SearchResult search(Node node, int bound) {
    int f = node.getDepth() + node.getOptimisticDistanceToSolution();

    if (f > bound) {
      return new SearchResult(f);
    }

    if (node.isSolution()) {
      return new SearchResult(node);
    }

    int min = Integer.MAX_VALUE;
    Stack<Node> successors = node.nextNodes();

//    workStack.put(node, successors);
    // send to idle workers

    for (Node succ : successors) {

      // send work
      while(idleWorkers.size() < 1) {
        try {
          Thread.sleep(5);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      Integer worker = idleWorkers.pop();
      WorkSet work = new WorkSet(node, bound);
      WorkSet[] workBuffer = new WorkSet[] { work };
      MPI.COMM_WORLD.Isend(workBuffer , 0, 0, MPI.OBJECT, worker, Master.WORK_TAG);

      //revceive result

      SearchResult r = search(succ, bound);
      if (r.solutionNode != null) {
        return r;
      }
      if (r.bound < min) {
        min = r.bound;
      }
    }
    return new SearchResult(min);
  }

//  private void generateWork() {
//    Stack<Node> children = root.nextNodes();
//    Node parent = this.root;
//    workStack.put(parent, children);
//
//    for (int i = 0; i < 5; i++) {
//      if(!workStack.get(parent).empty()) {
//        parent = workStack.get(parent).pop();
//        children = parent.nextNodes();
//        workStack.put(parent, children);
//      }
//    }
//  }
}
