package vn.kltn.service.impl;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class ZipEntryDescriptor {
    private final String zipEntryName; // Tên file trong zip (VD: "Tài liệu - abc.pdf")
    private final CompletableFuture<InputStream> inputStreamFuture;

    public ZipEntryDescriptor(String zipEntryName, CompletableFuture<InputStream> inputStreamFuture) {
        this.zipEntryName = zipEntryName;
        this.inputStreamFuture = inputStreamFuture;
    }

    public String getZipEntryName() {
        return zipEntryName;
    }

    public CompletableFuture<InputStream> getInputStreamFuture() {
        return inputStreamFuture;
    }

}
