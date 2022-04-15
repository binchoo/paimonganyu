package org.binchoo.paimonganyu.hoyoapi.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HoyoResponse<T> {

    private int retcode;
    private String message;
    private T data;
}
