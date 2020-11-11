/**
 * 
 */
package net.npe.dds.util;

/**
 * @author danielsenff
 *
 */
public class Stopwatch {

	private long start;
	private long stop;
	
	public void start() {
		this.start = System.currentTimeMillis();
	}
	
	public void stop() {
		this.stop = System.currentTimeMillis();
	}
	
	public void reset() {
		this.stop = 0;
		this.start = 0;
	}
	
	public long getMilliseconds() {
		return stop - start;
	}
	
	public void printMilliseconds() {
		System.out.println((stop - start)+ " ms");
	}
	
	public void printMilliseconds(final String message) {
		System.out.println(message + (stop - start) + " ms");
	}
}
