

import java.io.Serializable;

import mpi.*;

public class RingComObject {
	public static void main(String[] args) {
		
		int rank, size;
		MPI.Init(args);
		double startTime = MPI.Wtime();
 		size = MPI.COMM_WORLD.Size();
		rank = MPI.COMM_WORLD.Rank();
		ObjectBuffer [] objBuf = new ObjectBuffer [2];
		int sum = rank;
		objBuf[0]= new ObjectBuffer();
		objBuf[0].id = "Buffer "+rank;
		if (size>1){
			// beginne mit meiner ID
			objBuf[0].val = rank;
			// mehrere Runden:
			while(true){
				// asynchrones senden 
				Request req = MPI.COMM_WORLD.Isend(objBuf,0, 1, MPI.OBJECT,(rank+1)%size, 1);
				// Der folgende Auffruf blockiert:
				MPI.COMM_WORLD. Recv(objBuf,1,1, MPI.OBJECT,(rank+size-1) % size, 1);
				if (objBuf[1].val==rank ) break; // eine Runde vollständig!
				sum += objBuf[1].val;
				// erhaltene ID umkopieren und weitersenden:
				req. Wait(); // erst weiter fahren wenn letztes write ok.	
				objBuf[0]=objBuf[1];
			}
		}
		System.out.println("sum:"+sum+", rank: "+rank+" bufId: "+objBuf[0].id);
		MPI.Finalize();
	}
	
	
	// Achtung: das class file muss an alle nodes versendet werden
	static class ObjectBuffer implements Serializable{
		String id;
		int val; 
	}
	
}
