package com.tesco.disco.browse.exceptions;

/**
	* Created by bruno on 24/04/16.
	*/
public class ClientException extends RuntimeException {
				public ClientException() {
								super();
				}

				public ClientException(String message) {
								super(message);
				}

				public ClientException(String message, Throwable throwable) {
								super(message, throwable);
				}

				public ClientException(Throwable throwable) {
								super(throwable);
				}
}
