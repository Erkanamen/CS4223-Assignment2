package cache;

import bus.Bus;
import processor.Processor;

public abstract class AbstractCacheController implements CacheController {
	protected final Processor processor;
	protected final Cache cache;
	protected boolean stalled;
	protected Bus bus;
	protected boolean canProvide;
	protected int id;
	protected int unstallAddress;
	protected String unstallAction;
	protected int hit;
	protected int miss;
	protected int privateAccess;
	protected int publicAccess;
	protected int wrNumber;
	protected int wrWaiting;
	protected boolean wrPending;

	public AbstractCacheController(int id, String file, int cacheSize, int associativity, int blockSize)
			throws Exception {
		this.id = id;
		processor = new Processor(id, file);
		cache = new Cache(cacheSize, blockSize, associativity, "I");
		stalled = false;
		canProvide = false;
		unstallAddress = 0;
		unstallAction = "";
		wrPending = false;
		hit = 0;
		miss = 0;
		privateAccess = 0;
		publicAccess = 0;
		wrNumber = 0;
		wrWaiting = 0;
	}

	public void nextTick() {
		if (done())
			return;
		if (!stalled) {
			processor.nextTick(this);
		}
		if (wrPending) wrWaiting++;
		//System.out.println(processor);
	}
	
	public boolean done() {
		return processor.done();
	}
	
	public boolean canProvide(int address) {
		boolean old = canProvide;
		canProvide = false;
		return old;
	}

	public void stall() {
		processor.setStall(true);
		stalled = true;
	}

	public void connectBus(Bus b) {
		bus = b;
	}

	public double missRate() {
		double total = hit + miss;
		return 100 * miss / total;
	}
	
	public int getHit() {
		return hit;
	}
	
	public int getMiss() {
		return miss;
	}
	
	public int getPrivateAccess() {
		return privateAccess;
	}

	public int getPublicAccess() {
		return publicAccess;
	}

	public int getwrNumber() {
		return wrNumber;
	}
	
	public int getwrWaiting() {
		return wrWaiting;
	}
	
	public void close() {
		processor.close();
	}
}
