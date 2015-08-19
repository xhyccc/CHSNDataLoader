package edu.uva.sys.ehrloader;

import java.util.HashSet;
import java.util.Set;

import edu.uva.sys.ehrloader.recovery.*;

public class CPTMain {

	public static void main(String[] args) {

		EHRecordBase base = CPTLineReader.load(
				"/Users/bertrandx/Box Sync/Hao Research/MH utilization study/subject1_mh/mh_sample.csv", "cptcode");
		System.out.println("patients: " + base.getPatients().size());
		System.out.println("codes: " + base.getCodes().size());
		System.out.println("dates: " + base.getDates().size());

		double[][] fm = base.getBinaryMatrix();
		double[][] fm2 = base.getBinaryMatrixWithRandomVisitMissing(0.2);
		Set<String> missingparts = new HashSet<String>();

		System.out.println("matrix " + fm.length + " x " + fm[0].length);
		int sum_miss_codes = 0;
		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j]==0&&fm[i][j]>0) {
					sum_miss_codes++;
					missingparts.add(i+","+ j);
				}
			}
		}
		System.out.println("code missing: " + sum_miss_codes);
		// Recovery r=new PCARecovery(0.1);
		
		Set<String> recoveryparts = new HashSet<String>();


	//	Recovery r = new L1NMFRecovery(5);
		Recovery r = new NMFRecovery(5);
		
		double[][] fm3 = r.recover(fm2);
		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j]==0 && fm3[i][j] > 0.0) {
				//	System.out.println(fm[i][j]+"\t"+fm2[i][j]+"\t"+fm3[i][j]);
					recoveryparts.add(i +","+ j);
				}
			}
		}
		System.out.println("f1 score: " + F1(missingparts,recoveryparts));
		System.out.println("precision score: " + precision(missingparts,recoveryparts));
		System.out.println("recall score: " + recall(missingparts,recoveryparts));


	}

	public static double F1(Set<String> miss, Set<String> recover){
		int intersect=0;
		for(String i:miss)
			if(recover.contains(i))
				intersect++;
		return 2.0*(double)intersect/(double)(miss.size()+recover.size());
	}
	public static double precision(Set<String> miss, Set<String> recover){
		int intersect=0;
		for(String i:miss)
			if(recover.contains(i))
				intersect++;
		return (double)intersect/(double)(recover.size());
	}
	public static double recall(Set<String> miss, Set<String> recover){
		int intersect=0;
		for(String i:miss)
			if(recover.contains(i))
				intersect++;
		return 2.0*(double)intersect/(double)(recover.size());
	}
	
}
