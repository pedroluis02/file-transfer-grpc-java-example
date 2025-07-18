package com.github.pedroluis02.grpc.filetransfer.server;

import io.grpc.ServerBuilder;

public class FileTransferServer {

    public static void main(String[] args) throws Exception {
        final var server = ServerBuilder.forPort(9090)
                .addService(new FileTransferServiceImpl())
                .build();

        server.start();
        server.awaitTermination();
    }
}
