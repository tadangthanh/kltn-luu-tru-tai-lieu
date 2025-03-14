package vn.kltn.common;

public enum RepoAction {
    ADD_FILE, // upload file
    DELETE_FILE, // xoa file
    ADD_MEMBER, // them thanh vien
    REMOVE_MEMBER, // xoa thanh vien
    UPDATE_REPOSITORY, // update thong tin repo
    UPDATE_PERMISSION, // update quyen thanh vien
    MEMBER_ACCEPT_INVITATION, // thanh vien chap nhan loi moi
    MEMBER_REJECT_INVITATION, // thanh vien tu choi loi moi
}
