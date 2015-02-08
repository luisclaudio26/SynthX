package synthExperiments;

import java.util.ArrayList;

public class LinearFilter extends Signal {

	/**
	 * Filter's frequency response
	 */
	private Signal frequencyResponse;
	
	//------------------------------------------------------------------------------------
	//--------------------------- Constructors -------------------------------------------	
	//------------------------------------------------------------------------------------
	public LinearFilter(int filterLength, int sampleRate) {
		super.setSampleRate(sampleRate);
		
		ArrayList<Double> freqResp = new ArrayList<Double>(filterLength);
		for(int i = 0; i < filterLength; i++) freqResp.add(0.0);
		
		this.frequencyResponse = new Signal(freqResp, sampleRate);
	}
	
	public LinearFilter() {
		this(32, 8000);
	}
	
	//------------------------------------------------------------------------------------
	//--------------------------- Access methods -----------------------------------------	
	//------------------------------------------------------------------------------------

	/**
	 * A deep copy of the table with frequency response of the filter.
	 * No references, so no hacking here ;)
	 * @return A clone of the signal with frequency response of this filter
	 */
	public Signal getFreqResponse() {
		if(this.frequencyResponse == null) return null;
		return frequencyResponse.clone();
	}
	
	/**
	 * Sets sample rate. Requires recalculating all the filter (which is a quadratic operation).
	 */
	@Override
	public void setSampleRate(int s) { 
		super.setSampleRate(s);
		this.calculateFilter();
	}
	
	//------------------------------------------------------------------------------------
	//--------------------------- Operations ---------------------------------------------	
	//------------------------------------------------------------------------------------
	public void addBand(double lowCutoff, double highCutoff, double gain) {
		for(double i = lowCutoff; i <= highCutoff; i += frequencyResponse.getFrequencyPerBucket()) {
			frequencyResponse.addBucket( frequencyResponse.getClosestBucket(i), gain);
			frequencyResponse.addBucket( frequencyResponse.getSymmetricalBucket(i), gain);
		}
		
		this.calculateFilter();
	}
	
	/**
	 * Gets a signal as parameter and returns the filtered signal
	 * @param s The signal which will be filtered
	 * @return The signal s after being filtered
	 */
	public Signal filter(Signal s) throws Exception {
		
		if(this.table.size() == 0)
			this.calculateFilter();
		
		//Convolve impulse response with operand
		return s.convolve(this);
	}
	
	/**
	 * Calculates impulse response based on frequency response
	 */
	protected void calcImpulseResponse() {
		frequencyResponse.iFFT().cloneSamples( this.table );
	}
	
	/**
	 * Permutates the halves of impulse response table. This is
	 * equivalent to Scilab's FFTSHIFT.
	 * Examples:
	 * [1 2 3 4 5 6] -> shiftHalves() -> [4 5 6 1 2 3]
	 * [1 2 3 4 5] 	 -> shiftHalves() -> [3 4 5 1 2]
	 */
	public void shiftHalves() {
		
		/*
		 * This problem is not so simples as it seems (or at least
		 * I'm not seeing something that should be obvious). Here's
		 * what we do:
		 * 
		 * 1) If number of samples is EVEN: run through vector until
		 * 		size()/2 exchanging element i with i + Half (n/2 exchanges)
		 * 2) If number of samples is ODD: put MIDDLE element in first position
		 * 		and shift the others one position to the right (N/2 exchanges).
		 * 		Shift halves now from element 1 as it was even. This will take
		 * 		n/2 exchanges. Total: n exchanges
		 */
		int half = size() / 2;
		int begin = 0;
		
		//If size is odd, put middle element in the beginning
		if( size() % 2 == 1) {
			double middle = table.get(half);
			for(int i = half; i > 0; i--)
				table.set(i, table.get(i-1) );
			table.set(0, middle);
			
			begin = 1;
		}
		
		//i = Zero if size is EVEN, = 1 if it is ODD 
		for(int i = begin; i < half + begin; i++) {
			double aux = table.get(i);
			table.set(i, table.get(i + half) );
			table.set(i + half, aux);
		}
	}
	
	/**
	 *  Recalculate filter
	 */
	protected void calculateFilter() {
		calcImpulseResponse();
		shiftHalves();
	}
	
	public static void main(String[] args) throws Exception {
		
		LinearFilter lf = new LinearFilter(32, 8000);
		lf.addBand(250, 500, 1.0);
		
		//System.out.println( "Filter coefficients: " + lf.toString() );
		
		Signal s = new SineSynth(8000).generate(1, 300);
		s.add( new SineSynth(8000).generate(1.0, 1000) );
		
		System.out.println(lf.toString(true));
		
		//System.out.println(lf.filter(s).toString(true));
		
		
		//System.out.println(s.toString(true));
		
		return;
	}
}
