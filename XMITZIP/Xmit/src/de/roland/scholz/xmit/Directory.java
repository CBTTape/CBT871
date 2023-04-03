package de.roland.scholz.xmit;

import java.util.Hashtable;
import java.util.Vector;

public class Directory {

	public enum DIRTYPE {
		TEXT, LOAD
	};

	private DIRTYPE dirtype;
	private Vector<Vector<String>> textlist = new Vector<Vector<String>>();
	private final String[] textheader = { "Member", "Alias", "Size", "Version",
			"Created", "Modified", "UserID" };
	private final String[] loadheader = { "Member", "Alias", "TTR", "Size",
			"Entry", "AM", "RM", "AC", "RENT", "REUS" };

	// private DirTableModel tm = null;
	private Hashtable<String, Integer> ttrList = new Hashtable<String, Integer>();

	public Directory(DIRTYPE t) {
		this.dirtype = t;
	}

	public String[] getHeader() {
		if (dirtype == DIRTYPE.TEXT)
			return textheader;
		return loadheader;
	}

	public void add(String memName, String aliasname, int ttr, int size,
			int version, int modification, String created, String modified,
			int hours, int minutes, String userid) {
		Vector<String> entry = new Vector<String>();
		entry.add(memName);
		entry.add(aliasname);

		if (version == 0 && modification == 0) {
			entry.add("");
			entry.add("");
		} else {
			entry.add(String.format("%06d", size));
			entry.add(String.format("%02d-%02d", version, modification));
		}
		entry.add(created);
		if (modified.equals("")) {
			entry.add(created);
		} else {
			entry.add(String.format("%s %02d:%02d", modified, hours, minutes));
		}
		entry.add(userid);
		textlist.add(entry);

		ttrList.put(memName, ttr);
	}

	public void add(String memName, String aliasname, int ttr, int modlen,
			int modentry, String amode, String rmode, int ac, String rent,
			String reus) {
		Vector<String> entry = new Vector<String>();
		entry.add(memName);
		entry.add(aliasname);
		entry.add(String.format("%06X", ttr));
		entry.add(String.format("%06X", modlen));
		entry.add(String.format("%06X", modentry));
		entry.add(amode);
		entry.add(rmode);
		entry.add(String.format("%02d", ac));
		entry.add(rent);
		entry.add(reus);
		textlist.add(entry);

		ttrList.put(memName, ttr);
	}

	public DIRTYPE getDirType() {
		return this.dirtype;
	}

	public int getTTR(String member) {
		return ttrList.get(member);
	}
	
	public String getDirHeader() {
		switch (dirtype) {
		case LOAD:
			return String.format(
					"%-8s %-8s %-6s %-6s %-6s %-3s %-3s %-2s %-4s %-4s",
					"Member", "Alias", "TTR", "Size", "Entry", "AM", "RM",
					"AC", "RENT", "REUS");
		case TEXT:
			return String.format("%-8s %-8s %-6s %-7s %-10s %-15s  %-8s",
					"Member", "Alias", "Size", "Version", "Created",
					"Modified", "UserID");
		}
		return null;
	}

	public String[] getMembers() {
		String[] ss = new String[textlist.size()];
		int i = 0;
		for (Vector<String> entry : textlist) {
			ss[i++] = entry.get(0);
		}
		return ss;
	}

	public String getDirText(String member) {
		Vector<String> e = null;
		for (Vector<String> entry : textlist) {
			if (entry.get(0).equals(member)) {
				e = entry;
			}
		}
		if (e == null)
			return null;

		switch (dirtype) {
		case LOAD:
			return String.format(
					"%-8s %-8s %-6s %-6s %-6s %-3s %-3s %-2s %-4s %-4s",
					e.get(0), e.get(1), e.get(2), e.get(3), e.get(4), e.get(5),
					e.get(6), e.get(7), e.get(8), e.get(9));
		case TEXT:
			return String.format("%-8s %-8s %-6s %-7s %-10s %-16s %-8s", e.get(0),
					e.get(1), e.get(2), e.get(3), e.get(4), e.get(5), e.get(6));

		}
		return null;
	}
	/*
	 * public DirTableModel getTableModel() { this.tm = new
	 * DirTableModel(getHeader(), 0); for (int i = 0; i < textlist.size(); i++)
	 * { tm.addRow(textlist.get(i)); } return this.tm; }
	 * 
	 * class DirTableModel extends DefaultTableModel {
	 * 
	 * private static final long serialVersionUID = 3525338472005722727L;
	 * 
	 * public DirTableModel(String[] header, int i) { super(header, i); }
	 * 
	 * @Override public boolean isCellEditable(int arg0, int arg1) { return
	 * false; } }
	 */
}
