package cache;

public class CacheFactory {
	private CacheFactory() {

	}

	public static CacheController createCacheController(int id, String protocol, String file, int cacheSize,
			int associativity, int blockSize) throws Exception {
		switch (protocol) {
		case "MSI":
			return new MSI(id, file, cacheSize, associativity, blockSize);
		case "MESI":
			return new MESI(id, file, cacheSize, associativity, blockSize);
		case "Dragon":
		case "dragon":
			return new Dragon(id, file, cacheSize, associativity, blockSize);
		case "MOESI":
			return new MOESI(id, file, cacheSize, associativity, blockSize);
		default:
			return null;
		}
	}
}
