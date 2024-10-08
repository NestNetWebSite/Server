package NestNet.NestNetWebSite.domain.attachedfile;

import NestNet.NestNetWebSite.domain.post.Post;
import NestNet.NestNetWebSite.exception.CustomException;
import NestNet.NestNetWebSite.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.Normalizer;
import java.util.UUID;

/**
 * 첨부파일 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
public class AttachedFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attached_file_id")
    private Long id;                                            // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;                                          // 첨부파일이 포함된 게시물

    private String originalFileName;                            // 사용자가 입력한 파일 이름
    private String saveFileName;                                // 실제 저장된 파일 이름
    private String saveFilePath;                                // 파일 경로 (서버)

    /*
    생성자
     */
    @Builder
    public AttachedFile(Post post, String originalFileName, String saveFileName, String saveFilePath) {
        this.post = post;
        this.originalFileName = originalFileName;
        this.saveFileName = saveFileName;
        this.saveFilePath = saveFilePath;
    }

    //== setter ==//
    public void injectPost(Post post) {
        this.post = post;
    }

    //== 비지니스 로직 ==//


}
