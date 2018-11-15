package de.evoila.cf.autoscaler.tests.wrapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.evoila.cf.autoscaler.core.model.RequestWrapper;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.tests.TestBase;

public class RequestWrapperTest extends TestBase {
	
	@Test
	public void testRequest() {

		RequestWrapper req = app.getRequest();
		
		req.setThresholdPolicy(ScalableApp.MAX);
		assertEquals(metricReader.getRequestMax(), req.getValueOfHttpRequests());
		
		req.setThresholdPolicy(ScalableApp.MEAN);
		assertEquals(metricReader.getRequestMean(), req.getValueOfHttpRequests());
		
		req.setThresholdPolicy(ScalableApp.MIN);
		assertEquals(metricReader.getRequestMin(), req.getValueOfHttpRequests());
		
		assertEquals(0, req.getQuotient());
		
		System.out.println(app.getCpu().getUpperLimit());
		
		req.setRequestsPerInstance();
		assertEquals(85,req.getQuotient());
		
		req.setMinQuotient(101);
		assertEquals(101, req.getQuotient());
		
		req.setRequestsPerInstance();
		assertEquals(136,req.getQuotient());
		
		req.resetQuotient();
		assertEquals(101, req.getQuotient());
	}

}
