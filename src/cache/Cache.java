package cache;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Cache {
	public static class CacheLine {
		private String state;
		private int tag;

		public CacheLine(String state, int tag) {
			this.state = state;
			this.tag = tag;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public int getTag() {
			return tag;
		}

		public void setTag(int tag) {
			this.tag = tag;
		}
	}

	public static class CacheBlock {
		private final int size;
		private final LinkedList<Integer> LRU;
		private final List<CacheLine> block;

		public CacheBlock(int s, String defaultState) {
			size = s;
			LRU = new LinkedList<>();
			block = new ArrayList<>(size);
			for (int i = 0; i < size; ++i) {
				LRU.add(i);
				block.add(new CacheLine(defaultState, 0));
			}
		}

		public int contains(int tag) {
			int index = -1;
			for (int i = 0; i < size; ++i) {
				if (block.get(i).getTag() == tag) {
					index = i;
					break;
				}
			}
			return index;
		}

		public CacheLine add(CacheLine l) {
			int index = -1;
			for (int j = 0; j < size; ++j) {
				if (block.get(j).getState() == "I") {
					index = j;
					break;
				}
			}
			if (index >= 0) LRU.removeFirstOccurrence(index);
			else index = LRU.removeLast();
			CacheLine removed = block.get(index);
			block.set(index, l);
			LRU.addFirst(index);
			return removed;
		}

		public String getState(int index) {
			return block.get(index).getState();
		}

		public void update(int index, String state) {
			LRU.removeFirstOccurrence(index);
			LRU.addFirst(index);
			block.get(index).setState(state);
		}
	}

	private final int size;
	private final List<CacheBlock> lines;
	private final int blockBits;
	private final int indexBits;

	public Cache(int allowedMemory, int blockSize, int associativity, String defaultState) {
		size = allowedMemory / (blockSize * associativity);
		blockBits = (int) (Math.log(blockSize) / Math.log(2));
		indexBits = (int) (Math.log(size) / Math.log(2));
		//System.out.println("size : " + size + " blockbits : " + blockBits + " indexBits :" + indexBits);
		lines = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			lines.add(new CacheBlock(associativity, defaultState));
		}
	}

	public int contains(int address) {
		return lines.get(AddressToIndex(address)).contains(AddressToTag(address));
	}

	public CacheLine add(int address, String state) {
		return lines.get(AddressToIndex(address)).add(new CacheLine(state, AddressToTag(address)));
	}

	public String getState(int address, int index) {
		return lines.get(AddressToIndex(address)).getState(index);
	}
	
	public void update(int address, int index, String state) {
		lines.get(AddressToIndex(address)).update(index, state);
	}

	public int AddressToIndex(int address) {
		int up = address << (32 - (indexBits + blockBits));
		return up >>> 32 - indexBits;
	}

	public int AddressToTag(int address) {
		return address >>> indexBits + blockBits;
	}
}
