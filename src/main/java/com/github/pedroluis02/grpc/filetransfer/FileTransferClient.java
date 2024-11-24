package com.github.pedroluis02.grpc.filetransfer;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;

public class FileTransferClient {

    public static void main(String[] args) {
        final var channel = ManagedChannelBuilder.forTarget("localhost:8080")
                .usePlaintext()
                .build();

        final var stub = FileTransferServiceGrpc.newBlockingStub(channel);
        final var request = Empty.newBuilder().build();

        final var response = stub.getInfo(request);
        System.out.println(response);

        channel.shutdownNow();
    }
}
