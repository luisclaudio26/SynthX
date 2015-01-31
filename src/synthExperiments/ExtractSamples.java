package synthExperiments;

import java.io.FileOutputStream;

public class ExtractSamples {

	private SampleSize 	sizeInBytes;
	
	//------------------------------------------------------------
	public ExtractSamples(SampleSize sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}
	
	private byte[] getBytes(long sampleValue) 
	{
		int N = sizeInBytes.value;
		byte[] valueBytes = new byte[N];
		long mask = 0x00000000000000FF;
		for(int i = 0; i < N; i++)
		{
			long masked = sampleValue & mask; 
			valueBytes[N-i-1] = (byte)(masked >> i*8);
			
			mask = mask << 8;
		}
		
		return valueBytes;
	}
	
	/**
	 * "Converts" floating-point value in range [-1,1] to byte array.
	 * WARNING: this -1 is a "correction" factor. Samples with value maxValue() tend 
	 * to suffer signal clipping and procure noise. Taking 1 avoids this.
	 * @param value Sample value.
	 * @return byte array with samples in BigEndian format.
	 * @throws Exception Thrown when sample is not in range -1 <= value <= 1
	 */
	public byte[] sample(double value) throws Exception
	{
		if(value < -1.0 || value > 1.0)
			throw new Exception("Sample must be a value between -1 et 1.");
		
		//Avoid negative values if we sampling with unsigned 8 bit
		if(!sizeInBytes.signed)
			value += 1.0;
		
		double step = 2.0 / Math.pow(2, sizeInBytes.value*8);
		long sampleValue = (long)Math.round(value/step);
		
		//Prevent overflow
		//WARNING: this -1 is a "correction" factor. Samples with value maxValue() tend
		//to suffer signal clipping and procure noise. Taking 1 avoids this.
		long maxValue = sizeInBytes.maxValue() - 1;
		sampleValue = (sampleValue > maxValue) ? maxValue : sampleValue;
		
		return getBytes(sampleValue);
	}
	
	/**
	 * Test drive for the class
	 */
	public static void main(String[] args) 
	{
		try {
		
			double sampleRate = 10000.0;
			double periode = 1/sampleRate;
			double duration = 1;
			SampleSize sampleSizeBytes = SampleSize.S16BIT;
			double freq = 100.0;

			ExtractSamples sampler = new ExtractSamples(sampleSizeBytes);
			
			FileOutputStream out = new FileOutputStream("raw", false);
			for(double t = 0; t < duration; t += periode)
			{
				double value = Math.sin(Math.PI*2*t*freq);
				
				byte[] s = sampler.sample(value);
				
				for(byte sample: s) {
					out.write(sample);
				}
				
			}
			out.close();

		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
