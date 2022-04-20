package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.domain.model.Employee;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	
	/** JobBuilderのFactoryクラス */
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	/** StepBuilderのFactoryクラス */
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	/** Reader */
	@Autowired
	private ItemReader<Employee> employeeReader;

	/** DataSource(JDBCで必須) */
	@Autowired
	private DataSource dataSource;

	/** insert-sql(JDBC用) */
	private static final String INSERT_EMPLOYEE_SQL = 
			"INSERT INTO employee (id, name, age, gender)"
			+ "VALUES (:id, :name, :age, :gender)";

	/** Writer(JDBC) */
	@Bean
	@StepScope
	public JdbcBatchItemWriter<Employee> jdbcWriter() {
		// Provider生成
		BeanPropertyItemSqlParameterSourceProvider<Employee> provider = 
				new BeanPropertyItemSqlParameterSourceProvider<>();
		
		// 設定
		return new JdbcBatchItemWriterBuilder<Employee>() // Builderの生成
				.itemSqlParameterSourceProvider(provider)
				.sql(INSERT_EMPLOYEE_SQL)
				.dataSource(this.dataSource)
				.build();
	}
	
	/** Stepの生成(JDBC) */
	@Bean
	public Step inMemoryStep() {
		return this.stepBuilderFactory.get("InMemoryStep")
				.<Employee,Employee>chunk(10)
				.reader(employeeReader)
				.writer(jdbcWriter())
				.build();
	}

	/** Jobの生成(JDBC) */
	@Bean
	public Job inMemoryJob() {
		return this.jobBuilderFactory.get("InMemoryJob")
				.incrementer(new RunIdIncrementer())
				.start(inMemoryStep())
				.build();
	}

}
