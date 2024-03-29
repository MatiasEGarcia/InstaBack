package com.instaback.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaback.dto.CommentDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Comment;

@Mapper(componentModel = "spring", uses = {PublicatedImageMapper.class , UserMapper.class})
public interface CommentMapper {

	CommentDto commentToCommentDto(Comment comment);
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<CommentDto> pageAndPageInfoDtoToResPaginationG(Page<Comment> page,PageInfoDto pageInfoDto);
}
