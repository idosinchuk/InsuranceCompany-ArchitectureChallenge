package com.idosinchuk.architecturechallenge.insurancecompany.service.impl;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idosinchuk.architecturechallenge.insurancecompany.common.CustomMessage;
import com.idosinchuk.architecturechallenge.insurancecompany.controller.HolderController;
import com.idosinchuk.architecturechallenge.insurancecompany.controller.PolicyController;
import com.idosinchuk.architecturechallenge.insurancecompany.dto.PolicyRequestDTO;
import com.idosinchuk.architecturechallenge.insurancecompany.dto.PolicyResponseDTO;
import com.idosinchuk.architecturechallenge.insurancecompany.entity.HolderEntity;
import com.idosinchuk.architecturechallenge.insurancecompany.entity.PolicyEntity;
import com.idosinchuk.architecturechallenge.insurancecompany.entity.ProductEntity;
import com.idosinchuk.architecturechallenge.insurancecompany.entity.VehicleEntity;
import com.idosinchuk.architecturechallenge.insurancecompany.repository.HolderRepository;
import com.idosinchuk.architecturechallenge.insurancecompany.repository.PolicyRepository;
import com.idosinchuk.architecturechallenge.insurancecompany.repository.ProductRepository;
import com.idosinchuk.architecturechallenge.insurancecompany.repository.VehicleRepository;
import com.idosinchuk.architecturechallenge.insurancecompany.service.PolicyService;
import com.idosinchuk.architecturechallenge.insurancecompany.util.ArrayListCustomMessage;
import com.idosinchuk.architecturechallenge.insurancecompany.util.CustomErrorType;

/**
 * Implementation for policy service
 * 
 * @author Igor Dosinchuk
 *
 */
@Service("PolicyService")
public class PolicyServiceImpl implements PolicyService {

	@Autowired
	private PolicyRepository policyRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private VehicleRepository vehicleRepository;

	@Autowired
	private HolderRepository holderRepository;

	@Autowired
	private ModelMapper modelMapper;

	public static final Logger logger = LoggerFactory.getLogger(PolicyServiceImpl.class);

	/**
	 * {@inheritDoc}
	 */
	public Page<PolicyResponseDTO> getAllPolicies(Pageable pageable) {

		Page<PolicyEntity> entityResponse = policyRepository.findAll(pageable);

		// Convert Entity response to DTO
		return modelMapper.map(entityResponse, new TypeToken<Page<PolicyResponseDTO>>() {
		}.getType());

	}

