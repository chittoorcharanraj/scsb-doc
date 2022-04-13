package org.recap.controller;


import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.job.JobDto;
import org.recap.model.job.JobParamDataDto;
import org.recap.model.job.JobParamDto;
import org.recap.model.jpa.JobEntity;
import org.recap.model.jpa.JobParamDataEntity;
import org.recap.model.jpa.JobParamEntity;
import org.recap.repository.jpa.JobDetailsRepository;
import org.recap.repository.jpa.JobParamDetailRepository;
import org.recap.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by rajeshbabuk on 20/4/17.
 */
public class UpdateJobControllerUT extends BaseTestCaseUT {

    @InjectMocks
    UpdateJobController updateJobController;

    @Mock
    JobDetailsRepository jobDetailsRepository;

    @Mock
    JobParamDetailRepository jobParamDetailRepository;

    @Mock
    CommonUtil commonUtil;

    @Mock
    JobParamDto jobParamDto;

    @Mock
    JobParamEntity jobParamEntity;

    @Mock
    JobEntity jobEntity;

    @Mock
    JobDto jobDto;

    @Mock
    JobParamDataDto jobParamDataDto;

    @Mock
    JobParamDataEntity jobParamDataEntity;

    @Test
    @DisplayName("Test the method which is used to update the job entity")
    public void testUpdateJob() throws Exception {
        JobEntity jobEntity = new JobEntity();
        jobEntity.setJobName(ScsbConstants.PURGE_EXCEPTION_REQUESTS);
        String status=updateJobController.updateJob(jobEntity);
        assertEquals(ScsbCommonConstants.SUCCESS,status);
    }

    @Test
    @DisplayName("Test the method which returns all jobs empty jobEntities")
    public void testGetAllJobsEmpty() throws Exception {
        List<JobDto> jobDtoList = updateJobController.getAllJobs();
        assertNotNull(jobDtoList);
    }

    @Test
    @DisplayName("Test the method which returns all jobs")
    public void testGetAllJobs() throws Exception {
        List<JobEntity> jobEntities=new ArrayList<>();
        jobEntities.add(jobEntity);
        Mockito.when(jobDetailsRepository.findAll()).thenReturn(jobEntities);
        List<JobDto> jobDtoList = updateJobController.getAllJobs();
        assertNotNull(jobDtoList);
    }

    @Test
    @DisplayName("Test the job dto by job name")
    public void testGetJobByName() throws Exception {
        Mockito.when(jobDetailsRepository.findByJobName(Mockito.anyString())).thenReturn(jobEntity);
        Mockito.when(commonUtil.convertToDto(Mockito.any(),Mockito.any())).thenReturn(jobDto);
        JobDto job = updateJobController.getJobByName(ScsbConstants.PURGE_EXCEPTION_REQUESTS);
        assertNotNull(job);
    }

    @Test
    @DisplayName("Test the job dto by job name as null")
    public void testGetJobByNameNull() throws Exception {
        Mockito.when(jobDetailsRepository.findByJobName(Mockito.anyString())).thenReturn(null);
        JobDto job = updateJobController.getJobByName(ScsbConstants.PURGE_EXCEPTION_REQUESTS);
        assertNull(job);
    }

    @Test
    @DisplayName("Test the job param dto by job name")
    public void testGetJobParamsByJobName() throws Exception {
        List<JobParamDataEntity> jobParamDataEntities=new ArrayList<>();
        jobParamDataEntities.add(jobParamDataEntity);
        Mockito.when(jobParamEntity.getJobParamDataEntities()).thenReturn(jobParamDataEntities);
        Mockito.when(commonUtil.convertToDto(Mockito.any(),Mockito.any())).thenReturn(jobParamDto).thenReturn(jobParamDataDto);
        Mockito.when(jobParamDetailRepository.findByJobName(Mockito.anyString())).thenReturn(jobParamEntity);
        JobParamDto jobParamDto = updateJobController.getJobParamsByJobName(ScsbConstants.GENERATE_ACCESSION_REPORT_JOB);
        assertNotNull(jobParamDto);
    }

    @Test
    @DisplayName("Test the job param dto by job name for empty JobParamDataEntities")
    public void testGetJobParamsByJobNameEmpty() throws Exception {
        Mockito.when(commonUtil.convertToDto(Mockito.any(),Mockito.any())).thenReturn(jobParamDto);
        Mockito.when(jobParamDetailRepository.findByJobName(Mockito.anyString())).thenReturn(jobParamEntity);
        JobParamDto jobParamDto = updateJobController.getJobParamsByJobName(ScsbConstants.GENERATE_ACCESSION_REPORT_JOB);
        assertNotNull(jobParamDto);
    }

    @Test
    @DisplayName("Test the job param dto by job name for Null Value")
    public void testGetJobParamsByJobNameNull() throws Exception {
        JobParamDto jobParamDto = updateJobController.getJobParamsByJobName(ScsbConstants.GENERATE_ACCESSION_REPORT_JOB);
        assertNull(jobParamDto);
    }
}
