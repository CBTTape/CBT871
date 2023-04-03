package de.roland.jxmit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import de.roland.jxmit.Directory.DIRTYPE;

public class JXmit {

	public final static String title = "JXmit (c) 2011 by Roland Scholz (roland_scholz@web.de)";
	public final static String inmr01 = new String("INMR01");
	public final static String inmr02 = new String("INMR02");
	public final static String inmr03 = new String("INMR03");
	public final static String inmr06 = new String("INMR06");
	public final static String[] amodes = { "24", "64", "31", "ANY" };
	public final static byte charDot = 0x4b;

	public static boolean copyR2start = false;
	public static CopyR1 copyR1 = null;
	public static CopyR2 copyR2 = null;
	public static int lastdirblock = 0;
	public static Hashtable<String, Integer> dir = new Hashtable<String, Integer>();
	public static boolean dumpflag = true;
	public static boolean logflag = false;
	public static boolean iebcopy = false;
	public static int membercount = 0;
	public static long headerlen = 0;
	public static Vector<TextType> texttypes = new Vector<TextType>();
	public static Vector<byte[]> segs = new Vector<byte[]>();
	public static GregorianCalendar greg = new GregorianCalendar();
	public static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
	public static FileInputStream fs = null;
	public static Directory directory = null;

	public static String codepage = "IBM1047";
	public static String fname = "C:\\zOS\\xmit\\file742.xmi";

	// public static String fname = "C:\\zOS\\xmit\\output.xmi";

	public static void main(String[] args) {
		// String fname = "C:\\zOS\\xmit\\CBT645.XMI";
		// String fname = "C:\\zOS\\xmit\\FILE657.XMI";
		// String fname = "C:\\zOS\\xmit\\FILE135.XMI";
		// String fname = "C:\\zOS\\xmit\\dfhmac.xmi";
		// String fname = "C:\\zOS\\xmit\\loadbat.xmi";
		// String fname = "C:\\zOS\\xmit\\output.xmi";
		// String fname = "C:\\zOS\\xmit\\c.xmi";

		fname = "C:\\zOS\\xmit\\file182.xmi";
		fname = "C:\\zOS\\xmit\\output.xmi";

		error(title);

		try {
			readHeader(fname);
			byte[] b;
			boolean first = true;
			while ((b = getFileData(first)) != null) {
				first = false;
				System.out.println(getEbcdic(b, 0, b.length));
			}

		} catch (IOException e) {
			error(e.getStackTrace());
		}

		try {
			fs.close();
		} catch (IOException e) {
		}
	}

	public static byte[] readDataSegment(boolean first) throws IOException {
		if (first) {
			fs = new FileInputStream(fname);
			fs.skip(headerlen);
		}
		return readSegment(false);
	}

	public static void readHeader(String filename) throws IOException {
		iebcopy = false;
		headerlen = 0;
		directory = null;
		byte[] b;

		texttypes.clear();
		fname = filename;
		fs = new FileInputStream(fname);

		do {
			b = readSegment(true);
			analyseSegment(b);
		} while (!(getSegtype(b, 0).equals(inmr03)));

		if (iebcopy) {
			// COPY1R
			analyseSegment(readSegment(true));
			// COPY2R
			analyseSegment(readSegment(true));
			// Directory
			readDirectory();
		}
		fs.close();
		// error(Integer.toString((int) headerlen));
	}

	public static byte[] readSegment(boolean header) throws IOException {
		byte[] a = new byte[256];
		byte[] b = new byte[0];
		int seglen, flags;
		do {
			seglen = (fs.read() & 0xff) - 2;
			flags = fs.read() & 0xff;
			fs.read(a, 0, seglen);
			b = append(b, a, seglen);
			if (header) {
				headerlen += 2 + seglen;
			}
		} while ((flags & 0x40) == 0);

		return b;
	}

