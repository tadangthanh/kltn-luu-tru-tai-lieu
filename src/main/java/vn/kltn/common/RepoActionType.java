package vn.kltn.common;

public enum RepoActionType {
    SEND_MEMBER_INVITE, // them thanh vien
    REMOVE_MEMBER, // xoa thanh vien
    UPDATE_REPOSITORY, // update thong tin repo
    CHANGE_MEMBER_PERMISSION, // update quyen thanh vien
    MEMBER_ACCEPT_INVITATION, // thanh vien chap nhan loi moi
    MEMBER_REJECT_INVITATION, // thanh vien tu choi loi moi
    SETTINGS_UPDATE,// update thong tin cai dat
}
