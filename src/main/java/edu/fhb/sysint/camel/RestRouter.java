package edu.fhb.sysint.camel;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.CxfConstants;

import edu.fhb.sysint.camel.dao.EarthpartDao;
import edu.fhb.sysint.camel.dao.EarthquakeDao;
import edu.fhb.sysint.camel.model.Earthpart;
import edu.fhb.sysint.camel.model.Earthquake;

public class RestRouter extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		String name = RestServiceImpl.class.getName();
		from("cxfrs://http://localhost:9000?resourceClasses=" + name).process(
				new Processor() {
					public void process(Exchange exchange) throws Exception {
						// custom processing here
						boolean foundSomething = false;
						Message inMessage = exchange.getIn();
						String operationName = inMessage.getHeader(
								CxfConstants.OPERATION_NAME, String.class);
						// syso("the operation name is: " + operationName);
						// String httpMethod = inMessage.getHeader(
						// Exchange.HTTP_METHOD, String.class);
						// assertEquals("Get a wrong http method", "GET",
						// httpMethod);
						String path = inMessage.getHeader(Exchange.HTTP_PATH,
								String.class);
						// The parameter of the invocation is stored in the
						// body of in message
						String id = (String) inMessage.getBody(String.class);

						if ("getAllEarthparts".equals(operationName)) {

							if (path.contains("/Earthparts")) {
								exchange.getOut().setBody(EarthpartDao.all());
								foundSomething = true;
							}
						}

						if ("getEarthquake".equals(operationName)) {

							String findByIdPath = "/Earthquake/findById/";
							if (path.contains(findByIdPath)) {
								String ident = path.split(findByIdPath)[1];
								syso(ident);
								Earthquake findById = EarthquakeDao
										.findById(Integer.parseInt(ident));
								syso(findById);
								exchange.getOut().setBody(findById);
								foundSomething = true;
							}
						}
						if (!foundSomething) {
							syso("the path was: " + path);
							Response r = Response
									.status(404)
									.entity("Can't find the customer with uri "
											+ path).build();
							throw new WebApplicationException(r);
						}
						syso("the operation name was: " + operationName);

					}
				});
	}

	protected void syso(Object string) {
		System.out.println(string);

	}
}
