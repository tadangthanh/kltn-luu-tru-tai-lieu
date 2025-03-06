package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.service.IAuthenticationService;

import java.io.ByteArrayInputStream;

@Slf4j(topic = "KEY_DOWNLOAD_CONTROLLER")
@RestController
@RequiredArgsConstructor
public class KeyDownloadRest {
    private final IAuthenticationService authenticationService;

    @GetMapping("/download-private-key")
    public ResponseEntity<InputStreamResource> getPrivateKey() {
        // Trả về file để tải xuống
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"private_key.pem\"");
        byte[] privateKey = authenticationService.getPrivateKey();
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(privateKey)));
    }
}
