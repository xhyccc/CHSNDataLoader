package edu.uva.sys.ehrloader;



import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uva.hdstats.Estimator;
import edu.uva.hdstats.da.AdaboostLRClassifier;
import edu.uva.hdstats.da.Classifier;
import edu.uva.hdstats.da.DaehrLDA;
import edu.uva.hdstats.da.LDA;
import edu.uva.hdstats.da.LRClassifier;
import edu.uva.hdstats.da.ODaehrLDA;
import edu.uva.hdstats.da.OLDA;
import edu.uva.hdstats.da.OrgLDA;
import edu.uva.hdstats.da.PDLassoLDA;
import edu.uva.hdstats.da.PseudoInverse;
import edu.uva.hdstats.da.SVMClassifier;
import edu.uva.hdstats.da.ShLDA;
import edu.uva.hdstats.da.ShrinkageLDA;
import edu.uva.hdstats.da.mDaehrLDA;
import edu.uva.libopt.numeric.*;
import edu.uva.sys.ehrloader.ml.BalanceTTSelection;
import edu.uva.sys.ehrloader.recovery.*;
import smile.math.matrix.Matrix;

public class MatrixCompare {

	public static PrintStream ps = null;
	public static int t_size = 400;
	public static int te_size = 100;
	public static int days = 30;

	public static void main(String[] args) {

		if (args.length >= 1)
			t_size = Integer.parseInt(args[0]);

		if (args.length >= 2)
			te_size = Integer.parseInt(args[1]);

		if (args.length >= 3)
			days = Integer.parseInt(args[2]);

		ps = System.out;

		EHRRecordMap map = new EHRRecordMap("/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/mapping.txt");

		EHRecordBase base = ICDLineReader.load(map,
				"/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/non-mh_icd.csv", "x_icdcode", 300000);

		EHRecordBase base_2 = ICDLineReader.load(map, "/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/icd_MD.csv",
				"y_icdcode", 300000);

		// base_2.removeVisitsAfter(MHCode.codes);

		// base.removePatientLessNVisit(5);
		// base_2.removePatientLessNVisit(5);

		// base.setLabelsForAllPatients(0);
		// base_2.setLabelsForAllPatients(1);
		base.insertRecords(base_2);
		base.setPositiveLabel(MHCode.codes);
		base.removeVisitsAfter(MHCode.codes, days);
		base.removePatientLessNVisit(3);

		ps.println("patients: " + base.getPatients().size());
		ps.println("codes: " + base.getCodes().size());
		ps.println("dates: " + base.getDates().size());

		double[][] fm = base.getFrequencyMatrix();
		// HashMap<Integer, Set<Integer>> missingcodes = new HashMap<Integer,
		// Set<Integer>>();

		System.out.println("matrix " + fm.length + " x " + fm[0].length);
		// ps.println("using NMF 10");
		// double[][] recoveredData = dataRecovery(new NMFRecovery(10), fm, fm,
		// missingcodes, 0);

		for (int t = 200; t <= 20000; t *= 10) 
		// for (int te = 100; te <= 500; te += 100) {
		for (int days = 30; days <= 30; days += 30) {
			// t_size = t;
			te_size = 1000;
			try {
				ps = new PrintStream("/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/error-precision-matrix－200.txt");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String path="/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/";
			
			for (int r = 0; r < 1; r++) {
				BalanceTTSelection s1 = new BalanceTTSelection(fm, base.getLabels(), t, 10);
				s1.select();

				mDaehrLDA LDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);
				mDaehrLDA sparseLDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);
				mDaehrLDA glassoLDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);
				mDaehrLDA nonSparseLDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);

				BalanceTTSelection ss = new BalanceTTSelection(fm, base.getLabels(), 10000, te_size);
				ss.select();
				mDaehrLDA large = new mDaehrLDA(ss.getTrainingSet(), ss.getTrainingLabels(), false);

				double[][]	covLDA = LDA.getSamplePrecisionMatrix();
				double[][] covDaehr = sparseLDA.getSparseCovPrecisionMatrx();
				double[][] covGlasso = glassoLDA.getGLassoPrecisionMatrx();
				double[][] covNonSparse = nonSparseLDA.getNonSparsePrecisionMatrx();
				double[][] covLarge = large.getSamplePrecisionMatrix();
				
				plotAccuracy("sample", covLarge, covLarge);
			//	plotAccuracy("sample-100-", covLDA[1], covLarge[1]);
				saveMatrxInFile(path+"large",covLarge,base._codes,map.nameMap);
				
