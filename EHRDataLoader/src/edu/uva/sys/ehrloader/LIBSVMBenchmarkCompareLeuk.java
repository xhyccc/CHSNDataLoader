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
import xiong.hdstats.da.comb.OMPDA;
import xiong.hdstats.da.comb.TruncatedRayleighFlowDBSDA;
import xiong.hdstats.da.comb.TruncatedRayleighFlowSpiked;
import xiong.hdstats.da.CovLDA;
import xiong.hdstats.da.mcmc.BayesLDA;
import xiong.hdstats.da.mcmc.LiklihoodBayesLDA;
import xiong.hdstats.da.mcmc.MCBayesLDA;
import xiong.hdstats.da.mcmc.MCRegularizedBayesLDA;
import xiong.hdstats.da.mcmc.RegularizedBayesLDA;
import xiong.hdstats.da.mcmc.RegularizedLikelihoodBayesLDA;
import xiong.hdstats.da.ml.AdaBoostTreeClassifier;
import xiong.hdstats.da.ml.AdaboostLRClassifier;
import xiong.hdstats.da.ml.DTreeClassifier;
import xiong.hdstats.da.ml.LRClassifier;
import xiong.hdstats.da.ml.NonlinearSVMClassifier;
import xiong.hdstats.da.ml.RandomForestClassifier;
import xiong.hdstats.da.ml.SVMClassifier;
import xiong.hdstats.da.shruken.DBSDA;
import xiong.hdstats.da.shruken.DBSpikedDA;
import xiong.hdstats.da.shruken.ODaehrLDA;
import xiong.hdstats.da.shruken.SDA;
import xiong.hdstats.da.shruken.ShLDA;
import xiong.hdstats.da.shruken.ShrinkageLDA;
import xiong.hdstats.da.shruken.SpikedDA;
import xiong.hdstats.da.shruken.InvalidLDA;
import xiong.hdstats.gaussian.CovarianceEstimator;

public class LIBSVMBenchmarkCompareLeuk {

	public static PrintStream ps = null;
	public static int t_size = 400;
	public static int te_size = 100;
	public static int days = 30;

	public static String path = "C:/Users/xiongha/Desktop/leuk/";
	public static String[] datasets = { "leuk_small" };
	public static String[][] datafiles = { { "leukemia_small.csv" } };// , {
																		// "w3a.txt",
																		// "w3b.txt"
																		// }};

	public static void main(String[] args) {
		for (int i = 0; i < datasets.length; i++)
			_main(datasets[i], datafiles[i]);
	}

