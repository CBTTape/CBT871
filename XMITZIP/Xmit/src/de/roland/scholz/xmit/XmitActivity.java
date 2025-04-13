package de.roland.scholz.xmit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.roland.scholz.xmit.Directory.DIRTYPE;

public class XmitActivity extends Activity implements OnItemClickListener {

	private final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
	private final int REQUEST_CODE_SAVE_MEMBER = 2;
	private final int REQUEST_CODE_VIEW_MEMBER = 3;
	private final int REQUEST_CODE_DUMP_MEMBER = 4;

	private final String donateURL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=V3KDUBWNYX82Q";

	private final String[] from = { "1", "2", "3" };
	private final int[] to = { R.id.text1, R.id.text2, R.id.text3 };

	private TextView mEditFileName, memberCount, memberText;
	private ListView memberList;
	private RadioButton rbText, rbBin;
	
	private Directory dir = null;
	private String member = null;

	private String xmitFileName = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	private File file = null;
	
	private ArrayList<HashMap<String, String>> list2 = new ArrayList<HashMap<String, String>>(
			from.length);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mEditFileName = (TextView) findViewById(R.id.editTextFileName);
		memberList = (ListView) findViewById(R.id.memberList);
		rbText = (RadioButton) findViewById(R.id.radioText);
		rbBin = (RadioButton) findViewById(R.id.radioBinary);
		memberCount = (TextView) findViewById(R.id.memberCount);
		memberText = (TextView) findViewById(R.id.memberText);

		memberList.setOnItemClickListener(this);
		registerForContextMenu(memberList);
		mEditFileName.setText(xmitFileName);

		if (memberCount.getText().equals(""));
			memberCount.setText("0");

		if (this.getIntent().getData() == null) {
			if (!(new File(xmitFileName).isFile()))
				return;
		} else {
			xmitFileName = this.getIntent().getData().getPath();
		}
		mEditFileName.setText(xmitFileName);

