package vn.kltn.common;

import lombok.Getter;

@Getter
public enum RepoActionType {
    SEND_MEMBER_INVITE("Gửi lời mời tham gia"),
    REMOVE_MEMBER("Xóa thành viên"),
    UPDATE_REPOSITORY("cập nhât repository"),
    CHANGE_MEMBER_PERMISSION("cập nhật quyền của thành viên"),
    MEMBER_ACCEPT_INVITATION("thành viên chấp nhận lời mời"),
    MEMBER_REJECT_INVITATION("thành viên từ chối lời mời"),
    SETTINGS_UPDATE("cập nhật cài đặt repository");

    private final String description;

    RepoActionType(String description) {
        this.description = description;
    }

}
