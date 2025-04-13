package de.roland.jxmit;

public class XmitUtils {
	private static final String HEXES = "0123456789ABCDEF";

	public static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	public static String getHex(byte b) {
		final StringBuilder hex = new StringBuilder(2);
		hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
				HEXES.charAt((b & 0x0F)));

		return hex.toString();
	}
	
	public static int byte4int (byte[] b, int offset) {			
		int i = (b[offset] & 0xFF) << 24;
		i += (b[offset + 1] & 0xFF) << 16;
		i += (b[offset + 2] & 0xFF) << 8;
		i += (b[offset + 3] & 0xFF);
		return i;
	}

	public static int byte3int (byte[] b, int offset) {			
		int i = (b[offset] & 0xFF) << 16;
		i += (b[offset + 1] & 0xFF) << 8;
		i += (b[offset + 2] & 0xFF);
		return i;
	}

	public static int byte2int (byte[] b, int offset) {			
		int i = (b[offset] & 0xFF) << 8;
		i += (b[offset + 1] & 0xFF);
		return i;
	}
	
	public static int packed2int (byte[] b, int offset, int len) {
		int i = 0;
		int d = 0;
		int power = 1;
		boolean lo = true;
		
		for(int j = len-1; j >= 0; j--) {
			if (lo) {
				d = b[offset + (j >> 1)] & 0x0f;
				lo = false;
			} else {
				d = (b[offset + (j >> 1)] & 0xf0) >> 4;
				lo = true;				
			}
			if (d <= 9) {
				i += d * power;
				power *= 10;
			}
		}
		return i;
	}
}
