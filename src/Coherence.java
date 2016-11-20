import java.util.ArrayList;
import java.util.List;

import bus.Bus;
import bus.BusConstant;
import cache.CacheController;
import cache.CacheFactory;

public class Coherence {
	private static final int WORD_SIZE = 4;
	private static final int BLOCK_SIZE = 16;
	private static final int CACHE_SIZE = 4096;
	private static final int ASSOCIATIVITY = 1;

	public static void main(String[] args) {
		int wordSize = WORD_SIZE;
		int blockSize = BLOCK_SIZE;
		int cacheSize = CACHE_SIZE;
		int associativity = ASSOCIATIVITY;

		// Parsing
		if (!(args.length == 2 || args.length == 5)) {
			System.err.println("Incorrect number of arguments : " + args.length);
			System.exit(-1);
		}

		String protocol = args[0];
		String inputFile = args[1];
		if (args.length > 2) {
			cacheSize = Integer.parseInt(args[2]);
			associativity = Integer.parseInt(args[3]);
			blockSize = Integer.parseInt(args[4]);
		}

		// Configuration
		System.out.println("--CONFIG--");
		System.out.println(protocol + " on " + inputFile);
		System.out.println("Caches : " + cacheSize + " allowed bytes, associativity : " + associativity
				+ ", block size : " + blockSize);

		List<CacheController> listC = new ArrayList<>();
		try {
			for (int i = 0; i < 4; ++i) {
				listC.add(CacheFactory.createCacheController(i, protocol, inputFile, cacheSize, associativity,
						blockSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Bus b = new Bus(listC, new BusConstant(blockSize / wordSize, blockSize));

		// Main Loop
		long counterCycle = 0;
		while (!checkDone(listC)) {
			counterCycle++;
			for (CacheController c : listC) {
				c.nextTick();
			}
			b.nextTick();
		}

		// Close all
		for (CacheController c : listC) {
			c.close();
		}
		counterCycle--; // Remove last cycle where we just checked that it was
						// finished
		// Statistics
		int cont = 0;
		int publicAccess = 0, privateAccess = 0, wrNumber = 0, wrWaiting = 0;
		System.out.println("--STATISTICS--");
		for (CacheController c : listC) {
			System.out.println("Core #" + cont++ + " Miss rate : " + c.missRate());
			System.out.println("Hit : " + c.getHit() + ", Miss : " + c.getMiss());
			publicAccess += c.getPublicAccess();
			privateAccess += c.getPrivateAccess();
			wrNumber += c.getwrNumber();
			wrWaiting += c.getwrWaiting();
		}
		System.out.println("Total Statistics :");
		double pubAccesPercentage = publicAccess * 100. / (publicAccess + privateAccess);
		System.out.println("Public Access (" + pubAccesPercentage + "%): " + publicAccess);
		System.out.println("Private Access (" + (100 - pubAccesPercentage) + "%) : " + privateAccess);
		System.out.println("Avg Write Latency : " + 1. * wrWaiting / wrNumber + " cycles");
		b.statistics();
		System.out.println("Done in " + counterCycle + " cycles");
	}

	private static boolean checkDone(List<CacheController> l) {
		boolean done = true;
		for (CacheController c : l) {
			if (!c.done()) {
				done = false;
				break;
			}
		}
		return done;
	}
}
