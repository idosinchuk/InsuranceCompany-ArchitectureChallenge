package com.idosinchuk.insurancecompany;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@SpringBootApplication
public class InsuranceCompanyApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsuranceCompanyApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any())
				.paths(paths()).build();
	}

	// Describe your apis
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Swagger Filmproducer")
				.description("This page lists all the rest apis for Swagger Filmproducer.").version("1.0-SNAPSHOT")
				.build();
	}

	// Only select apis that matches the given Predicates.
	private Predicate<String> paths() {
		// Match all paths except /error
		return Predicates.and(PathSelectors.regex("/.*"), Predicates.not(PathSelectors.regex("/error.*")));
	}

}