package edu.uva.sys.ehrloader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EHRecordBase {
	public static int missingLines = 0;

	public static SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");

	public HashMap<String, HashMap<Date, List<String>>> _database = new HashMap<String, HashMap<Date, List<String>>>();

	public List<String> _patients = new ArrayList<String>();
	public List<String> _codes = new ArrayList<String>();
	public List<Date> _dates = new ArrayList<Date>();
	public HashMap<String, Integer> _hospitals = new HashMap<String, Integer>();
	public HashMap<String, Integer> _ages = new HashMap<String, Integer>();
	public HashMap<String, Integer> _gender = new HashMap<String, Integer>();
	public HashMap<String, Integer> _labels = new HashMap<String, Integer>();
	public String _name;

	public static HashMap<String, EHRecordBase> _bases = new HashMap<String, EHRecordBase>();

	public EHRecordBase(String name) {
		EHRecordBase._bases.put(name, this);
		this._name = name;
	}

	public EHRecordBase(String name, EHRecordBase _base) {
		EHRecordBase._bases.put(name, this);
		this.insertRecords(_base);
		this._name = name;
	}

	public static EHRecordBase getBase(String name) {
		if (_bases.containsKey(name))
			return _bases.get(name);
		return new EHRecordBase(name);
	}

	public void removeVisitsAfter(Set<String> codes, int days) {
		for (String patient : _patients) {
			HashMap<Date, List<String>> ap = _database.get(patient);
			List<Date> ds = new ArrayList<Date>(ap.keySet());
			Collections.sort(ds);
			boolean detect = false;
			int index = 0;
			for (; index < ds.size() && !detect; index++) {
				Date d = ds.get(index);
				for (String c : ap.get(d)) {
					if (codes.contains(c)) {
						detect = true;
						System.out.println("hit:\t" + c);
					}
				}
			}
			index--;

			for (int i = index; i < ds.size(); i++)
				ap.remove(ds.get(i));
			if (index > 1) {
				for (int i = index - 1; i >= 0; i--) {
					if(ds.get(index).getTime()-ds.get(i)
							.getTime()<=(24*3600*days)){
						ap.remove(i);
					}
				}
			}
		}

	}

	public void setPositiveLabel(Set<String> codes) {
		for (String patient : _patients) {
			HashMap<Date, List<String>> ap = _database.get(patient);
			List<Date> ds = new ArrayList<Date>(ap.keySet());
			Collections.sort(ds);
			boolean positive = false;
			for (Date d : ds) {
				for (String c : ap.get(d)) {
					if (codes.contains(c)) {
						positive = true;
					}
				}
			}
			if (positive) {
				this._labels.put(patient, 1);
			} else {
				this._labels.put(patient, 0);
			}
		}

	}

	public void removePatientLessNVisit(int visit) {
		Set<String> toRemove = new HashSet<String>();
		for (String pid : this._database.keySet()) {
			if (_database.get(pid).keySet().size() < visit) {
				toRemove.add(pid);
			}
		}
		this._patients.removeAll(toRemove);
		for (String pid : toRemove)
			this._database.remove(pid);
	}

	public EHRecordBase rebase() {
		EHRecordBase ebase = new EHRecordBase(this._name + "_rebase", this);
		return ebase;
	}

	public void insertRecords(EHRecordBase _base) {
		for (String pid : _base._database.keySet()) {
			for (Date dTime : _base._database.get(pid).keySet()) {
				for (String code : _base._database.get(pid).get(dTime)) {
					this.insertRecord(pid, dTime, code);
				}
			}
		}
		this._labels.putAll(_base._labels);
		this._ages.putAll(_base._ages);
		this._gender.putAll(_base._gender);
		this.missingLines = _base.missingLines;
	}

	public void insertRecord(String pid, String dtime, String code, int age, int gender, int hospital) {
		try {

			Date dIns = dt.parse(dtime);
			insertRecord(pid, dIns, code);
			this._gender.put(pid, gender);
			this._ages.put(pid, age);
			this._hospitals.put(pid, hospital);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertRecord(String pid, Date dIns, String code) {
		if (!_dates.contains(dIns))
			this._dates.add(dIns);
		if (!_codes.contains(code))
			this._codes.add(code);
		if (!_patients.contains(pid))
			this._patients.add(pid);
		if (!_database.containsKey(pid)) {
			_database.put(pid, new HashMap<Date, List<String>>());
		}
		if (!_database.get(pid).containsKey(dIns)) {
			_database.get(pid).put(dIns, new ArrayList<String>());
		}
		_database.get(pid).get(dIns).add(code);
	}

	public List<String> getPatients() {
		Collections.sort(this._patients);
		return _patients;
	}

	public List<String> getCodes() {
		Collections.sort(this._codes);
		return _codes;
	}

	public List<Date> getDates() {
		Collections.sort(this._dates);
		return _dates;
	}

	public double[][] getFrequencyMatrix() {
		this.getPatients();
		this.getDates();
		this.getCodes();

		double[][] m = new double[this._patients.size()][this._codes.size()];

		for (String pid : this._database.keySet()) {
			for (Date dTime : this._database.get(pid).keySet()) {
				for (String code : this._database.get(pid).get(dTime)) {
					m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
				}
			}
		}

		return m;
	}

	public double[][] getBinaryMatrix() {
		this.getPatients();
		this.getDates();
		this.getCodes();

		double[][] m = new double[this._patients.size()][this._codes.size()];

		for (String pid : this._database.keySet()) {
			for (Date dTime : this._database.get(pid).keySet()) {
				System.out.println(pid + "\t" + this._database.get(pid).get(dTime).size());
				for (String code : this._database.get(pid).get(dTime)) {
					if (m[this._patients.indexOf(pid)][this._codes.indexOf(code)] == 0)
						m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
				}
			}
		}

		return m;
	}

	public double[][] getFrequencyMatrixWithRandomVisitMissing(double seed) {
		this.getPatients();
		this.getDates();
		this.getCodes();

		double[][] m = new double[this._patients.size()][this._codes.size()];

		for (String pid : this._database.keySet()) {
			for (Date dTime : this._database.get(pid).keySet()) {
				if (Math.random() >= seed) {
					for (String code : this._database.get(pid).get(dTime)) {
						m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
					}
				}
			}
		}

		return m;
	}

	public double[][] getFrequencyMatrixWithRandomCodeMissing(double seed) {
		this.getPatients();
		this.getDates();
		this.getCodes();

		double[][] m = new double[this._patients.size()][this._codes.size()];

		for (String pid : this._database.keySet()) {
			for (Date dTime : this._database.get(pid).keySet()) {
				for (String code : this._database.get(pid).get(dTime)) {
					if (Math.random() >= seed) {
						m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
					}
				}
			}
		}

		return m;
	}

	public double[][] getBinaryMatrixWithRandomVisitMissing(double seed) {
		this.getPatients();
		this.getDates();
		this.getCodes();

		double[][] m = new double[this._patients.size()][this._codes.size()];

		for (String pid : this._database.keySet()) {
			for (Date dTime : this._database.get(pid).keySet()) {
				if (Math.random() >= seed) {
					for (String code : this._database.get(pid).get(dTime)) {
						if (m[this._patients.indexOf(pid)][this._codes.indexOf(code)] == 0)
							m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
					}
				}
			}
		}

		return m;
	}

	public double[][] getBinaryMatrixWithRandomCodeMissing(double seed) {
		this.getPatients();
		this.getDates();
		this.getCodes();

		double[][] m = new double[this._patients.size()][this._codes.size()];

		for (String pid : this._database.keySet()) {
			for (Date dTime : this._database.get(pid).keySet()) {
				for (String code : this._database.get(pid).get(dTime)) {
					if (Math.random() >= seed) {
						if (m[this._patients.indexOf(pid)][this._codes.indexOf(code)] == 0)
							m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
					}
				}
			}
		}

		return m;
	}

	public void setLabelsForAllPatients(Integer label) {
		this.getPatients();
		this._labels.clear();
		for (String pid : this._database.keySet()) {
			this._labels.put(pid, label);
		}

	}

	public int[] getLabels() {
		List<String> lls = this.getPatients();
		int[] labels = new int[lls.size()];
		int index = 0;
		for (String pid : lls) {
			labels[index++] = this._labels.get(pid);
		}
		return labels;
	}

}
