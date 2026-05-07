package com.project.food.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.project.food.dto.MemberDto;

@Mapper
public interface MemberMapper {

    void insertMember(MemberDto memberDto);

    MemberDto login(MemberDto memberDto);

    MemberDto findById(int memberId);
}