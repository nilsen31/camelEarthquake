package edu.fhb.sysint.camel.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.fhb.sysint.camel.model.EarthPartCollection;
import edu.fhb.sysint.camel.model.Earthpart;
import edu.fhb.sysint.camel.model.Earthquake;
import edu.fhb.sysint.camel.model.EarthquakeCollection;

public class EarthpartDao {

	public static EarthPartCollection all() {

		EarthquakeCollection quakeCollection = JaxBUtil.unmarshall();
		List<Earthpart> partList = new ArrayList<Earthpart>();
		Set<String> distinctCountrys = new HashSet<String>();

		for (Earthquake iterable_element : quakeCollection.getEntries()) {
			distinctCountrys.add(iterable_element.getCounrty());
		}

		List<Earthquake> tmpList = new ArrayList<Earthquake>();
		for (String country : distinctCountrys) {

			for (Earthquake iterable_element : quakeCollection.getEntries()) {
				if (iterable_element.getCounrty().equals(country)) {
					tmpList.add(iterable_element);
				}
			}
			partList.add(new Earthpart(country, new EarthquakeCollection(
					tmpList)));
			tmpList = new ArrayList<Earthquake>();
		}
		
		EarthPartCollection partCollection = new EarthPartCollection();
		partCollection.setEarthparts(partList);
		System.out.println("----------------->collection:" + partCollection);
		return partCollection;
	}
}
