package synthExperiments;

/**
 * This class provides an abstract interface to synthesize signals with
 * various waveforms. Extend it and modify method s(double t) so to have the desired
 * waveform.
 * */
public abstract class Synthesizer {
	
	/**
	 * Sample frequency: this is used as reference so we can know
	 * what length our tables/waveforms must have. For example: a signal
	 * of frequency = 1 Hz must have one cycle per second. If it is
	 * sampled with a frequency of 8 kHz, in 1 second we have 8000 samples
	 * that must "draw" only one cycle of the waveform with these 8000 samples.
	 * Hence, having a "table" of 8000 samples is necessary to correctly (?) draw it.
	 * Obviously, we don't really need to have a table at all: to draw a square wave,
	 * for example, it is enough to check whether we wanna sample a point
	 * in range 0 < k < 4000 [mod 8000] (first half) or in the other half.
	 */
	protected int samplingFreq;
	
	
	//---------------------------------------------------------------------------
	//------------------------------ Constructors -------------------------------
	//---------------------------------------------------------------------------
	public Synthesizer(int samplingFreq) {
		this.samplingFreq = samplingFreq;
	}
	
	public Synthesizer() { this(-1); }
	
	//---------------------------------------------------------------------------
	//------------------------------ Access methods -----------------------------
	//---------------------------------------------------------------------------
	public int getSamplingFreq() 		{ return samplingFreq; }
	public void setSamplingFreq(int f) 	{ if(f > 0) this.samplingFreq = f; }
	
	//---------------------------------------------------------------------------
	//------------------------------ Operators--- -------------------------------
	//---------------------------------------------------------------------------
	protected abstract double s(double t);
	
	/**
	 * Returns a signal with the desired waveform and frequency.
	 * Here, we pass integer values to the function s(double), which evaluates
	 * it in the convenient manner: for some functions, is it better to deal
	 * with time values, so we do i/samplingFreq; for others, it's better to
	 * deal with integer values (for example, when we deal with tables describing
	 * wave shapes).
	 * 
	 * @param duration Duration of the signal 
	 * @param frequency Frequency of the signal
	 * @return A signal with wave form defined by s(double), with the specified duration and frequency
	 */
	public Signal generate(double duration, double frequency) throws Exception {
		
		//Check for Shannon's constraint
		if(samplingFreq < 2*frequency)
			throw new Exception("Synthesizer does not respect Shannon's constraint");
		
		int nSamples = (int)Math.ceil(duration * samplingFreq);
		double[] table = new double[nSamples];
		
		for(int i = 0; i < nSamples; i++)
			table[i] = s( i * frequency );
		
		//Not that beautiful
		Signal out = new Signal(table); out.setSampleRate( samplingFreq );
		return out;
	}
}
