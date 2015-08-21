package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ICDLineReader {
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	public static EHRecordBase load(String filepath, String name) {
		EHRecordBase _base = EHRecordBase.getBase(name);
		int lindex = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String ln = br.readLine();
			while (ln != null && lindex < 10000) {
				if (!ln.toLowerCase().contains("null")) {
					String[] lns = ln.split(",");
				//	if (isNumeric(lns[3]))
						_base.insertRecord(lns[0], "2014-05-10", lns[2]);
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

