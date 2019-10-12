package com.huiway.activiti.exception;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

public class ValidationError extends BaseError {

	private static final long serialVersionUID = 6369030003831700139L;

	@ApiModelProperty("发生校验错误的字段的列表")
	private List<FieldError> fields = new ArrayList<>();

	@ApiModelProperty("校验错误列表")
	private List<ValidationError> errors = null;

	/**
	 * 构造方法。
	 */
	public ValidationError() {
		super("error.validation", HttpStatus.BAD_REQUEST);
	}

	/**
	 * 构造方法。
	 */
	public ValidationError(String errorMessage) {
		this();
		setMessage(errorMessage);
	}

	/**
	 * 构造方法。
	 */
	public ValidationError(List<ValidationError> errors) {
		this();
		this.errors = errors;
	}

	/**
	 * 构造方法。
	 */
	public ValidationError(MethodArgumentNotValidException exception) {

		this();

		exception.getBindingResult().getAllErrors().forEach(fieldError -> fields.add((FieldError) fieldError));

	}

	public List<FieldError> getFields() {
		return fields;
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

}
