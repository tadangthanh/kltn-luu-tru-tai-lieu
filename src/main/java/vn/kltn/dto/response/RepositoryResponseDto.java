package vn.kltn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepositoryResponseDto extends BaseDto {
    private String name;
    private String uuid;
    private String description;
    private Long ownerId;
    private String ownerEmail;
    private String ownerName;
}
