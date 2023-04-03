package de.roland.jxmit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import de.roland.jxmit.Directory.DIRTYPE;

public class JXmitGui extends JFrame implements ActionListener, FocusListener,
		MouseListener {

	private static JTextArea logTa = new JTextArea();
	private static final long serialVersionUID = -7081306970385131402L;
	JPanel jPanelExtract = new JPanel();
	JScrollPane jPanelCenter = new JScrollPane();
	JTabbedPane jPanelBottom = new JTabbedPane();
	JTabbedPane jPanelLeft = new JTabbedPane();
	JScrollPane jPanelLog = new JScrollPane();
	JTextArea dataTa = new JTextArea();
	JScrollPane jPanelData = new JScrollPane(dataTa);
	JTable jtable = new JTable();
	Font fontData = new Font("Monospaced", Font.PLAIN, 14);
	Font fontLog = new Font("Monospaced", Font.PLAIN, 12);

	JMenuBar menueLeiste = new JMenuBar();
	JMenu datei = new JMenu("File");
	JMenu hilfe = new JMenu("Help");
	JMenuItem oeffnen = new JMenuItem("Open...");
	JMenuItem extract = new JMenuItem("Extract...");
	JMenuItem view = new JMenuItem("View");
	JMenuItem beenden = new JMenuItem("Quit");
	JMenuItem faq = new JMenuItem("Help Text");
	JMenuItem about = new JMenuItem("About");
	JComboBox cmb = new JComboBox();
	JRadioButton rbBin = new JRadioButton("Bin");
	JRadioButton rbText = new JRadioButton("Text");
	ButtonGroup bgTextBin = new ButtonGroup();
	JRadioButton rbLf = new JRadioButton("Unix (LF)");
	JRadioButton rbCrLf = new JRadioButton("Win (CR LF)");
	ButtonGroup bgCrLf = new ButtonGroup();
	JTextField txtFilename = new JTextField(20);
	JFileChooser xmitFc = new JFileChooser();
	JFileChooser extractFc = new JFileChooser();
	JLabel l1 = new JLabel("Extract parameter:");
	JLabel l2 = new JLabel("Codepage:");
	JLabel l3 = new JLabel("Loaded File:");
	JLabel l4 = new JLabel("Newline:");
	JLabel lDSNtxt = new JLabel("DSN:");
	JLabel lDSN = new JLabel();

	JPopupMenu pm = new JPopupMenu();
	JMenuItem popupView = new JMenuItem("View");
	JMenuItem popupExtract = new JMenuItem("Extract");

	Directory dir = null;

	public static void main(String[] args) {
		String filename = null;
		if (args.length > 0) {
			filename = args[0];
		}
		new JXmitGui(filename);
	}

	public static void ibm1047() {
		byte[] char1047 = new byte[256];
		for (int i = 0; i < char1047.length; i++) {
			char1047[i] = (byte) (i);
		}
		ByteBuffer bb = ByteBuffer.wrap(char1047);
		Charset charset = Charset.forName("IBM1047");
		CharBuffer cb = charset.decode(bb);
		char[] chars = cb.array();
		try {
			FileWriter fw = new FileWriter("ibm1047.txt");
			for (int i = 0; i < chars.length; i++) {
				if (i <= 64) {
					fw.write("' ',");
				} else {
					fw.write("'" + chars[i] + "',");

				}
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public JXmitGui(String filename)  {
		super("jXmit");
		addLogRow(JXmit.title);

		lDSN.setForeground(Color.BLUE);

		pm.add(popupExtract);
		pm.add(popupView);
		popupExtract.addActionListener(this);
		popupView.addActionListener(this);

		jtable.getTableHeader().setReorderingAllowed(false);
		jtable.setFont(fontLog);
		jtable.addMouseListener(this);

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		dataTa.setFont(fontData);
		dataTa.setEditable(false);
		dataTa.setForeground(Color.BLACK);
		logTa.setFont(fontLog);
		logTa.setForeground(Color.RED);
		txtFilename.addFocusListener(this);

		jPanelLog.getViewport().add(logTa);

		jPanelCenter.getViewport().add(jtable);
		jPanelCenter.setPreferredSize(new Dimension(700, 400));
		jPanelLog.setPreferredSize(new Dimension(700, 250));
		jPanelData.setPreferredSize(new Dimension(700, 250));
		jPanelBottom.add("Log", jPanelLog);
		jPanelBottom.add("Data", jPanelData);

		jPanelLeft.add("Extract", jPanelExtract);

		jPanelExtract.setLayout(gbl);
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = c.gridy = 0;
		jPanelExtract.add(l3, c); // Loaded File

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		jPanelExtract.add(txtFilename, c); // filename

		c.gridy++; // Transmitted DSN
		jPanelExtract.add(lDSNtxt, c);
		c.gridx = 1;
		jPanelExtract.add(lDSN, c);

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		jPanelExtract.add(l1, c); // Extract parameter:

		c.gridy++;
		c.gridx = 0;
		jPanelExtract.add(rbText, c);
		c.gridx = 1;
		jPanelExtract.add(rbBin, c);

		c.gridy++;
		c.gridx = 0;
		jPanelExtract.add(l2, c); // Codepage
		c.gridx = 1;
		jPanelExtract.add(cmb, c);

		c.gridy++;
		c.gridx = 0;
		jPanelExtract.add(l4, c);

		c.gridy++;
		c.gridx = 0;
		jPanelExtract.add(rbLf, c);
		c.gridx = 1;
		jPanelExtract.add(rbCrLf, c);

		Collection<Charset> values = Charset.availableCharsets().values();

		for (Charset cs : values) {
			cmb.addItem(cs.displayName());
		}
		cmb.setSelectedItem("IBM1047");
		cmb.addActionListener(this);

		bgTextBin.add(rbBin);
		bgTextBin.add(rbText);

		bgCrLf.add(rbLf);
		bgCrLf.add(rbCrLf);

		rbCrLf.setSelected(true);

		Container contentpane = this.getContentPane();
		contentpane.add(jPanelLeft, BorderLayout.WEST);
		contentpane.add(jPanelCenter, BorderLayout.CENTER);
		contentpane.add(jPanelBottom, BorderLayout.SOUTH);

		createMenu();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		this.setVisible(true);

		if (!(filename == null)) {
			openXmitFile(filename);
			xmitFc.setSelectedFile(new File(filename));
		}
	}

	private void createMenu() {

		oeffnen.addActionListener(this);
		beenden.addActionListener(this);
		faq.addActionListener(this);
		about.addActionListener(this);
		extract.addActionListener(this);
		view.addActionListener(this);

		menueLeiste.add(datei);
		menueLeiste.add(hilfe);

		datei.add(oeffnen);
		datei.addSeparator();
		datei.add(extract);
		datei.add(view);
		datei.addSeparator();
		datei.add(beenden);

		hilfe.add(faq);
		hilfe.add(about);

		this.getContentPane().add(menueLeiste, BorderLayout.NORTH);
	}

	public static void addLogRow(String s) {
		logTa.append(s + "\n");
		logTa.setCaretPosition(logTa.getText().length());
	}

	public void actionPerformed(ActionEvent object) {
		Object o = object.getSource();
		if (o == oeffnen) {
			int retval = xmitFc.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				String filename = xmitFc.getSelectedFile().getParent()
						+ File.separator + xmitFc.getSelectedFile().getName();
				openXmitFile(filename);
			}
		}
		if (o == extract || o == popupExtract) {
			extractMember();
		}
		if (o == view || o == popupView) {
			viewData();
		}
		if (o == beenden) {
			this.dispose();
		}
		if (o == faq) {
		}
		if (o == about) {
		}
		if (o == cmb) {
			JXmit.codepage = (String) cmb.getSelectedItem();
		}
	}

	private void openXmitFile(String filename) {
		txtFilename.setText(filename);
		try {
			JXmit.readHeader(filename);
		} catch (IOException e) {
		}
		lDSN.setText(JXmit.getFilename());
		rbText.setSelected(true);

		// JXmit.display();
		dir = JXmit.getDirectory();
		if (!(dir == null)) {
			if (dir.getDirType() == DIRTYPE.TEXT)
				rbText.setSelected(true);
			else
				rbBin.setSelected(true);

			jtable.setModel(dir.getTableModel());
			TableColumn tc;
			try {
				if (!((tc = jtable.getColumn("Modified")) == null)) {
					tc.setPreferredWidth(100);
				}
			} catch (Exception e) {
			}

			addLogRow("File: " + filename + " read.");
		} else {
			jtable.setModel(new DefaultTableModel());
		}
	}

	private void extractMember() {
		int[] rows = jtable.getSelectedRows();
		int returnVal = 0;
		String filepattern = null;
		Vector<byte[]> v = new Vector<byte[]>();
		File f = null;

		String newline;
		if (rbLf.isSelected()) {
			newline = "\n";
		} else {
			newline = "\r\n";
		}

		for (int i = 0; i < rows.length; i++) {
			String s = (String) jtable.getValueAt(rows[i], 0);
			if (extractFc.getSelectedFile() == null) {
				extractFc.setSelectedFile(xmitFc.getSelectedFile());
			}
			filepattern = extractFc.getSelectedFile().getPath()
					+ File.separator;

			if (rbBin.isSelected()) {
				filepattern = filepattern + s + ".bin";
			} else {
				filepattern = filepattern + s + ".txt";
			}
			f = new File(filepattern);

			extractFc.setSelectedFile(f);
			returnVal = extractFc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				f = extractFc.getSelectedFile();
				addLogRow("writing File: " + f + " ...");
				try {
					FileWriter fw = null;
					FileOutputStream fs = null;

					if (rbText.isSelected()) {
						fw = new FileWriter(f);
					} else {
						fs = new FileOutputStream(f);
					}

					byte[] c = null;
					v = JXmit.openMember(s);
					while (v != null) {
						for (byte[] b : v) {
							c = b;
							if (b != null) {
								if (fw != null)
									fw.write(JXmit.getEbcdic(b, 0, b.length)
											+ newline);
								if (fs != null)
									fs.write(b);
							}
						}
						if (c == null)
							v = null;
						else
							v = JXmit.getMemberData(null);
					}
					if (fw != null)
						fw.close();
					if (fs != null)
						fs.close();
					addLogRow("ready.");
				} catch (IOException e) {
					addLogRow(e.getStackTrace().toString());
				}
			}
		}
	}

	private void viewData() {
		if (JXmit.iebcopy)
			viewMember();
		else
			viewFile();
	}

	private void viewFile() {
		byte[] b = null;
		boolean first = true;
		String msg = "Data of File: " + lDSN.getText()
				+ " written to data panel";
		String data = null;
		dataTa.setText(null);
		try {
			while ((b = JXmit.getFileData(first)) != null) {
				first = false;
				if (rbBin.isSelected()) {
					data = JXmit.dump(b, b.length);
					dataTa.append(data + "\n");
				} else {
					dataTa.append(JXmit.getEbcdic(b, 0, b.length) + "\n");
				}
			}
		} catch (IOException e) {
		}

		dataTa.setCaretPosition(0);
		addLogRow(msg);
	}

	private void viewMember() {
		int[] rows = jtable.getSelectedRows();
		Vector<byte[]> v = new Vector<byte[]>();
		String member = null;
		String msg = null;
		dataTa.setText(null);

		for (int i = 0; i < rows.length; i++) {
			member = (String) jtable.getValueAt(rows[i], 0);
			msg = "Data of Member: " + member.trim();
			dataTa.append("\n");

			String data = null;
			try {
				byte[] c = null;
				v = JXmit.openMember(member);
				while (v != null) {
					for (byte[] b : v) {
						c = b;
						if (b != null) {
							if (rbBin.isSelected()) {
								data = JXmit.dump(b, b.length);
								dataTa.append(data + "\n");
							} else {
								dataTa.append(JXmit.getEbcdic(b, 0, b.length)
										+ "\n");
							}
						}
					}
					if (c == null)
						v = null;
					else
						v = JXmit.getMemberData(null);
				}
			} catch (IOException e) {
			}

			dataTa.setCaretPosition(0);
			addLogRow(msg + " written to data panel");
		}
	}

	@Override
	public void focusGained(FocusEvent arg0) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		// Object o = e.getSource();
		// if (o == txtFilename) {
		// openXmitFile(txtFilename.getText());
		// }
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
		showPopup(arg0);
	}

	public void mouseReleased(MouseEvent arg0) {
		showPopup(arg0);
	}

	private void showPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			jtable.changeSelection(jtable.rowAtPoint(e.getPoint()), 0, false,
					false);
			pm.show(e.getComponent(), e.getX(), e.getY());
		}
	}

}
