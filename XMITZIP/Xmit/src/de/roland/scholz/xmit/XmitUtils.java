package de.roland.scholz.xmit;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class XmitUtils {
	private static  final String HEXES = "0123456789ABCDEF";
	private static  final char[] ibm1047 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', '�', '�', '�', '�', '�', '�', '�',
			'�', '�', '�', '.', '<', '(', '+', '|', '&', '�', '�', '�', '�',
			'�', '�', '�', '�', '�', '!', '$', '*', ')', ';', '^', '-', '/',
			'�', '�', '�', '�', '�', '�', '�', '�', '�', ',', '%', '_', '>',
			'?', '�', '�', '�', '�', '�', '�', '�', '�', '�', '`', ':', '#',
			'@', '\'', '=', '"', '�', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
			'i', '�', '�', '�', '�', '�', '�', '�', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', '�', '�', '�', '�', '�', '�', '�', '~', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z', '�', '�', '�', '[', '�', '�',
			'�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�',
			']', '�', '�', '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
			'�', '�', '�', '�', '�', '�', '}', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', '�', '�', '�', '�', '�', '�', '\\', '�', 'S', 'T',
			'U', 'V', 'W', 'X', 'Y', 'Z', '�', '�', '�', '�', '�', '�', '0',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '�', '�', '�', '�',
			'�', '?' };
	private static GregorianCalendar greg = new GregorianCalendar();
	private static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

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

	public static long byte8long(byte[] b, int offset) {
		long l = (byte4int(b, offset) & 0xffffffffL) << 32;
		l |= byte4int(b, offset + 4);
		return l;
	}

	public static int byte4int(byte[] b, int offset) {
		int i = (b[offset] & 0xFF) << 24;
		i += (b[offset + 1] & 0xFF) << 16;
		i += (b[offset + 2] & 0xFF) << 8;
		i += (b[offset + 3] & 0xFF);
		return i;
	}

	public static int byte3int(byte[] b, int offset) {
		int i = (b[offset] & 0xFF) << 16;
		i += (b[offset + 1] & 0xFF) << 8;
		i += (b[offset + 2] & 0xFF);
		return i;
	}

	public static int byte2int(byte[] b, int offset) {
		int i = (b[offset] & 0xFF) << 8;
		i += (b[offset + 1] & 0xFF);
		return i;
	}

	public static int packed2int(byte[] b, int offset, int len) {
		int i = 0;
		int d = 0;
		int power = 1;
		boolean lo = true;

		for (int j = len - 1; j >= 0; j--) {
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

	public static String ispfToDate(byte[] b, int offset) {
		int year = 1900 + (b[offset] & 0xff) * 100
				+ XmitUtils.packed2int(b, offset + 1, 2);

		int julian = XmitUtils.packed2int(b, offset + 2, 4);
		greg.set(GregorianCalendar.YEAR, year);
		greg.set(GregorianCalendar.DAY_OF_YEAR, julian);
		return df.format(greg.getTime());
	}

	public static String getEbcdic(byte[] b, int offset, int len) {
		char[] cc = new char[len];
		for (int i = 0; i < len; i++) {
			cc[i] = XmitUtils.ibm1047[b[offset + i] & 0xff];
		}
		return new String(cc);
	}

}
