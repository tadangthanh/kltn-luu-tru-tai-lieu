package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Item;
@Repository
public interface ItemRepo extends JpaRepository<Item,Long> {
}