	public static byte[] append(byte[] a, byte[] b, int blength) {
		byte[] c = new byte[a.length + blength];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, blength);
		return c;
	}

	public static String getFilename() {
		for (TextType t : texttypes) {
			if (t.getType().equals(inmr02)
					&& t.getMemnonic().equals("INMDSNAM")) {
				return getEbcdic(t.getBytes(), 0, t.getBytes().length);
			}
		}
		return null;
	}

	public static byte[] getFileData(boolean first) throws IOException {
		byte[] b = null;
		boolean lreclFound = false;

		for (TextType t : texttypes) {
			if (t.getType().equals(inmr02)
					&& t.getMemnonic().equals("INMLRECL")) {
				lreclFound = true;
				break;
			}
		}

		if (lreclFound) {
			b = readDataSegment(first);
			if (getEbcdic(b, 0, 6).equals(inmr06)) {
				return null;
			}
		} else {
			return null;
		}
		return b;
	}

	public static Vector<byte[]> openMember(String member) throws IOException {
		Integer ttrI = dir.get(member);
		byte[] b = null;
		boolean first = true;

		if (ttrI != null) {
			int absTrack[] = convertTTRtoAbsolute(ttrI);
			do {
				b = readDataSegment(first);
				first = false;
				if (XmitUtils.byte2int(b, 4) == absTrack[0]
						&& XmitUtils.byte2int(b, 6) == absTrack[1]
						&& (b[8] & 0xff) == absTrack[2]) {
					break;
				}
			} while (true);
		} else {
			throw new IOException("Member not found!");
		}
		return getMemberData(b);
	}

	public static boolean isStartofMember(byte[] b) {
		Enumeration<Integer> e = dir.elements();
		while (e.hasMoreElements()) {
			int abs[] = convertTTRtoAbsolute(e.nextElement());
			if (XmitUtils.byte2int(b, 4) == abs[0]
					&& XmitUtils.byte2int(b, 6) == abs[1]
					&& (b[8] & 0xff) == abs[2]) {
				return true;
			}
		}
		return false;
	}

	public static int[] convertTTRtoAbsolute(int ttr) {
		int[] retvals = new int[3];
		int ttMem = ttr >> 8;
		int rMem = ttr & 0xff;
		int i, j;

		for (i = 0, j = 0; i < copyR2.numDebs; i++) {
			if (copyR2.relative[i] <= ttMem) {
				j = i;
			}
		}

		int ccMem = copyR2.cc[j] + (ttMem - copyR2.relative[j])
				/ copyR1.trackspercyl;
		int hhMem = copyR2.hh[j] + (ttMem - copyR2.relative[j])
				% copyR1.trackspercyl;
		if (hhMem >= copyR1.trackspercyl) {
			ccMem++;
			hhMem -= copyR1.trackspercyl;
		}

		retvals[0] = ccMem;
		retvals[1] = hhMem;
		retvals[2] = rMem;

		return retvals;
	}

	public static Vector<byte[]> getMemberData(byte[] b) throws IOException {

		Vector<byte[]> memberBytes = new Vector<byte[]>();
		int seglen = 0;
		int records = 0;
		int offset = 0;
		int reclen = 0;
		boolean isFixed;

		if (b == null) {
			b = readDataSegment(false);
		}

		isFixed = copyR1.isFixed();

		do {
			seglen = XmitUtils.byte2int(b, offset + 10);
			if (seglen == 0) {
				memberBytes.add(null);
				break;
			}
			offset += 12;
			records = 1;
			reclen = seglen;
			if (isFixed) {
				records = seglen / copyR1.r1lrecl;
				reclen = copyR1.r1lrecl;
			}
			for (int r = 0; r < records; r++) {
				byte[] c = new byte[reclen];
				System.arraycopy(b, offset, c, 0, reclen);
				memberBytes.add(c);
				offset += reclen;
			}
			if (offset >= b.length)
				break;
			
		} while (true);

		return memberBytes;
	}

	public static Directory getDirectory() {
		return directory;
	}

	private static Directory readDirectory() {
		int len = 0;
		int dirindex = 0;
		int index = 0;
		int memKey1 = 0;
		int memKey2 = 0;
		int ttr = 0;
		int halfwords = 0;
		String memKey = null;
		String memName = null;
		boolean alias;
		boolean once = true;

		if (!iebcopy) {
			return null;
		}

		try {
			membercount = 0;

			byte[] b = readSegment(true);

			do {
				len = XmitUtils.byte2int(b, index + 10);
				memKey = getMember(b, index + 12);
				memKey1 = XmitUtils.byte4int(b, index + 12);
				memKey2 = XmitUtils.byte4int(b, index + 16);

				dirindex = index;
				do {
					memName = getMember(b, dirindex + 22);
					if (!(XmitUtils.byte4int(b, dirindex + 22) == -1)) {
						ttr = XmitUtils.byte3int(b, dirindex + 30);
						halfwords = b[dirindex + 33] & 0x1f;
						alias = false;
						String rent = "";
						String reus = "";
						String amode = "";
						String rmode = "24";
						String aliasname = "";
						int ac = 0;
						int modlen = 0;
						int entry = 0;
						int version = 0;
						int modification = 0;
						int julian = 0;
						int year = 0;
						String created = "";
						String modified = "";
						int hours = 0;
						int minutes = 0;
						String userid = "";
						int size = 0;

						if ((b[dirindex + 33] & 0x80) == 0x80) {
							alias = true;
						}
						dir.put(memName, Integer.valueOf(ttr));

						membercount++;
						if (copyR1.r1lrecl == 0) {
							if ((b[dirindex + 22 + 20] & 0x80) == 0x80) {
								rent = "RENT";
							}
							if ((b[dirindex + 22 + 20] & 0x40) == 0x40) {
								reus = "REUS";
							}
							modlen = XmitUtils
									.byte3int(b, dirindex + 22 + 22);
							entry = XmitUtils.byte3int(b, dirindex + 22 + 27);
							if ((b[dirindex + 22 + 31] & 0x10) == 0x10) {
								rmode = "ANY";
							}
							amode = amodes[b[dirindex + 22 + 31] & 0x03];

							if (!alias && halfwords == 12) {
								ac = b[dirindex + 22 + 34] & 0xff;
							}
							if (alias && halfwords == 17) {
								aliasname = getEbcdic(b, dirindex + 22 + 36, 8);
								ac = b[dirindex + 22 + 45] & 0xff;
							}
							if (once) {
								directory = new Directory(DIRTYPE.LOAD);
								log(directory.getDirHeader());
								once = false;
							}
							directory.add(memName, aliasname, ttr, modlen,
									entry, amode, rmode, ac, rent, reus);
							log(directory.getDirText(memName));
						} else {
							if (halfwords == 15) {
								version = b[dirindex + 22 + 12] & 0xff;
								modification = b[dirindex + 22 + 13] & 0xff;

								year = 1900
										+ (b[dirindex + 22 + 16] & 0xff)
										* 100
										+ XmitUtils.packed2int(b,
												dirindex + 22 + 17, 2);
								;
								julian = XmitUtils.packed2int(b,
										dirindex + 22 + 18, 4);
								greg.set(GregorianCalendar.YEAR, year);
								greg.set(GregorianCalendar.DAY_OF_YEAR, julian);
								created = df.format(greg.getTime());

								year = 1900
										+ (b[dirindex + 22 + 20] & 0xff)
										* 100
										+ XmitUtils.packed2int(b,
												dirindex + 22 + 21, 2);
								;
								julian = XmitUtils.packed2int(b,
										dirindex + 22 + 22, 4);
								greg.set(GregorianCalendar.YEAR, year);
								greg.set(GregorianCalendar.DAY_OF_YEAR, julian);
								modified = df.format(greg.getTime());

								hours = XmitUtils.packed2int(b,
										dirindex + 22 + 24, 2);
								minutes = XmitUtils.packed2int(b,
										dirindex + 22 + 25, 2);

								size = XmitUtils.byte2int(b,
										dirindex + 22 + 26);
								userid = getEbcdic(b, dirindex + 22 + 32, 8);
							}
							if (once) {
								directory = new Directory(DIRTYPE.TEXT);
								log(directory.getDirHeader());
								once = false;

							}
							directory.add(memName, aliasname, ttr, size, version,
									modification, created, modified, hours,
									minutes, userid);
							log(directory.getDirText(memName));

						}

						dirindex += 12 + 2 * halfwords;
					}
				} while (!memKey.equals(memName));
				index += len + 20;
				if (index >= b.length) {
					b = readSegment(true);
					index = 0;
				}
			} while (!(memKey1 == 0xFFFFFFFF && memKey2 == 0xFFFFFFFF));
		} catch (IOException e) {
			error(e.getStackTrace());
		}

		return directory;
	}

	public static String getEbcdic(byte[] b, int offset, int len) {
		String s = null;
		try {
			s = new String(b, offset, len, codepage);
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getStackTrace());
		}
		return s;
	}

	public static String getSegtype(byte[] b, int offset) {
		return getEbcdic(b, offset, 6);
	}

	public static String getMember(byte[] b, int offset) {
		return getEbcdic(b, offset, 8);
	}

	public static void analyseSegment(byte[] b) throws IOException {
		String segtype = getSegtype(b, 0);
		int filenumber = 0;
		// dump(b, b.length);
		if (segtype.equals(inmr01) || segtype.equals(inmr03)) {
			analyseKey(b, 6, segtype);
		}
		if (segtype.equals(inmr02)) {
			if (filenumber == 0) {
				filenumber = XmitUtils.byte4int(b, 6);
			}
			analyseKey(b, 10, segtype);
		}
		if (copyR2start) {
			copyR2 = new CopyR2(b, copyR1.trackspercyl);
			copyR2start = false;
		}
		if ((XmitUtils.byte4int(b, 0) & 0xffffff) == CopyR1.copy1RHeader) {
			copyR1 = new CopyR1(b);
			copyR2start = true;
		}
	}

	private static void analyseKey(byte[] b, int index, String segtype) {
		TextType tt;
		int key = 0;
		for (; index < b.length;) {
			key = XmitUtils.byte2int(b, index);
			if ((tt = TextTypes.getTextType(key)) != null) {
				index += 2;
				int num = XmitUtils.byte2int(b, index);
				index += 2;
				int bindex = 0;
				byte[] bytes = new byte[44];
				int len = 0;
				for (int n = 0; n < num; n++) {
					len = XmitUtils.byte2int(b, index);
					index += 2;
					System.arraycopy(b, index, bytes, bindex, len);
					bindex += len;
					if (tt.getMemnonic().equals("INMDSNAM") && n < num - 1) {
						bytes[bindex] = charDot;
						bindex++;
					}
					index += len;
				}
				tt.setLen(bindex);
				tt.setBytes(bytes);
				tt.setType(segtype);
				texttypes.add(tt);

				if (tt.getMemnonic().equals("INMUTILN")) {
					if (getEbcdic(bytes, 0, bindex).equals("IEBCOPY")) {
						iebcopy = true;
					}
				}
			} else {
				System.out.println("Key " + key + " not found!");
			}
		}
	}

	public static void display() {
		String s = null;
		byte[] b;
		int i = 0;
		try {
			for (TextType tt : texttypes) {
				b = tt.getBytes();
				i = 0;
				switch (b.length) {
				case 1:
					i = b[0] & 0xff;
					break;
				case 2:
					i = XmitUtils.byte2int(b, 0);
					break;
				case 4:
					i = XmitUtils.byte4int(b, 0);
					break;
				}

				s = String.format("%-8s %-8s %-32s %-32s %04d", tt.getType(),
						tt.getMemnonic(), tt.getDescription(), new String(b,
								"Cp1047"), i);

				log(s);
				dump(tt.getBytes());
			}
		} catch (UnsupportedEncodingException e) {
			e.getStackTrace();
		}
	}

	public static long openXmit(String fname) throws IOException {
		File f = new File(fname);
		long flen = f.length();
		fs = new FileInputStream(f);
		return flen;
	}

	static void dump(byte[] b, int offset, int len) {
		byte[] c = new byte[len];
		System.arraycopy(b, offset, c, 0, len);
		dump(c);
	}

	static String dump(byte[] b) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < b.length; i++) {
			sb.append(String.format("%02X ", b[i]));
		}
		System.out.println(sb);
		return sb.toString();
	}

	static String dump(byte[] b, int length) {
		StringBuffer ebcdic = new StringBuffer(getEbcdic(b, 0, b.length));
		StringBuffer line = null;
		StringBuffer sb = new StringBuffer();
		int width = 16;
		int lines = length / width;
		int off = 0;
		for (int i = 0; i < lines; i++) {
			line = formatDumpLine(b, ebcdic, off, width);
			if (dumpflag)
				System.out.print(line);
			sb.append(line);
			off += width;
		}
		if ((width = length % width) != 0) {
			line = formatDumpLine(b, ebcdic, off, width);
			if (dumpflag)
				System.out.print(line);
			sb.append(line);
		}
		return sb.toString();
	}
	
	public static StringBuffer formatDumpLine(byte[] b, StringBuffer ebcdic,
			int off, int width) {
		StringBuffer line = new StringBuffer(String.format("%06d %06X" + " : ",
				off, off));
		for (int j = 0; j < width; j++) {
			line.append(String.format("%02X ", b[off + j]));
		}
		line.append(" |" + dumpString(b, ebcdic, off, width) + "|\n");
		return line;
	}
	
	public static StringBuffer dumpString(byte[] b, StringBuffer ebcdic,
			int off, int len) {
		StringBuffer char16 = new StringBuffer(ebcdic.substring(off, off + len));
		for (int j = 0; j < len; j++) {
			if ((b[off + j] & 0xff) < 64) {
				char16.setCharAt(j, '.');
			}
		}
		return char16;
	}

	public static void log(String msg) {
		if (dumpflag)
			System.out.println(msg);
		if (logflag)
			JXmitGui.addLogRow(msg);
	}

	public static void error(StackTraceElement[] ss) {
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement s : ss) {
			sb.append(s.toString());
			sb.append("\n");
		}
		error(sb.toString());

	}

	public static void error(String s) {
		System.out.println(s);
	}

}
