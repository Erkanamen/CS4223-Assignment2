package cache;

import bus.Bus;
import bus.Transaction;

public interface CacheController {
	// Application context
	public void nextTick();
	public boolean done();
	public void stall();
	
	// Bus context
	public void connectBus(Bus b);
	public boolean snoop(Transaction t);
	public boolean canProvide(int address);
	public void unstall(boolean shared);
	
	// Processor context
	public void prRd(int address);
	public void prWr(int address);
	
	// Statistics
	public double missRate();
	public int getHit();
	public int getMiss();
	public int getPrivateAccess();
	public int getPublicAccess();
	public int getwrNumber();
	public int getwrWaiting();
}
