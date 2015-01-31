package synthExperiments;

public class DSPMath {

	//TODO: Generate windows
	//TODO: Interpolate stuff: get an array of points, aproximate by the best polynom. Should this be in a separate class?
	
	/**
	 * Converts a number to decibel scale
	 */
	public static double toDB(double v) {
		return 20.0 * Math.log10( v );
	}
	
	/**
	 * Takes a number in decibel values and converts it to "normal" form
	 */
	public static double fromDB(double v) {
		return Math.pow(10.0, v/20);
	}
	
	public static Signal hammingWindow(int size) {
		
		double alpha = 0.53836;
		double beta = 0.46164;
		
		double[] window = new double[size];
		
		for(int i = 0; i < size; i++)
			window[i] = alpha - beta * Math.cos( 2.0 * Math.PI * i / size);
		
		return new Signal(window);
	}
		
	public static void main(String[] args) {
		
		System.out.println(10.0 + " = " + DSPMath.toDB(10.0) + " dB");
		System.out.println(2.0 + " = " + DSPMath.toDB(2.0) + " dB");
		System.out.println(1.0 + " = " + DSPMath.toDB(1.0) + " dB");
		System.out.println(0.5 + " = " + DSPMath.toDB(0.5) + " dB");
		System.out.println(0.1 + " = " + DSPMath.toDB(0.1) + " dB");
		
		System.out.println(120 + "dB = " + DSPMath.fromDB(120));
		System.out.println(60 + "dB = " + DSPMath.fromDB(60));
		System.out.println(20 + "dB = " + DSPMath.fromDB(20));
		System.out.println(-6 + "dB = " + DSPMath.fromDB(-6));
		return;
	}

}
