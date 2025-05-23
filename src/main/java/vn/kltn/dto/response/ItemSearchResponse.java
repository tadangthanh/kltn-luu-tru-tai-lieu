package vn.kltn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.kltn.index.ItemIndex;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchResponse {
    private ItemIndex item; // dữ liệu gốc
    private Map<String, List<String>> highlights; // highlight của các field
}
