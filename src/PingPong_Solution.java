import mpi.MPI;

public class PingPong_Solution {
	static int buffer[] = new int[10000];
	static final int PING = 101;
	static final int PONG = 102;
	static final int PROC_A = 0;
	static final int PROC_B= 1;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int rank, size, i;
		MPI.Init(args);
		double startTime = MPI.Wtime();

		size = MPI.COMM_WORLD.Size();
		rank = MPI.COMM_WORLD.Rank();
		if (size == 2){
			if (rank==PROC_A) processor_A();
			else processor_B();
		}
		else System.out.println(" please run with '-n 2' ");
		MPI.Finalize();
	}
	
	static void processor_A(){
		double start, finish, time;
		int length;
		System.out.println("Length\tTotal Time\tTransfer Rate\n");
		for (length = 100; length <=  2000; length += 100){ 
			start = MPI.Wtime();
			for (int i=0;i<100;i++){
				MPI.COMM_WORLD.Send(buffer,0,length, MPI.INT, PROC_B, PING);
				MPI.COMM_WORLD.Recv( buffer, 0, length, MPI.INT, PROC_B, PONG);
			}
			finish = MPI.Wtime();
			time = finish - start;
			System.out.println(String.format("%d\t%f\t%f\n", length, time/200.,
					(float)(2 * 4 * 100 * length)/time));
		}
	}

	static void processor_B( ) {
	    for (int length = 100; length <= 2000; length += 100) { 
	    	for (int i=0;i<100;i++){
	    		MPI.COMM_WORLD.Recv(buffer, 0,length,MPI.INT, PROC_A, PING);
	    		MPI.COMM_WORLD.Send(buffer, 0,length,MPI.INT, PROC_A, PONG);
	    	}

	    }

	}
}
