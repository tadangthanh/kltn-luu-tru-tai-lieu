package vn.kltn.service;

import vn.kltn.dto.request.PreviewPageSelectionRequest;

public interface IPreviewImageService {
    String createPreviewImages(PreviewPageSelectionRequest request);
}
