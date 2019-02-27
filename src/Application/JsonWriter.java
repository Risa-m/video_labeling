package Application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;

public class JsonWriter {

	private PrintWriter jsonFileWriter;
	private String filename;
	// private int fileSaveCount = 1;
	private int fileColNum;
	private boolean isFileOpen = false;

	public JsonWriter(String filename) {
		this.filename = filename;
		fileColNum = 0;
		// fileSaveCount = 1;
	}

	public JsonWriter() {
		fileColNum = 0;
		// fileSaveCount = 1;
	}

	public void setFileName(String filename) {
		this.filename = filename;
	}

	public void writerOpen(String filename) {
		this.filename = filename;
		writerOpen();
	}

	public void writerOpen() {
		File file = new File(filename + ".json");
		// fileSaveCount++;
		try {
			jsonFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			isFileOpen = true;
			jsonFileWriter.println("[");
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	public void writerClose() {
		if (isFileOpen) {
			jsonFileWriter.println();
			jsonFileWriter.println("]");
			jsonFileWriter.close();
			fileColNum = 0;
			// fileSaveCount = 1;
			isFileOpen = false;
		}
	}

	public void write(double second, int label) {
		if (fileColNum != 0 && isFileOpen) {
			jsonFileWriter.println(",");
		}
		if (isFileOpen) {
			StringBuilder sb = new StringBuilder();
			sb.append(formatJSON(second, label).toJSONString());
			jsonFileWriter.print(sb.toString());
			fileColNum++;
		}
	}
	
	public void writeComment(String comment) {
		if (fileColNum != 0 && isFileOpen) {
			jsonFileWriter.println(",");
		}
		if (isFileOpen) {
			jsonFileWriter.print("{\"comment\": \""+comment+"\"}");
			fileColNum++;
		}		
	}

	@SuppressWarnings("unchecked")
	private JSONObject formatJSON(double second, int label) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("label", "" + label);
		jsonObject.put("second", "" + second);
		return jsonObject;
	}

}
