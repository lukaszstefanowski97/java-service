package com.avid.connector.tutorial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class JavaServiceResponse {
    @JsonProperty
    public final String jobStatus;
    @JsonProperty
    public final Integer numberOfSubmittedJobs;

    @JsonCreator
    public JavaServiceResponse(@JsonProperty("jobStatus") String jobStatus, @JsonProperty("numberOfSubmittedJobs") Integer numberOfSubmittedJobs) {
        this.jobStatus = jobStatus;
        this.numberOfSubmittedJobs = numberOfSubmittedJobs;
    }
}
