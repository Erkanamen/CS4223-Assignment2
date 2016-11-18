package processor;

import java.io.FileNotFoundException;

import cache.CacheController;
import processor.TraceReader.Command;

public class Processor {
	private final TraceReader trace;
	private boolean stall, done;
	private int counter;
	private int instructionCounter;
	private int id;
	
	public Processor(int id, String file) throws FileNotFoundException {
		trace = new TraceReader(file + "_" + id);
		stall = false;
		done = false;
		counter = 0;
		instructionCounter = 0;
		this.id = id;
	}
	
	public void test() {
		Command m = trace.getNextCommand();
		 while (m.getType() != 3) {
			//System.out.println(m.getType() + " " + m.getMessage());
			m = trace.getNextCommand();
		}
	}
	
	public void setStall(boolean s) {
		//System.out.println("core #" + id + " set to " + s);
		stall = s;
	}
	
	public boolean done() {
		return done;
	}
	
	public void nextTick(CacheController c) {
		if (!stall && counter == 0) {
			instructionCounter++;
			Command m = trace.getNextCommand();
			switch (m.getType()) {
			case 0:
				c.prRd(Integer.parseInt(m.getMessage().substring(2), 16));
				break;
			case 1:
				c.prWr(Integer.parseInt(m.getMessage().substring(2), 16));
				break;
			case 2:
				counter = Integer.parseInt(m.getMessage().substring(2), 16);
				break;
			case 3:
				done = true;
				break;
			default:
				break;
			}
		}
		else if (!stall && counter > 0) {
			--counter;
		}
	}
	
	@Override
	public String toString() {
		return "Core #" + id + " is stalled : " + stall + ", line :" + instructionCounter + ", remaining : " + counter;
	}
}
