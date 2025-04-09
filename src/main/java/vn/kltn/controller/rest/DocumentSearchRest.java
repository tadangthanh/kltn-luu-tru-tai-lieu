package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.index.DocumentSearchEntity;
import vn.kltn.service.impl.DocumentSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents-search")
@RequiredArgsConstructor
public class DocumentSearchRest {
    private final DocumentSearchService documentSearchService;

    @GetMapping
    public ResponseData<List<DocumentSearchEntity>> search(@RequestParam String keyword) {
        return new ResponseData<>(200, "thành công", documentSearchService.search(keyword));
    }

    @GetMapping("/tag")
    public ResponseData<List<DocumentSearchEntity>> searchByTag(@RequestParam String tag) {
        return new ResponseData<>(200, "thành công", documentSearchService.searchByTag(tag));
    }

    @PostMapping("/index")
    public void index(@RequestBody DocumentSearchEntity doc) {
        documentSearchService.indexDocument(doc);
    }
}
