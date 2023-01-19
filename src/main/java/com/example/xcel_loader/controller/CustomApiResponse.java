package com.example.xcel_loader.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class CustomApiResponse {
    private Object data;
    private String message;
    private int status;
    private String error;
}
