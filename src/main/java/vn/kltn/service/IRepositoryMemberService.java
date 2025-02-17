package vn.kltn.service;

public interface IRepositoryMemberService {
    void deleteMember(Long repositoryId, Long memberId);
    void deleteMemberByRepositoryId(Long repositoryId);
}
