package edu.uva.sys.ehrloader.recovery;

public class RecoveryUtil {

	public static double[][] maxMerge(double[][] rec, double[][] org){
		for(int i=0;i<rec.length;i++){
			for(int j=0;j<rec[i].length;j++){
				rec[i][j]=Math.max(rec[i][j], org[i][j]);
			}
		}
		return rec;
	}
	
}
