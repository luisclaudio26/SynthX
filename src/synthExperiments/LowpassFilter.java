package synthExperiments;

public class LowpassFilter extends LinearFilter {

	/**
	 * REMARK: This method will be called n times, where n is the filter length
	 * Here we calculate sampleRate-cutoff; is this little calculus a performance issue?
	 */
	@Override
	protected boolean isFrequencyInRange(double freq) {
		return freq <= cutoff && freq >= sampleRate - cutoff;
	}
}
