package io.spring.billrun.job;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JobPreparationParameterValidator implements JobParametersValidator {


    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        Set<Map.Entry<String, JobParameter<?>>> allParameters = parameters.getIdentifyingParameters().entrySet();
        Iterator<Map.Entry<String, JobParameter<?>>> iteratorParameter = allParameters.iterator();
        while(iteratorParameter.hasNext()) {
            Map.Entry<String, JobParameter<?>> parameterEntry = iteratorParameter.next();
            if(isMandatoryParameter(parameterEntry.getKey()) && !isValidParameter(parameterEntry.getValue().getValue().toString())){
                throw new JobParametersInvalidException("parameters: ".concat(parameterEntry.getKey()).concat(" is mandatory"));
            }
        }
    }

    private Boolean isValidParameter(String parameter) {
        return parameter != null && !parameter.trim().isEmpty();
    }

    private Boolean isMandatoryParameter(String parameter) throws JobParametersInvalidException{

        if(parameter == null || parameter.trim().isEmpty()) {
            throw new JobParametersInvalidException("parameter must be valid");
        }
        Map<String, Boolean> allMandatoryParameters = new HashMap<>();
        allMandatoryParameters.put("output.file", true);
        allMandatoryParameters.put("input.file", true);
        allMandatoryParameters.put("data.year", true);
        allMandatoryParameters.put("data.month", true);
        return allMandatoryParameters.get(parameter);
    }
}
