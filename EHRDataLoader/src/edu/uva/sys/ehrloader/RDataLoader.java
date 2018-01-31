package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RDataLoader {
	private List<List<Double>> data = new ArrayList<List<Double>>();
	private List<String> labels = new ArrayList<String>();
	private Set<String> labelSet = new HashSet<String>();
	private int sampleNum = 0;
	private int featureNum = 0;

	public void load(String metaPath, String[] paths) {
		for (String path : paths)
			load(metaPath + path);
	}

	public void load(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String ln = br.readLine();
			String[] lnns = ln.split(",");
			for (String lnn : lnns) {
				labels.add(lnn);
				this.labelSet.add(lnn);
				data.add(new ArrayList<Double>());
			}
			sampleNum = lnns.length;
			ln = br.readLine();
			while (ln != null) {
				String[] lns = ln.split(",");
				if (lns.length == sampleNum) {
					int sampleIndex = 0;
					for (String lnn : lns) {
						data.get(sampleIndex).add(Double.parseDouble(lnn));
						sampleIndex++;
					}
					this.featureNum++;
				}
				ln = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public double[][] getDataMatrix() {
		double[][] dataMatrix = new double[this.sampleNum][this.featureNum];
		int iindex = 0;
		for (List<Double> dataIns : data) {
			int jindex = 0;
			for (double value : dataIns) {
				dataMatrix[iindex][jindex++] = value;
			}
			iindex++;
		}
		return dataMatrix;
	}

	public int[] getLabel() {
		int[] _labels = new int[this.labels.size()];
		ArrayList<String> labelItems = new ArrayList<String>(this.labelSet);
		Collections.sort(labelItems);
		int i = 0;
		for (String l : this.labels) {
		//	System.out.println(l+"\t"+labelItems.indexOf(l));
			_labels[i++] = labelItems.indexOf(l);
		}
		return _labels;
	}

}
