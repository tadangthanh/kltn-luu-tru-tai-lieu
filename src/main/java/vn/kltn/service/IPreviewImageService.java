package vn.kltn.service;

import vn.kltn.dto.request.PreviewPageSelectionRequest;
import vn.kltn.dto.response.PreviewImageResponse;

import java.util.List;

public interface IPreviewImageService {

    String createPreviewImages(PreviewPageSelectionRequest request);
}
