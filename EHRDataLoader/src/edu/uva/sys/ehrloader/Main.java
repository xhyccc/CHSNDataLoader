package edu.uva.sys.ehrloader;

import edu.uva.sys.ehrloader.recovery.PCARecovery;
import edu.uva.sys.ehrloader.recovery.Recovery;

public class Main {
	
	public static void main(String[] args){
		
		EHRecordBase base=LineReader.load("/Users/bertrandx/Box Sync/Hao Research/MH utilization study/subject1_mh/mh_sample.csv", "cptcode");
		System.out.println("patients: "+base.getPatients().size());
		System.out.println("codes: "+base.getCodes().size());
		System.out.println("dates: "+base.getDates().size());

		double[][] fm=base.getBinaryMatrix();
		double[][] fm2=base.getBinaryMatrixWithRandomVisitMissing(0.1);

		System.out.println("matrix "+fm.length+" x "+fm[0].length);
		int sum_miss_codes=0;
		for(int i=0;i<fm.length;i++){
			for(int j=0;j<fm[i].length;j++){
				if(fm[i][j]!=fm2[i][j])
					sum_miss_codes++;
			}
		}
		System.out.println("code missing: "+sum_miss_codes);
		Recovery r=new PCARecovery();
		int sum_mismatch_codes=0;
		double[][] fm3=r.recover(fm2);
		for(int i=0;i<fm.length;i++){
			for(int j=0;j<fm[i].length;j++){
				if(Math.abs(fm[i][j]-fm3[i][j])>0.5)
					sum_mismatch_codes++;
			}
		}
		System.out.println("code mismatching: "+sum_mismatch_codes);

	}

}
