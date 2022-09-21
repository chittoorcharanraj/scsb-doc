package org.recap.controller;

import org.recap.ScsbCommonConstants;
import org.recap.model.job.JobDto;
import org.recap.model.job.JobParamDataDto;
import org.recap.model.job.JobParamDto;
import org.recap.model.jpa.JobEntity;
import org.recap.model.jpa.JobParamEntity;
import org.recap.repository.jpa.JobDetailsRepository;
import org.recap.repository.jpa.JobParamDetailRepository;
import org.recap.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by rajeshbabuk on 12/4/17.
 */
@RestController
@RequestMapping("/updateJobService")
public class UpdateJobController {

    @Autowired
    private JobDetailsRepository jobDetailsRepository;

    @Autowired
    private JobParamDetailRepository jobParamDetailRepository;

    @Autowired
    private CommonUtil commonUtil;

    /**
     * This method is used to update the job entity.
     *
     * @param jobEntity the job entity
     * @return the string
     */
    @PostMapping(value="/updateJob")
    public String updateJob(@RequestBody JobEntity jobEntity) {
        jobDetailsRepository.save(jobEntity);
        return ScsbCommonConstants.SUCCESS;
    }

    /**
     * This method returns all jobs
     * @return Job Dto List
     */
    @GetMapping(value = "/getAllJobs")
    public List<JobDto> getAllJobs() {
        List<JobEntity> jobEntities = jobDetailsRepository.findAll();
        return jobEntities.isEmpty() ? new ArrayList<>() : jobEntities.stream().map(jobEntity -> (JobDto) commonUtil.convertToDto(jobEntity, JobDto.class)).collect(Collectors.toCollection(ArrayList::new)
        );
    }

    /**
     * This method returns the job dto by job name
     * @return Job Dto
     */
    @GetMapping(value = "/getJobByName")
    public JobDto getJobByName(@RequestParam String jobName) {
        JobEntity jobEntity = jobDetailsRepository.findByJobName(jobName);
        return jobEntity != null ? (JobDto) commonUtil.convertToDto(jobEntity, JobDto.class) : null;
    }

    /**
     * This method returns the job param dto by job name
     * @return Job Param Dto
     */
    @GetMapping(value = "/getJobParamsByJobName")
    public JobParamDto getJobParamsByJobName(@RequestParam String jobName) {
        JobParamEntity jobParamEntity = jobParamDetailRepository.findByJobName(jobName);
        if (jobParamEntity != null) {
            JobParamDto jobParamDto = (JobParamDto) commonUtil.convertToDto(jobParamEntity, JobParamDto.class);
            List<JobParamDataDto> jobParamDataDtos = !jobParamEntity.getJobParamDataEntities().isEmpty() ? jobParamEntity.getJobParamDataEntities().stream().map(jobParamDataEntity -> (JobParamDataDto) commonUtil.convertToDto(jobParamDataEntity, JobParamDataDto.class)).collect(Collectors.toCollection(ArrayList::new)) : new ArrayList<>();
            jobParamDto.setJobParamDataDtos(jobParamDataDtos);
            return jobParamDto;
        }
        return null;
    }
}
