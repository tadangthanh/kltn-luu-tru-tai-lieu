package vn.kltn.dto;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.CancellationToken;
import vn.kltn.entity.Document;
import vn.kltn.index.ItemIndex;

import java.util.List;

@Getter
@Setter
public class UploadContext {
    private final CancellationToken token;
    private final List<Document> documents;
    private List<String> blobNames;
    private List<ItemIndex> documentIndices;

    public UploadContext(CancellationToken token, List<Document> documents) {
        this.token = token;
        this.documents = documents;
    }
}
