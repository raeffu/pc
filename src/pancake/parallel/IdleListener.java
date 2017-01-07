package pancake.parallel;

import mpi.MPI;
import mpi.Request;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rlaubscher on 26.11.16.
 */
public class IdleListener extends Thread {
  private boolean running = true;
  private int[] no_data = new int[0];
  private int slave;
  private LinkedBlockingQueue<Integer> idleWorkers;

  private static final long WAIT_TIME = 5;

  public IdleListener(int slave, LinkedBlockingQueue<Integer> idleWorkers) {
    this.slave = slave;
    this.idleWorkers = idleWorkers;
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
    Request request = MPI.COMM_WORLD.Irecv(this.no_data, 0, 1, MPI.INT, this.slave, Master.IDLE_TAG);

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
      idleWorkers.put(this.slave);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void stopListening() {
    this.running = false;
  }
}
