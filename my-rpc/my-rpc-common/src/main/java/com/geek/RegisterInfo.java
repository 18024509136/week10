package com.geek;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterInfo {

    private String ip;

    private int port;

    private String group;

    private double version;
}
