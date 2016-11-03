import java.io.Serializable;

import mpi.MPI;
import mpi.Request;

public class RingCom {
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
				// asynchrones senden 
				Request req = MPI.COMM_WORLD. Isend(sndBuf,0, 1, MPI.INT,(rank+1)%size, 1);
				// Der folgende Auffruf blockiert:
				MPI.COMM_WORLD.Recv(recBuf,0,1, MPI.INT,(rank+size-1) % size, 1);
				if (recBuf[0]==rank ) break; // eine Runde vollständig!
				//req.Wait(); // erst weiter fahren wenn letztes write ok.	
				sum += recBuf[0];
				// erhaltene ID umkopieren und weitersenden:
				sndBuf[0]=recBuf[0];
			}
		}
		System.out.println("sum:"+sum+", rank: "+rank);
		MPI.Finalize();
	}
}
