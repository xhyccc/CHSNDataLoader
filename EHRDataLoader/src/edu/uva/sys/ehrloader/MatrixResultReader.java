package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MatrixResultReader {

public static void main(String[] args) throws IOException{
	HashMap<String, Double> error_1 = new HashMap<String, Double>();
	HashMap<String, Double> error_2 = new HashMap<String, Double>();
	
	int sampleSize=500;
	
	BufferedReader br = new BufferedReader(
			new FileReader("/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/error-precision-matrix－"+sampleSize+".txt"));
	String ln = br.readLine();
	int count=0;
	while (ln != null) {
		String[] lns = ln.split("\t");
		if (lns.length > 1) {
			if (!error_1.containsKey(lns[0])) {
				error_1.put(lns[0], 0.0);
				error_2.put(lns[0], 0.0);
			}
			double _e1=Double.parseDouble(lns[1]);
			double _e2=Double.parseDouble(lns[2]);

			error_1.put(lns[0], error_1.get(lns[0])+_e1);
			error_2.put(lns[0], error_2.get(lns[0])+_e2);
		}
		ln = br.readLine();
		count++;
	}
	for(String key:error_1.keySet()){
	System.out.println(sampleSize+"\t"+key+"\t"+(double)(error_1.get(key))/count+"\t"+(double)(error_2.get(key))/count);
	}
	br.close();


}

}
