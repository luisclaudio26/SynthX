package synthExperiments;

public enum SampleSize {
	U8BIT(1, false), 
	S16BIT(2, true), 
	S24BIT(3, true), 
	S32BIT(4, true);
	
	public final int 		value;
	public final boolean 	signed;
	
	SampleSize(int v, boolean s) {
		this.value = v;
		this.signed = s;
	}
	public long maxValue() {
		
		//In 2's complement, biggest value for a number
		//is 011111...1 (binary). We shift the first octet(01111111 - 0x7F) 'till the correct
		//position (that is: first for U8Bit, second for S16Bit, etc.) and pad the rest with 1's.
		long out = (signed) ? 0x7F : 0xFF;
		
		for(int i = 1; i < value; i++) {
			
			out = out << 8;
			out = out | 0xFF;
		}
		
		return out;
	}
}
