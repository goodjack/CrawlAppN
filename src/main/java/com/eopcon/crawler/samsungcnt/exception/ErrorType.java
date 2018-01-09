package com.eopcon.crawler.samsungcnt.exception;

public enum ErrorType {
	
	NETWORK_CONNECTION_FAIL((short) 1, "network_connection_fail"),
    TOO_MANY_REDIRECTS((short) 2, "too_many_redirects"),
    SSL_CONNECTION_FAIL((short) 3, "ssl_connection_fail"),
    SOCKET_CLOSED((short) 4, "socket_closed"),
    BAD_REQUEST((short) 400, "bad_request_error"),
    GONE((short) 410, "gone_error"),
    REQUEST_TIMEOUT((short) 408, "request_timeout_error"),
    ERROR_LOGIN_FAIL((short) 1001, "error_login_fail"),
    ERROR_REQUEST_FAIL((short) 1002, "error_request_fail"),
    ERROR_PARSING_FAIL((short) 1003, "error_parsing_fail"),
    ERROR_EXPIRED_SESSION((short) 1004, "error_expired_session"),
    ERROR_TASK_INTERRUPTED((short) 1005, "error_task_interrupted"),
    ERROR_CATEGORY_MAPPING_FAIL((short) 1006, "error_category_mapping_fail"),
    ERROR_ASSERTION_FAIL((short) 1007, "error_assertion_fail"),
    ERROR_ASSERTION_MATERIALS_FAIL((short) 1008, "error_assertion_materials_fail"),
    ERROR_NOT_APPLICABLE_GOODS((short) 1009, "error_not_applicable_goods"),
    ERROR_DB_WRITE_FAIL((short) 1010, "error_db_write_fail"),
    ERROR_PARSING_MATERIALS_FAIL((short) 1011, "error_parsing_materials_fail"),
    ERROR_SAVE_IMAGE_FAIL((short) 1012, "error_save_image_fail"),
    ERROR_END_OF_SALES((short) 1013, "error_end_of_sales"),
    ERROR_ETC((short) 9999, "error_occurred"),
    NULL((short) -1, null);

    private short errorNumber;
    private String messageCode;

    ErrorType(short errorNumber, String messageCode) {
        this.errorNumber = errorNumber;
        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public short getErrorNumber() {
        return errorNumber;
    }
}
