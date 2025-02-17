package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepositoryResponseDto;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IRepositoryService;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/repository")
public class RepositoryRest {
    private final IRepositoryService repositoryService;

    @PostMapping
    public ResponseData<RepositoryResponseDto> login(@Validated @RequestBody RepositoryRequestDto repositoryRequestDto) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Tạo kho lưu trữ thành công", repositoryService.createRepository(repositoryRequestDto));
    }
}
