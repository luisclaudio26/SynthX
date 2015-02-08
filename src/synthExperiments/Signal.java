package synthExperiments;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.*;

public class Signal {

	protected ArrayList<Double> table;
	protected int 				sampleRate;
	
	//------------------------------------------------------------------------------------
	//--------------------------- Access methods -----------------------------------------	
	//------------------------------------------------------------------------------------
	
	/**
	 * O(n) method. Avoid it if it is possible!
	 */
	public double at(int i) {
		if(i >= table.size() || i < 0) return Double.NaN;
		return table.get(i);
	}
	
	public int 	size() 						{ return table.size(); }
	public int 	getSampleRate() 			{ return sampleRate; }
	public void setSampleRate(int s) { 
		if(s > 0) sampleRate = s; 
		else return; 
	}
	
	/**
	 * Copies values to extern table.
	 * It doesn't returns a cloned table because if we want to copy the values
	 * to another signal we would have to do two loops (one for cloning the table,
	 * another time to add the values to the other signal).
	 * @param target Values will be copied to this location.
	 */
	public void cloneSamples(ArrayList<Double> target) {
		if(target == null) return;
		
		for( Iterator<Double> it = table.iterator(); it.hasNext(); )
			target.add(it.next());
	}
	
	/**
	 * Get bucket's associated frequency
	 * @param n Bucket's index
	 * @return Bucket's frequency. Returns NaN if invalid index is provided.
	 */
	public double getBucketsFrequency(int n) {
		if(n < 0 || n >= size()) return Double.NaN;
		return (double)sampleRate * n/ size();
	}
	
	/**
	 * Returns the bucket whose frequency is the closest to f. If one needs do know exactly
	 * what is that frequency, use getBucketsFrequency() together with this function.
	 * @param f The desired frequency
	 * @return The bucket. -1 if negative frequency is provided. 
	 */
	public int getClosestFrequency(double f) {
		if(f < 0) return -1;
		return (int)( f * size() / sampleRate );
	}
	
	//------------------------------------------------------------------------------------
	//--------------------------- Constructors -------------------------------------------	
	//------------------------------------------------------------------------------------
	public Signal(ArrayList<Double> table, int sampleRate) {
		this.table = table;
		this.sampleRate = sampleRate;
	}
	
	/**
	 * Sets default sample rate to 8000 Hz and creates empty table
	 */
	public Signal() {
		this(new ArrayList<Double>(), 8000);
	}
	
	/**
	 * Gets an array of double as parameter that will be stored internally
	 * @param signal An array of double which will be stored in this signal
	 */
	public Signal(double[] signal) {
		this();
		
		for(double d: signal)
			table.add(d);
	}
	
	//------------------------------------------------------------------------------------
	//--------------------------- Operations ---------------------------------------------	
	//------------------------------------------------------------------------------------
	/**
	 * Naïve implementation of Fast Fourier Transform, i. e., directy implementation of FFT's
	 * definition.
	 */
	protected Signal fastFourierTransform(boolean inverse) {
		ArrayList<Double> out = new ArrayList<>();
		int N = table.size();
		
		double theta = (inverse ? 1 : -1) * 2 * Math.PI / N;
		
		//Each bucket represents the amplitude of the component with frequency k/N Hz
		for(int k = 0; k < N; k++) 
		{
			double sumReal = 0, sumIm = 0;
			double thetaK = theta * k;
			
			for(int i = 0; i < N; i++) {
				double arg = thetaK * i;
				double mod = table.get(i);
				
				sumReal += mod*Math.cos(arg);
				
				if(!inverse)
					sumIm += mod*Math.sin(arg);
			}
			
			double res;
			if(inverse)
				res = sumReal / N;
			else
				res = Math.sqrt(sumReal*sumReal + sumIm*sumIm);
			out.add( res );
		}
		
		return new Signal( out, this.sampleRate );
	}
	
	
	public Signal fft() {
		return fastFourierTransform(false);
	}
	
	public Signal iFFT() {
		return fastFourierTransform(true);
	}
	
	/**
	 * Convolves This signal with S. The result is stored in This.
	 * @param S Our "operand".
	 */
	public Signal convolve(Signal S) {
		
		ArrayList<Double> out = new ArrayList<>();
		
		for(int i = 0; i < table.size(); i++) {
			double sum = 0;
			
			for(int k = 0; k < S.size(); k++)
			{
				if(k > i) break;
				sum += S.at(k) * table.get(i-k);
			}
			
			out.add(sum);
		}
		
		return new Signal( out, this.sampleRate );
	}
	
