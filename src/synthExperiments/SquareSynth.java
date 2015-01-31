package synthExperiments;

public class SquareSynth extends Synthesizer {

	public static void main(String[] args) {
		SquareSynth ss = new SquareSynth(48000);
		try { 
			ss.generate(3, 400).writeToWAVE("SQUARE.wav", SampleSize.S24BIT);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public SquareSynth(int samplingFreq) {
		super(samplingFreq);
	}
	
	/**
	 * Returns a square signal ranging from -1 to 1.
	 */
	@Override
	protected double s(double t) {
		int pos = (int)t % samplingFreq;
		return (pos > samplingFreq/2) ? -1.0 : 1.0;
	}

}
