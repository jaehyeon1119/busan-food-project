package com.project.food.service;

import org.springframework.stereotype.Service;

import com.project.food.dto.MemberDto;
import com.project.food.mapper.MemberMapper;

@Service
public class MemberService {

    private final MemberMapper memberMapper;

    public MemberService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    public void join(MemberDto memberDto) {
        memberMapper.insertMember(memberDto);
    }

    public MemberDto login(MemberDto memberDto) {
        return memberMapper.login(memberDto);
    }

    public MemberDto findById(int memberId) {
        return memberMapper.findById(memberId);
    }
}