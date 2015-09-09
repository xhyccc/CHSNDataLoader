package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CPTLineReader {
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
			while (ln != null&&lindex<lnn) {
				if (!ln.toLowerCase().contains("null")) {
					String[] lns = ln.split(",");
					if (isNumeric(lns[3]))
						_base.insertRecord(lns[0], lns[2], lns[3], Integer.parseInt(lns[4]), 
								lns[5].toLowerCase().equals("M") ? 1 : 0,Integer.parseInt(lns[0]));
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
