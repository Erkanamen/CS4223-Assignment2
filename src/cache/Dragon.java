package cache;

import bus.Transaction;
import bus.Transaction.Type;
import cache.Cache.CacheLine;

public class Dragon extends AbstractCacheController {
	public Dragon(int id, String file, int cacheSize, int associativity, int blockSize) throws Exception {
		super(id, file, cacheSize, associativity, blockSize);
	}

	@Override
	public void unstall(boolean shared) {
		if (unstallAddress == -1) { // After eviction
			processor.setStall(false);
			stalled = false;
			return;
		}

		String unstallState = "";
		if (unstallAction == "PrRdMiss") {
			unstallState = shared ? "Sc" : "E";
		}
		else if (unstallAction == "BusUpd") {
			unstallState = shared ? "Sm" : "M";
		}
		else if (unstallAction == "PrWrMiss" && shared) {
			generateBusUpd(unstallAddress);
			return;
		}
		else unstallState = "M";
		
		int index = cache.contains(unstallAddress);
		if (index >= 0) {
			cache.update(unstallAddress, index, unstallState);
		}
		else {
			CacheLine rem = cache.add(unstallAddress, unstallState);
			if (rem.getState() == "M" || rem.getState() == "Sm") { // need to evict
				bus.addTransaction(new Transaction(Type.Evict, id, unstallAddress));
				unstallAddress = -1;
				return;
			}
		}
		unstallAddress = 0;
		unstallAction = "";
		if (wrPending) {
			wrWaiting--;
			wrPending = false;
		}
		
		processor.setStall(false);
		stalled = false;
	}

	@Override
	public boolean snoop(Transaction t) {
		boolean shared = false;
		int index = cache.contains(t.getAddress());
		if (index >= 0) {
			switch (cache.getState(t.getAddress(), index)) {
			case "E":
				if (t.getT() == Type.BusRd)
					cache.update(t.getAddress(), index, "Sc");
				shared = true;
				break;

			case "Sc":
				if (t.getT() == Type.BusUpd)
					cache.update(t.getAddress(), index, "Sc");
				shared = true;
				break;

			case "Sm":
				switch (t.getT()) {
				case BusRd:
					cache.update(t.getAddress(), index, "Sm");
					canProvide = true;
					break;
				case BusUpd:
					cache.update(t.getAddress(), index, "Sc");
					break;

				default:
					break;
				}
				shared = true;
				break;

			case "M":
				if (t.getT() == Type.BusRd) {
					cache.update(t.getAddress(), index, "Sm");
					canProvide = true;
				}
				shared = true;
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
		if (index >= 0) {
			switch (cache.getState(address, index)) {
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
			case "Sc":
				hit++;
				publicAccess++;
				cache.update(address, index, "Sc");
				break;
			case "Sm":
				hit++;
				publicAccess++;
				cache.update(address, index, "Sm");
				break;

			default:
				break;
			}
		} else {
			miss++;
			rdMiss(address);
		}
	}

	private void rdMiss(int address) {
		bus.addTransaction(new Transaction(Type.BusRd, id, address));
		stall();
		unstallAddress = address;
		unstallAction = "PrRdMiss";
	}

	@Override
	public void prWr(int address) {
		wrNumber++;
		int index = cache.contains(address);
		if (index >= 0) {
			switch (cache.getState(address, index)) {
			case "Sc":
			case "Sm":
				publicAccess++;
				hit++;
				generateBusUpd(address);
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
		} else {
			miss++;
			prWrMiss(address);
		}
	}
	
	private void generateBusUpd(int address) {
		bus.addTransaction(new Transaction(Type.BusUpd, id, address));
		stall();
		unstallAddress = address;
		unstallAction = "BusUpd";
		wrPending = true;
	}
	
	private void prWrMiss(int address) {
		bus.addTransaction(new Transaction(Type.BusRd, id, address));
		stall();
		unstallAddress = address;
		unstallAction = "PrWrMiss";
		wrPending = true;
	}
}
