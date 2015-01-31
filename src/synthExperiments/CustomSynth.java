package synthExperiments;

public class CustomSynth extends Synthesizer {

	public static void main(String[] args) {
		CustomSynth ss = new CustomSynth(8000);
		try { 
			ss.generate(3, 440).writeToWAVE("CUSTOM.wav", SampleSize.S24BIT);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public CustomSynth(int freq) {
		super(freq);
		
		waveform = new double[freq];
		
		//TODO: TEST. ERASE THIS AFTER. SAWTOOTH:
		double freq2 = freq/2;
		for(int i = 0; i < freq; i++)
			waveform[i] = (-freq2 + i)/freq2;
		
		//for(int i = 0; i < freq; i++) waveform[i] = 0.0;
	}
	
	protected double[] waveform;
	
	/**
	 * Frequencies which are too low compared to sampling frequency
	 * makes us use interpolation to have more precision.
	 * TODO: INTERPOLATE STUFF
	 */
	@Override
	protected double s(double t) {
		int pos = (int)Math.round(t) % samplingFreq;
		return waveform[ pos ];
	}

}
