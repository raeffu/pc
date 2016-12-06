package pancake;

import mpi.MPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

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
  private Stack<State> workStack = new Stack<>();
  private LinkedBlockingQueue<Integer> idleWorkers;
  private List<IdleListener> iListeners = new ArrayList<>();
  private List<ResultListener> rListeners = new ArrayList<>();
  //  private List<SearchResult> results;
  private LinkedBlockingQueue<Result> results;
  private List<Integer> answers = new ArrayList<>();
  private static final long WAIT_TIME = 5;

  public Master(int[] state, int nSlaves) {
    this.state = state;
    this.nSlaves = nSlaves;
    this.slaves = new int[nSlaves];
    this.root = new Node(state, 0, null, "");
  }

  @Override public void run() {

    // save listener messages from idle workers
    this.idleWorkers = new LinkedBlockingQueue<>();
    this.results = new LinkedBlockingQueue<>();
    for (int i = 0; i < nSlaves; i++) {
      IdleListener idleListener = new IdleListener(i + 1, this.idleWorkers);
      idleListener.start();
      iListeners.add(idleListener);
      ResultListener resultListener = new ResultListener(i + 1, this.results);
      resultListener.start();
      rListeners.add(resultListener);
      this.slaves[i] = i + 1;
    }

    // solve
    System.out.println("initial configuration:");
    System.out.println(Arrays.toString(this.state) + "\n");

    long start = System.currentTimeMillis();
    Node solution = null;
    try {
      solution = solve(this.root);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
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

  private Node solve(Node root) throws InterruptedException {
    Node solutionNode = null;
    int bound = root.getOptimisticDistanceToSolution();
    //    int bound = 4;
    int maxBound = 2 * root.getState().length;

    System.out.println("Master thread, solve, bound: " + bound);

    Stack<Node> children = root.nextNodes();
    workStack.push(new State(root, children, bound));

    System.out.println("roots children");
    for (Node child : children) {
      System.out.println(child);
    }

    while (bound < maxBound) {
      State current = workStack.pop();
      Node node = current.getNode();
      Stack<Node> successors = current.getChildren();
      int currentBound = current.getBound();

      System.out.println("currentBound: " + currentBound);

      for (Node succ : successors) {
        // send work
        while (idleWorkers.size() < 1) {
          try {
            Thread.sleep(5);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        Integer worker = idleWorkers.take();
        WorkSet work = new WorkSet(succ, currentBound);
        WorkSet[] workBuffer = new WorkSet[] {work};
        System.out.println("Send work to slave " + worker + ": " + workBuffer[0].getNode().toString());
        MPI.COMM_WORLD.Isend(workBuffer, 0, 1, MPI.OBJECT, worker, Master.WORK_TAG);
        answers.add(worker);
      }

      // receive result
      System.out.println("receive result");
      while (this.results.size() < answers.size()) {
        try {
          System.out.println("sleep");
          Thread.sleep(WAIT_TIME);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

//      System.out.println("result list is not empty: " + this.results.size());

      for (Result result : results) {
        //      while (this.results.size() <= this.nSlaves) {
        //      Result result = this.results.take();
        SearchResult r = result.getSearchResult();
        System.out.println("Search Result, received by Master: " + r.toString());

        if(r.solutionNode != null) {
          return r.solutionNode;
        }
      }
      // no solution found
      System.out.println(">>>>>>>>>>>>> increment bound");
      bound++;
      workStack.push(new State(root, children, bound));

      //        if(r.bound >= maxBound) {
      //          return null;
      //        }
      //      }
    }

    // TODO: Bound erhöhen

    // go back in stack if no result found

    return solutionNode;
  }

  //    while (solutionNode == null) {
  //      SearchResult result = search(root, bound);
  //
  //      if (result.solutionNode != null) {
  //        solutionNode = result.solutionNode;
  //      }
  //
  //      if (result.bound >= maxBound) {
  //        return null;
  //      }
  //      bound = result.bound;
  //    }

  //  private SearchResult search(Node node, int bound) throws InterruptedException {
  //    int f = node.getDepth() + node.getOptimisticDistanceToSolution();
  //
  //    if (f > bound) {
  //      return new SearchResult(f);
  //    }
  //
  //    if (node.isSolution()) {
  //      return new SearchResult(node);
  //    }
  //
  //    int min = Integer.MAX_VALUE;
  //    Stack<Node> successors = node.nextNodes();
  //
  ////    workStack.put(node, successors);
  //    // send to idle workers
  //
  //    for (Node succ : successors) {
  //
  //      // send work
  //      while(idleWorkers.size() < 1) {
  //        try {
  //          Thread.sleep(5);
  //        }
  //        catch (InterruptedException e) {
  //          e.printStackTrace();
  //        }
  //      }
  //      Integer worker = idleWorkers.take();
  //      WorkSet work = new WorkSet(node, bound);
  //      WorkSet[] workBuffer = new WorkSet[] { work };
  //      MPI.COMM_WORLD.Isend(workBuffer , 0, 1, MPI.OBJECT, worker, Master.WORK_TAG);
  //
  //      //revceive result
  //
  //      SearchResult r = search(succ, bound);
  //      if (r.solutionNode != null) {
  //        return r;
  //      }
  //      if (r.bound < min) {
  //        min = r.bound;
  //      }
  //    }
  //    return new SearchResult(min);
  //  }

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
