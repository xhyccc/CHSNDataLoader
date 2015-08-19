package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class DataOutput {
	
	public static void saveFile(EHRecordBase base, double[][] matrix, String fpath){
		try {
			PrintStream ps=new PrintStream(fpath);
			List<String> patients=base.getPatients();
			List<String> codes=base.getCodes();
			ps.print("patient id");
			for(String code:codes){
				ps.print(",code:"+code);
			}
			for(int i=0;i<patients.size();i++){
				ps.print(i);
				for(int j=0;j<codes.size();j++){
					ps.print(","+matrix[i][j]);
				}
				ps.println();
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		EHRecordBase base=LineReader.load("/Users/bertrandx/Box Sync/Hao Research/MH utilization study/subject1_mh/mh_sample.csv", "cptcode");
		double[][] fm=base.getBinaryMatrix();
		saveFile(base,fm, "/Users/bertrandx/Box Sync/Hao Research/MH utilization study/subject1_mh/code_matrix.csv");

	}

}
