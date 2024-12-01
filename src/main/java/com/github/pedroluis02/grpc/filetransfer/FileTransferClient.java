package com.github.pedroluis02.grpc.filetransfer;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class FileTransferClient {

    public static void main(String[] args) {
        final var channel = ManagedChannelBuilder.forTarget("localhost:8080")
                .usePlaintext()
                .build();

        transfer(channel, "<FILE_PATH>");
    }

    private static void getInfo(ManagedChannel channel) {
        final var stub = FileTransferServiceGrpc.newBlockingStub(channel);

        final var request = Empty.newBuilder().build();
        final var response = stub.getInfo(request);
        System.out.println(response);

        channel.shutdownNow();
    }

    private static void transfer(ManagedChannel channel, String filePath) {
        final var stub = FileTransferServiceGrpc.newStub(channel);

        final var latch = new CountDownLatch(1);

        final var clientObserver = new StreamObserver<TransferStatus>() {
            @Override
            public void onNext(TransferStatus transferStatus) {
                System.out.println(String.format("transfer file: code=%s msg=%s", transferStatus.getCode(),
                        transferStatus.getMessage()));
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("onError: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
                latch.countDown();
            }
        };

        final var serverObserver = stub.transfer(clientObserver);
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
            System.err.println("error: " + e.getMessage());

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
