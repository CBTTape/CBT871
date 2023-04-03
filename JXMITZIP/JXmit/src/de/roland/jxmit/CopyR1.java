package de.roland.jxmit;

import java.io.IOException;

public class CopyR1  {
	public static int copy1RHeader = 0xCA6D0F;
	public int header;
	public int r1dsorg;
	public int r1blksi;
	public int r1lrecl;
	public String  r1recfmS;
	public int r1recfm;
	public int r1keylen;
	public int r1optcd;
	public int r1tbklsi;
	public int r1firstbyte;
	public int ucbtype;
	public int maxrecsize;
	public int numcyls;
	public int trackspercyl;
	
	CopyR1(byte[] b) throws IOException {
		int offset = 0;
		this.header = XmitUtils.byte4int(b, offset) & 0xffffff;
		if(!isHeader()) {
			throw new IOException("COY1R Header not found!");
		}
		this.r1dsorg = XmitUtils.byte2int(b, offset + 4);
		this.r1blksi = XmitUtils.byte2int(b, offset + 6);
		this.r1lrecl = XmitUtils.byte2int(b, offset + 8);
		this.r1recfm = b[offset + 10] & 0xff;
		
		switch (this.r1recfm >> 6) {
		case 1:
			this.r1recfmS = "V";
			break;
		case 2:
			this.r1recfmS = "F";
			break;
		case 3:
			this.r1recfmS = "U";
			break;			
		}
		if ((this.r1recfm & 0x10) == 0x10) {
			this.r1recfmS = this.r1recfmS + "B";
		}
		if ((this.r1recfm & 0x08) == 0x08) {
			this.r1recfmS = this.r1recfmS + "S";
		}		
		this.r1firstbyte = (this.r1recfm >> 1) & 0x3;
		
		this.r1keylen = b[offset + 11] & 0xff;
		this.r1tbklsi = XmitUtils.byte2int(b, offset + 14);
		
		this.ucbtype = XmitUtils.byte4int(b, offset + 16);
		this.maxrecsize = XmitUtils.byte4int(b, offset + 20);
		this.numcyls = XmitUtils.byte2int(b, offset + 24);
		this.trackspercyl = XmitUtils.byte2int(b, offset + 26);
					
	}
	
	public boolean isHeader() {
		return (this.header == copy1RHeader);
	}
	
	public String getRecfm() {
		return this.r1recfmS;
	}
	
	public boolean isFixed() {
		return (this.r1recfmS.charAt(0) == 'F');
	}
	public void dump() {
		System.out.println("r1dsorg      :" + r1dsorg);
		System.out.println("r1blksi      :" + r1blksi);
		System.out.println("r1lrecl      :" + r1lrecl);
		System.out.println("r1recfm      :" + r1recfm + " " + r1recfmS);
		System.out.println("r1firstbyte  :" + r1firstbyte);
		System.out.println("r1keylen     :" + r1keylen);
		System.out.println("r1tbklsi     :" + r1tbklsi);
		System.out.println("maxrecsize   :" + maxrecsize);
		System.out.println("numcyls      :" + numcyls);
		System.out.println("trackspercyl :" + trackspercyl);
	}
	
}
