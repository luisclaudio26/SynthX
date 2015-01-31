package synthExperiments;

public class NewtonInterpolation implements Interpolate {

	//-------------------------------------------
	//---------------- Attributes ---------------
	//-------------------------------------------
	private double[][] 	coeffTable;
	private double[]	divDiff;
	private int			nPoints;
	
	//---------------------------------------------
	//---------------- Constructors ---------------
	//---------------------------------------------
	public NewtonInterpolation() {
		coeffTable = null;
	}
	

	//-------------------------------------------
	//---------------- Operations ---------------
	//-------------------------------------------
	/**
	 * Calculate vector of divided differences
	 * @return
	 */
	protected void dividedDifferences() {
		
		if(coeffTable == null) return;
		
		//First two columns contains X and Y. We start from 2 TODO: ADJUST BOUNDS
		for(int i = 2; i <= nPoints; i++) 
		{ 
			for(int j = 0; j < coeffTable[i].length; j++)
			{
				double dy = coeffTable[i-1][j+1] - coeffTable[i-1][j];
				double dx = coeffTable[0][j+1] - coeffTable[0][j];
				coeffTable[i][j] = dy/dx;
			}
			
		}
		
		/*
		 * 0 0 S
		 * 1 1
		 * 2 4
		 * 3 9
		 */
		
		
		return;
	}
	
	/**
	 * Evaluates a polynom by Horner's algorithm
	 * @param x X coordinate of the point we will interpolate
	 * @param newtonCoef Newton coefficient, calculated by divided differences
	 * @return Y coordinate of the point X passed as argument
	 */
	protected double hornersAlg(double x, double[] newtonCoef) {
		return 0.0;
	}
	
	
	//--------------------------------------------------
	//---------------- Interface methods ---------------
	//--------------------------------------------------
	@Override
	public void dataset(double[] t, double[] y) {
		//TODO: assert length(t) == length(y), t is crescent
		
		//Create and initialize matrice. Initial capacity is
		//2 times the number of coefficients we have (plus the column
		//for X coefficients).
		this.coeffTable = new double[t.length*2 + 1][y.length*2];
		for(int i = 0; i < y.length; i++) {
			coeffTable[0][i] = t[i];
			coeffTable[1][i] = y[i];
		}
	}
	
	@Override
	public double evaluate(double x) {
		return 0;
	}
	
	@Override
	public void addPoint(double t, double y) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Test driver for this class
	 */
	public static void main(String[] args) {
		return;
	}
}
