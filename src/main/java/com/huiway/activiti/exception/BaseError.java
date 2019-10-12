package com.huiway.activiti.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.Serializable;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.huiway.activiti.common.config.WebConfiguration;

import io.swagger.annotations.ApiModelProperty;

/**
 * 业务异常基类。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseError extends RuntimeException implements Serializable {

	private static final long serialVersionUID = 2177612828588673257L;

	private static final MessageSource MESSAGE_SOURCE = WebConfiguration.messageSource();

	private static final String[] NO_PARAMETER = new String[] {};

	@ApiModelProperty("错误代码")
	private String code;

	@ApiModelProperty("错误消息")
	private String message;

	@ApiModelProperty("HTTP 状态码")
	private int status;

	/**
	 * 构造方法。
	 */
	public BaseError() {
	}

	/**
	 * 构造方法。
	 * 
	 * @param code       错误代码
	 * @param parameters 消息参数
	 */
	public BaseError(String code, String... parameters) {
		this(code, INTERNAL_SERVER_ERROR, parameters);
	}

	/**
	 * 构造方法。
	 * 
	 * @param code       错误代码
	 * @param status     HTTP 状态码
	 * @param parameters 消息参数
	 */
	public BaseError(String code, HttpStatus status, String... parameters) {
		this(code, status.value(), parameters);
	}

	/**
	 * 构造方法。
	 * 
	 * @param code       错误代码
	 * @param status     HTTP 状态码
	 * @param parameters 消息参数
	 */
	public BaseError(String code, int status, String... parameters) {
		this.code = code;
		this.status = status;
		setMessageSource(parameters);
	}

	/**
	 * 构造方法。
	 * 
	 * @param exception  异常
	 * @param parameters 消息参数
	 */
	public BaseError(Exception exception, String... parameters) {

		code = "error";
		status = INTERNAL_SERVER_ERROR.value();

		if (exception instanceof BaseError) {
			BaseError error = (BaseError) exception;
			code = error.getCode();
			status = error.getStatus();
			message = error.getMessage();
		} else if (exception instanceof CannotAcquireLockException) {
			code = "error.db.lock-acquisition";
		} else if (exception != null) {
			message = exception.getMessage();
		}

		setMessageSource(parameters);
	}

	/**
	 * 设置消息源，更新消息内容。
	 * 
	 * @param parameters 消息参数
	 */
	private void setMessageSource(String... parameters) {

		try {

			if (message != null && !message.equals(code)) {
				return;
			}

			Locale locale = LocaleContextHolder.getLocale();

			setMessage(MESSAGE_SOURCE.getMessage(code, setMessageParameters(parameters, locale), locale));

		} catch (NoSuchMessageException e) {
			setMessage(message == null ? code : message);
		}

	}

	/**
	 * 设置消息参数。
	 * 
	 * @param keys   消息名
	 * @param locale 地区
	 * @return 参数列表
	 */
	private String[] setMessageParameters(String[] keys, Locale locale) {

		if (keys == null) {
			return new String[] {};
		}

		String[] parameters = new String[keys.length];

		for (int i = 0; i < keys.length; i++) {
			try {
				parameters[i] = MESSAGE_SOURCE.getMessage(keys[i], NO_PARAMETER, locale);
			} catch (NoSuchMessageException e) {
				parameters[i] = keys[i];
			}
		}

		return parameters;
	}

	/**
	 * 取得错误代码。
	 * 
	 * @return 错误代码
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * 取得 HTTP 状态码。
	 * 
	 * @return HTTP 状态码
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * 取得错误消息。
	 * 
	 * @return 错误消息
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * 设置错误消息。
	 * 
	 * @param message 错误消息
	 */
	@JsonSetter
	protected void setMessage(String message) {
		this.message = message;
	}

}
