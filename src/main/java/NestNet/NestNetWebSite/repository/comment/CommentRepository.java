package NestNet.NestNetWebSite.repository.comment;

import NestNet.NestNetWebSite.domain.comment.Comment;
import NestNet.NestNetWebSite.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // id(PK)로 단건 조회
    Optional<Comment> findById(Long id);
}
