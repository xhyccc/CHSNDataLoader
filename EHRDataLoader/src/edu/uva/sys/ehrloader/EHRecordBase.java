package edu.uva.sys.ehrloader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EHRecordBase {
	private static SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd"); 

	private HashMap<String, HashMap<Date, Set<String>>> _database=new HashMap<String, HashMap<Date, Set<String>>>();

	private List<String> _patients=new ArrayList<String>();
	private List<String> _codes=new ArrayList<String>();
	private List<Date> _dates=new ArrayList<Date>();

	public static HashMap<String,EHRecordBase> _bases=new HashMap<String,EHRecordBase> ();
	
	public EHRecordBase(String name){
		EHRecordBase._bases.put(name, this);
	}
	
	public EHRecordBase(String name, EHRecordBase _base){
		EHRecordBase._bases.put(name, this);
		this.insertRecords(_base);
	}
	
	public static EHRecordBase getBase(String name){
		if(_bases.containsKey(name))
			return _bases.get(name);
		return new EHRecordBase(name);
	}
	
	public void insertRecords(EHRecordBase _base){
		for(String pid:_base._database.keySet()){
			for(Date dTime:_base._database.get(pid).keySet()){
				for(String code:_base._database.get(pid).get(dTime)){
					this.insertRecord(pid, dTime, code);
				}
			}
		}
	}
	
	public void insertRecord(String pid, String dtime, String code){
		try {
			
			Date dIns=dt.parse(dtime);
			insertRecord(pid, dIns, code);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void insertRecord(String pid, Date dIns, String code) {
		if(!_dates.contains(dIns))
			this._dates.add(dIns);
		if(!_codes.contains(code))
			this._codes.add(code);
		if(!_patients.contains(pid))
			this._patients.add(pid);
		if(!_database.containsKey(pid)){
			_database.put(pid, new HashMap<Date,Set<String>>());
		}
		if(!_database.get(pid).containsKey(dIns)){
			_database.get(pid).put(dIns, new HashSet<String>());
		}
		_database.get(pid).get(dIns).add(code);
	}
	
	public List<String> getPatients() {
		Collections.sort(this._patients);
		return _patients;
	}
	public List<String> getCodes(){
		Collections.sort(this._codes);
		return _codes;
	}
	public List<Date> getDates(){
		Collections.sort(this._dates);
		return _dates;
	}
	
	public double[][] getFrequencyMatrix(){
		this.getPatients();
		this.getDates();
		this.getCodes();
		
		
		double[][] m=new double[this._patients.size()][this._codes.size()];
		
		for(String pid:this._database.keySet()){
			for(Date dTime:this._database.get(pid).keySet()){
				for(String code:this._database.get(pid).get(dTime)){
					m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
				}
			}
		}

		
		return m;
	}
	
	public double[][] getBinaryMatrix(){
		this.getPatients();
		this.getDates();
		this.getCodes();

		double[][] m=new double[this._patients.size()][this._codes.size()];
		
		for(String pid:this._database.keySet()){
			for(Date dTime:this._database.get(pid).keySet()){
				for(String code:this._database.get(pid).get(dTime)){
					if(m[this._patients.indexOf(pid)][this._codes.indexOf(code)]==0)
						m[this._patients.indexOf(pid)][this._codes.indexOf(code)]++;
				}
			}
		}

		return m;
	}


}