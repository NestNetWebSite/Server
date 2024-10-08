package NestNet.NestNetWebSite.service.comment;

import NestNet.NestNetWebSite.domain.comment.Comment;
import NestNet.NestNetWebSite.domain.member.Member;
import NestNet.NestNetWebSite.domain.post.Post;
import NestNet.NestNetWebSite.dto.request.CommentRequest;
import NestNet.NestNetWebSite.exception.CustomException;
import NestNet.NestNetWebSite.exception.ErrorCode;
import NestNet.NestNetWebSite.repository.comment.CommentRepository;
import NestNet.NestNetWebSite.repository.member.MemberRepository;
import NestNet.NestNetWebSite.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    /*
    댓글 저장
     */
    @Transactional
    public void saveComment(CommentRequest commentRequest, Long postId, String memberLoginId, LocalDateTime currTime){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Member member = memberRepository.findByLoginId(memberLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        Comment comment = commentRequest.toEntity(post, member, currTime);

        // 양방향 연관관계 주입
        post.addComment(comment);

        commentRepository.save(comment);
    }

    /*
    댓글 수정
     */
    @Transactional
    public void modifyComment(CommentRequest commentRequest, Long commentId){

        Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        comment.modifyContent(commentRequest.getContent());
    }

    /*
    댓글 단건 삭제
     */
    @Transactional
    public void deleteComment(Long commentId){

        Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(comment);
    }
}
