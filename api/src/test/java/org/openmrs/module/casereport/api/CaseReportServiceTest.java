/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.api;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.casereport.DemoListener;
import org.openmrs.module.casereport.UuidAndValue;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains tests for CaseReportService
 */
public class CaseReportServiceTest extends BaseModuleContextSensitiveTest {
	
	private static final String XML_DATASET = "moduleTestData-initialCaseReports.xml";
	
	private static final String XML_OTHER_DATASET = "moduleTestData-other.xml";
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	private PatientService patientService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private ObjectMapper mapper = null;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(XML_DATASET);
		if (mapper == null) {
			mapper = new ObjectMapper();
		}
	}
	
	/**
	 * @see CaseReportService#getCaseReport(Integer)
	 * @verifies return the case report that matches the specified id
	 */
	@Test
	public void getCaseReport_shouldReturnTheCaseReportThatMatchesTheSpecifiedId() throws Exception {
		CaseReport report = service.getCaseReport(1);
		assertNotNull(report);
		assertEquals("5f7d57f0-9077-11e1-aaa4-00248140a5ef", report.getUuid());
		assertEquals(2, report.getReportTriggers().size());
		Iterator<CaseReportTrigger> it = report.getReportTriggers().iterator();
		assertEquals("HIV Switched To Second Line", it.next().getName());
		assertEquals("New HIV Case", it.next().getName());
	}
	
	/**
	 * @see CaseReportService#getCaseReportByUuid(String)
	 * @verifies return the case report that matches the specified uuid
	 */
	@Test
	public void getCaseReportByUuid_shouldReturnTheCaseReportThatMatchesTheSpecifiedUuid() throws Exception {
		CaseReport report = service.getCaseReportByUuid("5f7d57f0-9077-11e1-aaa4-00248140a5ef");
		assertNotNull(report);
		assertEquals(1, report.getId().intValue());
	}
	
	/**
	 * @see CaseReportService#getCaseReportByPatient(Patient)
	 * @verifies get the case report for the patient
	 */
	@Test
	public void getCaseReportByPatient_shouldGetTheCaseReportForThePatient() throws Exception {
		CaseReport caseReport = service.getCaseReportByPatient(patientService.getPatient(2));
		assertNotNull(caseReport);
		assertEquals(1, caseReport.getId().intValue());
	}
	
	/**
	 * @see CaseReportService#getCaseReports()
	 * @verifies return all non voided case reports in the database
	 */
	@Test
	public void getCaseReports_shouldReturnAllNonVoidedCaseReportsInTheDatabase() throws Exception {
		assertEquals(3, service.getCaseReports().size());
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies return all case reports in the database if all arguments are set to true
	 */
	@Test
	public void getCaseReports_shouldReturnAllCaseReportsInTheDatabaseIfAllArgumentsAreSetToTrue() throws Exception {
		List<CaseReport> reports = service.getCaseReports(true, true, true);
		assertEquals(9, reports.size());
		//Should be ordered by date created with latest first
		assertEquals(9, reports.get(0).getId().intValue());
		assertEquals(4, reports.get(7).getId().intValue());
		assertEquals(1, reports.get(8).getId().intValue());
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies include dismissed reports in the database if includeDismissed is set to true
	 */
	@Test
	public void getCaseReports_shouldIncludeDismissedReportsInTheDatabaseIfIncludeDismissedIsSetToTrue() throws Exception {
		List<CaseReport> reports = service.getCaseReports(false, false, true);
		assertEquals(4, reports.size());
		assertTrue(TestUtil.containsId(reports, 1));
		assertTrue(TestUtil.containsId(reports, 2));
		assertTrue(TestUtil.containsId(reports, 4));
		assertTrue(TestUtil.containsId(reports, 6));
		
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies include submitted reports in the database if includeSubmitted is set to true
	 */
	@Test
	public void getCaseReports_shouldIncludeSubmittedReportsInTheDatabaseIfIncludeSubmittedIsSetToTrue() throws Exception {
		List<CaseReport> reports = service.getCaseReports(false, true, false);
		assertEquals(5, reports.size());
		assertTrue(TestUtil.containsId(reports, 1));
		assertTrue(TestUtil.containsId(reports, 2));
		assertTrue(TestUtil.containsId(reports, 4));
		assertTrue(TestUtil.containsId(reports, 5));
		assertTrue(TestUtil.containsId(reports, 8));
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies include voided reports in the database if includeVoided is set to true
	 */
	@Test
	public void getCaseReports_shouldIncludeVoidedReportsInTheDatabaseIfIncludeVoidedIsSetToTrue() throws Exception {
		List<CaseReport> reports = service.getCaseReports(true, false, false);
		assertEquals(5, reports.size());
		assertTrue(TestUtil.containsId(reports, 1));
		assertTrue(TestUtil.containsId(reports, 2));
		assertTrue(TestUtil.containsId(reports, 4));
		assertTrue(TestUtil.containsId(reports, 3));
		assertTrue(TestUtil.containsId(reports, 7));
	}
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies return the saved case report
	 */
	@Test
	public void saveCaseReport_shouldReturnTheSavedCaseReport() throws Exception {
		int originalCount = service.getCaseReports().size();
		CaseReport cr = new CaseReport(patientService.getPatient(7), "HIV Switched To Second Line");
		service.saveCaseReport(cr);
		assertNotNull(cr.getId());
		assertEquals(++originalCount, service.getCaseReports().size());
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if the case report is null
	 */
	@Test
	public void submitCaseReport_shouldFailIfTheCaseReportIsNull() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Case report form cannot be blank"));
		CaseReport cr = service.getCaseReport(1);
		cr.setReportForm(null);
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if the case report is a white space character
	 */
	@Test
	public void submitCaseReport_shouldFailIfTheCaseReportIsAWhiteSpaceCharacter() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Case report form cannot be blank"));
		CaseReport cr = service.getCaseReport(1);
		cr.setReportForm(" ");
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if the case report is blank
	 */
	@Test
	public void submitCaseReport_shouldFailIfTheCaseReportIsBlank() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Case report form cannot be blank"));
		CaseReport cr = service.getCaseReport(1);
		cr.setReportForm("");
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if submitter and openmrs impl id GP are not set
	 */
	@Test
	public void submitCaseReport_shouldFailIfSubmitterAndOpenmrsImplIdGPAreNotSet() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		assertNull(Context.getAdministrationService().getImplementationId());
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Implementation id must be set to submit case reports if the submitter and "
		        + "assigning authority id are not set"));
		CaseReport cr = service.getCaseReport(1);
		cr.setReportForm(mapper.writeValueAsString(new CaseReportForm(cr)));
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if assigningAuthorityId and openmrs impl id GP are not set
	 */
	@Test
	public void submitCaseReport_shouldFailIfAssigningAuthorityIdAndOpenmrsImplIdGPAreNotSet() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		assertNull(Context.getAdministrationService().getImplementationId());
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Implementation id must be set to submit case reports if the submitter and "
		        + "assigning authority id are not set"));
		CaseReport cr = service.getCaseReport(1);
		CaseReportForm form = new CaseReportForm(cr);
		form.setSubmitter(new UuidAndValue("some uuid", "some system id"));
		cr.setReportForm(mapper.writeValueAsString(form));
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies pass if submitter and authorityId are set and impl id GP is not set
	 */
	@Test
	public void submitCaseReport_shouldPassIfSubmitterAndAuthorityIdAreSetAndImplIdGPIsNotSet() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		assertNull(Context.getAdministrationService().getImplementationId());
		CaseReport cr = service.getCaseReport(1);
		CaseReportForm form = new CaseReportForm(cr);
		final String uuid = "some uuid";
		final String implId = "some id";
		form.setSubmitter(new UuidAndValue(uuid, "some system id"));
		form.setAssigningAuthorityId(implId);
		cr.setReportForm(mapper.writeValueAsString(form));
		service.submitCaseReport(cr);
		CaseReportForm savedForm = mapper.readValue(cr.getReportForm(), CaseReportForm.class);
		assertEquals(implId, savedForm.getAssigningAuthorityId());
		assertEquals(uuid, savedForm.getSubmitter().getUuid());
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies overwrite the assigning authority id if submitter is set to authenticated user
	 */
	@Test
	public void submitCaseReport_shouldOverwriteTheAssigningAuthorityIdIfSubmitterIsSetToAuthenticatedUser()
	    throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		final String implId = "Test_Impl";
		final String implName = "Some name";
		//set the implementation id for test purposes
		AdministrationService adminService = Context.getAdministrationService();
		String implementationIdGpValue = "<implementationId id=\"1\" implementationId=\"" + implId + "\">"
		        + "   <passphrase id=\"2\"><![CDATA[Some passphrase]]></passphrase>"
		        + "   <description id=\"3\"><![CDATA[Some descr]]></description>" + "   <name id=\"4\"><![CDATA[" + implName
		        + "]]></name>" + "</implementationId>";
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID, implementationIdGpValue);
		adminService.saveGlobalProperty(gp);
		
		CaseReport cr = service.getCaseReport(1);
		CaseReportForm form = new CaseReportForm(cr);
		form.setAssigningAuthorityId("some other id that will be overwritten");
		cr.setReportForm(mapper.writeValueAsString(form));
		service.submitCaseReport(cr);
		CaseReportForm savedForm = mapper.readValue(cr.getReportForm(), CaseReportForm.class);
		assertEquals(implId, savedForm.getAssigningAuthorityId());
		assertEquals(Context.getAuthenticatedUser().getUuid(), savedForm.getSubmitter().getUuid());
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies submit the specified case report
	 */
	@Test
	public void submitCaseReport_shouldSubmitTheSpecifiedCaseReport() throws Exception {
		final String implId = "Test_Impl";
		final String implName = "Some name";
		//set the implementation id for test purposes
		AdministrationService adminService = Context.getAdministrationService();
		String implementationIdGpValue = "<implementationId id=\"1\" implementationId=\"" + implId + "\">"
		        + "   <passphrase id=\"2\"><![CDATA[Some passphrase]]></passphrase>"
		        + "   <description id=\"3\"><![CDATA[Some descr]]></description>" + "   <name id=\"4\"><![CDATA[" + implName
		        + "]]></name>" + "</implementationId>";
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID, implementationIdGpValue);
		adminService.saveGlobalProperty(gp);
		
		CaseReport cr = service.getCaseReport(2);
		CaseReportForm form = new ObjectMapper().readValue(cr.getReportForm(), CaseReportForm.class);
		assertNull(null, form.getSubmitter());
		assertNull(null, form.getSubmitter());
		assertNull(null, form.getSubmitter());
		assertNull(null, form.getAssigningAuthorityId());
		assertNull(null, form.getAssigningAuthorityName());
		assertFalse(cr.isSubmitted());
		
		service.submitCaseReport(cr);
		assertTrue(cr.isSubmitted());
		form = new ObjectMapper().readValue(cr.getReportForm(), CaseReportForm.class);
		assertEquals(Context.getAuthenticatedUser().getUuid(), form.getSubmitter().getUuid());
		assertEquals(Context.getAuthenticatedUser().getSystemId(), form.getSubmitter().getValue());
		assertEquals(implId, form.getAssigningAuthorityId());
		assertEquals(implName, form.getAssigningAuthorityName());
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies call the registered submit event listeners
	 */
	@Test
	public void submitCaseReport_shouldCallTheRegisteredSubmitEventListeners() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		final String implId = "Test_Impl";
		final String implName = "Some name";
		//set the implementation id for test purposes
		AdministrationService adminService = Context.getAdministrationService();
		String implementationIdGpValue = "<implementationId id=\"1\" implementationId=\"" + implId + "\">"
		        + "   <passphrase id=\"2\"><![CDATA[Some passphrase]]></passphrase>"
		        + "   <description id=\"3\"><![CDATA[Some descr]]></description>" + "   <name id=\"4\"><![CDATA[" + implName
		        + "]]></name>" + "</implementationId>";
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID, implementationIdGpValue);
		adminService.saveGlobalProperty(gp);
		
		CaseReport cr = service.getCaseReport(1);
		cr.setReportForm(new ObjectMapper().writeValueAsString(new CaseReportForm(cr)));
		DemoListener listener = Context.getRegisteredComponent("demoListener", DemoListener.class);
		//Reset
		listener.setReportUuid(null);
		assertNull(listener.getReportUuid());
		final String expectedUuid = cr.getUuid();
		service.submitCaseReport(cr);
		assertTrue(StringUtils.isNotBlank(listener.getReportUuid()));
		assertEquals(expectedUuid, listener.getReportUuid());
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if the case report is voided
	 */
	@Test
	public void submitCaseReport_shouldFailIfTheCaseReportIsVoided() throws Exception {
		CaseReport cr = service.getCaseReport(7);
		assertFalse(cr.isSubmitted());
		assertTrue(cr.isVoided());
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Cannot submit a voided case report"));
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if the case report is already dismissed
	 */
	@Test
	public void submitCaseReport_shouldFailIfTheCaseReportIsAlreadyDismissed() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Cannot submit a dismissed case report"));
		CaseReport cr = service.getCaseReport(6);
		assertTrue(cr.isDismissed());
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if the case report is already submitted
	 */
	@Test
	public void submitCaseReport_shouldFailIfTheCaseReportIsAlreadySubmitted() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Cannot submit a submitted case report"));
		CaseReport cr = service.getCaseReport(5);
		assertTrue(cr.isSubmitted());
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#dismissCaseReport(CaseReport)
	 * @verifies dismiss the specified case report
	 */
	@Test
	public void dismissCaseReport_shouldDismissTheSpecifiedCaseReport() throws Exception {
		CaseReport cr = service.getCaseReport(4);
		assertFalse(cr.isDismissed());
		service.dismissCaseReport(cr);
		assertTrue(cr.isDismissed());
	}
	
	/**
	 * @see CaseReportService#dismissCaseReport(CaseReport)
	 * @verifies fail if the case report is voided
	 */
	@Test
	public void dismissCaseReport_shouldFailIfTheCaseReportIsVoided() throws Exception {
		CaseReport cr = service.getCaseReport(7);
		assertFalse(cr.isDismissed());
		assertTrue(cr.isVoided());
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Cannot dismiss a voided case report"));
		service.dismissCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#dismissCaseReport(CaseReport)
	 * @verifies fail if the case report is already dismissed
	 */
	@Test
	public void dismissCaseReport_shouldFailIfTheCaseReportIsAlreadyDismissed() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Cannot dismiss a dismissed case report"));
		CaseReport cr = service.getCaseReport(6);
		assertTrue(cr.isDismissed());
		service.dismissCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#dismissCaseReport(CaseReport)
	 * @verifies fail if the case report is already submitted
	 */
	@Test
	public void dismissCaseReport_shouldFailIfTheCaseReportIsAlreadySubmitted() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Cannot dismiss a submitted case report"));
		CaseReport cr = service.getCaseReport(5);
		assertTrue(cr.isSubmitted());
		service.dismissCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#voidCaseReport(CaseReport,String)
	 * @verifies void the specified case report
	 */
	@Test
	public void voidCaseReport_shouldVoidTheSpecifiedCaseReport() throws Exception {
		CaseReport cr = service.getCaseReport(1);
		assertFalse(cr.isVoided());
		assertNull(cr.getDateVoided());
		assertNull(cr.getVoidedBy());
		assertNull(cr.getDateVoided());
		
		service.voidCaseReport(cr, "some reason");
		assertTrue(cr.isVoided());
		assertNotNull(cr.getDateVoided());
		assertNotNull(cr.getVoidedBy());
		assertNotNull(cr.getDateVoided());
	}
	
	/**
	 * @see CaseReportService#unvoidCaseReport(CaseReport)
	 * @verifies unvoid the specified case report
	 */
	@Test
	public void unvoidCaseReport_shouldUnvoidTheSpecifiedCaseReport() throws Exception {
		CaseReport cr = service.getCaseReport(7);
		assertTrue(cr.isVoided());
		assertNotNull(cr.getDateVoided());
		assertNotNull(cr.getVoidedBy());
		assertNotNull(cr.getDateVoided());
		
		service.unvoidCaseReport(cr);
		assertFalse(cr.isVoided());
		assertNull(cr.getDateVoided());
		assertNull(cr.getVoidedBy());
		assertNull(cr.getDateVoided());
	}
	
	/**
	 * @see CaseReportService#getSubmittedCaseReports(Patient)
	 * @verifies return all the previously submitted case reports for the specified patient
	 */
	@Test
	public void getSubmittedCaseReports_shouldReturnAllThePreviouslySubmittedCaseReportsForTheSpecifiedPatient()
	    throws Exception {
		List<CaseReport> caseReports = service.getSubmittedCaseReports(patientService.getPatient(7));
		assertEquals(2, caseReports.size());
		assertTrue(TestUtil.containsId(caseReports, 5));
		assertTrue(TestUtil.containsId(caseReports, 8));
	}
	
	/**
	 * @see CaseReportService#getSubmittedCaseReports(Patient)
	 * @verifies return all the previously submitted case reports if no patient is specified
	 */
	@Test
	public void getSubmittedCaseReports_shouldReturnAllThePreviouslySubmittedCaseReportsIfNoPatientIsSpecified()
	    throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		List<CaseReport> submittedReports = service.getSubmittedCaseReports(null);
		assertEquals(4, submittedReports.size());
		assertTrue(TestUtil.containsId(submittedReports, 5));
		assertTrue(TestUtil.containsId(submittedReports, 8));
		assertTrue(TestUtil.containsId(submittedReports, 200));
		assertTrue(TestUtil.containsId(submittedReports, 201));
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if no concept is linked to the trigger
	 */
	@Test
	public void submitCaseReport_shouldFailIfNoConceptIsLinkedToTheTrigger() throws Exception {
		CaseReport cr = service.getCaseReport(4);
		assertEquals(1, cr.getReportTriggers().size());
		String triggerName = cr.getReportTriggers().iterator().next().getName();
		cr.setReportForm("some json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(CoreMatchers.equalTo("No concept was found that is linked to the trigger: "
		        + triggerName));
		service.submitCaseReport(cr);
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies fail if the linked concept isn't mapped to ciel
	 */
	@Test
	public void submitCaseReport_shouldFailIfTheLinkedConceptIsntMappedToCiel() throws Exception {
		CaseReport cr = service.getCaseReport(4);
		assertEquals(1, cr.getReportTriggers().size());
		String triggerName = cr.getReportTriggers().iterator().next().getName();
		SchedulerService ss = Context.getSchedulerService();
		TaskDefinition td = ss.getTaskByName(triggerName);
		td.setProperty(CaseReportConstants.CONCEPT_TASK_PROPERTY, "LOINC_123");
		ss.saveTaskDefinition(td);
		cr.setReportForm("some json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(CoreMatchers.equalTo("Only CIEL concept mappings are currently allowed"));
		service.submitCaseReport(cr);
	}
}
