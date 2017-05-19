package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ICDLineReader {
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	public static EHRecordBase load(EHRRecordMap map, String filepath, String name, int lnn) {
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
					String code = lns[3].replaceAll("\\.", "");
					//String code = 
					if (map.codeMap.containsKey(code)) {
						_base.insertRecord(lns[1], lns[2], map.codeMap.get(code), Integer.parseInt(lns[4]),
								lns[5].toLowerCase().equals("m") ? 1 : 0, 1);
					}else{
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

}
