package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class DataOutput {

	public static void saveFile(EHRecordBase base, double[][] matrix, String fpath) {
		try {
			PrintStream ps = new PrintStream(fpath + ".csv");
			List<String> patients = base.getPatients();
			List<String> codes = base.getCodes();
			// ps.print("patient id");
			// ps.print(",age");
			// ps.print(",gender");
			// for (String code : codes) {
			// ps.print(",code:" + code);
			// }
			// ps.print(",mental-disorder");
			// ps.println();
			base.getLabels();

			for (int i = 0; i < patients.size(); i++) {
				ps.print(i);
				ps.print("," + base._ages.get(patients.get(i)));
				ps.print("," + base._gender.get(patients.get(i)));
				for (int j = 0; j < codes.size(); j++) {
					ps.print("," + matrix[i][j]);
				}
				if (base._labels.get(patients.get(i)) == 1)
					ps.println(",YES");
				else
					ps.println(",NO");
			}
			ps.close();

			ps = new PrintStream(fpath + ".txt");
			ps.println("description of each row");
			ps.println("1st row: patient id");
			ps.println("2nd row: age");
			ps.println("3rd row: gender");
			int index=1;
			for (String code : codes) {
				ps.println((3+index)+"th row: "+code);
				index++;
			}
			ps.println("label: mental-disorder");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		EHRRecordMap map = new EHRRecordMap("/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/mapping.txt");

		EHRecordBase base = ICDLineReader.load(map,
				"/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/non-mh_icd.csv", "x_icdcode", 300000);

		EHRecordBase base_2 = ICDLineReader.load(map, "/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/icd_MD.csv",
				"y_icdcode", 300000);

		base_2.setLabelsForAllPatients(1);
		base.setLabelsForAllPatients(0);

		base.removePatientLessNVisit(10);
		base_2.removePatientLessNVisit(10);

		base.insertRecords(base_2);

		double[][] fm = base.getBinaryMatrix();
		System.out.println("matrix " + fm.length + " x " + fm[0].length);

		saveFile(base, fm, "/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/code_matrix");
		System.out.println("missing lines\t"+base.missingLines);
	}

}
