package de.roland.jxmit;

public class TextType {

	private int id;
	private String memnonic;
	private String destricption;
	private int len;
	private byte[] bytes;
	private String type;

	TextType(TextType t) {
		this.id = t.id;
		this.memnonic = new String(t.memnonic);
		this.destricption = new String(t.destricption);
		this.len = t.len;
		bytes = new byte[t.len];
		if(t.bytes != null) {
			System.arraycopy(t.bytes, 0, this.bytes, 0, t.len);
		}
		this.type = t.type;
		
	}
	
	TextType(int id, String memnonic, String destricption) {
		this.id = id;
		this.memnonic = new String(memnonic);
		this.destricption = new String(destricption);
		this.len = 0;
		this.bytes = null;
		this.type = null;
	}
	
	TextType(int id, String memnonic, String destricption, int len, byte[] bytes) {
		this.id = id;
		this.memnonic = new String(memnonic);
		this.destricption = new String(destricption);
		this.len = len;
		this.bytes = bytes;
		this.type = null;
	}

	public int getId() {
		return this.id;
	}

	public String getMemnonic() {
		return this.memnonic;
	}

	public String getDescription() {
		return this.destricption;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setLen(int len) {
		this.len = len;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public byte[] getBytes() {
		return this.bytes;
	}
	
	public void setBytes(byte[] bytes) {
		byte[] b = new byte[this.len];
		System.arraycopy(bytes, 0, b, 0, this.len);
		this.bytes = b;
	}
	
	
}
