package synthExperiments;

public interface Interpolate {
	
	/**
	 * Returns the interpolated value of X based on the data set built
	 * @param x The point whose value we want to know
	 * @return Returns the interpolated value of X
	 */
	double evaluate(double x);
	
	/**
	 * Passes as argument our dataset, composed by abscissas and ordinates.
	 * As it would be too complicated (and not that useful) creating a class
	 * "Point" to pass it as argument, we choose to receive two vectors of
	 * points. Please make sure they're consistent: t is strictly crescent,
	 * t and y have the same length.
	 * 
	 * @param t Abscissas. For example, the moment in time for each point
	 * @param y Ordinates. Our s(t).
	 */
	void dataset(double[] t, double[] y);
	
	/**
	 * Adds a single point to our data set and recalculate polynom
	 * @param t Read definition for parameter t from dataset()
	 * @param y Read definition for parameter y from dataset()
	 */
	void addPoint(double t, double y);
}
