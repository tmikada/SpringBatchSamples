package com.example.demo.config.jdbc;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import com.example.demo.config.BaseConfig;
import com.example.demo.domain.model.Employee;

@Configuration
public class JdbcCursorBatchConfig extends BaseConfig {

	/** DataSource(JDBCで必須) */
	@Autowired
	private DataSource dataSource;

	/** SELECT用のSQL(JDBC用) */
	private static final String SELECT_EMPLOYEE_SQL = 
			"SELECT * FROM employee where gender = ?";

	/** JdbcCursorItemReader */
	@Bean
	@StepScope
	public JdbcCursorItemReader<Employee> jdbcCursorReader() {
		
		// クエリーに渡すパラメーター
		Object[] params = new Object[] {1}; // 性別：男性
		
		// RowMapper
		RowMapper<Employee> rowMapper = new BeanPropertyRowMapper<>(Employee.class);
		
		return new JdbcCursorItemReaderBuilder<Employee>()
				.dataSource(this.dataSource)
				.name("jdbcCursorItemReader")
				.sql(SELECT_EMPLOYEE_SQL)
				.queryArguments(params)
				.rowMapper(rowMapper)
				.build();
		
	}
	
	/** Stepの生成(JDBC) */
	@Bean
	public Step exportJdbcCursorStep() {
		return this.stepBuilderFactory.get("ExportJdbcCursorStep")
				.<Employee,Employee>chunk(10)
				.reader(jdbcCursorReader()).listener(this.readListener)
				.processor(this.genderConvertProcessor)
				.writer(csvWriter()).listener(this.writeListener)
				.build();
	}

	/** Jobの生成(JDBC) */
	@Bean("JdbcCursorJob")
	public Job exportJdbcCursorJob() {
		return this.jobBuilderFactory.get("ExportJdbcCursorJob")
				.incrementer(new RunIdIncrementer())
				.start(exportJdbcCursorStep())
				.build();
	}
}
