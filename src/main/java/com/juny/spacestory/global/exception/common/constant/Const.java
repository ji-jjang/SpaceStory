package com.juny.spacestory.global.exception.common.constant;

public class Const {

  public static final String SERVER_ERROR_CODE = "E1";
  public static final String SERVER_ERROR_MSG = "Interner server error occured.";

  public static final String BAD_REQUEST_CODE = "E2";
  public static final String BAD_REQUEST_MSG = "Invalid request format.";

  public static final String UNAUTHORIZED_CODE = "E3";
  public static final String UNAUTHORIZED_MSG = "Unauthorized access.";

  public static final String PARAMETER_NULL_OR_EMPTY_CODE = "P1";
  public static final String PARAMETER_NULL_OR_EMPTY_MSG = "Parameter is null or empty.";

  public static final String RESERVATION_INVALID_ID_CODE = "R1";
  public static final String RESERVATION_INVALID_ID_MSG = "Reservation requested ID is invalid.";

  public static final String RESERVATION_MINIMUM_TIME_CODE = "R2";
  public static final String RESERVATION_MINIMUM_TIME_MSG =
      "Minimum booking duration must be at least 1 hour.";

  public static final String RESERVATION_OVERLAPPED_TIME_CODE = "R3";
  public static final String RESERVATION_OVERLAPPED_TIME_MSG = "There is a scheduling conflict.";

  public static final String SPACE_INVALID_ID_CODE = "S1";
  public static final String SPACE_INVALID_ID_MSG = "Space requested ID is invalid.";

  public static final String SPACE_INVALID_DETAILED_TYPE_CODE = "S2";
  public static final String SPACE_INVALID_DETAILED_TYPE_MSG = "The space detailed type is invalid";

  public static final String HOST_INVALID_ID_CODE = "H1";
  public static final String HOST_INVALID_ID_MSG = "Host requested is invalid.";

  public static final String REVIEW_INVALID_ID_CODE = "REV1";
  public static final String REVIEW_INVALID_ID_MSG = "Review requested ID is invalid.";

  public static final String USER_INVALID_ID_CODE = "U1";
  public static final String USER_INVALID_ID_MSG = "User requested is invalid.";

  public static final String USER_NOT_ENOUGH_POINTS_CODE = "U2";
  public static final String USER_NOT_ENOUGH_POINTS_MSG = "The User's point exceeded limit.";

  public static final String USER_UNAUTHORIZED_MODIFY_CODE = "U3";
  public static final String USER_UNAUTHORIZED_MODIFY_MSG = "Unauthorized modification.";

  public static final String USER_DUPLICATED_EMAIL_CODE = "U4";
  public static final String USER_DUPLICATED_EMAIL_MSG = "User email is duplicated.";

  public static final String USER_NOT_MATCH_PASSWORD_CODE = "U5";
  public static final String USER_NOT_MATCH_PASSWORD_MSG = "User password is different.";

  public static final String USER_PASSWORD_IS_SHORT_CODE = "U6";
  public static final String USER_PASSWORD_IS_SHORT_MSG = "User password is short.";

  public static final String USER_EMAIL_INVALID_CODE = "U7";
  public static final String USER_EMAIL_INVALID_MSG = "User email is invalid.";

  public static final String USER_PHONE_NUMBER_INVALID_CODE = "U7";
  public static final String USER_PHONE_NUMBER_INVALID_MSG = "User phoneNumber is invalid.";

  public static final String ACCESS_TOKEN_EXPIRED_CODE = "AT1";
  public static final String ACCESS_TOKEN_EXPIRED_MSG = "Access token is expired.";

  public static final String ACCESS_TOKEN_INVALID_CODE = "AT2";
  public static final String ACCESS_TOKEN_INVALID_MSG = "Access token is invalid.";

  public static final String REFRESH_TOKEN_INVALID_CODE = "RT1";
  public static final String REFRESH_TOKEN_INVALID_MSG = "Refresh token is invalid.";

  public static final String EMAIL_CODE_INVALID_CODE = "EM1";
  public static final String EMAIL_CODE_INVALID_MSG = "Email code is invalid.";

  public static final String EMAIL_CODE_IS_EXPIRED_CODE = "EM2";
  public static final String EMAIL_CODE_IS_EXPIRED_MSG = "Email code is expired.";

  public static final String TOTP_IS_NOT_ACTIVATED_CODE = "TO1";
  public static final String TOTP_IS_NOT_ACTIVATED_MSG = "Totp is not activated.";
  public static final String TOTP_CODE_IS_INVALID_CODE = "TO2";
  public static final String TOTP_CODE_IS_INVALID_MSG = "Totp code is invalid.";

  private Const() {}
}