				Estimator.lambda = 100;
				for (int i = 0; i < 3; i++) {
					sparseLDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);
					glassoLDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);
					nonSparseLDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);
					
					covDaehr = sparseLDA.getSparseCovPrecisionMatrx();
					covGlasso = glassoLDA.getGLassoPrecisionMatrx();
					covNonSparse = nonSparseLDA.getNonSparsePrecisionMatrx();

			//		plotAccuracy("sparse-" + Estimator.lambda , covDaehr, covLarge);
			//		saveMatrxInFile(path+"sparse-"+ Estimator.lambda+"-"+t,new Matrix(covDaehr).inverse(),base._codes,map.nameMap);

					plotAccuracy("glasso-" + Estimator.lambda, covGlasso, covLarge);
					saveMatrxInFile(path+"glasso-" + Estimator.lambda+"_"+t,(covGlasso),base._codes,map.nameMap);

					plotAccuracy("nonsparse-" + Estimator.lambda , covNonSparse, covLarge);
					saveMatrxInFile(path+"nonsparse-" + Estimator.lambda+"-"+t,(covNonSparse),base._codes,map.nameMap);

					
					
					Estimator.lambda *= 0.1;
				}

				mDaehrLDA.slambda = 0.75;
				for (int i = 0; i < 4; i++) {
					sparseLDA = new mDaehrLDA(s1.getTrainingSet(), s1.getTrainingLabels(), false);
					covDaehr = sparseLDA.getShrinkagedCovPrecisionMatrx();
					plotAccuracy("shrinkage-"+mDaehrLDA.slambda, covDaehr, covLarge);
					saveMatrxInFile(path+"shrinkage-"+mDaehrLDA.slambda+"-"+t,covDaehr,base._codes,map.nameMap);

					mDaehrLDA.slambda -=0.25;
				}

			}

		}
		// }
	}

	// }
	
	private static void saveMatrxInFile(String path, double[][] matrx, List<String> codes, Map<String, String> map){
		try {
			PrintStream ps=new PrintStream(path+".txt");
			for(int i=0;i<matrx.length;i++){
				for(int j=0;j<matrx[i].length;j++){
					ps.println(map.get(codes.get(i))+"\t"+map.get(codes.get(j))+"\t"+matrx[i][j]);
				}
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

	private static void accuracy(String name, double[][] data, int[] labels, Classifier<double[]> classifier, long t1,
			long t2) {
		// int[] plabels=new int[labels.length];
		System.out.println("accuracy statistics");
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < labels.length; i++) {
			int pl = classifier.predict(data[i]);
			if (pl == 1 && labels[i] == 1) {
				tp++;
			} else if (pl == 0 && labels[i] == 0) {
				tn++;
			} else if (pl == 1 && labels[i] == 0) {
				fp++;
			} else {
				fn++;
			}

		}
		long train_time = t2 - t1;
		double test_time = ((double) (System.currentTimeMillis() - t2)) / ((double) labels.length);
		ps.println(name + "\t" + tp + "\t" + tn + "\t" + fp + "\t" + fn + "\t" + train_time + "\t" + test_time);

	}

	private static double[][] dataRecovery(Recovery r, double[][] fm, double[][] fm2,
			HashMap<Integer, Set<Integer>> missingcodes, int sum_miss_codes) {
		// Recovery r = new NMFRecovery(5);

		double[][] fm3 = r.recover(fm2);
		// int sum_recover_codes = 0;

		// ps.println("original matrix " + fm.length + " x " + fm[0].length);
		// ps.println("missing matrix " + fm2.length + " x " + fm2[0].length);
		// ps.println("recovered matrix " + fm3.length + " x " + fm3[0].length);
		//
		// ps.print("threshold,");
		// ps.print("missing,");
		// ps.print("recover," );
		// ps.print("intersect,");
		// ps.print("f1 score,");
		// ps.print("precision score,");
		// ps.print("recall score,");
		// ps.print("normalized l1 error,");
		// ps.println("normalized l2 error");
		//
		//
		//
		// // plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.0);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.1);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.2);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.3);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.4);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.5);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.6);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.7);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.8);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.9);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.0);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.1);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.2);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.3);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.4);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.5);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.6);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.7);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.8);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.9);

		RecoveryUtil.maxMerge(fm3, fm);
		return fm3;
	}

	private static void plotAccuracy(String name, double[][] fm, double[][] fm2) {

		double err_l1 = Utils.normalizedErrorL1((fm2), (fm), 0);
		double err_l2 = Utils.normalizedErrorL2((fm2), (fm), 0);

		ps.print(name + "\t" + err_l1);
		ps.println("\t" + err_l2);

	}

	public static double F1(Set<String> miss, Set<String> recover) {
		int intersect = 0;
		for (String i : miss)
			if (recover.contains(i))
				intersect++;
		return 2.0 * (double) intersect / (double) (miss.size() + recover.size());
	}

	public static int intersection(HashMap<Integer, Set<Integer>> miss, HashMap<Integer, Set<Integer>> recover) {
		int intersect = 0;
		for (int p : miss.keySet()) {
			if (recover.containsKey(p)) {
				for (int code : miss.get(p)) {
					if (recover.get(p).contains(code)) {
						intersect++;
					}
				}
			}
		}

		return intersect;
	}

}
