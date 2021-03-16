package com.watermelon.demo;

import a.b.c.GetTableNumberRequest;
import a.b.c.GetTableNumberResponse;
import a.b.c.TableNumberServiceGrpc;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GetTableNumberServer extends TableNumberServiceGrpc.TableNumberServiceImplBase {
    @Override
    public void getTableNumber(GetTableNumberRequest request, StreamObserver<GetTableNumberResponse> responseObserver) {
        int count = getTableNumber(request.getCommodityId(),request.getTableCount());
        responseObserver.onNext(GetTableNumberResponse.newBuilder().setTableNumber(count).build());
        responseObserver.onCompleted();
    }

    public int getTableNumber(String commodityId, int count) {
        System.out.println(String.format("GetTableNumberServer getTableNumber(%s,%s)", commodityId, count));
        return (commodityId.hashCode() & Integer.MAX_VALUE) % count;
    }


    public static void main(String[] args) throws IOException {
        ServerBuilder.forPort(9999)
                .addService(new GetTableNumberServer())
                .build()
                .start();

        while (true){

        }

    }
}
