package cache;

import bus.Transaction;
import bus.Transaction.Type;
import cache.Cache.CacheLine;

public class MESI extends AbstractCacheController {
	public MESI(int id, String file, int cacheSize, int associativity, int blockSize) throws Exception {
		super(id, file, cacheSize, associativity, blockSize);
	}

	@Override
	public void unstall(boolean shared) {
		if (unstallAddress == -1) { // After eviction
			processor.setStall(false);
			stalled = false;
			return;
		}

		// IF it wasn't an eviction, try to update
		int index = cache.contains(unstallAddress);
		String unstallState = "";
		if (unstallAction == "PrRd")
			unstallState = shared ? "S" : "E";
		else
			unstallState = "M";
		if (index >= 0) {
			cache.update(unstallAddress, index, unstallState);
		} else {
			CacheLine rem = cache.add(unstallAddress, unstallState);
			if (rem.getState() == "M") { // need to evict
				bus.addTransaction(new Transaction(Type.Evict, id, unstallAddress));
				unstallAddress = -1;
				return;
			}
		}
		unstallAddress = 0;
		unstallAction = "";
		wrPending = false;
		processor.setStall(false);
		stalled = false;

	}

	@Override
	public boolean snoop(Transaction t) {
		boolean shared = false;
		int index = cache.contains(t.getAddress());
		if (index >= 0) {
			switch (cache.getState(t.getAddress(), index)) {
			case "M":
			case "E":
				switch (t.getT()) {
				case BusRd:
					cache.update(t.getAddress(), index, "S");
					canProvide = true;
					break;
				case BusRdX:
					cache.update(t.getAddress(), index, "I");
					canProvide = true;
					break;
				default:
					break;
				}
				shared = true;
				break;

			case "S":
				switch (t.getT()) {
				case BusRdX:
					cache.update(t.getAddress(), index, "I");
					break;

				default:
					break;
				}
				shared = true;
				break;
			case "I":
				break;
			default:
				break;
			}
		}
		return shared;
	}

	@Override
	public void prRd(int address) {
		int index = cache.contains(address);
		// if contained, check status
		if (index >= 0) {
			switch (cache.getState(address, index)) {
			case "I":
				miss++;
				rdMiss(address);
				break;

			case "S":
				hit++;
				publicAccess++;
				cache.update(address, index, "S");
				break;

			case "E":
				hit++;
				privateAccess++;
				cache.update(address, index, "E");
				break;
			case "M":
				hit++;
				privateAccess++;
				cache.update(address, index, "M");
				break;

			default:
				break;
			}
		} else { // cold miss
			miss++;
			rdMiss(address);
		}
	}

	private void rdMiss(int address) {
		bus.addTransaction(new Transaction(Type.BusRd, id, address));
		stall();
		unstallAddress = address;
		unstallAction = "PrRd";
	}

	@Override
	public void prWr(int address) {
		wrNumber++;
		int index = cache.contains(address);
		if (index >= 0) {
			switch (cache.getState(address, index)) {
			case "S":
				publicAccess++;
				hit++;
				rdX(address);
				break;
			case "I":
				miss++;
				rdX(address);
				break;

			case "M":
			case "E":
				privateAccess++;
				wrWaiting++;
				hit++;
				cache.update(address, index, "M");
				break;

			default:
				break;
			}
		} else { // cold miss
			miss++;
			rdX(address);
		}

	}

	private void rdX(int address) {
		bus.addTransaction(new Transaction(Type.BusRdX, id, address));
		stall();
		unstallAddress = address;
		unstallAction = "PrWr";
		wrPending = true;
	}
}
