package com.avid.connector.tutorial;

import com.avid.acs.bus.BusAccess;
import com.avid.acs.bus.BusAccessException;
import com.avid.acs.bus.OperationContext;
import com.avid.acs.bus.ResponseHandler;
import com.avid.acs.bus.error.BusError;
import com.avid.acs.bus.error.ErrorSet;
import com.avid.acs.bus.message.Message;
import com.avid.acs.bus.message.MessageOptions;
import com.avid.acs.bus.message.MutableMessage;
import com.avid.acs.bus.message.data.Data;
import com.avid.acs.bus.message.data.JsonData;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;


    public class JavaServiceImpl implements JavaService {
        private static final Logger LOG = LoggerFactory.getLogger(JavaServiceImpl.class);

        private final BusAccess busAccess;

        public JavaServiceImpl(BusAccess busAccess) {
            this.busAccess = busAccess;
        }

        @Override
        public void submitJob(Integer jobId, Integer execTime, final OperationContext<JavaServiceResponse> operationContext) {
            if (jobId == null || jobId < 1) {
                Map<String, String> params = Collections.singletonMap("jobId", jobId == null ? "null" : String.valueOf(jobId));
                operationContext.error(new BusError("INVALID_JOB_ID", params, "Invalid jobId was provided. Must be integer > 0."));
            } else if (execTime == null || execTime < 100) {
                Map<String, String> params = Collections.singletonMap("execTime", execTime == null ? "null" : String.valueOf(execTime));
                operationContext.error(new BusError("INVALID_EXECUTION_TIME", params, "Invalid execution time provided. Must be integer >= 100."));
            } else {
                final SetNumberOfSubmittedJobsCallback setNumberOfSubmittedJobsCallback = new SetNumberOfSubmittedJobsCallback() {
                    @Override
                    public void handle(boolean responseMissing, ErrorSet errors, Integer oldNumber, Integer newNumber) {
                        JavaServiceResponse response;
                        if (responseMissing || errors.hasErrors()) {
                            response = new JavaServiceResponse("submitted", oldNumber);
                        } else {
                            response = new JavaServiceResponse("submitted", newNumber);
                        }

                        operationContext.respond(response);
                    }
                };

                final GetNumberOfSubmittedJobsCallback getNumberOfSubmittedJobsCallback = new GetNumberOfSubmittedJobsCallback() {
                    @Override
                    public void handle(boolean responseMissing, ErrorSet errors, Integer number) {
                        Integer newNumber;

                        if (!responseMissing && !errors.hasErrors()) {
                            newNumber = number + 1;
                            setNumberOfSubmittedJobs(number, newNumber, setNumberOfSubmittedJobsCallback);
                        } else if (errors != null && errors.hasErrors() && "avid.acs.attributes/NOT_FOUND".equals(errors.iterator().next().getCode())) {
                            newNumber = 1;
                            setNumberOfSubmittedJobs(number, newNumber, setNumberOfSubmittedJobsCallback);
                        } else {
                            JavaServiceResponse response = new JavaServiceResponse("submitted", null);
                            operationContext.respond(response);
                        }
                    }
                };


                getNumberOfSubmittedJobs(getNumberOfSubmittedJobsCallback);
            }
        }

        private void getNumberOfSubmittedJobs(GetNumberOfSubmittedJobsCallback callback) {
            Message message = new MutableMessage("avid.acs.attributes", "global", 3, "fetch");
            message.parameters().put("name", "java.service.store");

            try {
                MessageOptions options = new MessageOptions(2000);
                busAccess.query(message, createGetNumberOfSubmittedJobsResponseHandler(callback), options);
            } catch (BusAccessException e) {
                callback.handle(true, null, null);
            }
        }

        private void setNumberOfSubmittedJobs(Integer number, int newNumber, SetNumberOfSubmittedJobsCallback callback) {
            Message message = new MutableMessage("avid.acs.attributes", "global", 3, "store");
            message.parameters().put("name", "java.service.store");
            message.parameters().put("value", Collections.singletonMap("numberOfSubmittedJobs", newNumber));

            try {
                MessageOptions options = new MessageOptions(2000);
                busAccess.query(message, createSetNumberOfSubmittedJobsResponseHandler(callback, number, newNumber), options);
            } catch (BusAccessException e) {
                callback.handle(true, null, number, newNumber);
            }
        }

        private ResponseHandler createGetNumberOfSubmittedJobsResponseHandler(final GetNumberOfSubmittedJobsCallback callback) {
            return new ResponseHandler() {
                @Override
                public void onResponse(@NotNull Message response) {
                    final Data value = response.results().get("value");
                    Integer number = null;

                    if (value != null && value instanceof JsonData) {
                        final JsonNode jsonNode = ((JsonData) value).get().get("numberOfSubmittedJobs");
                        if (jsonNode != null) {
                            number = jsonNode.asInt();
                        }
                    } else {
                        if (value == null) {
                            number = 0;
                        }
                    }

                    callback.handle(false, response.errors(), number);
                }

                @Override
                public void onMessageProcessingError(@NotNull String error) {
                    LOG.info("Error occurred");
                    callback.handle(true, null, null);
                }

                @Override
                public void onTimeout() {
                    LOG.info("Timeout occurred");
                    callback.handle(true, null, null);
                }
            };
        }

        private ResponseHandler createSetNumberOfSubmittedJobsResponseHandler(final SetNumberOfSubmittedJobsCallback callback, final Integer number, final Integer newNumber) {
            return new ResponseHandler() {

                @Override
                public void onResponse(@NotNull Message response) {
                    callback.handle(false, response.errors(), number, newNumber);
                }

                @Override
                public void onMessageProcessingError(@NotNull String error) {
                    LOG.info("Error occurred");
                    callback.handle(true, null, number, newNumber);
                }

                @Override
                public void onTimeout() {
                    LOG.info("Timeout occurred");
                    callback.handle(true, null, number, newNumber);
                }
            };
        }

        private interface GetNumberOfSubmittedJobsCallback {
            void handle(boolean responseMissing, ErrorSet errors, Integer number);
        }

        private interface SetNumberOfSubmittedJobsCallback {
            void handle(boolean responseMissing, ErrorSet errors, Integer oldNumber, Integer newNumber);
        }
    }


