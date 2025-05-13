package vn.kltn.dto;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.CancellationToken;
import vn.kltn.entity.Item;
import vn.kltn.index.ItemIndex;

import java.util.List;

@Getter
@Setter
public class UploadContext {
    private final CancellationToken token;
    private final List<Item> items;
    private List<String> blobNames;
    private List<ItemIndex> documentIndices;

    public UploadContext(CancellationToken token, List<Item> items) {
        this.token = token;
        this.items = items;
    }
}
