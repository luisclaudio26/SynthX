package synthExperiments;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.LinkedList;

public class WAVEWriter {

	private final byte[] RIFF = {'R', 'I', 'F', 'F'};
	private final byte[] WAVE = {'W', 'A', 'V', 'E'};
	private final byte[] FMT = 	{'f', 'm', 't', ' '};
	private final byte[] DATA = {'d', 'a', 't', 'a'};
	private final short S_AUDIOFORMAT = 1; //PCM format, no compression
	
	/**
	 * This is where we store our samples. Channels must be stored in sequence!
	 * First, store all sample from channel 1, then all samples from channel 2, etc.
	 * Ex.: s1c1 s2c1 s3c1 ... sNc1 s1c2 s2c2 s3c2 ... sNc2
	 */
	private LinkedList<byte[]> samples; //Little Endian
	
	/**
	 * This will help us to encode double values in byte arrays.
	 */
	private ExtractSamples sampler;
	
	/**
	 * Store relevant data to build WAVE header. i_ stands for INTEGER (4 bytes) and
	 * s_ for SHORT (2 bytes).
	 */
	private int 	i_chunkSize, i_subchunk1Size, i_sampleRate, i_byteRate, i_subchunk2Size;
	private short 	s_audioFormat, s_numChannels, s_blockAlign, s_bitsPerSample;
	
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Constructor that takes main info to build the header of the WAVE file.
	 * Must check after whether this exception class is good for handling the problems
	 * with sample size.
	 * 
	 * @param sampleRate Sample rate of this audio file
	 * @param sizeInBytes The size in bytes for each sample
	 * @param numChannels Number of channels in this audio file
	 */
	public WAVEWriter(int sampleRate, SampleSize sizeInBytes, int numChannels)
	{
		samples = new LinkedList<>();
		sampler = new ExtractSamples(sizeInBytes);
		
		s_audioFormat = 	S_AUDIOFORMAT;
		s_numChannels = 	(short)numChannels;
		i_sampleRate = 		sampleRate;
		s_bitsPerSample = 	(short)(sizeInBytes.value*8);
		
		i_byteRate = sampleRate * numChannels * sizeInBytes.value;
		s_blockAlign = (short)(s_numChannels * sizeInBytes.value);
		
		i_subchunk1Size = 2 +		//AudioFormat = Short (2 bytes)
					2 +	//NumChannels = Short (2 bytes)
					4 +	//Sample rate = Integer (4 bytes)
					4 +	//Byte rate = Integer (4 bytes)
					2 +	//Block align = Short (2 bytes)
					2;	//Bits per sample = Short (2 bytes)
		
		//Both depend on number of samples
		i_subchunk2Size = 0;
		i_chunkSize = 0;
	}
	
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Manage sample storage.
	 * @param sample A sample which will be add to the file
	 * @throws Throw an exception if sample size is wrong (that is, is different from what
	 * we defined in bitsPerSample).
	 */
	private void addSample(byte[] sample) throws Exception
	{
		if(sample.length*8 != this.s_bitsPerSample)
			throw new Exception("Bad sample size!");
		
		byte[] sample_le = bigToLittleEndian(sample);
		samples.add(sample_le);
	}
	
