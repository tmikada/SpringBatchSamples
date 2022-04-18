package com.example.demo.config;

import java.nio.charset.StandardCharsets;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.example.demo.domain.model.Employee;
import com.example.demo.property.SampleProperty;

@EnableBatchProcessing
public abstract class BaseConfig {
	
	/** JobBuilderのFactoryクラス */
	@Autowired
	protected JobBuilderFactory jobBuilderFactory;
	
	/** StepBuilderのFactoryクラス */
	@Autowired
	protected StepBuilderFactory stepBuilderFactory;

	/** 性別の文字列を数値に変換するProcessor */
	@Autowired
	@Qualifier("GenderConvertProcessor")
	protected ItemProcessor<Employee, Employee> genderConvertProcessor;
	
	@Autowired
	protected ItemReadListener<Employee> readListener;
	
	@Autowired
	protected ItemWriteListener<Employee> writeListener;

	@Autowired
	protected SampleProperty property;
	
	@Autowired
	protected FlatFileHeaderCallback csvHeaderCallback;
	
	@Autowired
	protected FlatFileFooterCallback csvFooterCallback;
	
	/** CSV出力のWriterを生成 */
	@Bean
	@StepScope
	public FlatFileItemWriter<Employee> csvWriter() {
		
		// ファイル出力先設定
		String filePath = property.outputPath();
		Resource outputResource = new FileSystemResource(filePath);
		
		// 区切り文字設定
		DelimitedLineAggregator<Employee> aggregator = new DelimitedLineAggregator<Employee>();
		aggregator.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);
		
		// 出力フィールドの設定
		BeanWrapperFieldExtractor<Employee> extractor = new BeanWrapperFieldExtractor<Employee>();
		extractor.setNames(new String[] {"id","name","age","genderString"});
		aggregator.setFieldExtractor(extractor);
		
		return new FlatFileItemWriterBuilder<Employee>()
				.name("employeeCsvWriter")
				.resource(outputResource) // ファイル出力先
				.append(false) // 追記設定
				.lineAggregator(aggregator) // 区切り文字
				.headerCallback(csvHeaderCallback) // header
				.footerCallback(csvFooterCallback) // footer
				.encoding(StandardCharsets.UTF_8.name()) // 文字コード
				.build();
	}
	
}
