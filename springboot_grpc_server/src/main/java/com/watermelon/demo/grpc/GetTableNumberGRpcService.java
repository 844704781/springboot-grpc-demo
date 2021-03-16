package com.watermelon.demo.grpc;

import com.watermelon.grpc.GetTableNumberRequest;
import com.watermelon.grpc.GetTableNumberResponse;
import com.watermelon.grpc.TableNumberServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GetTableNumberGRpcService extends TableNumberServiceGrpc.TableNumberServiceImplBase {
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
}
