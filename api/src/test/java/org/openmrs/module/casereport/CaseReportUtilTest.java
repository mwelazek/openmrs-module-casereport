/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportUtilTest extends BaseModuleContextSensitiveTest {
	
	private static final String XML_DATASET = "moduleTestData-initial.xml";
	
	private static final String XML_CONCEPT_DATASET = "moduleTestData-initialConcepts.xml";
	
	private static final String XML_OTHER_DATASET = "moduleTestData-other.xml";
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	PatientService patientService;
	
	@Autowired
	SchedulerService schedulerService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	public static SqlCohortDefinition createTestSqlCohortDefinition(String name, String sql, boolean retired,
	                                                                Parameter... parameters) {
		SqlCohortDefinition definition = new SqlCohortDefinition(sql);
		definition.setName(name);
		definition.setRetired(retired);
		for (Parameter param : parameters) {
			definition.addParameter(param);
		}
		return DefinitionContext.saveDefinition(definition);
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentViralLoads(Patient)
	 * @verifies return the 3 most recent Viral load observations
	 */
	@Test
	public void getMostRecentViralLoads_shouldReturnThe3MostRecentViralLoadObservations() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_CONCEPT_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> viralLoads = CaseReportUtil.getMostRecentViralLoads(patient);
		assertEquals(3, viralLoads.size());
		assertEquals(8003, viralLoads.get(0).getId().intValue());
		assertEquals(8001, viralLoads.get(1).getId().intValue());
		assertEquals(8000, viralLoads.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentCD4counts(Patient)
	 * @verifies return the 3 most recent cd4 count observations
	 */
	@Test
	public void getMostRecentCD4counts_shouldReturnThe3MostRecentCd4CountObservations() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		executeDataSet(XML_CONCEPT_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> cd4counts = CaseReportUtil.getMostRecentCD4counts(patient);
		assertEquals(3, cd4counts.size());
		assertEquals(8010, cd4counts.get(0).getId().intValue());
		assertEquals(8008, cd4counts.get(1).getId().intValue());
		assertEquals(8007, cd4counts.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentHIVTests(Patient)
	 * @verifies return the 3 most recent HIV test observations
	 */
	@Test
	public void getMostRecentHIVTests_shouldReturnThe3MostRecentHIVTestObservations() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_CONCEPT_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> hivTests = CaseReportUtil.getMostRecentHIVTests(patient);
		assertEquals(3, hivTests.size());
		assertEquals(8016, hivTests.get(0).getId().intValue());
		assertEquals(8014, hivTests.get(1).getId().intValue());
		assertEquals(8013, hivTests.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentWHOStage(Patient)
	 * @verifies return the most recent WHO stage observation
	 */
	@Test
	public void getMostRecentWHOStage_shouldReturnTheMostRecentWHOStageObservation() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_CONCEPT_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(8020, CaseReportUtil.getMostRecentWHOStage(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getCurrentARVMedications(Patient, java.util.Date)
	 * @verifies get the current ARV medications for the specified patient
	 */
	@Test
	public void getCurrentARVMedications_shouldGetTheCurrentARVMedicationsForTheSpecifiedPatient() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_CONCEPT_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		Date asOfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2016-01-14 00:00:00.0");
		List<Drug> meds = CaseReportUtil.getCurrentARVMedications(patient, asOfDate);
		assertEquals(1, meds.size());
		assertEquals(20000, meds.get(0).getId().intValue());
		asOfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2016-01-16 00:00:00.0");
		
		meds = CaseReportUtil.getCurrentARVMedications(patient, asOfDate);
		assertEquals(2, meds.size());
		TestUtil.containsId(meds, 20000);
		TestUtil.containsId(meds, 20001);
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentReasonARVsStopped(Patient)
	 * @verifies return the most recent obs for the reason why the patient stopped taking ARVs
	 */
	@Test
	public void getMostRecentReasonARVsStopped_shouldReturnTheMostRecentObsForTheReasonWhyThePatientStoppedTakingARVs()
	    throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_CONCEPT_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(8024, CaseReportUtil.getMostRecentReasonARVsStopped(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getLastVisit(Patient)
	 * @verifies return the last visit for the specified patient
	 */
	@Test
	public void getLastVisit_shouldReturnTheLastVisitForTheSpecifiedPatient() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(101, CaseReportUtil.getLastVisit(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies return null if no cohort query is found that matches the trigger name
	 */
	@Test
	public void getSqlCohortDefinition_shouldReturnNullIfNoCohortQueryIsFoundThatMatchesTheTriggerName() throws Exception {
		assertNull(CaseReportUtil.getSqlCohortDefinition("some name that does not exist"));
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies fail if multiple cohort queries are found that match the trigger name
	 */
	@Test
	public void getSqlCohortDefinition_shouldFailIfMultipleCohortQueriesAreFoundThatMatchTheTriggerName() throws Exception {
		final String name = "some name that is a duplicate";
		createTestSqlCohortDefinition(name, "some query", false);
		createTestSqlCohortDefinition(name, "some query", false);
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Found multiple Sql Cohort Queries with name:" + name));
		CaseReportUtil.getSqlCohortDefinition(name);
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies not return a retired cohort query
	 */
	@Test
	public void getSqlCohortDefinition_shouldNotReturnARetiredCohortQuery() throws Exception {
		final String name = "some retired cohort query";
		createTestSqlCohortDefinition(name, "some query", true);
		assertNull(CaseReportUtil.getSqlCohortDefinition(name));
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies return the matched cohort query
	 */
	@Test
	public void getSqlCohortDefinition_shouldReturnTheMatchedCohortQuery() throws Exception {
		executeDataSet(XML_DATASET);
		SqlCohortDefinition definition = CaseReportUtil.getSqlCohortDefinition("HIV Switched To Second Line");
		assertNotNull(definition);
		Assert.assertEquals("5b4f091e-4f28-4810-944b-4e4ccf9bfbb3", definition.getUuid());
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies not create a duplicate trigger for the same patient
	 */
	@Test
	public void executeTask_shouldNotCreateADuplicateTriggerForTheSamePatient() throws Exception {
		executeDataSet(XML_DATASET);
		final String name = "HIV Switched To Second Line";
		final Integer patientId = 2;
		CaseReport caseReport = service.getCaseReport(1);
		Assert.assertEquals(patientId, caseReport.getPatient().getId());
		Assert.assertEquals(2, caseReport.getReportTriggers().size());
		int originalCaseReportCount = service.getCaseReports().size();
		assertNotNull(caseReport.getCaseReportTriggerByName(name));
		SqlCohortDefinition definition = CaseReportUtil.getSqlCohortDefinition(name);
		definition.setQuery("select patient_id from patient where patient_id=" + patientId);
		DefinitionContext.saveDefinition(definition);
		
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
		Assert.assertEquals(2, caseReport.getReportTriggers().size());
		Assert.assertEquals(originalCaseReportCount, service.getCaseReports().size());
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies set the concept mappings in the evaluation context
	 */
	@Test
	public void executeTask_shouldSetTheConceptMappingsInTheEvaluationContext() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_CONCEPT_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		final String name = "HIV Patient Died";
		Integer[] patientIds = { 2, 7 };
		String[] params = { "CIEL_856", "CIEL_1040" };
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		def.setQuery("select distinct person_id from obs where concept_id = :" + params[0] + " or concept_id = :"
		        + params[0]);
		def.addParameter(new Parameter(params[0], null, Integer.class));
		def.addParameter(new Parameter(params[1], null, Integer.class));
		DefinitionContext.saveDefinition(def);
		int originalCount = service.getCaseReports().size();
		int originalTriggerCount = service.getCaseReportByPatient(patientService.getPatient(patientIds[0]))
		        .getReportTriggers().size();
		Assert.assertEquals(2, originalTriggerCount);
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
		
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
		List<CaseReport> reports = service.getCaseReports();
		int newCount = reports.size();
		Assert.assertEquals(++originalCount, newCount);
		Assert.assertEquals(++originalTriggerCount, service.getCaseReportByPatient(patientService.getPatient(patientIds[0]))
		        .getReportTriggers().size());
		assertNotNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies fail if no sql cohort query matches the specified trigger name
	 */
	@Test
	public void executeTask_shouldFailIfNoSqlCohortQueryMatchesTheSpecifiedTriggerName() throws Exception {
		final String name = "some name that doesn't exist";
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("No sql cohort query was found that matches the name:" + name));
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY, name);
		CaseReportUtil.executeTask(taskDefinition);
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies fail for a task where the last execution time cannot be resolved
	 */
	@Test
	public void executeTask_shouldFailForATaskWhereTheLastExecutionTimeCannotBeResolved() throws Exception {
		final String name = "some cohort query";
		CaseReportUtilTest.createTestSqlCohortDefinition(name, "select patient_id from patient where date_changed > :"
		        + CaseReportConstants.LAST_EXECUTION_TIME, false, new Parameter(CaseReportConstants.LAST_EXECUTION_TIME,
		        null, Date.class));
		
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY, name);
		taskDefinition.setRepeatInterval(0L);
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Failed to resolve the value for the last execution time"));
		CaseReportUtil.executeTask(taskDefinition);
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies create case reports for the matched patients
	 */
	@Test
	public void executeTask_shouldCreateCaseReportsForTheMatchedPatients() throws Exception {
		executeDataSet(XML_DATASET);
		final String name = "New HIV Case";
		Integer[] patientIds = { 7, 8 };
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		def.setQuery("select patient_id from patient where patient_id in (" + patientIds[0] + "," + patientIds[1] + ")");
		DefinitionContext.saveDefinition(def);
		int originalCount = service.getCaseReports().size();
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[0])));
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
		List<CaseReport> reports = service.getCaseReports();
		int newCount = reports.size();
		Assert.assertEquals(originalCount + 2, newCount);
		CaseReport caseReport1 = service.getCaseReportByPatient(patientService.getPatient(patientIds[0]));
		assertNotNull(caseReport1);
		Assert.assertEquals(1, caseReport1.getReportTriggers().size());
		Assert.assertEquals(name, caseReport1.getReportTriggers().iterator().next().getName());
		CaseReport caseReport2 = service.getCaseReportByPatient(patientService.getPatient(patientIds[1]));
		assertNotNull(caseReport2);
		Assert.assertEquals(1, caseReport2.getReportTriggers().size());
		Assert.assertEquals(name, caseReport2.getReportTriggers().iterator().next().getName());
		
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies set the last execution time in the evaluation context
	 */
	@Test
	public void executeTask_shouldSetTheLastExecutionTimeInTheEvaluationContext() throws Exception {
		final String name = "some cohort query";
		Integer[] patientIds = { 7, 8 };
		CaseReportUtilTest.createTestSqlCohortDefinition(name, "select patient_id from patient where patient_id in ("
		        + patientIds[0] + "," + patientIds[1] + ") and date_changed > :" + CaseReportConstants.LAST_EXECUTION_TIME,
		    false, new Parameter(CaseReportConstants.LAST_EXECUTION_TIME, null, Date.class));
		int originalCount = service.getCaseReports().size();
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[0])));
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
		
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY, name);
		taskDefinition.setLastExecutionTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-18 12:25:57"));
		CaseReportUtil.executeTask(taskDefinition);
		List<CaseReport> reports = service.getCaseReports();
		Assert.assertEquals(originalCount, reports.size());
		
		taskDefinition.setLastExecutionTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-18 12:24:34"));
		CaseReportUtil.executeTask(taskDefinition);
		reports = service.getCaseReports();
		int newCount = reports.size();
		Assert.assertEquals(++originalCount, newCount);
		assertNotNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[0])));
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies add a new trigger to an existing queue item for the patient
	 */
	@Test
	public void executeTask_shouldAddANewTriggerToAnExistingQueueItemForThePatient() throws Exception {
		executeDataSet(XML_DATASET);
		final String name = "HIV Patient Died";
		final Integer patientId = 2;
		CaseReport caseReport = service.getCaseReportByPatient(patientService.getPatient(patientId));
		assertNotNull(caseReport);
		int originalTriggerCount = caseReport.getReportTriggers().size();
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		def.setQuery("select patient_id from patient where patient_id = " + patientId);
		DefinitionContext.saveDefinition(def);
		int originalCount = service.getCaseReports().size();
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
		Assert.assertEquals(originalCount, service.getCaseReports().size());
		caseReport = service.getCaseReportByPatient(patientService.getPatient(patientId));
		Assert.assertEquals(++originalTriggerCount, caseReport.getReportTriggers().size());
	}
}
