package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ICDLineReader2 {
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	public static EHRecordBase load(String filepath, String name, int lnn) {
		EHRecordBase _base = EHRecordBase.getBase(name);
		int lindex = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String ln = br.readLine();
			ln = br.readLine();
			while (ln != null && lindex < lnn) {
				if (!ln.toLowerCase().contains("null")) {
					String[] lns = ln.split(",");
					// if (isNumeric(lns[3]))
					String code = lns[3];// .replaceAll("\\.", "");
					System.out.println(lns[3]);
					if (hit(code)) {
						_base.insertRecord(lns[1], lns[2],
								map(code) /* map.codeMap.get(code) */, Integer.parseInt(lns[4]),
								lns[5].toLowerCase().equals("m") ? 1 : 0, 1);
					} else {
						_base.missingLines++;
					}
				}
				System.out.println("read lines " + (lindex++));
				ln = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return _base;
	}

	public static boolean hit(String code) {
		if (code.startsWith("300.")) {
			return true;
		}
		if (code.startsWith("296.")) {
			return true;
		}
		if (code.startsWith("303.")) {
			return true;
		}
		if (code.startsWith("304.")) {
			return true;
		}
		if (code.startsWith("305.")) {
			return true;
		}
		if (code.toLowerCase().startsWith("v11.3")) {
			return true;
		}
		if (code.toLowerCase().startsWith("v65.42")) {
			return true;
		}
		if(code.startsWith("977")){
			return true;
		}
		if(code.startsWith("292")){
			return true;
		}
		if(code.startsWith("980")){
			return true;
		}
		

		return false;
	}

	
	public static String map(String code) {
		if (code.startsWith("300.")) {
			return "row_1";
		}
		if (code.startsWith("296.")) {
			return "row_1";
		}
		if (code.startsWith("303.")) {
			return "row_2";
		}
		if (code.startsWith("304.")) {
			return "row_2";
		}
		if (code.startsWith("305.")) {
			return "row_2";
		}
		if (code.toLowerCase().startsWith("v11.3")) {
			return  "row_2";
		}
		if (code.toLowerCase().startsWith("v65.42")) {
			return  "row_2";
		}
		if(code.startsWith("977")){
			return "row_2";
		}
		if(code.startsWith("292")){
			return "row_2";
		}
		if(code.startsWith("980")){
			return "row_2";
		}


		return  "row_2";
	}
}