	/**
	 * {@inheritDoc}
	 */
	public PolicyResponseDTO getPolicies(String policyCode) {

		PolicyEntity entityResponse = policyRepository.findByPolicyCode(policyCode);

		return modelMapper.map(entityResponse, PolicyResponseDTO.class);

	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public ResponseEntity<?> addPolicy(PolicyRequestDTO policyRequestDTO) {

		Resources<CustomMessage> resource = null;

		try {
			List<CustomMessage> customMessageList = null;

			PolicyEntity entityRequest = modelMapper.map(policyRequestDTO, PolicyEntity.class);

			PolicyEntity policyEntity = policyRepository.findByPolicyCode(policyRequestDTO.getPolicyCode());

			// Check if policyCode exists in the database
			if (policyEntity != null) {
				customMessageList = ArrayListCustomMessage.setMessage(
						"The requested policy actually exists. Please change policyCode.", HttpStatus.BAD_REQUEST);
				resource = new Resources<>(customMessageList);
				resource.add(linkTo(PolicyController.class).withSelfRel());

				return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
			}

			// Check if product exists in the database
			ProductEntity productEntity = productRepository.findByProductCode(policyRequestDTO.getProductCode());

			// Check if holder exists in the database
			HolderEntity holderEntity = holderRepository.findByPassportNumber(policyRequestDTO.getPassportNumber());

			// Check if vehicle exists in the database
			VehicleEntity vehicleEntity = vehicleRepository.findByLicensePlate(policyRequestDTO.getLicensePlate());

			if (productEntity != null && holderEntity != null && vehicleEntity != null) {
				entityRequest.setProduct(productEntity);
				entityRequest.setHolder(holderEntity);
				entityRequest.setVehicle(vehicleEntity);
			} else {
				customMessageList = ArrayListCustomMessage.setMessage("Some of the requested data are not correct",
						HttpStatus.BAD_REQUEST);
				resource = new Resources<>(customMessageList);
				resource.add(linkTo(PolicyController.class).withSelfRel());
				return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
			}

			policyRepository.save(entityRequest);

			customMessageList = ArrayListCustomMessage.setMessage("Created new policy", HttpStatus.CREATED);

			resource = new Resources<>(customMessageList);
			resource.add(linkTo(PolicyController.class).withSelfRel());
		} catch (Exception e) {
			logger.error("An error occurred! {}", e.getMessage());
			return CustomErrorType.returnResponsEntityError(e.getMessage());
		}

		return new ResponseEntity<>(resource, HttpStatus.OK);

	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public ResponseEntity<?> updatePolicy(String policyCode, PolicyRequestDTO policyRequestDTO) {

		Resources<CustomMessage> resource = null;

		try {

			List<CustomMessage> customMessageList = null;

			customMessageList = ArrayListCustomMessage.setMessage("Patch policy process", HttpStatus.OK);

			// Find policy by policy code for check if exists in DB
			PolicyEntity policyEntity = policyRepository.findByPolicyCode(policyCode);

			// If exists
			if (policyEntity != null) {

				// The policy's code and ID will always be the same, so we do not allow it to be
				// updated, for them we overwrite the field with the original value.
				policyRequestDTO.setPolicyCode(policyCode);
				policyRequestDTO.setId(policyEntity.getId());

				PolicyEntity entityRequest = modelMapper.map(policyRequestDTO, PolicyEntity.class);

				if (policyRequestDTO.getProductCode() != null && !policyRequestDTO.getProductCode().isEmpty()) {

					ProductEntity productEntity = productRepository
							.findByProductCode(policyRequestDTO.getProductCode());

					// Check if product exists in the database
					if (productEntity != null) {
						entityRequest.setProduct(productEntity);
					} else {
						customMessageList = ArrayListCustomMessage.setMessage(
								"Product code " + policyRequestDTO.getProductCode() + " does not exist!",
								HttpStatus.BAD_REQUEST);
						resource = new Resources<>(customMessageList);
						resource.add(linkTo(PolicyController.class).withSelfRel());
						return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
					}
				}

				if (policyRequestDTO.getPassportNumber() != null && !policyRequestDTO.getProductCode().isEmpty()) {
					HolderEntity holderEntity = holderRepository
							.findByPassportNumber(policyRequestDTO.getPassportNumber());

					// Check if holder exists in the database
					if (holderEntity != null) {
						entityRequest.setHolder(holderEntity);
					} else {
						customMessageList = ArrayListCustomMessage.setMessage(
								"Holder passport number " + policyRequestDTO.getPassportNumber() + " does not exist!",
								HttpStatus.BAD_REQUEST);
						resource = new Resources<>(customMessageList);
						resource.add(linkTo(PolicyController.class).withSelfRel());
						return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
					}
				}

				if (policyRequestDTO.getLicensePlate() != null && !policyRequestDTO.getProductCode().isEmpty()) {
					VehicleEntity vehicleEntity = vehicleRepository
							.findByLicensePlate(policyRequestDTO.getLicensePlate());

					// Check if vehicle exists in the database
					if (vehicleEntity != null) {
						entityRequest.setVehicle(vehicleEntity);
					} else {
						customMessageList = ArrayListCustomMessage.setMessage(
								"Vehicle license plate " + policyRequestDTO.getLicensePlate() + " does not exist!",
								HttpStatus.BAD_REQUEST);
						resource = new Resources<>(customMessageList);
						resource.add(linkTo(PolicyController.class).withSelfRel());
						return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
					}
				}

				// Check if there are changes
				if (!policyEntity.equals(entityRequest)) {
					policyRepository.save(entityRequest);
				} else {
					customMessageList = ArrayListCustomMessage.setMessage("There are no changes, please try again",
							HttpStatus.BAD_REQUEST);

					resource = new Resources<>(customMessageList);
					resource.add(linkTo(HolderController.class).withSelfRel());

					return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
				}

			} else {
				customMessageList = ArrayListCustomMessage.setMessage(
						"Policy code" + policyRequestDTO.getPolicyCode() + " Not Found!", HttpStatus.BAD_REQUEST);

				resource = new Resources<>(customMessageList);
				resource.add(linkTo(PolicyController.class).withSelfRel());

				return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
			}

			resource = new Resources<>(customMessageList);
			resource.add(linkTo(PolicyController.class).slash(policyRequestDTO.getPolicyCode()).withSelfRel());
		} catch (Exception e) {
			logger.error("An error occurred! {}", e.getMessage());
			return CustomErrorType.returnResponsEntityError(e.getMessage());

		}

		return new ResponseEntity<>(resource, HttpStatus.OK);

	}
}
