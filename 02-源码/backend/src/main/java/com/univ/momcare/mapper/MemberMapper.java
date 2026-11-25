package com.univ.momcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/** member Mapper */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
