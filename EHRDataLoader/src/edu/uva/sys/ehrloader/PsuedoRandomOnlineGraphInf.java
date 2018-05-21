package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Jama.Matrix;
import edu.uva.libopt.numeric.*;
import smile.stat.distribution.MultivariateGaussianDistribution;
import smile.stat.distribution.SpikedMultivariateGaussianDistribution;
import xiong.hdstats.gaussian.GLassoEstimator;
import xiong.hdstats.gaussian.SampleCovarianceEstimator;
import xiong.hdstats.gaussian.SpikedUpperEstimator;
import xiong.hdstats.gaussian.online.GLassoIntialOnlineGraphEstimator;
import xiong.hdstats.gaussian.online.OnlineGraphEstimator;
import xiong.hdstats.gaussian.online.SampleInitialOnlineGraphEstimator;
import xiong.hdstats.gaussian.online.SpikedInitialOnlineGraphEstimator;
import xiong.hdstats.graph.SampleGraph;
import xiong.hdstats.mat.TruncatedSVD;

public class PsuedoRandomOnlineGraphInf {
	public static ExecutorService ctp = Executors.newFixedThreadPool(2);

	public static void main(String[] args) {
		// int[] pa = { 320};
		int[] pa = { 50, 100, 150, 200 };
		double[] cis ={0.25};
		int[] inits = { 20 };
		for(double ci:cis)
		for (int p : pa) {
			for (int init_size : inits) {
				// try {
				PrintStream pps;
				try {
					pps = new PrintStream("C:/Users/xiongha/Desktop/OGM/infresult-"+ci+"-" + init_size + "-" + p + ".txt");
					for (int i = 0; i < 5; i++)
						_main(init_size, p, ci, pps);
					pps.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// ctp.execute(thread);
				// } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
		}
		ctp.shutdown();
	}

	public static double[][] binaryGraphGen(int p, int n) {
		double[][] basis = new double[p][n];
		for (int i = 0; i < p; i++) {
			for (int j = 0; j < n; j++) {
				basis[i][j] = (Math.random() > 0.5) ? 1 : 0;
			}
		}
		Matrix bmtx = new Matrix(basis);
		return bmtx.times(bmtx.transpose()).getArray();
	}

	public static void _main(int init_size, int p, double ci, PrintStream ps) {
		// int[] est_density = new int[4];
		int s = p;

		double[][] icov = binaryGraphGen(p, p); // new double[p][p];
		double[][] cov = TruncatedSVD.eigenTruncatedInverse(icov, s);
		// for (int i = 0; i < cov.length; i++) {
		// for (int j = 0; j < cov.length; j++) {
		// cov[i][j] = Math.pow(0.8, Math.abs(i - j));
		// }
		// }
		double[] mean = new double[p];
		// double[][] theta_s = new Matrix(cov).inverse();

		// double[][] ocov = TruncatedSVD.eigenTruncate(cov, s);
		// double[][] icov = TruncatedSVD.eigenTruncatedInverse(cov, s);
		// Matrix icovmtx = new Matrix(icov);
		// Matrix ocovmtx = new Matrix(ocov);
		// double infNorm = icovmtx.normInf();
		// ocov = ocovmtx.times(1.0 * infNorm).getArray();
		// icov = icovmtx.times(1.0 / infNorm).getArray();

		System.out.println("infnty norm" + new Matrix(icov).normInf());

		SpikedMultivariateGaussianDistribution gaussian = new SpikedMultivariateGaussianDistribution(mean, cov);
		double[][] init = new double[init_size][p];
		double[][] allsamples = new double[init_size + 3000][p];
		for (int i = 0; i < init.length + 3000; i++) {
			double[] randv = gaussian.rand();
			for (int j = 0; j < mean.length; j++) {
				if (i < init.length)
					init[i][j] = randv[j];
				allsamples[i][j] = randv[j];
			}
		}
		// SampleInitialOnlineGraphEstimator ogel = new
		// SampleInitialOnlineGraphEstimator();
		// SpikedInitialOnlineGraphEstimator osgel10 = new
		// SpikedInitialOnlineGraphEstimator(10);
		SampleInitialOnlineGraphEstimator osgel = new SampleInitialOnlineGraphEstimator(0.1);
		// SpikedInitialOnlineGraphEstimator osgel50 = new
		// SpikedInitialOnlineGraphEstimator(50);
		// SpikedInitialOnlineGraphEstimator osgel100 = new
		// SpikedInitialOnlineGraphEstimator(100);
		// HashMap<String, OnlineGraphEstimator> oges = new HashMap<String,
		// OnlineGraphEstimator>();
		// oges.put("SampleOnline", ogel);
		// oges.put("SpikedOnline-10", osgel10);
		// oges.put("Online-Spiked", osgel);
		// oges.put("SpikedOnline-50", osgel50);
		// oges.put("SpikedOnline-100", osgel100);
		// oges.put("GLassoOnline-0.1", oggel01);
		// oges.put("GLassoOnline-1.0", oggel1);
		// oges.put("GLassoOnline-2.0", oggel2);
		// oges.put("GLassoOnline-5.0", oggel5);
		// oges.put("GLassoOnline-10.0", oggel10);

		// for (String name : oges.keySet()) {
		long start = System.nanoTime();
		osgel.init(init);
		double[][] graphEst = osgel.getGraph();
		// double eFNorm = new Matrix(graphEst).normF();
		// double eInfNorm = new Matrix(graphEst).normInf();
		long end = System.nanoTime();
		ps.println("OGM\t initialization\t" + Utils.getErrorL2(icov, graphEst) + "\t"
				+ Utils.getErrorInf(icov, graphEst) + "\t" + (end - start));

		// }

		for (int t = 1; t <= 2000; t++) {
			int index = init_size + t;
			double[] randov = allsamples[index - 1];
			// for (String name : oges.keySet()) {
			start = System.nanoTime();
			System.out.println("OGM\t updating...." + t);
			osgel.update(index, randov);
			System.out.println("OGM\t finished updating....");
			end = System.nanoTime();
			graphEst = osgel.getGraph();
			// eFNorm = new Matrix(graphEst).normF();
			// eInfNorm = new Matrix(graphEst).normInf();
			if (t % 200 == 0) {
				SampleGraph infGraph = new SampleGraph(graphEst, true);
				int[][] igraph = infGraph.adaptiveThresholding(Gaussian.PhiInverse(1-ci/p/(p-1)) / Math.sqrt(t + init_size));
				ps.println("OGM\t" + t + "\t updating\t" + overlap(igraph, icov) / nonZeros(icov) + "\t"
						+ overlap(igraph, icov) / nonZeros(igraph) + "\t" + (end - start));
				// }
				// if (t % 200 == 0) {
				double[][] dat = new double[init_size + t][p];
				for (int i = 0; i < dat.length; i++) {
					for (int j = 0; j < dat[i].length; j++) {
						dat[i][j] = allsamples[i][j];
					}
				}

				// System.out.println("offline estimation");
				// start = System.nanoTime();
				// double[][] SpikedGraph10 = new
				// SpikedUpperEstimator(10).inverseCovariance(dat);
				// end = System.nanoTime();
				// ps.println("Spiked-off-10" + "\t" + t + " \t updating\t" +
				// Utils.getErrorL2(icov, SpikedGraph10) + "\t"
				// + Utils.getErrorInf(icov, SpikedGraph10) + "\t" + (end -
				// start));
				//
				// start = System.nanoTime();
				// double[][] SpikedGraph20 = new
				// SpikedUpperEstimator(20).inverseCovariance(dat);
				// end = System.nanoTime();
				// ps.println("Spiked-off-20" + "\t" + t + " \t updating\t" +
				// Utils.getErrorL2(icov, SpikedGraph20) + "\t"
				// + Utils.getErrorInf(icov, SpikedGraph20) + "\t" + (end -
				// start));

				start = System.nanoTime();
				// double[][] SampleGraph;
				// if (dat[0].length <= 400 && dat.length > dat[0].length) {
				double[][] SampleGraph = new SampleCovarianceEstimator().covariance(dat);
				Matrix lambI = Matrix.identity(SampleGraph.length, SampleGraph.length).times(0);
				SampleGraph = (new Matrix(SampleGraph).plus(lambI)).inverse().getArray();
				// SampleGraph = TruncatedSVD.eigenTruncatedInverse(SampleGraph,
				// s);
				end = System.nanoTime();
				SampleGraph spGraph = new SampleGraph(SampleGraph, true);
				igraph = spGraph.adaptiveThresholding(Gaussian.PhiInverse(1-ci/p/(p-1)) / Math.sqrt(t + init_size));

				// double sFNorm = new Matrix(SampleGraph).normF();
				// double sInfNorm = new Matrix(SampleGraph).normInf();
				ps.println("Offline" + "\t" + t + " \t updating\t" + overlap(igraph, icov) / nonZeros(icov) + "\t"
						+ overlap(igraph, icov) / nonZeros(igraph) + "\t" + (end - start));

				// // }else{
				// start = System.nanoTime();
				// SampleGraph = new SpikedUpperEstimator(dat[0].length /
				// 3).inverseCovariance(dat);
				// // }
				// end = System.nanoTime();
				// ps.println("offline-spiked" + "\t" + t + " \t updating\t" +
				// Utils.getErrorL2(icov, SampleGraph) + "\t"
				// + Utils.getErrorInf(icov, SampleGraph) + "\t" + (end -
				// start));

				start = System.nanoTime();
				double[][] SpikedGraph5 = new SpikedUpperEstimator(((int) (p * 0.25))).inverseCovariance(dat);
				end = System.nanoTime();

				SampleGraph spGraph5 = new SampleGraph(SpikedGraph5, true);
				igraph = spGraph5.adaptiveThresholding(Gaussian.PhiInverse(1-ci/p/(p-1)) / Math.sqrt(t + init_size));

				ps.println("TSVD-25" + "\t" + t + " \t updating\t" + overlap(igraph, icov) / nonZeros(icov) + "\t"
						+ overlap(igraph, icov) / nonZeros(igraph) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] SpikedGraph10 = new SpikedUpperEstimator(((int) (p * 0.5))).inverseCovariance(dat);
				end = System.nanoTime();
				SampleGraph spGraph50 = new SampleGraph(SpikedGraph10, true);
				igraph = spGraph50.adaptiveThresholding(Gaussian.PhiInverse(1-ci/p/(p-1)) / Math.sqrt(t + init_size));

				ps.println("TSVD-50" + "\t" + t + " \t updating\t" + overlap(igraph, icov) / nonZeros(icov) + "\t"
						+ overlap(igraph, icov) / nonZeros(igraph) + "\t" + (end - start));

				// start = System.nanoTime();
				// double[][] SpikedGraph20 = new
				// SpikedUpperEstimator(20).inverseCovariance(dat);
				// end = System.nanoTime();
				// ps.println("TSVD-20" + "\t" + t + " \t updating\t" +
				// Utils.getErrorL2(icov, SpikedGraph20) + "\t"
				// + Utils.getErrorInf(icov, SpikedGraph20) + "\t" + (end
				// -start));

				// start = System.nanoTime();
				// double[][] SpikedGraph100 = new
				// SpikedUpperEstimator(100).inverseCovariance(dat);
				// end = System.nanoTime();
				// ps.println("Spiked-off-100" + "\t" + t + " \t updating\t" +
				// Utils.getErrorL2(icov, SpikedGraph100)
				// + "\t" + Utils.getErrorInf(icov, SpikedGraph100) + "\t" +
				// (end - start));

			}
		}
	}

	public static double overlap(int[][] m1, int[][] m2) {
		int overlap = 0;
		for (int i = 0; i < m1.length; i++) {
			for (int j = 0; j < m1.length; j++) {
				if (m1[i][j] != 0 && m2[i][j] != 0)
					overlap++;
			}
		}
		return overlap;
	}

	public static double overlap(int[][] m1, double[][] m2) {
		int overlap = 0;
		for (int i = 0; i < m1.length; i++) {
			for (int j = 0; j < m1.length; j++) {
				if (m1[i][j] != 0 && m2[i][j] != 0)
					overlap++;
			}
		}
		return overlap;
	}

	public static double overlap(double[][] m1, int[][] m2) {
		int overlap = 0;
		for (int i = 0; i < m1.length; i++) {
			for (int j = 0; j < m1.length; j++) {
				if (m1[i][j] != 0 && m2[i][j] != 0)
					overlap++;
			}
		}
		return overlap;
	}

	public static double nonZeros(int[][] x) {
		int s = 0;
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x.length; j++) {
				if (x[i][j] != 0)
					s++;
			}
		}
		return s;
	}

	public static double nonZeros(double[][] x) {
		int s = 0;
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x.length; j++) {
				if (x[i][j] != 0.0)
					s++;
			}
		}
		return s;
	}

	public static double overlap(double[][] m1, double[][] m2) {
		int overlap = 0;
		for (int i = 0; i < m1.length; i++) {
			for (int j = 0; j < m1.length; j++) {
				if (m1[i][j] != 0 && m2[i][j] != 0)
					overlap++;
			}
		}
		return overlap;
	}
}
