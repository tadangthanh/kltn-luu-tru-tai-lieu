package vn.kltn.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.exception.InternalServerErrorException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j(topic = "ZIP_SERVICE")
@Service
public class ZipService {
    public void streamZipToOutput(List<ZipEntryDescriptor> entries, OutputStream outputStream) {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            List<CompletableFuture<Void>> tasks = entries.stream().map(entry -> CompletableFuture.runAsync(() -> {
                try (InputStream is = entry.getInputStreamFuture().join()) {
                    // ZipOutputStream không thread-safe, nên bắt buộc phải đồng bộ để chỉ một thread ghi vào một thời điểm.
                    synchronized (zos) {
                        zos.putNextEntry(new ZipEntry(entry.getZipEntryName()));

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        //Đóng entry hiện tại trong ZIP để bắt đầu file tiếp theo.
                        zos.closeEntry();
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi ghi file vào ZIP: {}", entry.getZipEntryName(), e);
                }
            })).toList();
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
            zos.finish();
        } catch (Exception e) {
            log.error("Lỗi khi tạo ZIP", e);
            throw new InternalServerErrorException("Tạo ZIP thất bại");
        }
    }
}
