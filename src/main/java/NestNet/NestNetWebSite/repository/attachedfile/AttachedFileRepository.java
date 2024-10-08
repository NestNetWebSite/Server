package NestNet.NestNetWebSite.repository.attachedfile;

import NestNet.NestNetWebSite.domain.attachedfile.AttachedFile;
import NestNet.NestNetWebSite.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttachedFileRepository extends JpaRepository<AttachedFile, Long> {

    // id(PK)로 단건 조회
    Optional<AttachedFile> findById(Long id);

    // 게시물에 해당하는 파일 조회
    List<AttachedFile> findAllByPost(@Param("post") Post post);

}
