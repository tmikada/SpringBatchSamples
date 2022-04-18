package com.example.demo.config.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import com.example.demo.config.BaseConfig;
import com.example.demo.domain.model.Employee;

@Configuration
public class JdbcPagingBatchConfig extends BaseConfig {

	/** DataSource(JDBCで必須) */
	@Autowired
	private DataSource dataSource;

	/** Pagingのクエリー設定(JDBC用) */
	@Bean
	public SqlPagingQueryProviderFactoryBean queryProvider() {
		//SQL
		SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
		
		provider.setDataSource(this.dataSource);
		provider.setSelectClause("SELECT id, name, age, gender");
		provider.setFromClause("FROM employee");
		provider.setWhereClause("WHERE gender = :genderParam");
		provider.setSortKey("id");
		
		return provider;
	}

	/** JdbcPagingItemReader 
	 * @throws Exception */
	@Bean
	@StepScope
	public JdbcPagingItemReader<Employee> jdbcPagingReader() throws Exception {
		
		// クエリーに渡すパラメーター
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("genderParam", 1); // 性別：男性;
		
		// RowMapper
		RowMapper<Employee> rowMapper = new BeanPropertyRowMapper<>(Employee.class);
		
		return new JdbcPagingItemReaderBuilder<Employee>()
				.dataSource(this.dataSource)
				.name("jdbcPagingtemReader")
				.queryProvider(queryProvider().getObject())
				.parameterValues(parameterValues)
				.rowMapper(rowMapper)
				.pageSize(5)
				.build();
		
	}
	
	/** Stepの生成(JDBC) 
	 * @throws Exception */
	@Bean
	public Step exportJdbcPagingStep() throws Exception {
		return this.stepBuilderFactory.get("ExportJdbcPagingStep")
				.<Employee,Employee>chunk(10)
				.reader(jdbcPagingReader()).listener(this.readListener)
				.processor(this.genderConvertProcessor)
				.writer(csvWriter()).listener(this.writeListener)
				.build();
	}

	/** Jobの生成(JDBC) 
	 * @throws Exception */
	@Bean("JdbcPagingJob")
	public Job exportJdbcPagingJob() throws Exception {
		return this.jobBuilderFactory.get("ExportJdbcPagingJob")
				.incrementer(new RunIdIncrementer())
				.start(exportJdbcPagingStep())
				.build();
	}
}
