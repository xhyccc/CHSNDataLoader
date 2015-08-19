package edu.uva.sys.ehrloader;

import java.util.HashSet;
import java.util.Set;

import edu.uva.sys.ehrloader.recovery.*;

public class Main {

	public static void main(String[] args) {

		EHRecordBase base = LineReader.load(
				"/Users/bertrandx/Box Sync/Hao Research/MH utilization study/subject1_mh/mh_sample.csv", "cptcode");
		System.out.println("patients: " + base.getPatients().size());
		System.out.println("codes: " + base.getCodes().size());
		System.out.println("dates: " + base.getDates().size());

		double[][] fm = base.getBinaryMatrix();
		double[][] fm2 = base.getBinaryMatrixWithRandomVisitMissing(0.1);
		Set<Integer> missingparts = new HashSet<Integer>();

		System.out.println("matrix " + fm.length + " x " + fm[0].length);
		int sum_miss_codes = 0;
		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j]==0&&fm[i][j]>0) {
					sum_miss_codes++;
					missingparts.add(i * fm[i].length + j);
				}
			}
		}
		System.out.println("code missing: " + sum_miss_codes);
		// Recovery r=new PCARecovery(0.1);
		
		Set<Integer> recoveryparts = new HashSet<Integer>();


		Recovery r = new L1NMFRecovery(5);
		double[][] fm3 = r.recover(fm2);
		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j]==0 && fm3[i][j] > 0.05) {
					System.out.println(fm[i][j]+"\t"+fm2[i][j]+"\t"+fm3[i][j]);
					recoveryparts.add(i * fm[i].length + j);
				}
			}
		}
		System.out.println("f1 score: " + F1(missingparts,recoveryparts));

	}

	public static double F1(Set<Integer> miss, Set<Integer> recover){
		int intersect=0;
		for(int i:miss)
			if(recover.contains(i))
				intersect++;
		return 2.0*(double)intersect/(double)(miss.size()+recover.size());
	}
	
}
