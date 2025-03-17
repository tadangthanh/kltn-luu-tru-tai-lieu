package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.common.FileActionType;
import vn.kltn.dto.response.FileActivityResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.File;
import vn.kltn.entity.FileActivity;
import vn.kltn.entity.User;
import vn.kltn.map.FileActivityMapper;
import vn.kltn.repository.FileActivityRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IFileActivityService;
import vn.kltn.service.IFileService;
import vn.kltn.validation.RequireRepoMemberActive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FILE_ACTIVITY_SERVICE")
public class FileActivityServiceImpl implements IFileActivityService {
    private final FileActivityRepo fileActivityRepo;
    private final IAuthenticationService authService;
    private final IFileService fileService;
    private final FileActivityMapper fileActivityMapper;

    @Override
    public void logActivity(Long fileId, FileActionType action, String detail) {
        log.info("Log activity for repoId: {}, action: {}, detail: {}", fileId, action, detail);
        File file = fileService.getFileById(fileId);
        saveActivity(file, action, detail);
    }

    private void saveActivity(File file, FileActionType action, String detail) {
        FileActivity activity = new FileActivity();
        User authUser = authService.getAuthUser();
        activity.setFile(file);
        activity.setUser(authUser);
        activity.setAction(action);
        activity.setDetails(detail);
        fileActivityRepo.save(activity);
    }

    @Override
    public void deleteActivitiesByFileId(Long fileId) {
        log.info("Delete activities by repoId: {}", fileId);
        fileActivityRepo.deleteByFileId(fileId);
    }

    @Override
    @RequireRepoMemberActive
    public PageResponse<List<FileActivityResponse>> advanceSearchBySpecification(Long fileId, Pageable pageable, String[] activities) {
        log.info("request search activity with specification");
        if (activities != null && activities.length > 0) {
            EntitySpecificationsBuilder<FileActivity> builder = new EntitySpecificationsBuilder<>();
//            Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            //patten chia ra thành 5 nhóm
            // nhóm 1: từ cần tìm kiếm (có thể là tên cột hoặc tên bảng) , ví dụ: name, age, subTopic.id=> subTopic là tên bảng, id là tên cột
            // nhóm 2: toán tử tìm kiếm
            // nhóm 3: giá trị cần tìm kiếm
            // nhóm 4: dấu câu cuối cùng
            // nhóm 5: dấu câu cuối cùng
            for (String s : activities) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            Specification<FileActivity> spec = builder.build();
            // nó trả trả về 1 spec mới
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("file").get("id"), fileId));
            Page<FileActivity> filePage = fileActivityRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(filePage, pageable, fileActivityMapper::toResponse);
        }
        return PaginationUtils.convertToPageResponse(fileActivityRepo.findAll(pageable), pageable, fileActivityMapper::toResponse);
    }

    @Override
    public PageResponse<List<FileActivityResponse>> searchByStartDateAndEndDate(Long fileId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay = startDate.atStartOfDay(); // 2025-03-05 00:00:00
        LocalDateTime endOfDay = endDate.atTime(23, 59, 59); // 2025-03-10 23:59:59
        Page<FileActivity> fileActivityPage = fileActivityRepo.findActivityRepositoriesByFileIdAndCreatedAtRange(fileId, startOfDay, endOfDay, pageable);
        return PaginationUtils.convertToPageResponse(fileActivityPage, pageable, fileActivityMapper::toResponse);
    }

}
