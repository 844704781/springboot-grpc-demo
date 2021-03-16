package com.watermelon.demo.client;

import com.watermelon.grpc.GetTableNumberRequest;
import com.watermelon.grpc.GetTableNumberResponse;
import com.watermelon.grpc.TableNumberServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class GetTableNumberRpcClient {

    @GrpcClient("grpc_server")
    private TableNumberServiceGrpc.TableNumberServiceBlockingStub stub;

    public Integer getTableNumber(){
        GetTableNumberRequest request = GetTableNumberRequest.newBuilder()
                .setCommodityId("112321321312waeffsd")
                .setTableCount(4).build();
        GetTableNumberResponse tableNumber = stub.getTableNumber(request);
        return tableNumber.getTableNumber();
    }

}
