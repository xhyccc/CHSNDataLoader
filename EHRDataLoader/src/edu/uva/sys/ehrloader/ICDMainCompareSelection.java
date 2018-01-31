package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.uva.libopt.numeric.*;
import edu.uva.sys.ehrloader.ml.BalanceTTSelection;
import edu.uva.sys.ehrloader.recovery.*;
import smile.projection.PCA;
import xiong.hdstats.da.Classifier;
import xiong.hdstats.da.LDA;
import xiong.hdstats.da.PseudoInverseLDA;
import xiong.hdstats.da.CovLDA;
import xiong.hdstats.da.comb.MetricTransfomer;
import xiong.hdstats.da.comb.OMPDA;
import xiong.hdstats.da.comb.RayleighFlowLDA;
import xiong.hdstats.da.comb.StochasticTruncatedRayleighFlowDBSDA;
import xiong.hdstats.da.comb.TruncatedRayleighFlowDBSDA;
import xiong.hdstats.da.comb.TruncatedRayleighFlowLDA;
import xiong.hdstats.da.mcmc.BayesLDA;
import xiong.hdstats.da.mcmc.LiklihoodBayesLDA;
import xiong.hdstats.da.mcmc.MCBayesLDA;
import xiong.hdstats.da.mcmc.MCRegularizedBayesLDA;
import xiong.hdstats.da.mcmc.RegularizedBayesLDA;
import xiong.hdstats.da.mcmc.RegularizedLikelihoodBayesLDA;
import xiong.hdstats.da.ml.AdaBoostTreeClassifier;
import xiong.hdstats.da.ml.AdaboostLRClassifier;
import xiong.hdstats.da.ml.DTreeClassifier;
import xiong.hdstats.da.ml.KNNClassifier;
import xiong.hdstats.da.ml.LRClassifier;
import xiong.hdstats.da.ml.NonlinearSVMClassifier;
import xiong.hdstats.da.ml.RandomForestClassifier;
import xiong.hdstats.da.ml.SVMClassifier;
import xiong.hdstats.da.shruken.DBSDA;
import xiong.hdstats.da.shruken.ODaehrLDA;
import xiong.hdstats.da.shruken.SDA;
import xiong.hdstats.da.shruken.ShLDA;
import xiong.hdstats.da.shruken.ShrinkageLDA;
import xiong.hdstats.da.shruken.InvalidLDA;
import xiong.hdstats.gaussian.CovarianceEstimator;

public class ICDMainCompareSelection {

	public static PrintStream ps = null;
	public static int t_size = 400;
	public static int te_size = 100;
	public static int days = 30;

	public static String path = "C://Users/xiongha/Dropbox/technical-reports/report-1/libsvm-data/";
	public static String[] datasets = { "adult1", "web1", "mushrooms", "madelon" };
	public static String[][] datafiles = { { "a1a.txt", "a1b.txt" }, { "w1a.txt", "w1b.txt" }, { "mushrooms.txt" },
			{ "madelon.txt", "madelon2.txt" } };

	public static void main(String[] args) {
		// for (int i = 0; i < datasets.length; i++)
		_main(null, null);
	}

	public static void _main(String dataset, String[] datafile) {
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
		int[] labels = base.getLabels();

		System.out.println("matrix " + fm.length + " x " + fm[0].length);

		for (int t = 10; t <= 50; t += 10) {
			t_size = t;
			te_size = 200;
			try {
				ps = new PrintStream("C://Users/xiongha/Desktop/ehr/accuracy-" + dataset + "-" + t_size * 2 + ".txt");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int r = 0; r < 100; r++) {
				// Estimator.lambda = 0.005 * 0.25;

				BalanceTTSelection s = new BalanceTTSelection(fm, labels, t_size, te_size);
				s.select();

				double[][] trainData = s.getTrainingSet();
				int[] trainLabel = s.getTrainingLabels();

				double[][] testData = s.getTestingSet();
				int[] testLabel = s.getTestingLabels();

				for (int i = 0; i < trainLabel.length; i++)
					if (trainLabel[i] == 0)
						trainLabel[i] = -1;

				for (int i = 0; i < testLabel.length; i++)
					if (testLabel[i] == 0)
						testLabel[i] = -1;

				long start, current, t1, t2;
				try {
					double[][] ttrain = trainData;
					double[][] ttest = testData;
					logFS("original", s, 0, ttrain, ttest);
				} catch (Exception exp) {
					exp.printStackTrace();
				}

				for (int i = 10; i <= 50; i += 20) {
					start = System.currentTimeMillis();
					OMPDA olda = new OMPDA(trainData, trainLabel, false, i);
					current = System.currentTimeMillis();
					double[] beta = olda.getBeta();
					double[][] ttrain = MetricTransfomer.getMetricTransformedData(beta, trainData);
					double[][] ttest = MetricTransfomer.getMetricTransformedData(beta, testData);

					try {
						logFS("OMP-" + i, s, i, ttrain, ttest);
					} catch (Exception exp) {
						exp.printStackTrace();
					}

				}

				// for (double l = 1; l <= 16; l *= 2) {
				CovarianceEstimator.lambda = 2;
				for (int i = 10; i <= 50; i += 20) {
					start = System.currentTimeMillis();
					TruncatedRayleighFlowDBSDA olda = new TruncatedRayleighFlowDBSDA(trainData, trainLabel, false, i);
					current = System.currentTimeMillis();

					double[] beta = olda.getBeta();
					double[][] ttrain = MetricTransfomer.getMetricTransformedData(beta, trainData);
					double[][] ttest = MetricTransfomer.getMetricTransformedData(beta, testData);

					try {
						logFS("TruncatedRayleighFlowDBSDA-" + i, s, i, ttrain, ttest);
					} catch (Exception exp) {
						exp.printStackTrace();
					}

				}

				// for (int i = 10; i <= 100; i += 30) {
				// start = System.currentTimeMillis();
				// TruncatedRayleighFlowLDA olda = new
				// TruncatedRayleighFlowLDA(trainData, trainLabel, false, i);
				// current = System.currentTimeMillis();
				//
				// double[] beta = olda.getBeta();
				// double[][] ttrain =
				// MetricTransfomer.getMetricTransformedData(beta, trainData);
				// double[][] ttest =
				// MetricTransfomer.getMetricTransformedData(beta, testData);
				//
				// try {
				// logFS("TruncatedRayleighFlowLDA-" + i, s, i, ttrain, ttest);
				// } catch (Exception exp) {
				// exp.printStackTrace();
				// }
				// }
			}

		}

	}

