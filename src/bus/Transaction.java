package bus;

public class Transaction {
	public static enum Type {
		BusRd, BusRdX, BusUpd, Evict
	}
	
	private final Type t;
	private final int from;
	private final int address;
	
	public Transaction(Type t, int from, int address) {
		this.t = t;
		this.from = from;
		this.address = address;
	}

	public Type getT() {
		return t;
	}

	public int getFrom() {
		return from;
	}

	public int getAddress() {
		return address;
	}	
}
