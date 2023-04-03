package de.roland.jxmit;

public class CopyR2 {

	public int numDebs = 0;
	private int rel = 0;
	private int size = 0;
	private int offset = 0;
	
	public int[] start = new int[16];
	public int[] end = new int[16];
	public int[] relative = new int [16];
	public int[] cc = new int [16];
	public int[] hh = new int [16];
	
	CopyR2(byte[] b, int trkscyl) {
		//JXmit.dump(b, b.length);
		numDebs = b[0] & 0xff;
		//System.out.println("Number of DEB: " + numDebs);
		for (int i = 0; i < numDebs; i++) {
			offset += 16;
			cc[i] = XmitUtils.byte2int(b, offset + 6);
			hh[i] = XmitUtils.byte2int(b, offset + 8);
			
			start[i] = cc[i] * trkscyl + hh[i];
			end[i] = XmitUtils.byte2int(b, offset + 10) * trkscyl 
			+ XmitUtils.byte2int(b, offset + 12);
			relative[i] = rel;
			rel += 1 + (end[i] - start[i]);
		}
	}
	
	public void dump() {
		for (int i = 0; i < numDebs; i++) {
			System.out.println("Start  " + i + " " + start[i]);
			System.out.println("End    " + i + " " + end[i]);
			System.out.println("RelTRK " + i + " " + relative[i]);
		}
	}
}
