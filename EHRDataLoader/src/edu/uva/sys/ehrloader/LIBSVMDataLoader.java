package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LIBSVMDataLoader {
	private List<HashMap<Integer, Double>> data = new ArrayList<HashMap<Integer, Double>>();
	private List<String> labels = new ArrayList<String>();
	private Set<String> labelSet = new HashSet<String>();
	private Set<Integer> index = new HashSet<Integer>();

	public void load(String metaPath, String[] paths) {
		for (String path : paths)
			load(metaPath + path);
	}

	public void load(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String ln = br.readLine();
			while (ln != null) {
				String[] lns = ln.split(" ");
				if (lns.length > 1) {
					labels.add((lns[0]));
					labelSet.add((lns[0]));
					HashMap<Integer, Double> dataIns = new HashMap<Integer, Double>();
					data.add(dataIns);
					for (int i = 1; i < lns.length; i++) {
						if (!lns[i].equals("")) {
							Integer _index = Integer.parseInt(lns[i].split(":")[0]);
							double _value = Double.parseDouble(lns[i].split(":")[1]);
							dataIns.put(_index, _value);
							index.add(_index);
						}
					}
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
		List<Integer> indexRank = new ArrayList<Integer>(this.index);
		Collections.sort(indexRank);
		double[][] dataMatrix = new double[data.size()][indexRank.size()];
		int jndex = 0;
		for (HashMap<Integer, Double> dataIns : data) {
			for (int iindex = 0; iindex < indexRank.size(); iindex++) {
				if (dataIns.containsKey(iindex))
					dataMatrix[jndex][iindex] = dataIns.get(iindex);
				else
					dataMatrix[jndex][iindex] = 0.0;
			}
			jndex++;
		}
		return dataMatrix;
	}

	public int[] getLabel() {
		int[] _labels = new int[this.labels.size()];
		ArrayList<String> labelItems = new ArrayList<String>(this.labelSet);
		Collections.sort(labelItems);
		int i = 0;
		for (String l : this.labels)
			_labels[i++] = labelItems.indexOf(l);
		return _labels;
	}

}
