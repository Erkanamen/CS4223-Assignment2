package cache;

import static org.junit.Assert.*;

import org.junit.Test;

import cache.Cache.CacheLine;

public class CacheTest {

	@Test
	public void AddressToIndexTest() {
		Cache c = new Cache(1024, 16, 1, "I");
		assertEquals(0x34, c.AddressToIndex(0xabc12340));
		
		Cache c2 = new Cache(2048, 16, 2, "I");
		assertEquals(0x34, c2.AddressToIndex(0xabc12340));
		
		Cache c3 = new Cache(8192, 16, 1, "I");
		// fffff340 = 1111 1111 1111 1111 1111 0011 0100 0000
		assertEquals(0x134, c3.AddressToIndex(0xfffff340));
	}
	
	@Test
	public void AddressToTagTest() {
		Cache c = new Cache(1024, 16, 1, "I");
		// 0xabc12340 = 1010 1011 1100 0001 0010 0011 0100 0000
		assertEquals(0x2AF048, c.AddressToTag(0xabc12340));
	}

	@Test
	public void emptyCacheCorrect() {
		Cache c = new Cache(1024, 16, 1, "I");
		assertEquals(-1, c.contains(0xabc12340));
		assertEquals(-1, c.contains(0xfffff340));
	}
	
	@Test
	public void cacheWithOneEntry() {
		Cache c = new Cache(1024, 16, 2, "I");
		assertEquals(-1, c.contains(0xabc12340));
		c.add(0xabc12340, "S");
		assertEquals(0, c.contains(0xabc12340));
		assertEquals("I", c.getState(0xabc12340, 1));
		assertEquals("S", c.getState(0xabc12340, 0));
		c.add(0xabc12340, "M");
		assertEquals("M", c.getState(0xabc12340, 1));
		assertEquals("S", c.getState(0xabc12340, 0));
		c.update(0xabc12340, 0, "I");
		assertEquals("I", c.getState(0xabc12340, 0));
		assertEquals("M", c.getState(0xabc12340, 1));
		CacheLine l = c.add(0xabc12340, "M");
		assertEquals("I", l.getState());
	}
}
