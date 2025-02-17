package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AddMemberRepoRequest;
import vn.kltn.dto.request.PermissionRepoDto;
import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IRepositoryService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/repository")
public class RepositoryRest {
    private final IRepositoryService repositoryService;

    @PostMapping
    public ResponseData<RepoResponseDto> login(@Validated @RequestBody RepositoryRequestDto repositoryRequestDto) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Tạo kho lưu trữ thành công", repositoryService.createRepository(repositoryRequestDto));
    }

    @DeleteMapping("/{repositoryId}")
    public ResponseData<Void> deleteRepository(@PathVariable Long repositoryId) {
        repositoryService.deleteRepository(repositoryId);
        return new ResponseData<>(HttpStatus.OK.value(), "Xóa kho lưu trữ thành công", null);
    }

    @PostMapping("/{repositoryId}/member")
    public ResponseData<RepoResponseDto> addMemberToRepository(@PathVariable Long repositoryId,@Validated @RequestBody AddMemberRepoRequest addMemberRepoRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Thêm thành viên vào kho lưu trữ thành công", repositoryService.addMemberToRepository(repositoryId, addMemberRepoRequest.getUserId(), addMemberRepoRequest.getPermissions()));
    }
}
