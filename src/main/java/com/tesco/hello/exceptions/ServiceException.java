package com.tesco.hello.exceptions;

/**
	* Created by bruno on 24/04/16.
	*/
public class ServiceException extends RuntimeException {
				public ServiceException() {
								super();
				}

				public ServiceException(String message) {
								super(message);
				}

				public ServiceException(String message, Throwable throwable) {
								super(message, throwable);
				}

				public ServiceException(Throwable throwable) {
								super(throwable);
				}
}
