package bus;

public class BusConstant {
	public final int MISS = 99;
	public final int EVICTION = 99;
	public final int UPDATE = 0;
	public final int BLOCKUPDATE;
	public final int BLOCKSIZE;
	public final int WORDSIZE = 4;
	
	public BusConstant(int blockUpdate, int blockSize) {
		BLOCKUPDATE = blockUpdate-1;
		BLOCKSIZE = blockSize;
	}
}
