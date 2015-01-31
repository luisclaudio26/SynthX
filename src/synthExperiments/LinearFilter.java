package synthExperiments;

public abstract class LinearFilter extends Signal {

	/**
	 * Filter's frequency response
	 */
	private Signal frequencyResponse;
	
	/**
	 * All frequencies above/below this (inclusive) will be attenuated
	 */
	protected double cutoff;
	
	/**
	 * Number of samples our filter will have
	 */
	protected int filterLength;
	
	/**
	 * Signal's passing band gain
	 */
	protected double gain;
	
	//------------------------------------------------------------------------------------
	//--------------------------- Constructors -------------------------------------------	
	//------------------------------------------------------------------------------------
	public LinearFilter(double cutoff, int filterLength, double gain, int sampleRate) {
		super.setSampleRate(sampleRate);
		this.cutoff = cutoff;
		this.filterLength = filterLength;
		this.gain = gain;
		this.frequencyResponse = null;
	}
	
	public LinearFilter() {
		this(-1.0, 0, 0, 8000);
	}
	
	//------------------------------------------------------------------------------------
	//--------------------------- Access methods -----------------------------------------	
	//------------------------------------------------------------------------------------
	public double getCutoff() 		{ return this.cutoff; }
	public int getFilterLength() 	{ return this.filterLength; }
	public double getGain() 		{ return this.gain; }
	
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
	 * Sets gain. Requires recalculating all the filter (which is a quadratic operation).
	 */
	public void setGain(double gain) {
		if(gain >= 0.0) this.gain = gain;
		else return;
		
		this.calculateFilter();
	}
	
	/**
	 * Sets cutoff frequency. Requires recalculating all the filter (which is a quadratic operation).
	 */
	public void setCutoff(double cutoff) { 
		if(cutoff >= 0.0) this.cutoff = cutoff; 
		else return;
		
		this.calculateFilter();
	}
	
	/**
	 * Sets filter length. Requires recalculating all the filter (which is a quadratic operation).
	 */
	public void setFilterLength(int N) { 
		if(N > 0) this.filterLength = N;
		else return;
		
		this.calculateFilter();
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
	
	/**
	 * Gets a signal as parameter and returns the filtered signal
	 * @param s The signal which will be filtered
	 * @return The signal s after being filtered
	 */
	public Signal filter(Signal s) throws Exception {
		if(cutoff < 0)
			throw new Exception("Bad cutoff frequency ( < 0.0 )");
		
		if(frequencyResponse == null)
			this.calculateFilter();
		
		//Convolve impulse response with operand
		return s.convolve(this);
	}
	
	/**
	 * Builds filter's frequency response based on parameters stored
	 */
	protected void calcFrequencyResponse() throws Exception {
		
		//Shannon's constraint
		if(cutoff > this.sampleRate/2) {
			cutoff = this.sampleRate/2; //Truncate
			throw new Exception("Filter does not respect Shannon's constraint! Cutoff frequency is too high.");
		}
		
		double[] freqTable = new double[this.filterLength];
		double f = (double)sampleRate / filterLength;

		for(int i = 0; i < filterLength; i++) {
			double freq = f * i;
			freqTable[i] = isFrequencyInRange(freq) ? gain : 0.0;
		}
		
		frequencyResponse = new Signal(freqTable);
	}
	
	/**
	 * Code here must answer to the question: when is a certain frequency set to ZERO or set
	 * to GAIN? For example, for a high pass filter: freq >= cutoff (&& freq <= sampleRate-cutoff).
	 * @param freq The frequency we want to check.
	 * @return TRUE or FALSE, whether this frequency is in filter range or not.
	 */
	protected abstract boolean isFrequencyInRange(double freq);
	
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
		try {
			calcFrequencyResponse();
		} catch(Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		
		calcImpulseResponse();
		shiftHalves();
	}
}
