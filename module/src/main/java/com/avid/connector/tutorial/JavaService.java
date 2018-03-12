package com.avid.connector.tutorial;

import com.avid.acs.bus.annotations.*;
import com.avid.acs.bus.annotations.Error;
import com.avid.acs.bus.OperationContext;
import com.avid.acs.bus.error.ErrorSeverity;
import com.avid.acs.bus.message.Message;
import com.avid.acs.bus.service.info.RequestMethod;

@BusService(type = "avid.tutorial.java.service", realm = "global", version = 1, desc = "Tutorial service based on Avid Connector API for Java")

@Errors({
        @Error(code = "INVALID_JOB_ID", severity = ErrorSeverity.ERROR, messageTemplate = "Invalid jobId: %{jobId}", status = 500)
})

public interface JavaService {

    @Operation(name = "submitJob", desc = "Submit job for execution. Returns response back with information whether job submitted successfully or not.")
    @Examples({
            @Example(name = "Submit valid job", file = "submit_valid_job.json"),
            @Example(name = "Submit invalid jobId", file = "submit_invalid_job_id.json"),
            @Example(name = "Submit invalid execTime", file = "submit_invalid_exec_time.json")

    })
    @RestRequest(path = "jobs/{jobId}", method = RequestMethod.POST, queryParams = { "execTime" })
    void submitJob(@Param("jobId") Integer jobId, @Param("execTime") Integer execTime, OperationContext<JavaServiceResponse> operationContext);
}
