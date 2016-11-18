package bus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cache.CacheController;

public class Bus {
	private final Queue<Transaction> transactionQueue;
	private final List<CacheController> snoopers;
	private int waitCounter;
	private int servingId;
	private boolean shared;
	private final BusConstant cst;
	private long dataTraffic;
	private int invalidations;
	private int evictions;

	public Bus(List<CacheController> s, BusConstant c) {
		transactionQueue = new LinkedList<>();
		snoopers = s;
		for (CacheController cc : s) {
			cc.connectBus(this);
		}
		shared = false;
		waitCounter = 0;
		cst = c;
		servingId = -1;
		dataTraffic = 0;
		invalidations = 0;
		evictions = 0;
	}

	public void addTransaction(Transaction t) {
		transactionQueue.add(t);
	}

	public void nextTick() {
		if (waitCounter > 0) {
			waitCounter--;
			return;
		} else if (servingId >= 0) {
			snoopers.get(servingId).unstall(shared);
			shared = false;
			servingId = -1;
		}

		Transaction next = transactionQueue.poll();
		if (next != null) {
			process(next);
		}
	}

	private void process(Transaction t) {
		List<Integer> sharedList = new ArrayList<>();
		servingId = t.getFrom();
		for (int i = 0; i < snoopers.size(); ++i) {
			if (i != servingId && snoopers.get(i).snoop(t)) {
				shared = true;
				sharedList.add(i);
			}
		}

		boolean canProvide = false;
		switch (t.getT()) {
		case BusRd:
			for (int i : sharedList) {
				if (snoopers.get(i).canProvide(t.getAddress())) {
					canProvide = true;
					break;
				}
			}
			if (canProvide)
				waitCounter = cst.BLOCKUPDATE;
			else
				waitCounter = cst.MISS;
			dataTraffic += cst.BLOCKSIZE;
			break;
		case BusRdX:
			for (int i : sharedList) {
				if (snoopers.get(i).canProvide(t.getAddress())) {
					canProvide = true;
					break;
				}
			}
			if (canProvide)
				waitCounter = cst.BLOCKUPDATE;
			else
				waitCounter = cst.MISS;
			dataTraffic += cst.BLOCKSIZE;
			invalidations++;
			break;

		case Evict:
			waitCounter = cst.EVICTION;
			dataTraffic += cst.BLOCKSIZE;
			evictions++;
			break;

		case BusUpd:
			waitCounter = cst.UPDATE;
			dataTraffic += cst.WORDSIZE;
			invalidations++;
			break;
		default:
			break;
		}
	}

	public void statistics() {
		System.out.println("DataTraffic : " + dataTraffic + " bytes");
		System.out.println("#Invalidations : " + invalidations);
		System.out.println("#Evictions : " + evictions);
	}

	@Override
	public String toString() {
		String s = "";
		for (Transaction t : transactionQueue) {
			s += t.getT() + ": " + t.getFrom() + " " + t.getAddress() + "; ";
		}
		return s;
	}
}
