package com.proper.data.enums;

/**
 * Created by Lebel on 26/02/2015.
 */
public enum HttpResponseCodes {
    WIFI_ERROR_CUSTOMIZED(0),
    ACCEPTED(202),
    ALREADY_REPORTED(208),
    BAD_GATEWAY(502),
    BAD_REQUEST(400),
    CONFLICT(409),
    CONTINUE(100),
    CREATED(201),
    DESTINATION_LOCKED(421),
    EXPECTATION_FAILED(417),
    FAILED_DEPENDENCY(424),
    FORBIDDEN(403),
    FOUND(302),
    GATEWAY_TIMEOUT(504),
    GONE(10),
    HTTP_VERSION_NOT_SUPPORTED(505),
    IM_USED(226),
    INSUFFICIENT_SPACE_ON_RESOURCE(419),
    INSUFFICIENT_STORAGE(507),
    INTERNAL_SERVER_ERROR(500),
    LENGTH_REQUIRED(411),
    LOCKED(423),
    LOOP_DETECTED(508),
    METHOD_FAILURE(420),
    METHOD_NOT_ALLOWED(405),
    MOVED_PERMANENTLY(301),
    MOVED_TEMPORARILY(302),
    MULTI_STATUS(207),
    MULTIPLE_CHOICES(300),
    NO_CONTENT(204),
    NON_AUTHORITATIVE_INFORMATION(203),
    NOT_ACCEPTABLE(406),
    NOT_EXTENDED(510),
    NOT_FOUND(404),
    NOT_IMPLEMENTED(501),
    NOT_MODIFIED(304),
    OK(200),
    PARTIAL_CONTENT(206),
    PAYMENT_REQUIRED(402),
    PRECONDITION_FAILED(412),
    PROCESSING(102),
    PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_ENTITY_TOO_LARGE(413),
    REQUEST_TIMEOUT(408),
    REQUEST_URI_TOO_LONG(414),
    REQUESTED_RANGE_NOT_SATISFIABLE(416),
    RESET_CONTENT(205),
    SEE_OTHER(303),
    SERVICE_UNAVAILABLE(503),
    SWITCHING_PROTOCOLS(101),
    TEMPORARY_REDIRECT(307),
    UNAUTHORIZED(401),
    UNPROCESSABLE_ENTITY(422),
    UNSUPPORTED_MEDIA_TYPE(415),
    UPGRADE_REQUIRED(426),
    USE_PROXY(305),
    VARIANT_ALSO_NEGOTIATES(506);
    private int value;

    private HttpResponseCodes(int value) {
        this.value = value;
    }

    public static HttpResponseCodes findCode(int value){
        HttpResponseCodes code = null;
        for(HttpResponseCodes v : values()){
            if( v.value == value){
                code = v;
            }
        }
        return code;
    }

    @Override
    public String toString() {
        String ret = "";
        switch (this) {
            case WIFI_ERROR_CUSTOMIZED:
                ret = "Wi-Fi Error";
                break;
            case ACCEPTED:
                ret = "Accepted";
                break;
            case ALREADY_REPORTED:
                ret = "Already Reported";
                break;
            case BAD_GATEWAY:
                ret = "Bad Gateway";
                break;
            case BAD_REQUEST:
                ret = "Bad Request";
                break;
            case CONFLICT:
                ret = "Conflict";
                break;
            case CONTINUE:
                ret = "Continue";
                break;
            case CREATED:
                ret = "Created";
                break;
            case DESTINATION_LOCKED:
                ret = "Destination Locked";
                break;
            case EXPECTATION_FAILED:
                ret = "Expectation Failed";
                break;
            case FAILED_DEPENDENCY:
                ret = "Failed Dependency";
                break;
            case FORBIDDEN:
                ret = "Forbidden";
                break;
            case FOUND:
                ret = "Found";
                break;
            case GATEWAY_TIMEOUT:
                ret = "Gateway Timeout";
                break;
            case GONE:
                ret = "Gone";
                break;
            case HTTP_VERSION_NOT_SUPPORTED:
                ret = "HTTP Version Not Supported";
                break;
            case IM_USED:
                ret = "IM Used";
                break;
            case INSUFFICIENT_SPACE_ON_RESOURCE:
                ret = "Insufficient Space on Resource";
                break;
            case INSUFFICIENT_STORAGE:
                ret = "Insufficient Storage";
                break;
            case INTERNAL_SERVER_ERROR:
                ret = "Internal Server Error";
                break;
            case LENGTH_REQUIRED:
                ret = "Length Required.";
                break;
            case LOCKED:
                ret = "Locked";
                break;
            case LOOP_DETECTED:
                ret = "Loop Detected";
                break;
            case METHOD_FAILURE:
                ret = "Method Failure";
                break;
            case METHOD_NOT_ALLOWED:
                ret = "Method Not Allowed";
                break;
            case MOVED_PERMANENTLY:
                ret = "Moved Permanently";
                break;
            case MOVED_TEMPORARILY:
                ret = "Moved Temporarily";
                break;
            case MULTI_STATUS:
                ret = "Multi-Status";
                break;
            case MULTIPLE_CHOICES:
                ret = "Multiple Choices";
                break;
            case NO_CONTENT:
                ret = "No Content";
                break;
            case NON_AUTHORITATIVE_INFORMATION:
                ret = "Non-Authoritative Information";
                break;
            case NOT_ACCEPTABLE:
                ret = "Not Acceptable";
                break;
            case NOT_EXTENDED:
                ret = "Not Extended";
                break;
            case NOT_FOUND:
                ret = "Not Found";
                break;
            case NOT_IMPLEMENTED:
                ret = "Not Implemented";
                break;
            case NOT_MODIFIED:
                ret = "Not Modified";
                break;
            case OK:
                ret = "OK";
                break;
            case PARTIAL_CONTENT:
                ret = "Partial Content";
                break;
            case PAYMENT_REQUIRED:
                ret = "Payment Required";
                break;
            case PRECONDITION_FAILED:
                ret = "Precondition failed";
                break;
            case PROCESSING:
                ret = "Processing";
                break;
            case PROXY_AUTHENTICATION_REQUIRED:
                ret = "Proxy Authentication Required";
                break;
            case REQUEST_ENTITY_TOO_LARGE:
                ret = "Request Entity Too Large";
                break;
            case REQUEST_TIMEOUT:
                ret = "Request Timeout";
                break;
            case REQUEST_URI_TOO_LONG:
                ret = "Request-URI Too Long";
                break;
            case REQUESTED_RANGE_NOT_SATISFIABLE:
                ret = "Requested Range Not Satisfiable.";
                break;
            case RESET_CONTENT:
                ret = "Reset Content";
                break;
            case SEE_OTHER:
                ret = "See Other";
                break;
            case SERVICE_UNAVAILABLE:
                ret = "Service Unavailable";
                break;
            case SWITCHING_PROTOCOLS:
                ret = "Switching Protocols";
                break;
            case TEMPORARY_REDIRECT:
                ret = "Temporary Redirect";
                break;
            case UNAUTHORIZED:
                ret = "Unauthorized";
                break;
            case UNPROCESSABLE_ENTITY:
                ret = "Unprocessable Entity";
                break;
            case UNSUPPORTED_MEDIA_TYPE:
                ret = "Unsupported Media Type";
                break;
            case UPGRADE_REQUIRED:
                ret = "Upgrade Required";
                break;
            case USE_PROXY:
                ret = "Use Proxy.";
                break;
            case VARIANT_ALSO_NEGOTIATES:
                ret = "Variant Also Negotiates";
                break;
        }
        return ret;
    }
}
