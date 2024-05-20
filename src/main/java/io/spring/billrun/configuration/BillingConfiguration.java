package io.spring.billrun.configuration;



import io.spring.billrun.job.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.support.JdbcTransactionManager;
import javax.sql.DataSource;



@Configuration
public class BillingConfiguration {


    private static final String FILE_PREPARATION_STEP = "File preparation";
    @Bean
    public Job job(JobRepository jobRepository, Step stepReadFile, Step stepWriteInDataSource, Step stepProcessBillingInformation) {
        return new JobBuilder("Billing Job", jobRepository)
                .start(stepReadFile)
                .next(stepWriteInDataSource)
                .next(stepProcessBillingInformation)
                .validator(new JobPreparationParameterValidator())
                .build();
    }

    @Bean
    public Step stepReadFile(JobRepository jobRepository, JdbcTransactionManager jdbcTransactionManager) {
        return new StepBuilder(FILE_PREPARATION_STEP, jobRepository).tasklet(new FilePreparationTasklet(), jdbcTransactionManager).build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<BillingData> billingDataReader(@Value("#{jobParameters['input.file']}") String  inputFile){
        return new FlatFileItemReaderBuilder<BillingData>()
                .name("BillingDataFileReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .names("dataYear", "dataMonth", "accountId", "phoneNumber", "dataUsage", "callDuration", "smsCount")
                .targetType(BillingData.class)
                .build();
    }

    @Bean

    public JdbcBatchItemWriter<BillingData> billingDataJdbcBatchItemWriter(DataSource dataSource) {
        String sql = "insert into BILLING_DATA values (:dataYear, :dataMonth, :accountId, :phoneNumber, :dataUsage, :callDuration, :smsCount)";
        return new JdbcBatchItemWriterBuilder<BillingData>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()
                .build();
    }

    @Bean
    public Step stepWriteInDataSource(JobRepository jobRepository,
                                      JdbcTransactionManager jdbcTransactionManager,
                                      @Qualifier(value = "billingDataReader") ItemReader<BillingData> billingDataItemReader,
                                      ItemWriter<BillingData> billingDataItemWriter
    )
    {
        return new StepBuilder("fileIngestion",jobRepository)
                .<BillingData, BillingData>chunk(100, jdbcTransactionManager)
                .reader(billingDataItemReader)
                .writer(billingDataItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<BillingData> billingDataItemreader(DataSource dataSource,
                                                                   @Value("#{jobParameters['data.year']}") Integer year,
                                                                   @Value("#{jobParameters['data.month']}") Integer month) {
        String sql = String.format("SELECT * FROM BILLING_DATA WHERE DATA_YEAR = %d AND DATA_MONTH = %d", year, month);
        return new JdbcCursorItemReaderBuilder<BillingData>()
                .name("BilllingDataItemReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new DataClassRowMapper<>(BillingData.class))
                .build();
    }

    @Bean
    public BillingDataProcessor billingDataProcessor() {
        return new BillingDataProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ReportingData> billingFileItemWriter(@Value("#{jobParameters['output.file']}") String outputFile) {
        return new FlatFileItemWriterBuilder<ReportingData>()
               .resource(new FileSystemResource(outputFile))
                .name("billingFileDataWriter")
                .delimited()
                .names("billingData.dataYear",
                        "billingData.dataMonth",
                        "billingData.accountId",
                        "billingData.phoneNumber",
                        "billingData.dataUsage",
                        "billingData.callDuration",
                        "billingData.smsCount",
                        "billingTotal")
                .build();
    }

    @Bean
    public Step stepProcessBillingInformation(JobRepository jobRepository,
                                              JdbcTransactionManager jdbcTransactionManager,
                                              @Qualifier(value = "billingDataItemreader") ItemReader<BillingData> billingDataTableReader,
                                              ItemProcessor<BillingData, ReportingData> billingDataProcessor,
                                              ItemWriter<ReportingData> billingFileWriter
                                              ) {
        return new StepBuilder("reportingGenerator", jobRepository)
                .<BillingData, ReportingData>chunk(100, jdbcTransactionManager)
                .reader(billingDataTableReader)
                .processor(billingDataProcessor)
                .writer(billingFileWriter)
                .build();
    }

}
