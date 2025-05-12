package vn.kltn.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.kltn.index.ItemIndex;

import java.util.List;

public interface ItemIndexRepo extends ElasticsearchRepository<ItemIndex, String> {

}
