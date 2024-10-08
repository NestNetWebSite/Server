package NestNet.NestNetWebSite.service.post;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.domain.attachedfile.AttachedFile;
import NestNet.NestNetWebSite.domain.comment.Comment;
import NestNet.NestNetWebSite.domain.member.Member;
import NestNet.NestNetWebSite.domain.post.introduction.IntroductionPost;
import NestNet.NestNetWebSite.dto.request.IntroductionPostModifyRequest;
import NestNet.NestNetWebSite.dto.request.IntroductionPostRequest;
import NestNet.NestNetWebSite.dto.response.AttachedFileDto;
import NestNet.NestNetWebSite.dto.response.CommentDto;
import NestNet.NestNetWebSite.dto.response.introductionpost.IntroductionPostDto;
import NestNet.NestNetWebSite.dto.response.introductionpost.IntroductionPostListDto;
import NestNet.NestNetWebSite.dto.response.introductionpost.IntroductionPostListResponse;
import NestNet.NestNetWebSite.dto.response.introductionpost.IntroductionPostResponse;
import NestNet.NestNetWebSite.exception.CustomException;
import NestNet.NestNetWebSite.exception.ErrorCode;
import NestNet.NestNetWebSite.repository.member.MemberRepository;
import NestNet.NestNetWebSite.repository.post.IntroductionPostRepository;
import NestNet.NestNetWebSite.service.attachedfile.AttachedFileService;
import NestNet.NestNetWebSite.service.like.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IntroductionPostService {

    private final IntroductionPostRepository introductionPostRepository;
    private final AttachedFileService attachedFileService;
    private final MemberRepository memberRepository;
    private final PostLikeService postLikeService;
    private final PostService postService;

    /*
    자기소개 게시판에 게시물 저장
     */
    @Transactional
    public ApiResult<?> savePost(IntroductionPostRequest introductionPostRequest, List<MultipartFile> files, String memberLoginId){

        Member member = memberRepository.findByLoginId(memberLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        IntroductionPost post = introductionPostRequest.toEntity(member);

        introductionPostRepository.save(post);

        if(!ObjectUtils.isEmpty(files)){
            List<AttachedFile> savedFileList = attachedFileService.save(post, files);

            // 양방향 연관관계 주입
            for(AttachedFile attachedFile : savedFileList){
                post.addAttachedFile(attachedFile);
            }
        }

        postService.addViewCount(post, member.getId());

        return ApiResult.success("게시물 저장 성공");
    }

    /*
    자기 소개 게시판 게시물 리스트 조회
     */
    public ApiResult<?> findPostListByPaging(int page, int size){

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<IntroductionPost> postPage = introductionPostRepository.findPostList(pageRequest);

        List<IntroductionPost> introductionPostList = postPage.getContent();

        List<IntroductionPostListDto> dtoList = new ArrayList<>();
        for(IntroductionPost post : introductionPostList){

            // 게시물에 첨부파일이 있는 경우
            if(!ObjectUtils.isEmpty(post.getAttachedFileList())){
                for(AttachedFile attachedFile : post.getAttachedFileList()){
                    dtoList.add(new IntroductionPostListDto(
                            post.getId(), post.getTitle(), post.getViewCount(), post.getLikeCount(), post.getCreatedTime(),
                            attachedFile.getSaveFilePath(), attachedFile.getSaveFileName()));
                }
            }
            // 게시물에 첨부 파일이 없는 경우
            else{
                dtoList.add(new IntroductionPostListDto(
                        post.getId(), post.getTitle(), post.getViewCount(), post.getLikeCount(), post.getCreatedTime(),
                        null, null));
            }

        }

        IntroductionPostListResponse result = new IntroductionPostListResponse(postPage.getTotalElements(), dtoList);

        return ApiResult.success(result);
    }


    /*
    자기 소개 게시판 게시물 단건 조회
     */
    @Transactional
    public ApiResult<?> findPostById(Long postId, String memberLoginId){

        Member loginMember = memberRepository.findByLoginId(memberLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        IntroductionPost post = introductionPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        List<AttachedFile> attachedFileList = post.getAttachedFileList();

        List<Comment> commentList = post.getCommentList();

        List<AttachedFileDto> fileDtoList = new ArrayList<>();
        List<CommentDto> commentDtoList = new ArrayList<>();
        IntroductionPostDto postDto = null;

        for(AttachedFile attachedFile : attachedFileList){
            fileDtoList.add(new AttachedFileDto(attachedFile.getId(), attachedFile.getOriginalFileName(),
                    attachedFile.getSaveFilePath(), attachedFile.getSaveFileName()));
        }

        for(Comment comment : commentList){
            if(loginMember.getId() == comment.getMember().getId()){
                commentDtoList.add(new CommentDto(comment.getId(), comment.getMember().getLoginId(), comment.getMember().getName(), comment.getMember().getMemberAuthority(),
                        comment.getContent(), comment.getCreatedTime(), comment.getModifiedTime(), true));
            }
            else{
                commentDtoList.add(new CommentDto(comment.getId(), comment.getMember().getLoginId(), comment.getMember().getName(), comment.getMember().getMemberAuthority(),
                        comment.getContent(), comment.getCreatedTime(), comment.getModifiedTime(), false));
            }
        }

        if(loginMember.getId() == post.getMember().getId()){
            postDto = IntroductionPostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .bodyContent(post.getBodyContent())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .memberLoginId(post.getMember().getLoginId())
                    .username(post.getMember().getName())
                    .createdTime(post.getCreatedTime())
                    .modifiedTime(post.getModifiedTime())
                    .isMemberWritten(true)
                    .build();
        }
        else{
            postDto = IntroductionPostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .bodyContent(post.getBodyContent())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .memberLoginId(post.getMember().getLoginId())
                    .username(post.getMember().getName())
                    .createdTime(post.getCreatedTime())
                    .modifiedTime(post.getModifiedTime())
                    .isMemberWritten(false)
                    .build();
        }

        boolean isMemberLiked = postLikeService.isMemberLikedByPost(post, loginMember);

        postService.addViewCount(post, loginMember.getId());

        return ApiResult.success(new IntroductionPostResponse(postDto, fileDtoList, commentDtoList, isMemberLiked));
    }

    /*
    자기소개 게시물 수정
     */
    @Transactional
    public void modifyPost(IntroductionPostModifyRequest modifyRequest, List<Long> fileIdList, List<MultipartFile> files){

        IntroductionPost post = introductionPostRepository.findById(modifyRequest.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        attachedFileService.modifyFiles(post, fileIdList, files);

        // 변경 감지 -> 자동 update
        post.modifyPost(modifyRequest.getTitle(), modifyRequest.getBodyContent());
    }

}
