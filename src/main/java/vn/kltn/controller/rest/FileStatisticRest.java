package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.response.FileStatisticResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFileStatisticService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file-statistic")
public class FileStatisticRest {
    private final IFileStatisticService fileStatisticService;

    @GetMapping("/{fileId}")
    public ResponseData<PageResponse<List<FileStatisticResponse>>> searchFileStatistic(Pageable pageable,
                                                                                       @PathVariable Long fileId) {
        return new ResponseData<>(200, "successfully", fileStatisticService.getAllByFileId(fileId, pageable));
    }
}
