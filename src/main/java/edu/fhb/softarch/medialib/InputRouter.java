package edu.fhb.softarch.medialib;

import java.io.File;
import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;

import edu.fhb.softarch.GlobalConstants;
import edu.fhb.softarch.medialib.model.Earthquake;
import edu.fhb.softarch.medialib.model.EarthquakeCollection;

public class InputRouter extends RouteBuilder {
	private static final String HTTP_WWW_W3_ORG_2003_01_GEO_WGS84_POS = "http://www.w3.org/2003/01/geo/wgs84_pos";

	@Override
	public void configure() throws Exception {

		final CommonUtils file = new CommonUtils();
		final DataFormat jaxb = new JaxbDataFormat(
				"edu.fhb.softarch.medialib.model");

		from(
				"http://geofon.gfz-potsdam.de/db/eqinfo.php?fmt=rss&splitEntries=false")
				// from("http://localhost/eqInfo.xml")
				.setHeader("visited", constant(true))
				.to("xslt:data/xsl/transformation.xsl").to("direct:start")
				.delay(1000);

		from(
				"http://earthquake.usgs.gov/eqcenter/catalogs/eqs1day-M2.5.xml?splitEntries=false")
				// from("http://localhost/eqs1dat.xml")
				.setHeader("visited", constant(true))
				.to("xslt:data/xsl/transformation2.xsl").to("direct:start")
				.delay(1000);

		from("direct:start")
				.aggregate(header("visited"), new MyAggregationStrategy())
				// .enrich("direct:enrichUri", new
				// MyEnrichAggregationStrategy())
				.completionSize(2).completionTimeout(3000).delay(3000)
				.to("direct:filterBiggestEarthquakes")
				.to("direct:UnmarshallMergedSources");

		from("direct:UnmarshallMergedSources").unmarshal(jaxb)
				.process(new Processor() {
					public void process(Exchange exchange) throws Exception {
						EarthquakeCollection ec = exchange.getIn().getBody(
								EarthquakeCollection.class);
						ArrayList<Earthquake> listClone = new ArrayList<Earthquake>();
						int i = 1;
						for (Earthquake e : ec.getEntries()) {
							String additionalInfo = CommonUtils
									.findAdditionalInfo(e.getLocation());

							e.setCountry(additionalInfo.contains("not found") ? "undefined"
									: additionalInfo);
							e.setId(i++);

							listClone.add(e);
						}
						ec.setEntries(listClone);
						exchange.getIn()
								.setBody(ec, EarthquakeCollection.class);
					}
				}).process(new Processor() {
					public void process(Exchange exchange) throws Exception {

						String body = exchange.getIn().getBody(String.class);
						body = body.replaceAll("<\\?xml(.*)>", "");

						file.writeToFile(
								GlobalConstants.IntermediateResult_ENRICHMENT,
								body, false);
					}
				}).marshal(jaxb)
				// .to("file://"+GlobalConstants.IntermediateResult+"?append=false");
				.to("file:/Users/nils/Desktop/result.xml").delay(3000);

		from("direct:filterBiggestEarthquakes")
				.split(xpath("/earthquakes/earthquake[size>5.4]"))
				.setHeader("splitted", constant(true))
				.aggregate(header("splitted"), new AnotherAggregationStrategy())
				.completionInterval(2000)
				.process(new Processor() {
					public void process(Exchange exchange) throws Exception {
						String body = exchange.getIn().getBody(String.class);
						body = "<earthquakes>" + body + "</earthquakes>";
						exchange.getIn().setBody(body, String.class);
					}
				})
				.unmarshal(jaxb)
				.process(new Processor() {

					public void process(Exchange exchange) throws Exception {
						EarthquakeCollection ec = exchange.getIn().getBody(
								EarthquakeCollection.class);

						String emailBody = "<b>Notification - Heavy Earthquakes</b><p/>"
								+ "<ul>";

						for (Earthquake e : ec.getEntries()) {
							emailBody += "<li><a href=\""
									+ GlobalConstants.PROTOCOL_HOST
									+ GlobalConstants.REST_SERVICE_RELATIVE_PATH
									+ e.getId() + "\">" + e.getTitle()
									+ " - M " + e.getSize() + "</a>" + "</li>";
						}

						emailBody += "</ul>";
						System.out.println(emailBody);
						exchange.getIn().setBody(emailBody, String.class);
					}
				})
				.to("smtps://camelfhb@smtp.gmail.com?password=camelfhb31&to=camelfhb@googlemail.com")
				.delay(120000);
		// .to("direct:filterBiggestEarthquakes")
		// .to("file:/Users/nils/Desktop/true").delay(1000)
		// .to("smtps://camelfhb@smtp.gmail.com?password=camelfhb31&to=camelfhb@googlemail.com&contentType=text/html");

		Thread.sleep(5000);
	}
}
