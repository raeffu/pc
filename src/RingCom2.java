import java.io.Serializable;

import mpi.*;
public class RingCom2 {
	public static void main(String[] args) {
		
		int rank, size;
		MPI.Init(args);
		double startTime = MPI.Wtime();
 		size = MPI.COMM_WORLD.Size();
		rank = MPI.COMM_WORLD.Rank();
		int [] recBuf = new int [1];
		int [] sndBuf = new int [1];
		int sum = rank;
		sndBuf[0]=rank;
		if (size>1){
			// beginne mit meiner ID
			sndBuf[0]=rank;
			// mehrere Runden:
			while(true){
				// Der folgende Auffruf blockiert nicht:
				Request req = MPI.COMM_WORLD.Irecv(recBuf,0,1, MPI.INT,(rank+size-1) % size, 1);
				// Der folgende Aufruf blockiert
				MPI.COMM_WORLD. Ssend(sndBuf,0, 1, MPI.INT,(rank+1)%size, 1);
				req.Wait(); // erst weiter fahren wenn ich empfangen habe
				if (recBuf[0]==rank ) break; // eine Runde vollständig!
					sum += recBuf[0];
				// erhaltene ID umkopieren und weitersenden:
				sndBuf[0]=recBuf[0];
			}
		}
		System.out.println("sum:"+sum+", rank: "+rank);
		MPI.Finalize();
	}
}