	public static void logFS(String name, BalanceTTSelection s, int i, double[][] ttrain, double[][] ttest) {
		long t1;
		long t2;

		for (int ii = 0; ii < s.getTrainingLabels().length; ii++)
			if (s.getTrainingLabels()[ii] == -1)
				s.getTrainingLabels()[ii] = 0;

		for (int ii = 0; ii < s.getTestingLabels().length; ii++)
			if (s.getTestingLabels()[ii] == -1)
				s.getTestingLabels()[ii] = 0;

		t1 = System.currentTimeMillis();
		KNNClassifier knn = new KNNClassifier(ttrain, s.getTrainingLabels(), 1);
		t2 = System.currentTimeMillis();
		accuracy(name + "-KNN-1-" + i, ttest, s.getTestingLabels(), knn, t1, t2);

		t1 = System.currentTimeMillis();
		knn = new KNNClassifier(ttrain, s.getTrainingLabels(), 1);
		t2 = System.currentTimeMillis();
		accuracy(name + "-KNN-3-" + i, ttest, s.getTestingLabels(), knn, t1, t2);

		t1 = System.currentTimeMillis();
		knn = new KNNClassifier(ttrain, s.getTrainingLabels(), 1);
		t2 = System.currentTimeMillis();
		accuracy(name + "-KNN-5-" + i, ttest, s.getTestingLabels(), knn, t1, t2);

		t1 = System.currentTimeMillis();
		DTreeClassifier dtc = new DTreeClassifier(ttrain, s.getTrainingLabels(), 10);
		t2 = System.currentTimeMillis();
		accuracy(name + "-DTree-10-" + i, ttest, s.getTestingLabels(), dtc, t1, t2);

		t1 = System.currentTimeMillis();
		dtc = new DTreeClassifier(ttrain, s.getTrainingLabels(), 20);
		t2 = System.currentTimeMillis();
		accuracy(name + "-DTree-20-" + i, ttest, s.getTestingLabels(), dtc, t1, t2);

		// t1 = System.currentTimeMillis();
		// RandomForestClassifier rfc = new RandomForestClassifier(ttrain,
		// s.getTrainingLabels(), 50);
		// t2 = System.currentTimeMillis();
		// accuracy(name + "-RFC-50", ttest, s.getTestingLabels(), rfc, t1, t2);
		//
		// t1 = System.currentTimeMillis();
		// rfc = new RandomForestClassifier(ttrain, s.getTrainingLabels(), 100);
		// t2 = System.currentTimeMillis();
		// accuracy(name + "-RFC-100", ttest, s.getTestingLabels(), rfc, t1,
		// t2);
		//
		t1 = System.currentTimeMillis();
		NonlinearSVMClassifier nsvm = new NonlinearSVMClassifier(ttrain, s.getTrainingLabels(), 0.1, 1);
		t2 = System.currentTimeMillis();
		accuracy(name + "-NLSVM-" + i, ttest, s.getTestingLabels(), nsvm, t1, t2);

		// t1 = System.currentTimeMillis();
		// nsvm = new NonlinearSVMClassifier(ttrain, s.getTrainingLabels(), 1,
		// 1);
		// t2 = System.currentTimeMillis();
		// accuracy(name + "-NLSVM-1.0-" + i, ttest, s.getTestingLabels(), nsvm,
		// t1, t2);

		for (int ii = 0; ii < s.getTrainingLabels().length; ii++)
			if (s.getTrainingLabels()[ii] == 0)
				s.getTrainingLabels()[ii] = -1;

		for (int ii = 0; ii < s.getTestingLabels().length; ii++)
			if (s.getTestingLabels()[ii] == 0)
				s.getTestingLabels()[ii] = -1;
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

	private static void accuracy2(String name, double[][] data, int[] labels, Classifier<double[]> classifier, long t1,
			long t2) {
		// int[] plabels=new int[labels.length];
		// System.out.println("accuracy statistics");
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < labels.length; i++) {
			int pl = classifier.predict(data[i]);
			// System.out.println(pl + "\t vs\t" + labels[i]);
			if (pl == 1 && labels[i] == 1) {
				tp++;
			} else if (pl == -1 && labels[i] == -1) {
				tn++;
			} else if (pl == 1 && labels[i] == -1) {
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
