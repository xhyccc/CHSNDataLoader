package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.uva.hdstats.Estimator;
import edu.uva.hdstats.da.Classifier;
import edu.uva.hdstats.da.LDA;
import edu.uva.hdstats.da.PDLassoLDA;
import edu.uva.libopt.numeric.*;
import edu.uva.sys.ehrloader.ml.FixedNumberTTSelection;
import edu.uva.sys.ehrloader.recovery.*;

public class ICDMainLDA {

	public static PrintStream ps= null;
	public static int t_size=2000;

	static {
		try {
			ps = new PrintStream("/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/accuracy-LDA"+t_size+".txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
		EHRRecordMap map = new EHRRecordMap(
				"/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/mapping.txt");

		EHRecordBase base = ICDLineReader.load(map,
				"/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/non-mh_icd.csv", "x_icdcode",300000);

		EHRecordBase base_2 = ICDLineReader.load(map,
				"/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/icd_MD.csv", "y_icdcode",300000);

		
		base.removePatientLessNVisit(5);
		base_2.removePatientLessNVisit(5);
	
		base.setLabelsForAllPatients(0);
		base_2.setLabelsForAllPatients(1);
		base.insertRecords(base_2);
	
		
		ps.println("patients: " + base.getPatients().size());
		ps.println("codes: " + base.getCodes().size());
		ps.println("dates: " + base.getDates().size());

		
		
		double[][] fm = base.getFrequencyMatrix();
		HashMap<Integer,Set<Integer>> missingcodes=new HashMap<Integer,Set<Integer>>();

		
		System.out.println("matrix " + fm.length + " x " + fm[0].length);
		ps.println("using NMF 20");
		double[][] recoveredData=dataRecovery(new NMFRecovery(20), fm, fm, missingcodes, 0);
		
		
		FixedNumberTTSelection s=new FixedNumberTTSelection(fm,base.getLabels(),5000);
		s.select();
		FixedNumberTTSelection ss=new FixedNumberTTSelection(recoveredData,base.getLabels(),5000);
		ss.select(s.trainIndex);

		LDA lda=new LDA(s.getTrainingSet(),s.getTrainingLabels());
		PDLassoLDA slda=new PDLassoLDA(s.getTrainingSet(),s.getTrainingLabels());
		lda=new LDA(ss.getTrainingSet(),ss.getTrainingLabels());
		slda=new PDLassoLDA(ss.getTrainingSet(),ss.getTrainingLabels());
		accuracy("LDA",s.getTestingSet(),s.getTestingLabels(),lda);
		accuracy("recoveredLDA",ss.getTestingSet(),ss.getTestingLabels(),lda);
		accuracy("sparseLDA-"+Estimator.lambda,s.getTestingSet(),s.getTestingLabels(),slda);
		accuracy("Daehr-"+Estimator.lambda,ss.getTestingSet(),ss.getTestingLabels(),slda);
		
		
		Estimator.lambda*=0.1;

		slda=new PDLassoLDA(s.getTrainingSet(),s.getTrainingLabels());
		accuracy("sparseLDA-"+Estimator.lambda,s.getTestingSet(),s.getTestingLabels(),slda);
		
		slda=new PDLassoLDA(ss.getTrainingSet(),ss.getTrainingLabels());
		accuracy("Daehr-"+Estimator.lambda,ss.getTestingSet(),ss.getTestingLabels(),slda);

		Estimator.lambda*=0.1;

		slda=new PDLassoLDA(s.getTrainingSet(),s.getTrainingLabels());
		accuracy("sparseLDA-"+Estimator.lambda,s.getTestingSet(),s.getTestingLabels(),slda);
		
		slda=new PDLassoLDA(ss.getTrainingSet(),ss.getTrainingLabels());
		accuracy("Daehr-"+Estimator.lambda,ss.getTestingSet(),ss.getTestingLabels(),slda);

		Estimator.lambda*=0.1;

		slda=new PDLassoLDA(s.getTrainingSet(),s.getTrainingLabels());
		accuracy("sparseLDA-"+Estimator.lambda,s.getTestingSet(),s.getTestingLabels(),slda);
		
		slda=new PDLassoLDA(ss.getTrainingSet(),ss.getTrainingLabels());
		accuracy("Daehr-"+Estimator.lambda,ss.getTestingSet(),ss.getTestingLabels(),slda);

		Estimator.lambda*=0.1;

		slda=new PDLassoLDA(s.getTrainingSet(),s.getTrainingLabels());
		accuracy("sparseLDA-"+Estimator.lambda,s.getTestingSet(),s.getTestingLabels(),slda);
		
		slda=new PDLassoLDA(ss.getTrainingSet(),ss.getTrainingLabels());
		accuracy("Daehr-"+Estimator.lambda,ss.getTestingSet(),ss.getTestingLabels(),slda);

		Estimator.lambda*=0.1;

		slda=new PDLassoLDA(s.getTrainingSet(),s.getTrainingLabels());
		accuracy("sparseLDA-"+Estimator.lambda,s.getTestingSet(),s.getTestingLabels(),slda);
		
		slda=new PDLassoLDA(ss.getTrainingSet(),ss.getTrainingLabels());
		accuracy("Daehr-"+Estimator.lambda,ss.getTestingSet(),ss.getTestingLabels(),slda);

		Estimator.lambda*=0.1;

		slda=new PDLassoLDA(s.getTrainingSet(),s.getTrainingLabels());
		accuracy("sparseLDA-"+Estimator.lambda,s.getTestingSet(),s.getTestingLabels(),slda);
		
		slda=new PDLassoLDA(ss.getTrainingSet(),ss.getTrainingLabels());
		accuracy("Daehr-"+Estimator.lambda,ss.getTestingSet(),ss.getTestingLabels(),slda);
		



	}

	
	private static void accuracy(String name, double[][] data, int[] labels, Classifier<double[]> classifier){
	//	int[] plabels=new int[labels.length];
		System.out.println("accuracy statistics");
		int tp=0,fp=0,tn=0,fn=0;
		for(int i=0;i<labels.length;i++){
			int pl=classifier.predict(data[i]);
			if(pl==1&&labels[i]==1){
				tp++;
			}else if(pl==0&&labels[i]==0){
				tn++;
			}else if(pl==1&&labels[i]==0){
				fp++;
			}else{
				fn++;
			}
			
		}
		ps.println(name+"\t"+tp+"\t"+tn+"\t"+fp+"\t"+fn);

	}
	
		
	private static double[][] dataRecovery(Recovery r,double[][] fm, double[][] fm2, HashMap<Integer, Set<Integer>> missingcodes,
			int sum_miss_codes) {
	//	Recovery r = new NMFRecovery(5);
		
		double[][] fm3 = r.recover(fm2);
//		int sum_recover_codes = 0;
		
//		ps.println("original matrix " + fm.length + " x " + fm[0].length);
//		ps.println("missing matrix " + fm2.length + " x " + fm2[0].length);
//		ps.println("recovered matrix " + fm3.length + " x " + fm3[0].length);
//
//	    ps.print("threshold,");
//		ps.print("missing,");
//		ps.print("recover," );
//		ps.print("intersect,");
//		ps.print("f1 score,");
//		ps.print("precision score,");
//		ps.print("recall score,");
//		ps.print("normalized l1 error,");
//		ps.println("normalized l2 error");
//
//
//		
//	//	plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.0);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.1);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.2);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.3);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.4);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.5);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.6);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.7);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.8);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,0.9);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.0);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.1);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.2);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.3);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.4);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.5);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.6);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.7);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.8);
//		plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3, sum_recover_codes,1.9);
		
		RecoveryUtil.maxMerge(fm3, fm);
		return fm3;
	}

	private static void plotAccuracy(double[][] fm, double[][] fm2, HashMap<Integer, Set<Integer>> missingcodes,
			int sum_miss_codes, double[][] fm3, int sum_recover_codes, double threshold) {
		HashMap<Integer,Set<Integer>> recoverycodes=new HashMap<Integer,Set<Integer>>();

		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j]==0 && fm3[i][j] > threshold) {
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
		double err_l1=Utils.normalizedErrorL1Recovery(fm, fm2, fm3, threshold);
		double err_l2=Utils.normalizedErrorL2Recovery(fm, fm2, fm3, threshold);

		ps.print(threshold);
		ps.print("," + sum_miss_codes);
		ps.print("," + sum_recover_codes);
		ps.print(","+intersection);
		ps.print("," + f1);
		ps.print("," + precision);
		ps.print("," + recall);
		ps.print("," + err_l1);
		ps.println("," + err_l2);

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
