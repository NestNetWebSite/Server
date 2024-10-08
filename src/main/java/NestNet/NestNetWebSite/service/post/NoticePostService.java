package NestNet.NestNetWebSite.service.post;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.domain.attachedfile.AttachedFile;
import NestNet.NestNetWebSite.domain.comment.Comment;
import NestNet.NestNetWebSite.domain.member.Member;
import NestNet.NestNetWebSite.domain.post.notice.NoticePost;
import NestNet.NestNetWebSite.dto.request.NoticePostModifyRequest;
import NestNet.NestNetWebSite.dto.request.NoticePostRequest;
import NestNet.NestNetWebSite.dto.response.AttachedFileDto;
import NestNet.NestNetWebSite.dto.response.CommentDto;
import NestNet.NestNetWebSite.dto.response.noticepost.NoticePostDto;
import NestNet.NestNetWebSite.dto.response.noticepost.NoticePostListDto;
import NestNet.NestNetWebSite.dto.response.noticepost.NoticePostListResponse;
import NestNet.NestNetWebSite.dto.response.noticepost.NoticePostResponse;
import NestNet.NestNetWebSite.exception.CustomException;
import NestNet.NestNetWebSite.exception.ErrorCode;
import NestNet.NestNetWebSite.repository.member.MemberRepository;
import NestNet.NestNetWebSite.repository.post.NoticePostRepository;
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
public class NoticePostService {

    private final NoticePostRepository noticePostRepository;
    private final MemberRepository memberRepository;
    private final AttachedFileService attachedFileService;
    private final PostLikeService postLikeService;
    private final PostService postService;

    /*
    공지사항 게시판에 게시물 저장
     */
    @Transactional
    public ApiResult<?> savePost(NoticePostRequest noticePostRequest, List<MultipartFile> files, String memberLoginId){

        Member member = memberRepository.findByLoginId(memberLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        NoticePost post = noticePostRequest.toEntity(member);

        noticePostRepository.save(post);

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
    공지사항 게시판 게시물 리스트 조회
     */
    public ApiResult<?> findPostListByPaging(int page, int size){

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<NoticePost> postPage = noticePostRepository.findAll(pageRequest);

        List<NoticePost> noticePostList = postPage.getContent();

        List<NoticePostListDto> dtoList = new ArrayList<>();
        for(NoticePost post : noticePostList){
            dtoList.add(new NoticePostListDto(post.getId(), post.getTitle(), post.getCreatedTime(),
                    post.getViewCount(), post.getLikeCount(), post.getMember().getName()));
        }

        NoticePostListResponse result = new NoticePostListResponse(postPage.getTotalElements(), dtoList);

        return ApiResult.success(result);
    }

    /*
    공지사항 게시판 게시물 단건 조회
     */
    @Transactional
    public ApiResult<?> findPostById(Long postId, String memberLoginId){

        Member loginMember = memberRepository.findByLoginId(memberLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));

        NoticePost post = noticePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<AttachedFile> attachedFileList = post.getAttachedFileList();

        List<Comment> commentList = post.getCommentList();

        List<AttachedFileDto> fileDtoList = new ArrayList<>();
        List<CommentDto> commentDtoList = new ArrayList<>();
        NoticePostDto postDto = null;

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
            postDto = NoticePostDto.builder()
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
            postDto = NoticePostDto.builder()
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

        return ApiResult.success(new NoticePostResponse(postDto, fileDtoList, commentDtoList, isMemberLiked));
    }

    /*
    공지사항 게시물 수정
     */
    @Transactional
    public void modifyPost(NoticePostModifyRequest modifyRequest, List<Long> fileIdList, List<MultipartFile> files){

        NoticePost post = noticePostRepository.findById(modifyRequest.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        attachedFileService.modifyFiles(post, fileIdList, files);

        // 변경 감지 -> 자동 update
        post.modifyPost(modifyRequest.getTitle(), modifyRequest.getBodyContent());
    }

}