	/**
	 * Write signal to wave file
	 * @param filepath File to save
	 * @param size Number of bytes we'll use to sample each point.
	 */
	public void writeToWAVE(String filepath, SampleSize size) {
		WAVEWriter writer = new WAVEWriter(sampleRate, size, 1);
		
		for(int i = 0; i < table.size(); i++)
			writer.addSample(table.get(i));
		
		try {
			writer.writeWAVE(filepath);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Multiply element per element. Works in similar way to Add(), but we multiply the elements
	 * instead of add.
	 * @param S Operand
	 * @param offset Shift signal.
	 */
	public void multiplyElements(Signal S, int offset) {
		if(offset + S.size() > table.size()) return;
		for(int i = 0; i < table.size(); i++)
			table.set(i, table.get(i) * S.at(i));
	}
	public void multiplyElements(Signal S) { multiplyElements(S, 0); }
	
	/**
	 * Keep signal in the range [-1,1]
	 */
	public void normalize() {
		double max = max();
		for(int i = 0; i < table.size(); i++)
			table.set(i, table.get(i) / max);
	}
	
	/**
	 * Multiply signal by scalar quantity
	 * @param scalar Scalar quantity
	 */
	public void scalarMultiply(double scalar) {
		for(int i = 0; i < table.size(); i++)
			table.set(i, table.get(i) * scalar);
	}
	
	/**
	 * Signal = Signal + S.
	 * Adds a signal to another. This will cause values to go above 1 or
	 * under -1! Must normalize after operation
	 * @param S Operand
	 * @param offset An offset to shift the operand. FOr example, if we have offset = 5, the first
	 * 5 positions will not be affected. Note that offset + operand's length must not be greater
	 * then Signal length.
	 */
	public void add(Signal S, int offset) {
		if(S.size() + offset > table.size()) return;
		for(int i = 0; i < table.size(); i++)
			table.set(offset+i, table.get(offset+i) + S.at(i));
	}
	public void add(Signal S) { add(S, 0); }
	
	/**
	 * Get standards deviation of the samples in the signal. Useful
	 * for evaluating signal-noise ratio.
	 * @return Standard deviation of samples.
	 */
	public double stdDev() {
		double mean = mean();
		double acc = 0;
		for(int i = 0; i < table.size(); i++)
			acc += Math.pow(mean-table.get(i), 2);
		return Math.sqrt(acc/(table.size()-1));
	}
	
	/**
	 * Returns the sample with greatest ABSOLUTE value.
	 */
	public double max() {
		double max = Double.NEGATIVE_INFINITY;
		for(int i = 1; i < table.size(); i++)
			if(table.get(i) > max) max = table.get(i);
		return max;
	}
	
	protected double mean() {
		double acc = 0.0;
		for(int i = 0; i < table.size(); i++)
			acc += table.get(i);
		return acc/table.size();
	}
	
	/**
	 * Forces This signal to be of length N by adding Zeros in the end.
	 * @param N Final length of signal
	 */
	public void padWithZeros(int N) {
		int nZeros = N - table.size();
		if(nZeros <= 0) return;
		
		for(int i = 0; i < nZeros; i++)
			table.add(0.0);
	}
	
	/**
	 * Returns a deep copy of this signal, i.e., no references.
	 * This obviously run in O(n), so be careful while cloning stuff
	 * @return A signal identical to the one copied but with no references.
	 */
	public Signal clone() {
		return clone(table.size());
	}
	
	public Signal clone(int n) {
		
		if(n > table.size()) n = table.size();
		
		ArrayList<Double> samples = new ArrayList<Double>(n);
		
		for(int i = 0; i < n; i++)
			samples.add( this.table.get(i) );
		
		return new Signal( samples, this.sampleRate );
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean lineBreak)
	{
		StringBuilder sb = new StringBuilder();
		
		if(lineBreak)
			for(int i = 0; i < table.size(); i++)
				sb.append(i + " " + table.get(i) + "\n");
		else
			for(int i = 0; i < table.size(); i++)
				sb.append(table.get(i) + " ");
		
		return sb.toString();
	}
	
	public static void main(String[] args) throws Exception
	{
		int sampleFreq = 8000;
		int fftWindowSize = 32;
		
		Signal _0hz = new SineSynth(8000).generate(1.0, 799.99);
		
		//System.out.println("Signal: ");
		//System.out.println( _0hz.toString() );
		
		System.out.println("\n\nFFT: ");
		System.out.println( _0hz.clone(fftWindowSize).fft().toString() );
		
		/*
		 * Symmetrical frequency: fe-f
		 * Bucket of symmetrical frequency: floor( fe-f/fB )
		 * Where fB (frequency per bucket) is: fe/fftWindowSize
		 */
	}
}
