package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class DataOutput {

	public static void saveFile(EHRecordBase base, EHRRecordMap map, double[][] matrix, String fpath) {
		try {
			PrintStream ps = new PrintStream(fpath + ".csv");
			PrintStream ps2 = new PrintStream(fpath + "fp.txt");

			List<String> patients = base.getPatients();
			List<String> codes = base.getCodes();
			ps.print("patient");
			ps.print(",age");
			// ps.print(",gender");
			for (String code : codes) {
				ps.print(",code:" + code);
			}
			ps.print(",mental-disorder");
			ps.println();
			base.getLabels();

			for (int i = 0; i < patients.size(); i++) {
				ps.print(i);
				ps.print("," + base._ages.get(patients.get(i)));
				// ps.print("," + base._gender.get(patients.get(i)));
				for (int j = 0; j < codes.size(); j++) {
					if (matrix[i][j] >= 1) {
						ps.print(","+matrix[i][j]);
						ps2.println(i + "," + codes.get(j));
					} else {
						ps.print(",0.0");
					}
				}
				if (base._labels.get(patients.get(i)) == 1) {
					ps.println(",True");
			//		ps2.println(i+",mental_disorder");
				} else {
					ps.println(",False");
				}
			}
			ps.close();
			ps2.close();
			ps = new PrintStream(fpath + ".txt");
			ps.println("description of each row");
			ps.println("1 col: patient id");
			ps.println("2 col: age");
			// ps.println("3rd col: gender");
			int index = 1;
			for (String code : codes) {
				ps.println((2 + index) + " col: " + code);
				index++;
			}
			ps.println("label: mental-disorder");
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		EHRRecordMap map = new EHRRecordMap("/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/mapping.txt");

		EHRecordBase base = ICDLineReader.load(map,
				"/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/non-mh_icd.csv", "x_icdcode", 30000);

		EHRecordBase base_2 = ICDLineReader.load(map, "/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/icd_MD.csv",
				"y_icdcode", 30000);

		base.insertRecords(base_2);
		base.setPositiveLabel(MHCode.codes);
		base.removeVisitsAfter(MHCode.codes, 30);
		base.removePatientLessNVisit(3);

		double[][] fm = base.getFrequencyMatrix();
		System.out.println("matrix " + fm.length + " x " + fm[0].length);

		saveFile(base, map, fm, "/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/code_matrix");
	//	System.out.println("missing lines\t" + base.missingLines);
	}

}
