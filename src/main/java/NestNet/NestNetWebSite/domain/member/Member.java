package NestNet.NestNetWebSite.domain.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 회원 정보를 담고 있는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;                                // PK

    private String loginId;                         // 로그인 아이디

    private String loginPassword;                   // 로그인 비밀번호

    private String name;                            // 이름 (본명)

    private boolean graduated;                      // 졸업 여부

    private int graduateYear;                       // 졸업 년도

    private String studentId;                       // 학번

    private int grade;                              // 학년

    private String emailAddress;                    // 이메일 주소

    @Enumerated(EnumType.STRING)
    private MemberAuthority memberAuthority;        // 권한 (PRESIDENT, VICE_PRESIDENT, MANAGER, GENERAL_MEMBER, GRADUATED_MEMBER, WAITING_FOR_APPROVAL, WITHDRAWN_MEMBER)

    private LocalDateTime joinDate;                 // 회원가입 날짜

    /*
    생성자
     */
    @Builder
    public Member(String loginId, String loginPassword, String name, boolean graduated, int graduateYear,
                  String studentId, int grade, String emailAddress, MemberAuthority memberAuthority, LocalDateTime joinDate) {
        this.loginId = loginId;
        this.loginPassword = loginPassword;
        this.name = name;
        this.graduated = graduated;
        this.graduateYear = graduateYear;
        this.studentId = studentId;
        this.grade = grade;
        this.emailAddress = emailAddress;
        this.memberAuthority = memberAuthority;
        this.joinDate = joinDate;
    }

    //== 비지니스 로직 ==//
    /*
    비밀번호 암호화
     */
    public Member encodePassword(PasswordEncoder passwordEncoder){
        this.loginPassword = passwordEncoder.encode(this.loginPassword);
        return this;
    }

    /*
    재학생 -> 졸업생으로 전환
     */
    public void changeMemberToGraduate(){
        this.graduated = true;
        this.graduateYear = LocalDateTime.now().getYear();
        this.memberAuthority = MemberAuthority.GRADUATED_MEMBER;
    }

    /*
    회원 권한 변경
     */
    public void changeAuthority(MemberAuthority authority){
        this.memberAuthority = authority;
    }

    /*
    회원 정보 수정 -> 아이디, 이름, 학번, 학년, 이메일
     */
    public void modifyInfo(String loginId, String name, String studentId, int grade, String emailAddress){
        this.loginId = loginId;
        this.name = name;
        this.studentId = studentId;
        this.grade = grade;
        this.emailAddress = emailAddress;
    }

    /*
    비밀번호 변경
     */
    public void changePassword(String loginPassword, PasswordEncoder passwordEncoder){
        this.loginPassword = passwordEncoder.encode(loginPassword);
    }

    /*
    탈퇴
     */
    public void withdraw(){
        this.loginId = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        this.loginPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        this.name = "알수없음";
        this.emailAddress = "";
        this.memberAuthority = MemberAuthority.WITHDRAWN_MEMBER;
    }

}
