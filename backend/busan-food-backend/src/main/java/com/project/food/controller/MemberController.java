package com.project.food.controller;

import org.springframework.web.bind.annotation.*;

import com.project.food.dto.MemberDto;
import com.project.food.service.MemberService;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/join")
    public void join(@RequestBody MemberDto memberDto) {
        memberService.join(memberDto);
    }

    @PostMapping("/login")
    public MemberDto login(@RequestBody MemberDto memberDto) {
        return memberService.login(memberDto);
    }
}