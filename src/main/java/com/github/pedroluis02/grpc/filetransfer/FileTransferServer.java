package com.github.pedroluis02.grpc.filetransfer;

import io.grpc.ServerBuilder;

public class FileTransferServer {

    public static void main(String[] args) throws Exception {
        final var server = ServerBuilder.forPort(8080)
                .addService(new FileTransferServiceImpl())
                .build();

        server.start();
        server.awaitTermination();
    }
}