	public static void _main(String dataset, String[] datafile) {
		RDataLoader sloader = new RDataLoader();
		sloader.load(path, datafile);
		double[][] fm = sloader.getDataMatrix();
		int[] labels = sloader.getLabel();

		System.out.println("matrix " + fm.length + " x " + fm[0].length);
		// ps.println("using NMF 10");
		// double[][] recoveredData = dataRecovery(new NMFRecovery(10), fm, fm,
		// missingcodes, 0);

		for (int t = 10; t <= 10; t += 10) {
			t_size = t;
			te_size = 15;
			try {
				ps = new PrintStream("C:/Users/xiongha/Desktop/leuk/" + dataset + "-" + t_size + ".txt");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int r = 0; r < 100; r++) {
				// Estimator.lambda = 0.005 * 0.25;

				BalanceTTSelection s = new BalanceTTSelection(fm, labels, t_size, te_size);
				s.select();

				long t1 = System.currentTimeMillis();
				// PseudoInverseLDA LDA = new
				// PseudoInverseLDA(s.getTrainingSet(), s.getTrainingLabels(),
				// false);
				long t2 = System.currentTimeMillis();
				/// accuracy("LDA", s.getTestingSet(), s.getTestingLabels(),
				/// LDA, t1, t2);

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

				for (int i = 2; i <= 32; i *= 2) {
					long start = System.currentTimeMillis();
					OMPDA olda = new OMPDA(trainData, trainLabel, false, i);
					long current = System.currentTimeMillis();
					accuracy2("OMP-" + (i), testData, testLabel, olda, start, current);
				}

				for (int i = 2; i <= 64; i *= 2) {
					// CovarianceEstimator.lambda = l;
					// for (int i = 2; i <= 32; i *= 2) {
					long start = System.currentTimeMillis();
					TruncatedRayleighFlowSpiked olda = new TruncatedRayleighFlowSpiked(trainData, trainLabel, false, 2,
							i);
					long current = System.currentTimeMillis();
					accuracy2("TruncatedRayleighFlow-" + i, testData, testLabel, olda, start, current);
					// }
				}

				for (int i = 2; i <= 32; i *= 2) {
					t1 = System.currentTimeMillis();
					SpikedDA oLDA = new SpikedDA(s.getTrainingSet(), s.getTrainingLabels(), false, i);
					t2 = System.currentTimeMillis();
					accuracy2("SDA-" + i, s.getTestingSet(), s.getTestingLabels(), oLDA, t1, t2);
				}
				//
				for (int i = 2; i <= 32; i *= 2) {
					t1 = System.currentTimeMillis();
					DBSpikedDA oLDA = new DBSpikedDA(s.getTrainingSet(), s.getTrainingLabels(), false, i);
					t2 = System.currentTimeMillis();
					accuracy2("DBSDA-" + i, s.getTestingSet(), s.getTestingLabels(), oLDA, t1, t2);
				}

				for (int i = 0; i < trainLabel.length; i++)
					if (trainLabel[i] == -1)
						trainLabel[i] = 0;

				for (int i = 0; i < testLabel.length; i++)
					if (testLabel[i] == -1)
						testLabel[i] = 0;

				t1 = System.currentTimeMillis();
				SVMClassifier svm = new SVMClassifier(s.getTrainingSet(), s.getTrainingLabels());
				t2 = System.currentTimeMillis();
				accuracy("SVM", s.getTestingSet(), s.getTestingLabels(), svm, t1, t2);

				// t1 = System.currentTimeMillis();
				// t1 = System.currentTimeMillis();
				// LRClassifier lr = new LRClassifier(s.getTrainingSet(),
				// s.getTrainingLabels(), 10);
				// t2 = System.currentTimeMillis();
				// accuracy("LR", s.getTestingSet(), s.getTestingLabels(), lr,
				// t1, t2);

				// t1 = System.currentTimeMillis();
				// double[][] train = s.getTrainingSet();
				// double[][] test = s.getTestingSet();
				// InvalidLDA large = new InvalidLDA(train,
				// s.getTrainingLabels(), false);
				// PCA pca = new PCA(large.getSampleCovarianceMatrix());
				// double[][] t_train = pca.project(train);
				// double[][] t_test = pca.project(test);
				// PseudoInverseLDA LDA = new PseudoInverseLDA(t_train,
				// s.getTrainingLabels(), false);
				// t2 = System.currentTimeMillis();
				// accuracy("Ye-LDA", t_test, s.getTestingLabels(), LDA, t1,
				// t2);

				try {
					t1 = System.currentTimeMillis();
					DTreeClassifier dtc = new DTreeClassifier(s.getTrainingSet(), s.getTrainingLabels(), 10);
					t2 = System.currentTimeMillis();
					accuracy("DTree-10", s.getTestingSet(), s.getTestingLabels(), dtc, t1, t2);

					t1 = System.currentTimeMillis();
					dtc = new DTreeClassifier(s.getTrainingSet(), s.getTrainingLabels(), 20);
					t2 = System.currentTimeMillis();
					accuracy("DTree-20", s.getTestingSet(), s.getTestingLabels(), dtc, t1, t2);
				} catch (Exception exp) {
					exp.printStackTrace();
				}

				t1 = System.currentTimeMillis();
				RandomForestClassifier rfc = new RandomForestClassifier(s.getTrainingSet(), s.getTrainingLabels(), 50);
				t2 = System.currentTimeMillis();
				accuracy("RFC-50", s.getTestingSet(), s.getTestingLabels(), rfc, t1, t2);

				t1 = System.currentTimeMillis();
				rfc = new RandomForestClassifier(s.getTrainingSet(), s.getTrainingLabels(), 100);
				t2 = System.currentTimeMillis();
				accuracy("RFC-100", s.getTestingSet(), s.getTestingLabels(), rfc, t1, t2);

				t1 = System.currentTimeMillis();
				NonlinearSVMClassifier nsvm = new NonlinearSVMClassifier(s.getTrainingSet(), s.getTrainingLabels(), 0.1,
						1);
				t2 = System.currentTimeMillis();
				accuracy("NLSVM-0.1", s.getTestingSet(), s.getTestingLabels(), nsvm, t1, t2);

				t1 = System.currentTimeMillis();
				nsvm = new NonlinearSVMClassifier(s.getTrainingSet(), s.getTrainingLabels(), 1, 1);
				t2 = System.currentTimeMillis();
				accuracy("NLSVM-1.0", s.getTestingSet(), s.getTestingLabels(), nsvm, t1, t2);

				t1 = System.currentTimeMillis();
				nsvm = new NonlinearSVMClassifier(s.getTrainingSet(), s.getTrainingLabels(), 10, 1);
				t2 = System.currentTimeMillis();
				accuracy("NLSVM-10", s.getTestingSet(), s.getTestingLabels(), nsvm, t1, t2);

			}

		}
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
