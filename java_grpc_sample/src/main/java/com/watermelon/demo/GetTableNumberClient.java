package com.watermelon.demo;

import a.b.c.GetTableNumberRequest;
import a.b.c.GetTableNumberResponse;
import a.b.c.TableNumberServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GetTableNumberClient {
    /**
     * 桩，rpc通过桩连接客户端与服务端，
     * 客户端存根(Client Stub)，存放服务端的地址消息，再将客户端的请求参数打包成网络消息，然后通过网络远程发送给服务方。
     * 服务端存根(Server Stub)，接收客户端发送过来的消息，将消息解包，并调用本地的方法。
     */
    private TableNumberServiceGrpc.TableNumberServiceBlockingStub stub;

    /**
     * 通道
     */
    private ManagedChannel channel;

    public GetTableNumberClient() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9898)
                .usePlaintext()
                .build();

        stub = TableNumberServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        String commodity="123213123cefafewafewafwe";
        int count=64;
        GetTableNumberClient client = new GetTableNumberClient();
        GetTableNumberResponse tableNumber = client.stub.getTableNumber(GetTableNumberRequest.newBuilder().setCommodityId(commodity)
                .setTableCount(count).build());
        System.out.println(tableNumber.getTableNumber());
    }
}
