package io.spring.billrun.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
@SpringBatchTest
public class FilePreparationTaskletTests {


    @BeforeEach
    public void setUp() {
        this.jobRepositoryTestUtils.removeJobExecutions();
        JdbcTestUtils.deleteFromTables(this.jdbcTemplate, "BILLING_DATA");
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTestUtils jdbcTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testJobExecution() throws Exception {
        JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addString("input.file", "src/main/resources/billing-2023-02.csv")
                .addString("output.file", "./staging/billing-report-2023-01.csv")
                .addString("data.year", "2023")
                .addString("data.month", "1")
                .toJobParameters();
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
        Assertions.assertTrue(Files.exists(Paths.get("./staging", "billing-2023-02.csv")));
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        Assertions.assertEquals(1000, JdbcTestUtils.countRowsInTable(jdbcTemplate, "BILLING_DATA"));
        Path billingReport = Paths.get("./staging", "billing-report-2023-01.csv");
        Assertions.assertTrue(Files.exists(billingReport));
        Assertions.assertEquals(781, Files.lines(billingReport).count());

    }

    @Test
    void testJobWithoutParameters() throws Exception {
       try{
           this.jobLauncherTestUtils.launchJob();
       }catch (JobParametersInvalidException e) {

       }catch (Exception e) {
           throw new Exception(e);
       }
    }
}
