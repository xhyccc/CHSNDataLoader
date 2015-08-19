package edu.uva.sys.ehrloader;

public class Main {
	
	public static void main(String[] args){
		
		EHRecordBase base=LineReader.load("/Users/bertrandx/Box Sync/Hao Research/MH utilization study/subject1_mh/mh_sample.csv", "cptcode");
		double[][] fm=base.getFrequencyMatrix();
		System.out.println(fm.length+"\t"+fm[0].length);
		
	}

}