	public void addSample(double sample)
	{
		try {
			byte[] s = sampler.sample(sample);
			addSample(s);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public void clearSamples() {
		samples.clear();
	}
	
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Calculates the size of chunks which are dependent
	 * of the number of samples.
	 * @return The chunk size in bytes
	 */
	private int calculateSubchunk2Size() 
	{
		int bytesPerSample = this.s_bitsPerSample / 8;
		this.i_subchunk2Size = this.s_numChannels * bytesPerSample * samples.size();
		
		return i_subchunk2Size;
	}
	
	private int calculateChunksize()
	{
		this.i_chunkSize = 4					//WAVE format tag
					+ 4				//FMT ID tag
					+ 4				//FMT subchunk size value
					+ i_subchunk1Size		//Actual FMT subchunk size
					+ 4				//DATA ID tag
					+ 4				//DATA subchunk size tag
					+ calculateSubchunk2Size();	//Actual DATA subchunk size
		return i_chunkSize;
	}
		
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Functions to write the WAVE file itself. writeWAVE is the main function
	 * and the other two are auxiliary.
	 * @param filepath Output file
	 * @throws Exception Thrown if filepath is invalid
	 */
	private void writeWAVEHeader(String filepath) throws Exception
	{
		FileOutputStream output = new FileOutputStream(filepath, true);
		
		//Convert integers and shorts in byte arrays
		byte[] chunkSize = 	bigToLittleEndian(calculateChunksize());
		byte[] subchunk1Size = 	bigToLittleEndian(this.i_subchunk1Size);
		byte[] audioFormat = 	bigToLittleEndian(this.s_audioFormat);
		byte[] numChannels = 	bigToLittleEndian(this.s_numChannels);
		byte[] sampleRate = 	bigToLittleEndian(this.i_sampleRate);
		byte[] byteRate = 	bigToLittleEndian(this.i_byteRate);
		byte[] blockAlign = 	bigToLittleEndian(this.s_blockAlign);
		byte[] bitsPerSample = 	bigToLittleEndian(this.s_bitsPerSample);
		byte[] subchunk2Size = 	bigToLittleEndian(calculateSubchunk2Size());
		
		//Write it to file
		output.write(RIFF);
		output.write(chunkSize);
		output.write(WAVE);
		output.write(FMT);
		output.write(subchunk1Size);
		output.write(audioFormat);
		output.write(numChannels);
		output.write(sampleRate);
		output.write(byteRate);
		output.write(blockAlign);
		output.write(bitsPerSample);
		output.write(DATA);
		output.write(subchunk2Size);
		
		output.close();
	}
	
	private void writeWAVEBody(String filepath) throws Exception
	{
		FileOutputStream out = new FileOutputStream(filepath, true);
		
		for(byte[] sample: samples) {
			out.write(sample);
		}
		
		out.close();
	}
	
	public void writeWAVE(String filepath) throws Exception
	{
		//Create new file then close. the other functions will overwrite to it.
		FileOutputStream out = new FileOutputStream(filepath, false); 
		out.close();
		
		writeWAVEHeader(filepath);
		writeWAVEBody(filepath);
	}
	
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Gets parameter N and converts to an array of bytes in LITTLE ENDIAN
	 * order.
	 * @param n An integer, a short or just an array of bytes
	 * @return An array of bytes with the same value in little endian
	 */
	private byte[] bigToLittleEndian(byte[] n) {
		
		int len = n.length;
		byte[] LittleEndian = new byte[len];
		
		for(int i = 0; i < len; i++)
			LittleEndian[i] = n[len-i-1];
		
		return LittleEndian;
	}
	
	private byte[] bigToLittleEndian(int n) {
		
		int INTSIZE = Integer.SIZE/8;		
		byte[] out = new byte[INTSIZE];
		int mask = 0x000000FF;
		
		for(int i = 0; i < INTSIZE; i++, mask = mask << 8)
		{
			int masked = (n & mask);
			masked = masked >> (i*8); //Shift bits to the beginning
			out[i] = (byte)masked;
		}
		
		return out;
	}
	
	private byte[] bigToLittleEndian(short n)
	{
		return Arrays.copyOfRange( bigToLittleEndian( (int)n ), 0, 2);
	}
	
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------
	/**
	 * A test driver for this class. Generates white noise for 1 second in 16 bits.
	 * @param args
	 */
	
	public static void main(String[] args) 
	{
		WAVEWriter WW = null;
		
		try {
			double sampleRate = 8000.0;
			double periode = 1/sampleRate;
			double duration = 1;
			SampleSize sampleSizeBytes = SampleSize.S16BIT;
			int nChannels = 1;
			double freq = 437.5;
			
			WW = new WAVEWriter((int)sampleRate, sampleSizeBytes, nChannels);
			
			for(double t = 0; t < duration; t += periode)
			{
				double value = Math.sin(Math.PI*2*t*freq);
				WW.addSample( value );
			}
			
			WW.writeWAVE("whiteNoise2.wav");
		
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
