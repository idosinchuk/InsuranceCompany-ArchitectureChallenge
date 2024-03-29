package com.idosinchuk.architecturechallenge.insurancecompany.dto;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Response DTO for product
 * 
 * @author Igor Dosinchuk
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@ApiModel(reference = "ProductResponse", description = "Model response for product.")
public class ProductResponseDTO {

	@Id
	@ApiModelProperty(value = "Id", example = "1")
	private int id;

	@ApiModelProperty(value = "Name of the product", example = "Full of risk")
	private String productName;

	@ApiModelProperty(value = "Code of the product", example = "S6DHD78S")
	private String productCode;

}
