package vn.kltn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LatestVersion {
    private Long documentId;
    private Integer version;

}
