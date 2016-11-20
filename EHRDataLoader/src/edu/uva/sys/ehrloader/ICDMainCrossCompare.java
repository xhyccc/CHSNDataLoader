package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.uva.hdstats.Estimator;
import edu.uva.hdstats.da.AdaboostLRClassifier;
import edu.uva.hdstats.da.BayesLDA;
import edu.uva.hdstats.da.LiklihoodBayesLDA;
import edu.uva.hdstats.da.RegularizedLikelihoodBayesLDA;
import edu.uva.hdstats.da.Classifier;
import edu.uva.hdstats.da.DaehrLDA;
import edu.uva.hdstats.da.GLassoLDA;
import edu.uva.hdstats.da.LDA;
import edu.uva.hdstats.da.LRClassifier;
import edu.uva.hdstats.da.NonSparseLDA;
import edu.uva.hdstats.da.ODaehrLDA;
import edu.uva.hdstats.da.OLDA;
import edu.uva.hdstats.da.OrgLDA;
import edu.uva.hdstats.da.PDLassoLDA;
import edu.uva.hdstats.da.RegularizedBayesLDA;
import edu.uva.hdstats.da.SVMClassifier;
import edu.uva.hdstats.da.ShLDA;
import edu.uva.hdstats.da.ShrinkageLDA;
import edu.uva.libopt.numeric.*;
import edu.uva.sys.ehrloader.ml.BalanceTTSelection;
import edu.uva.sys.ehrloader.recovery.*;

public class ICDMainCrossCompare {

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

		EHRecordBase base = ICDLineReader.load(map, "/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/non-mh_icd.csv",
				"x_icdcode", 300000);

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

