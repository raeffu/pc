package pancake;

import mpi.MPI;
import mpi.Request;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rlaubscher on 27.11.16.
 */
public class ResultListener extends Thread {
  private boolean running = true;
  private int slave;
  private LinkedBlockingQueue<Result> results;

  private static final long WAIT_TIME = 5;

  public ResultListener(int slave, LinkedBlockingQueue<Result> results) {
    this.slave = slave;
    this.results = results;
  }

  @Override public void run() {
    while(running) {
      listen();
    }

    try {
      this.join();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void listen() {
    SearchResult[] result = new SearchResult[1];
    Request request = MPI.COMM_WORLD.Irecv(result, 0, 1, MPI.OBJECT, this.slave, Master.RESULT_TAG);

    while (request.Test() == null) {
      if(!running)
        return;

      try {
        Thread.sleep(WAIT_TIME);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    try {
//      System.out.println("SearchResult " + result[0].solutionNode);
      results.put(new Result(this.slave, result[0]));
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void stopListening() {
    this.running = false;
  }
}
