package api;

public class Message {
	private String code;
	private String content;
	//200 success
	public Message(String code, String content) {
		super();
		this.code = code;
		this.content = content;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
