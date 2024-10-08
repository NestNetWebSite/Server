package NestNet.NestNetWebSite.repository.member;

import NestNet.NestNetWebSite.domain.member.Member;
import NestNet.NestNetWebSite.domain.member.MemberAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // Id(PK)로 회원 단건 조회
    Optional<Member> findById(Long id);

    // 로그인 아이디로 회원 단건 조회
    @Query("select m from Member m where m.loginId =:loginId")
    Optional<Member> findByLoginId(@Param("loginId") String loginId);

    // 이름 + 이메일로 회원 단건 조회
    @Query("select m from Member m where m.name =:name and m.emailAddress =:emailAddress")
    Optional<Member> findByNameAndEmail(@Param("name") String name, @Param("emailAddress") String emailAddress);

    // 모든 회원 조회 (탈퇴멤버, admin, 특정 권한 제외)
    @Query("select m from Member m where m.memberAuthority not in :excludeAuthorityList")
    List<Member> findAllApprovedMemberExceptAdmin(@Param("excludeAuthorityList") List<MemberAuthority> excludeAuthorityList);

}
