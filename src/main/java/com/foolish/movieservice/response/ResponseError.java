package com.foolish.movieservice.response;

public class ResponseError extends ResponseData {
  private int status;
  private String message;

  public ResponseError(int value, String message) {
    super(value, message);
  }
}
