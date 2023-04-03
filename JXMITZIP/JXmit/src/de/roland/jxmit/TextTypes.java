package de.roland.jxmit;

public class TextTypes {

	public static final TextType[] types = {
			new TextType(0x0030, "INMBLKSZ", "Block size"),
			new TextType(0x1022, "INMCREAT", "Creation date"),
			new TextType(0x0001, "INMDDNAM", "DDNAME for the file"),
			new TextType(0x000C, "INMDIR", "Number of directory blocks"),
			new TextType(0x0002, "INMDSNAM", "Name of the file"),
			new TextType(0x003C, "INMDSORG", "File organization"),
			new TextType(0x1027, "INMERRCD", "RECEIVE command error code"),
			new TextType(0x0022, "INMEXPDT", "Expiration date"),
			new TextType(0x1026, "INMFACK", "Originator requested notification"),
			new TextType(0x102D, "INMFFM", "Filemode number"),
			new TextType(0x1011, "INMFNODE", "Origin node name or node number"),
			new TextType(0x1024, "INMFTIME", "Origin timestamp"),
			new TextType(0x1012, "INMFUID", "Origin user ID"),
			new TextType(0x1023, "INMFVERS",
					"Origin version number of the data format"),
			new TextType(0x1021, "INMLCHG", "Date last changed"),
			new TextType(0x0042, "INMLRECL", "Logical record length"),
			new TextType(0x1020, "INMLREF", "Date last referenced"),
			new TextType(0x0003, "INMMEMBR", "Member name list"),
			new TextType(0x102F, "INMNUMF", "Number of files transmitted"),
			new TextType(0x102A, "INMRECCT", "Transmitted record count"),
			new TextType(0x0049, "INMRECFM", "Record format"),
			new TextType(0x000B, "INMSECND", "Secondary space quantity"),
			new TextType(0x102C, "INMSIZE", "File size in bytes"),
			new TextType(0x0028, "INMTERM", "Data transmitted as a message"),
			new TextType(0x1001, "INMTNODE", "Target node name or node number"),
			new TextType(0x1025, "INMTTIME", "Destination timestamp"),
			new TextType(0x1002, "INMTUID", "Target user ID"),
			new TextType(0x8012, "INMTYPE", "Data set type"),
			new TextType(0x1029, "INMUSERP", "User parameter string"),
			new TextType(0x1028, "INMUTILN", "Name of utility program") };

	public TextTypes() {
	}

	public static TextType getTextType(int j) {
		for (int i = 0; i < types.length; i++) {
			if (types[i].getId() == j) {
				return new TextType(types[i]);
			}
		}
		return null;
	}

	public TextType getTextType(String memnonic) {
		for (int i = 0; i < types.length; i++) {
			if (types[i].getMemnonic().equals(memnonic)) {
				return new TextType(types[i]);
			}
		}
		return null;
	}

}
