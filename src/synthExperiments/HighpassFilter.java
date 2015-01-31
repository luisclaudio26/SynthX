package synthExperiments;

import java.util.ArrayList;

/**
 * Implements a high pass filter with (ideal) square frequency response.
 * Attribute TABLE stores impulsive response while frequencyResponse...well, you go it.
 * Each time we change filters parameters, we recalculate frequency response and then
 * impulsive response.
 */
public class HighpassFilter extends LinearFilter {

	public HighpassFilter(double cutoff, int filterLength, double gain, int sampleRate) {
		super(cutoff, filterLength, gain, sampleRate);
	}
	
	/**
	 * REMARK: This method will be called n times, where n is the filter length
	 * Here we calculate sampleRate-cutoff; is this little calculus a performance issue?
	 */
	@Override
	protected boolean isFrequencyInRange(double freq) {
		return freq >= cutoff && freq <= sampleRate - cutoff;
	}
	
	public static void main(String[] args) throws Exception {
		
		//Create filter
		int sampleFreq = 8000;
		
		LinearFilter lf = new HighpassFilter(400, 32, 1.0, sampleFreq);
		
		//Create signal
		Signal s1 = new SineSynth(sampleFreq).generate(3, 50);
		Signal s2 = new SineSynth(sampleFreq).generate(3, 600);
		s1.add(s2);
		
		ArrayList<Double> s1Samp = new ArrayList<Double>();
		s1.cloneSamples(s1Samp);
		
		//Filter signal
		Signal s1F = lf.filter(s1);
		ArrayList<Double> s1FSamp = new ArrayList<Double>();
		s1F.cloneSamples(s1FSamp);
		
		for(int i = 0; i < s1Samp.size(); i++)
			System.out.println(i + " " + s1Samp.get(i) + " " + s1FSamp.get(i));
		
		return;
	}
}
