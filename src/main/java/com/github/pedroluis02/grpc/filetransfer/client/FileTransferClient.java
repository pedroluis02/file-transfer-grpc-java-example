package com.github.pedroluis02.grpc.filetransfer.client;

import com.github.pedroluis02.grpc.filetransfer.FileTransferServiceGrpc;
import com.github.pedroluis02.grpc.filetransfer.TransferFile;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransferClient {

    private static final Logger logger = Logger.getLogger(FileTransferClient.class.getName());

    public static void main(String[] args) {
        final var channel = ManagedChannelBuilder.forTarget("localhost:9090")
                .usePlaintext()
                .build();

        getInfo(channel);
        transfer(channel, "<FILE_PATH>");
    }

    private static void getInfo(ManagedChannel channel) {
        final var stub = FileTransferServiceGrpc.newBlockingStub(channel);

        final var request = Empty.newBuilder().build();
        final var response = stub.getInfo(request);
        logger.log(Level.INFO, "{0}", response);
    }

    private static void transfer(ManagedChannel channel, String filePath) {
        final var stub = FileTransferServiceGrpc.newStub(channel);

        final var latch = new CountDownLatch(1);

        final var serverObserver = stub.transfer(new ClientTransferStreamObserverImpl(latch));
        try (final var inputStream = Files.newInputStream(Path.of(filePath))) {
            var bytes = new byte[1024];
            int size;
            while ((size = inputStream.read(bytes)) > 0) {
                final var request = TransferFile.newBuilder()
                        .setContent(ByteString.copyFrom(bytes, 0, size))
                        .build();
                serverObserver.onNext(request);
            }
            serverObserver.onCompleted();
            latch.await();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
