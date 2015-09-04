package edu.uva.sys.ehrloader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.uva.sys.ehrloader.recovery.*;

public class ICDMain {

	public static void main(String[] args) {

		EHRecordBase base = ICDLineReader.load(
				"/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/non-mh_icd.csv", "icdcode");
		System.out.println("patients: " + base.getPatients().size());
		System.out.println("codes: " + base.getCodes().size());
		System.out.println("dates: " + base.getDates().size());

		double[][] fm = base.getBinaryMatrix();
		double[][] fm2 = base.getBinaryMatrixWithRandomVisitMissing(0.0);
		HashMap<Integer,Set<Integer>> missingcodes=new HashMap<Integer,Set<Integer>>();

		
		System.out.println("matrix " + fm.length + " x " + fm[0].length);
		int sum_miss_codes = 0;
		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j]==0&&fm[i][j]>0) {
					sum_miss_codes++;
					if(!missingcodes.containsKey(i))
						missingcodes.put(i, new HashSet<Integer>());
					missingcodes.get(i).add(j);
				}
			}
		}
		System.out.println("code missing: " + sum_miss_codes);
		// Recovery r=new PCARecovery(0.1);
		
		HashMap<Integer,Set<Integer>> recoverycodes=new HashMap<Integer,Set<Integer>>();


		Recovery r = new L1NMFRecovery(5);
	//	Recovery r = new NMFRecovery(5);
		
		double[][] fm3 = r.recover(fm2);
		int sum_recover_codes = 0;

		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j]==0 && fm3[i][j] > 0.0) {
					sum_recover_codes++;
					if(!recoverycodes.containsKey(i))
						recoverycodes.put(i, new HashSet<Integer>());
					recoverycodes.get(i).add(j);

				}
			}
		}
		
		int intersection=intersection(missingcodes,recoverycodes);
		double precision=(double)intersection/(double)sum_recover_codes;
		double recall=(double)intersection/(double)sum_miss_codes;
		double f1=2*precision*recall/(precision+recall);
		
		System.out.println("original matrix " + fm.length + " x " + fm[0].length);
		System.out.println("missing matrix " + fm2.length + " x " + fm2[0].length);
		System.out.println("recovered matrix " + fm3.length + " x " + fm3[0].length);

		
		System.out.println("missing: " + sum_miss_codes);
		System.out.println("recover: " + sum_recover_codes);
		System.out.println("intersect:"+intersection);
		System.out.println("f1 score: " + f1);
		System.out.println("precision score: " + precision);
		System.out.println("recall score: " + recall);


	}

	public static double F1(Set<String> miss, Set<String> recover){
		int intersect=0;
		for(String i:miss)
			if(recover.contains(i))
				intersect++;
		return 2.0*(double)intersect/(double)(miss.size()+recover.size());
	}
	public static int intersection(HashMap<Integer,Set<Integer>> miss, HashMap<Integer,Set<Integer>> recover){
		int intersect=0;
		for(int p:miss.keySet()){
			if(recover.containsKey(p)){
				for(int code:miss.get(p)){
					if(recover.get(p).contains(code)){
						intersect++;
					}
				}
			}
		}
		
		return intersect;
	}

}
