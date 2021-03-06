package de.codecentric.soap.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.codecentric.soap.SoapTestApplication;
import de.codecentric.soap.common.BusinessException;
import de.codecentric.soap.common.FaultConst;
import de.codecentric.soap.soaprawclient.SoapRawClient;
import de.codecentric.soap.soaprawclient.SoapRawClientResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes=SoapTestApplication.class)
// @WebIntegrationTest-Configuration "server.port:XYZ" overrides the config of the embedded server, that is used (and re-used) in the Tests 
// If you want to log the actual SOAP-Messages, e.g. by using TCP/IP-Monitor in Eclipse, just change port/url in dev-test.properties 
@WebIntegrationTest("server.port:8093") 
public class WeatherServiceEndpointXMLErrorTest {

	@Autowired
	private SoapRawClient soapRawClient;
	
	
	@Value(value="classpath:requests/xmlErrorNotXmlSchemeCompliantUnderRootElementTest.xml")
	private Resource xmlErrorNotXmlSchemeCompliantUnderRootElementTestXml;
	
	@Value(value="classpath:requests/xmlErrorNotXmlSchemeCompliantRootElementTest.xml")
	private Resource xmlErrorNotXmlSchemeCompliantRootElementTestXml;
	
	@Value(value="classpath:requests/xmlErrorSoapHeaderMissingSlash.xml")
	private Resource xmlErrorSoapHeaderMissingSlashXml;
	
	@Value(value="classpath:requests/xmlErrorSoapBodyTagMissingBracketTest.xml")
	private Resource xmlErrorSoapBodyTagMissingBracketTestXml;
	
	@Value(value="classpath:requests/xmlErrorSoapHeaderTagMissingBracketTest.xml")
	private Resource xmlErrorSoapHeaderTagMissingBracketTestXml;
	
	@Value(value="classpath:requests/xmlErrorSoapEnvelopeTagMissingBracketTest.xml")
	private Resource xmlErrorSoapEnvelopeTagMissingBracketTestXml;
	
	@Value(value="classpath:requests/xmlErrorXMLHeaderDefinitionMissingBracket.xml")
	private Resource xmlErrorXMLHeaderDefinitionMissingBracketXml;
	
	@Value(value="classpath:requests/xmlErrorXMLTagNotClosedInsideBodyTest.xml")
	private Resource xmlErrorXMLTagNotClosedInsideBodyTestXml;
	
	
	
	/*
	 * Non-Scheme-compliant Errors
	 */
	
	@Test
	public void xmlErrorNotXmlSchemeCompliantUnderRootElementTest() throws BusinessException, IOException {
		checkXMLErrorNotSchemeCompliant(xmlErrorNotXmlSchemeCompliantUnderRootElementTestXml);
	}
	
	@Test
	public void xmlErrorNotXmlSchemeCompliantRootElementTest() throws BusinessException, IOException {
		checkXMLErrorNotSchemeCompliant(xmlErrorNotXmlSchemeCompliantRootElementTestXml);
	}
	
	@Test
	public void xmlErrorSoapHeaderMissingSlash() throws BusinessException, IOException {
		checkXMLErrorNotSchemeCompliant(xmlErrorSoapHeaderMissingSlashXml);
	}
	
	private void checkXMLErrorNotSchemeCompliant(Resource testFile) throws BusinessException, IOException {
		checkXMLError(testFile, FaultConst.SCHEME_VALIDATION_ERROR);
	}	
	
	/*
	 * Errors with syntactically incorrect XML
	 */
	
	@Test
	public void xmlErrorSoapBodyTagMissingBracketTest() throws BusinessException, IOException {
		checkXMLErrorSyntacticallyIncorrect(xmlErrorSoapBodyTagMissingBracketTestXml);
	}
	
	@Test
	public void xmlErrorSoapHeaderTagMissingBracketTest() throws BusinessException, IOException {
		checkXMLErrorSyntacticallyIncorrect(xmlErrorSoapHeaderTagMissingBracketTestXml);
	}
	
	@Test
	public void xmlErrorSoapEnvelopeTagMissingBracketTest() throws BusinessException, IOException {
		checkXMLErrorSyntacticallyIncorrect(xmlErrorSoapEnvelopeTagMissingBracketTestXml);
	}
	
	@Test
	public void xmlErrorXMLHeaderDefinitionMissingBracket() throws BusinessException, IOException {
		checkXMLErrorSyntacticallyIncorrect(xmlErrorXMLHeaderDefinitionMissingBracketXml);
	}	
	
	@Test
	public void xmlErrorXMLTagNotClosedInsideBodyTest() throws BusinessException, IOException {
		checkXMLErrorSyntacticallyIncorrect(xmlErrorXMLTagNotClosedInsideBodyTestXml);
	}
	
	
	private void checkXMLErrorSyntacticallyIncorrect(Resource testFile) throws BusinessException, IOException {
		checkXMLError(testFile, FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
	}
	
	private void checkXMLError(Resource testFile, FaultConst faultContent) throws BusinessException, IOException {
		// Given
		// Resource testFile
		
		// When
		SoapRawClientResponse soapRawResponse = soapRawClient.callSoapService(testFile.getInputStream());
		
		// Then
		assertNotNull(soapRawResponse);
		assertEquals("500 Internal Server Error expected", 500, soapRawResponse.getHttpStatusCode());
        assertEquals(faultContent.getMessage(), soapRawResponse.getFaultstringValue());
        
        de.codecentric.namespace.weatherservice.exception.WeatherException weatherException = soapRawResponse.getUnmarshalledObjectFromSoapMessage(de.codecentric.namespace.weatherservice.exception.WeatherException.class);		
		assertNotNull("<soap:Fault><detail> has to contain a de.codecentric.namespace.weatherservice.exception.WeatherException",  weatherException);
		
		assertEquals("ExtremeRandomNumber", weatherException.getUuid());
		assertEquals("The correct BusinessId is missing in WeatherException according to XML-scheme.", faultContent.getId(), weatherException.getBusinessErrorId());
	}
}
