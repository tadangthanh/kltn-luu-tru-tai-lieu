package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.service.impl.ResourceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
public class ResourceRest {
    private final ResourceService resourceService;

    @GetMapping
    public ResponseData<?> getAll(){
        resourceService.getResource();
        return new ResponseData<>(200,"thành công");
    }
}
