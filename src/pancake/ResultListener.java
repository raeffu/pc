package pancake;

import mpi.MPI;
import mpi.Request;

import java.util.List;

/**
 * Created by rlaubscher on 27.11.16.
 */
public class ResultListener extends Thread {
  private boolean running = true;
  private int slave;
  private List<SearchResult> results;

  private static final long WAIT_TIME = 5;

  public ResultListener(int slave, List<SearchResult> results) {
    this.slave = slave;
    this.results = results;
  }

  @Override public void run() {
    while(running) {
      listen();
    }
  }

  private void listen() {
    SearchResult[] result = new SearchResult[1];
    Request request = MPI.COMM_WORLD.Irecv(result, 0, 0, MPI.INT, this.slave, Master.RESULT_TAG);

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

    results.add(result[0]);
  }

  public void stopListening() {
    this.running = false;
  }
}
