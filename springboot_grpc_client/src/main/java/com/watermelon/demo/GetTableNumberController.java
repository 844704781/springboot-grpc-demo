package com.watermelon.demo;

import com.watermelon.demo.client.GetTableNumberRpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/test")
public class GetTableNumberController {

    @Resource
    private GetTableNumberRpcClient getTableNumberRpcClient;

    @GetMapping("/test")
    public Integer test() {
        return getTableNumberRpcClient.getTableNumber();
    }
}
