package com.smartvoucher.webEcommercesmartvoucher.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshException extends RuntimeException{
    private int statusCode;
    private String message;
    private Exception exception;
}
