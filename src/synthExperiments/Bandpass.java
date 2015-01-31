package synthExperiments;

public class Bandpass extends LinearFilter {

	/**
	 * We consider the cutoff inherited from LinearFilter as the inferior
	 * cutoff frequency. We add the superior limit as attribute, then.
	 */
	protected double cutoffSup;
	
	//------------------------------------------------------------------------------------
	//--------------------------- Access methods -----------------------------------------	
	//------------------------------------------------------------------------------------
	public double 	getCutoffSup() 			{ return cutoffSup; }
	public void 	setCutoffSup(double d) 	{ this.cutoffSup = d; }
	
	public double 	getCutoffInf()			{ return getCutoff() ; }
	public void		setCutoffInf(double d)	{ setCutoff(d); }
	
	//------------------------------------------------------------------------------------
	//--------------------------- Method overriding---------------------------------------	
	//------------------------------------------------------------------------------------
	/**
	 * sampleRate-cutoffSup and sampleRate-cutoff...performance issues?
	 */
	@Override
	protected boolean isFrequencyInRange(double freq) {
		return (freq >= cutoff && freq <= cutoffSup)
				&& (freq >= sampleRate-cutoffSup && freq <= sampleRate-cutoff);
	}
	
	/**
	 * We override this method just to add extra security "clauses"
	 */
	@Override
	protected void calcFrequencyResponse() throws Exception {
		if(cutoffSup >= sampleRate/2) {
			cutoffSup = sampleRate/2;
			throw new Exception("Superior cutoff frequency is above sampleRate / 2.");
		}
		super.calcFrequencyResponse();
	}
	
	/**
	 * We override this method just to add extra security "clauses"
	 */
	@Override
	public Signal filter(Signal s) throws Exception {
		if(cutoffSup < 0.0)
			throw new Exception("Bad value for superior cutoff frequency (< 0.0 )");
		return super.filter(s);
	}
	
	public static void main(String[] args) {
		
	}


}
