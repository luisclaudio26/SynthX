package synthExperiments;

/**
 * This class generates a sinusoidal signal
 * @author luisclaudio
 */
public class SineSynth extends Synthesizer {

	/**
	 * Returns sin(2pi.f/fs), where fs is the sampling frequency and f is the signal frequency
	 */
	@Override
	protected double s(double t) {
		return Math.sin(2 * Math.PI * t/samplingFreq);
	}
	
	public SineSynth(int samplingFreq) {
		super(samplingFreq);
	}
	
	/**
	 * Test driver for class
	 */
	public static void main(String[] args) {
		SineSynth ss = new SineSynth(48000);
		try { 
			ss.generate(3, 440).writeToWAVE("SINE.wav", SampleSize.S24BIT);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}



}
