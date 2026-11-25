package com.univ.momcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 菜单 Mapper */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 按角色查可访问的菜单ID（走 sys_role_menu 关联） */
    @Select("SELECT m.id FROM sys_menu m JOIN sys_role_menu rm ON rm.menu_id = m.id "
          + "WHERE rm.role_id = #{roleId} AND m.deleted = 0 AND m.status = '正常'")
    List<Long> selectMenuIdsByRole(@Param("roleId") Long roleId);
}
