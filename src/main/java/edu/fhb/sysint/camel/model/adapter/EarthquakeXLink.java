package edu.fhb.sysint.camel.model.adapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.fhb.sysint.camel.GlobalConstants;
import edu.fhb.sysint.camel.model.Earthquake;

@XmlRootElement(namespace = "xlink")
public class EarthquakeXLink {

	@XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
	private String href = "http://www.example.org";

	@XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
	private String type = "simple";

	@XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
	private String show = "new";

	@XmlAttribute(namespace = "")
	private String title = "";

	public EarthquakeXLink() {
	}

	public EarthquakeXLink(Earthquake eq) {
		this.title = eq.getTitle();
		this.href = GlobalConstants.REST_SERVICE_RELATIVE_PATH
				+ eq.getId();
	}

}