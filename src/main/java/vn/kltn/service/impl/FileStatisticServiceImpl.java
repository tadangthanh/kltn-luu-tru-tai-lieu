package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.FileStatisticResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.File;
import vn.kltn.entity.FileStatistic;
import vn.kltn.exception.DuplicateResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FileStatisticMapper;
import vn.kltn.repository.FileStatisticRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IFileStatisticService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "FILE_STATISTIC_SERVICE")
public class FileStatisticServiceImpl implements IFileStatisticService {
    private final FileStatisticMapper fileStatisticMapper;
    private final FileStatisticRepo fileStatisticRepo;

    @Override
    public void increaseViewCount(Long fileId) {
        FileStatistic fileStatistic = findByFileIdOrThrow(fileId);
        fileStatistic.setViewCount(fileStatistic.getViewCount() + 1);
        fileStatisticRepo.save(fileStatistic);
    }

    private FileStatistic findByFileIdOrThrow(Long fileId) {
        return fileStatisticRepo.findByFileId(fileId).orElseThrow(() -> {
            log.warn("Không tìm thấy file statistic");
            return new ResourceNotFoundException("Không tìm thấy file statistic");
        });
    }

    @Override
    public void increaseDownloadCount(Long fileId) {
        FileStatistic fileStatistic = findByFileIdOrThrow(fileId);
        fileStatistic.setDownloadCount(fileStatistic.getDownloadCount() + 1);
        fileStatisticRepo.save(fileStatistic);
    }

    @Override
    public void increaseShareCount(Long fileId) {
        FileStatistic fileStatistic = findByFileIdOrThrow(fileId);
        fileStatistic.setShareCount(fileStatistic.getShareCount() + 1);
        fileStatisticRepo.save(fileStatistic);
    }

    @Override
    public void createFileStatistic(File file) {
        validateFileNotExist(file.getId());
        createFileStatisticFromFile(file);
    }

    private void validateFileNotExist(Long fileId) {
        if (fileStatisticRepo.existsByFileId(fileId)) {
            log.warn("Đã tồn tại file statistic");
            throw new DuplicateResourceException("Đã tồn tại file statistic");
        }
    }

    private void createFileStatisticFromFile(File file) {
        FileStatistic fileStatistic = new FileStatistic();
        fileStatistic.setFile(file);
        fileStatisticRepo.save(fileStatistic);
    }

    @Override
    public void deleteFileStatisticByFileId(Long fileId) {
        fileStatisticRepo.deleteByFileId(fileId);
    }

    @Override
    public PageResponse<List<FileStatisticResponse>> getAllByFileId(Long fileId, Pageable pageable) {
        log.info("get all file statistic by file id: {}", fileId);
        Page<FileStatistic> filePage = fileStatisticRepo.findAllByFileId(fileId, pageable);
        return PaginationUtils.convertToPageResponse(filePage, pageable, this::toFileStatisticResponse);
    }

    private FileStatisticResponse toFileStatisticResponse(FileStatistic fileStatistic) {
        return fileStatisticMapper.toFileStatisticResponse(fileStatistic);
    }
}
