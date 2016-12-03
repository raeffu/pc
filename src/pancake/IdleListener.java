package pancake;

import mpi.MPI;
import mpi.Request;

import java.util.List;
import java.util.Stack;

/**
 * Created by rlaubscher on 26.11.16.
 */
public class IdleListener extends Thread {
  private boolean running = true;
  private int[] no_data = new int[0];
  private int slave;
  private Stack<Integer> idleWorkers;

  private static final long WAIT_TIME = 5;

  public IdleListener(int slave, Stack<Integer> idleWorkers) {
    this.slave = slave;
    this.idleWorkers = idleWorkers;
  }

  @Override public void run() {
    while(running) {
      listen();
    }
  }

  private void listen() {
    Request request = MPI.COMM_WORLD.Irecv(this.no_data, 0, 0, MPI.INT, this.slave, Master.IDLE_TAG);

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

    idleWorkers.push(this.slave);
  }

  public void stopListening() {
    this.running = false;
  }
}
