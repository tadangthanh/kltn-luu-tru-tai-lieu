package vn.kltn.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionPlanRequest implements Serializable {

    @NotBlank(message = "Tên gói không được để trống")
    private String name;

    @NotNull(message = "Số repo tối đa không được để trống")
    @Min(value = 1, message = "Số repo tối đa phải lớn hơn 0")
    private Integer maxReposPerMember;

    @NotNull(message = "Số thành viên tối đa trong repo không được để trống")
    @Min(value = 1, message = "Số thành viên tối đa trong repo phải lớn hơn 0")
    private Integer maxMembersPerRepo;

    @NotNull(message = "Dung lượng lưu trữ tối đa không được để trống (Đơn vị tính: Byte)")
    @Positive(message = "Dung lượng lưu trữ tối đa phải lớn hơn 0 (Đơn vị tính: Byte)")
    private Long maxStorage; // Đơn vị: Byte (VD: 10GB = 10 * 1024 * 1024 * 1024)

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
    private BigDecimal price;
}
