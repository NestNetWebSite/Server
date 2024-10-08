package NestNet.NestNetWebSite.service.post;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.config.redis.RedisUtil;
import NestNet.NestNetWebSite.domain.member.Member;
import NestNet.NestNetWebSite.domain.post.Post;
import NestNet.NestNetWebSite.dto.response.RecentPostListDto;
import NestNet.NestNetWebSite.dto.response.RecentPostListResponse;
import NestNet.NestNetWebSite.exception.CustomException;
import NestNet.NestNetWebSite.exception.ErrorCode;
import NestNet.NestNetWebSite.repository.member.MemberRepository;
import NestNet.NestNetWebSite.repository.post.PostRepository;
import NestNet.NestNetWebSite.service.like.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostLikeService postLikeService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    // 조회수 update
    @Transactional
    public void addViewCount(Post post, Long memberId){

        String viewRecordKey = memberId + "_" +  post.getId().toString();     //사용자 id값(PK) + 게시물 id

        //24시간 내에 다시 조회해도 조회수 올라가지 않음 (조회하지 않았으면 레디스에 없음 -> 조회수 + 1)
        if(!redisUtil.hasKey(viewRecordKey)){

            System.out.println(viewRecordKey);

            post.addViewCount();        //변경 감지에 의해 update
            redisUtil.setData(viewRecordKey, "v", 24, TimeUnit.HOURS);      //24시간 유지 -> 자동 삭제
        }
    }

    /*
    최근 게시물 목록 조회
     */
    public ApiResult<?> findRecentPost(){

        int size = 5;

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

        List<Post> postList = postRepository.findAll(pageRequest).getContent();

        List<RecentPostListDto> dtoList = new ArrayList<>();
        for(Post post : postList){
            dtoList.add(new RecentPostListDto(post.getId(), post.getPostCategory(), post.getTitle(), post.getCreatedTime()));
        }

        return ApiResult.success(new RecentPostListResponse(dtoList));
    }

    /*
    게시물 삭제 -> soft delete
     */
    @Transactional
    public void deletePost(Long postId){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));


        postRepository.delete(post);
    }

    /*
    좋아요
     */
    @Transactional
    public ApiResult<?> like(Long id, String memberLoginId){

        Member member = memberRepository.findByLoginId(memberLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if(!postLikeService.isMemberLikedByPost(post, member)) {
            postLikeService.saveLike(post, memberLoginId);

            post.like();
        }

        return ApiResult.success(post.getLikeCount());
    }

    /*
    좋아요 취소
     */
    @Transactional
    public ApiResult<?> cancelLike(Long id, String memberLoginId){

        Member member = memberRepository.findByLoginId(memberLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if(postLikeService.isMemberLikedByPost(post, member)) {
            postLikeService.cancelLike(post, memberLoginId);

            post.cancelLike();
        }

        return ApiResult.success(post.getLikeCount());
    }

}
