package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.request.PreviewPageSelectionRequest;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IPreviewImageService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/previews")
public class PreviewImageRest {
    private final IPreviewImageService previewImageService;

    @PostMapping
    public ResponseData<String> createPreviewForDoc(@RequestBody PreviewPageSelectionRequest previewPageSelectionRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "thành công",
                previewImageService.createPreviewImages(previewPageSelectionRequest));
    }
}