		for (int t = 100; t <= 800; t += 200) {
			for (int te = 200; te <= 200; te += 200) {
				for (int days = 90; days <= 90; days += 30) {
					t_size = t;
					te_size = te;
					try {
						ps = new PrintStream("/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/accuracy-bayes-"
								+ t_size + "-" + te_size + "-" + days + ".txt");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					for (int r = 0; r < 5; r++) {
						// Estimator.lambda = 0.005 * 0.25;

						BalanceTTSelection s = new BalanceTTSelection(fm, base.getLabels(), t_size, te_size);
						s.select();

						long t1 = System.currentTimeMillis();
						OLDA LDA = new OLDA(s.getTrainingSet(), s.getTrainingLabels(), false);
						long t2 = System.currentTimeMillis();
						accuracy("LDA", s.getTestingSet(), s.getTestingLabels(), LDA, t1, t2);

						// for (double slambda = 0.0; slambda < 1; slambda +=
						// 0.25) {
						// ShrinkageLDA.slambda = slambda;
						// t1 = System.currentTimeMillis();
						// ShrinkageLDA sLDA = new
						// ShrinkageLDA(s.getTrainingSet(),
						// s.getTrainingLabels());
						// t2 = System.currentTimeMillis();
						// accuracy("Shrinkage-" + ShrinkageLDA.slambda,
						// s.getTestingSet(), s.getTestingLabels(), sLDA, t1,
						// t2);
						// }

						// for (double lambda = 100; lambda > 0.1; lambda*=0.1)
						// {
						// Estimator.lambda=lambda;
						// t1 = System.currentTimeMillis();
						// ODaehrLDA oLDA = new ODaehrLDA(s.getTrainingSet(),
						// s.getTrainingLabels(), false);
						// t2 = System.currentTimeMillis();
						// accuracy("SparseCov-" + Estimator.lambda,
						// s.getTestingSet(), s.getTestingLabels(), oLDA, t1,
						// t2);
						// }

						for (double lambda = 1; lambda >= 0.001; lambda*= 0.1) {
							Estimator.lambda = lambda;
							t1 = System.currentTimeMillis();
							RegularizedLikelihoodBayesLDA oLDA = new RegularizedLikelihoodBayesLDA(s.getTrainingSet(),
									s.getTrainingLabels(), 200, 0);
							t2 = System.currentTimeMillis();
						//	oLDA.setNumPredictors(5000);
							oLDA.setNumPredictors(200);
							accuracy("LikelihoodNonSparse-200-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(150);
							accuracy("LikelihoodNonSparse-150-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(100);
							accuracy("LikelihoodNonSparse-100-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(50);
							accuracy("LikelihoodNonSparse-50-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
						}
						
						for (double lambda = 1; lambda >= 0.001; lambda*= 0.1) {
							Estimator.lambda = lambda;
							t1 = System.currentTimeMillis();
							RegularizedBayesLDA oLDA = new RegularizedBayesLDA(s.getTrainingSet(),
									s.getTrainingLabels(), 200, 0);
							t2 = System.currentTimeMillis();
						//	oLDA.setNumPredictors(5000);
							oLDA.setNumPredictors(200);
							accuracy("LikelihoodNonSparse-200-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(150);
							accuracy("LikelihoodNonSparse-150-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(100);
							accuracy("LikelihoodNonSparse-100-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(50);
							accuracy("LikelihoodNonSparse-50-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
						}

						
						for (double lambda = 1; lambda >= 0.001; lambda*= 0.1) {
							Estimator.lambda = lambda;
							t1 = System.currentTimeMillis();
							RegularizedBayesLDA oLDA = new RegularizedBayesLDA(s.getTrainingSet(),
									s.getTrainingLabels(), 200, 0);
							t2 = System.currentTimeMillis();
						//	oLDA.setNumPredictors(5000);
							oLDA.setNumPredictors(200);
							accuracy("BayesNonSparse-200-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(150);
							accuracy("BayesNonSparse-150-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(100);
							accuracy("BayesNonSparse-100-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
							oLDA.setNumPredictors(50);
							accuracy("BayesNonSparse-50-10-" + Estimator.lambda, s.getTestingSet(),
									s.getTestingLabels(), oLDA, t1, t2);
						}
						for (double lambda = 1; lambda >= 0.00001; lambda*= 0.1) {
							Estimator.lambda = lambda;
							t1 = System.currentTimeMillis();
							GLassoLDA oLDA = new GLassoLDA(s.getTrainingSet(), s.getTrainingLabels(), false);
							t2 = System.currentTimeMillis();
							accuracy("GLasso-" + Estimator.lambda, s.getTestingSet(), s.getTestingLabels(), oLDA, t1,
									t2);
						}

						// for (double lambda = 0.001; lambda > 0.0001;
						// lambda*=0.1) {
						// Estimator.lambda=lambda;
						// t1 = System.currentTimeMillis();
						// NonSparseLDA oLDA = new
						// NonSparseLDA(s.getTrainingSet(),
						// s.getTrainingLabels(), false);
						// t2 = System.currentTimeMillis();
						// accuracy("NonSparse-" + Estimator.lambda,
						// s.getTestingSet(), s.getTestingLabels(), oLDA, t1,
						// t2);
						// }

						// for (double lambda = 100; lambda > 0.1; lambda*=0.1)
						// {
						// Estimator.lambda=lambda;
						// t1 = System.currentTimeMillis();
						// NonSparseLDA oLDA = new
						// NonSparseLDA(s.getTrainingSet(),
						// s.getTrainingLabels(), false);
						// t2 = System.currentTimeMillis();
						// accuracy("NonSparse-" + Estimator.lambda,
						// s.getTestingSet(), s.getTestingLabels(), oLDA, t1,
						// t2);
						// }

						// for (double lambda = 0.001; lambda >= 0.0001;
						// lambda-=0.0003) {
						// Estimator.lambda=lambda;
						t1 = System.currentTimeMillis();
						LiklihoodBayesLDA lLDA = new LiklihoodBayesLDA(s.getTrainingSet(),
								s.getTrainingLabels(), 200, 10);
						t2 = System.currentTimeMillis();
					//	oLDA.setNumPredictors(5000);
						lLDA.setNumPredictors(200);
						accuracy("LikelihoodLDA-200-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), lLDA, t1, t2);
						lLDA.setNumPredictors(150);
						accuracy("LikelihoodLDA-150-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), lLDA, t1, t2);
						lLDA.setNumPredictors(100);
						accuracy("LikelihoodLDA-100-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), lLDA, t1, t2);
						lLDA.setNumPredictors(50);
						accuracy("LikelihoodLDA-50-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), lLDA, t1, t2);
						// }

						
						t1 = System.currentTimeMillis();
						BayesLDA bLDA = new BayesLDA(s.getTrainingSet(),
								s.getTrainingLabels(), 200, 10);
						t2 = System.currentTimeMillis();
					//	oLDA.setNumPredictors(5000);
						bLDA.setNumPredictors(200);
						accuracy("BayesLDA-200-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), bLDA, t1, t2);
						bLDA.setNumPredictors(150);
						accuracy("BayesLDA-150-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), bLDA, t1, t2);
						bLDA.setNumPredictors(100);
						accuracy("BayesLDA-100-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), bLDA, t1, t2);
						bLDA.setNumPredictors(50);
						accuracy("BayesLDA-50-10-" + Estimator.lambda, s.getTestingSet(),
								s.getTestingLabels(), bLDA, t1, t2);
						
						t1 = System.currentTimeMillis();
						SVMClassifier svm = new SVMClassifier(s.getTrainingSet(), s.getTrainingLabels());
						t2 = System.currentTimeMillis();
						accuracy("SVM", s.getTestingSet(), s.getTestingLabels(), svm, t1, t2);

						t1 = System.currentTimeMillis();
						LRClassifier lr = new LRClassifier(s.getTrainingSet(), s.getTrainingLabels());
						t2 = System.currentTimeMillis();
						accuracy("LR", s.getTestingSet(), s.getTestingLabels(), lr, t1, t2);

						t1 = System.currentTimeMillis();
						AdaboostLRClassifier ab = new AdaboostLRClassifier(s.getTrainingSet(), s.getTrainingLabels(),
								10);
						t2 = System.currentTimeMillis();
						accuracy("ABLR-10", s.getTestingSet(), s.getTestingLabels(), ab, t1, t2);

						t1 = System.currentTimeMillis();
						ab = new AdaboostLRClassifier(s.getTrainingSet(), s.getTrainingLabels(), 50);
						t2 = System.currentTimeMillis();
						accuracy("ABLR-50", s.getTestingSet(), s.getTestingLabels(), ab, t1, t2);

					}

				}
			}
		}

	}

	private static void accuracy(String name, double[][] data, int[] labels, Classifier<double[]> classifier, long t1,
			long t2) {
		// int[] plabels=new int[labels.length];
		System.out.println("accuracy statistics");
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < labels.length; i++) {
			int pl = classifier.predict(data[i]);
			System.out.println(pl + "\t vs\t" + labels[i]);
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

	private static void plotAccuracy(double[][] fm, double[][] fm2, HashMap<Integer, Set<Integer>> missingcodes,
			int sum_miss_codes, double[][] fm3, int sum_recover_codes, double threshold) {
		HashMap<Integer, Set<Integer>> recoverycodes = new HashMap<Integer, Set<Integer>>();

		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j] == 0 && fm3[i][j] > threshold) {
					sum_recover_codes++;
					if (!recoverycodes.containsKey(i))
						recoverycodes.put(i, new HashSet<Integer>());
					recoverycodes.get(i).add(j);

				}
			}
		}

		int intersection = intersection(missingcodes, recoverycodes);
		double precision = (double) intersection / (double) sum_recover_codes;
		double recall = (double) intersection / (double) sum_miss_codes;
		double f1 = 2 * precision * recall / (precision + recall);
		double err_l1 = Utils.normalizedErrorL1Recovery(fm, fm2, fm3, threshold);
		double err_l2 = Utils.normalizedErrorL2Recovery(fm, fm2, fm3, threshold);

		ps.print(threshold);
		ps.print("," + sum_miss_codes);
		ps.print("," + sum_recover_codes);
		ps.print("," + intersection);
		ps.print("," + f1);
		ps.print("," + precision);
		ps.print("," + recall);
		ps.print("," + err_l1);
		ps.println("," + err_l2);

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