		openDialog();
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.memberList) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			HashMap<String, String> item = (HashMap<String, String>) (memberList
					.getAdapter().getItem(info.position));
			menu.setHeaderTitle(item.get("1"));

			menu.add(0, R.string.view, 0, R.string.view);
			menu.add(0, R.string.view_dump, 0, R.string.view_dump);
			menu.add(0, R.string.extract, 0, R.string.extract);
			menu.add(0, R.string.extract_dump, 0, R.string.extract_dump);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		int title = item.getItemId();
		HashMap<String, String> menuItem = (HashMap<String, String>) (memberList
				.getAdapter().getItem(info.position));
		member = menuItem.get("1");

		Intent intent = null;
		String fname = null;

		switch (title) {
		case R.string.view:
			viewMember(false);
			break;
		case R.string.view_dump:
			viewMember(true);
			break;
		case R.string.extract:
			intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
			fname = getExtractURL(rbText.isChecked());
			intent.setData(Uri.fromFile(new File(fname)));
			intent.putExtra(FileManagerIntents.EXTRA_TITLE,
					getString(R.string.save_title));
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT,
					getString(R.string.save_button));

			startActivityForResult(intent, REQUEST_CODE_SAVE_MEMBER);
			break;
		case R.string.extract_dump:
			intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
			fname = getExtractURL(true);
			intent.setData(Uri.fromFile(new File(fname)));
			intent.putExtra(FileManagerIntents.EXTRA_TITLE,
					getString(R.string.save_title));
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT,
					getString(R.string.save_button));

			startActivityForResult(intent, REQUEST_CODE_DUMP_MEMBER);
			break;
		}
		return true;
	}

	private String getExtractURL(boolean text) {
		String ext = (text ? ".txt" : ".bin");

		return new String("file://"
				+ xmitFileName.substring(0,
						xmitFileName.lastIndexOf(File.separator) + 1)
				+ member.trim() + ext);
	}

	public void selfDestruct(View v) {
		this.finish();
	}

	public void selectDialog(View v) {
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		intent.setData(Uri.fromFile(new File("file://" + xmitFileName)));
		intent.putExtra(FileManagerIntents.EXTRA_TITLE,
				getString(R.string.open_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT,
				getString(R.string.open_button));

		startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
	}

	public void donate(View v) {
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW,
				Uri.parse(donateURL));
		startActivity(launchBrowser);
	}

	public void openDialog() {
		String[] values;
		String dirtext;

		try {
			list2.clear();
			openXmitFile(xmitFileName);
			if (dir != null) {
				values = dir.getMembers();
				for (String member : values) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("1", member);
					dirtext = dir.getDirText(member).substring(18);
					if (dirtext.charAt(0) != ' ') {
						map.put("2", dir.getDirHeader().substring(18));
						map.put("3", dirtext);
					}
					list2.add(map);
				}
			} else {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("1", Xmit.getFilename());
				map.put("2", null);
				map.put("3", null);
				list2.add(map);
			}

			SimpleAdapter adapter = new SimpleAdapter(this, list2,
					R.layout.my_list_item_2, from, to);
			memberList.setAdapter(adapter);

		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * This is called after the file manager finished.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_CODE_PICK_FILE_OR_DIRECTORY:
			if (resultCode == RESULT_OK && data != null) {
				String filename = data.getData().getPath();
				if (filename != null) {
					xmitFileName = filename;
					mEditFileName.setText(xmitFileName);
					openDialog();
				}
			}
			break;
		case REQUEST_CODE_SAVE_MEMBER:
			if (resultCode == RESULT_OK && data != null) {
				String filename = null;
				if (resultCode == RESULT_OK && data != null) {
					filename = data.getData().getPath();
					extractMember(filename, rbText.isChecked(), false);
				}
				break;
			}
		case REQUEST_CODE_VIEW_MEMBER:
			break;

		case REQUEST_CODE_DUMP_MEMBER:
			String filename = null;
			if (resultCode == RESULT_OK && data != null) {
				filename = data.getData().getPath();
				extractMember(filename, true, true);
			}
		}
	}

	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {

		view = (RelativeLayout) view;
		SimpleAdapter a = (SimpleAdapter) arg0.getAdapter();
		HashMap<String, String> h = (HashMap<String, String>) (a.getItem(arg2));
		member = h.get("1");
		viewMember(false);
	}

	private void viewMember(boolean dump) {
		String ext = (rbText.isChecked() ? "txt" : "bin");

		file = new File(Environment.getExternalStorageDirectory(),
				member.trim() + '.' + ext);
		if (file.exists())
			file.delete();

		extractMember(file.getPath(), rbText.isChecked(), dump);

		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "text/*");
		startActivityForResult(intent, REQUEST_CODE_VIEW_MEMBER);
	}

	protected void extractMember(String filename, boolean text, boolean dump) {
		Vector<byte[]> v = new Vector<byte[]>();
		boolean first = true;
		boolean iebcopy = Xmit.getIebcopy();

		try {
			FileWriter fw = null;
			FileOutputStream fs = null;

			if (text || dump) {
				fw = new FileWriter(filename);
			} else {
				fs = new FileOutputStream(filename);
			}

			byte[] c = null;
			if (iebcopy) {
				v = Xmit.openMember(member);
				while (v != null) {
					for (byte[] b : v) {
						c = b;
						if (b != null) {
							if (fw != null)
								if (dump)
									fw.write(Xmit.dump(b, b.length) + "\n");
								else
									fw.write(XmitUtils
											.getEbcdic(b, 0, b.length) + "\n");
							if (fs != null)
								fs.write(b);
						}
					}
					if (c == null)
						v = null;
					else
						v = Xmit.getMemberData(null);
				}
			} else {
				while ((c = Xmit.getFileData(first)) != null) {
					first = false;
					if (fw != null)
						if (dump)
							fw.write(Xmit.dump(c, c.length) + "\n");
						else
							fw.write(XmitUtils.getEbcdic(c, 0, c.length) + "\n");
					if (fs != null)
						fs.write(c);
				}
			}
			if (fw != null)
				fw.close();
			if (fs != null)
				fs.close();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void openXmitFile(String filename) throws IOException {
		rbText.setChecked(true);
		memberText.setText(R.string.members);
		memberCount.setText("0");
		Xmit.readHeader(filename);
		dir = Xmit.getDirectory();
		if (dir != null && dir.getDirType() == DIRTYPE.LOAD) {
			rbBin.setChecked(true);
		}
		memberCount.setText(String.valueOf(Xmit.getMemberCount()));
	}
}