package com.univ.momcare.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.entity.BabyGoods;
import com.univ.momcare.entity.Coupon;
import com.univ.momcare.entity.Member;
import com.univ.momcare.entity.SysUser;
import com.univ.momcare.mapper.BabyGoodsMapper;
import com.univ.momcare.mapper.CouponMapper;
import com.univ.momcare.mapper.MemberMapper;
import com.univ.momcare.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 关联名称回填：列表接口只存外键 ID，前端需要展示会员昵称、商品名、券名、导购姓名。
 * 统一按「xxxId → xxxName」的命名约定批量查表回填，避免每个控制器各写一遍 join。
 *
 * 只填充实体上标注 {@code @TableField(exist = false)} 的同名瞬态字段，
 * 若实体本身已有同名数据库列（如 sys_oper_log.oper_name），则跳过，不会覆盖真实数据。
 */
@Component
@RequiredArgsConstructor
public class NameFill {

    /** 这些前缀的外键指向 sys_user */
    private static final Set<String> USER_BASES = Set.of(
            "create", "update", "owner", "operator", "grant", "audit", "adjust", "assign", "uploader", "handler");

    private final MemberMapper memberMapper;
    private final BabyGoodsMapper goodsMapper;
    private final CouponMapper couponMapper;
    private final SysUserMapper userMapper;

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    public void fill(List<?> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Long> memberIds = new HashSet<>();
        Set<Long> goodsIds = new HashSet<>();
        Set<Long> couponIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();

        for (Object row : rows) {
            for (Field f : idFields(row.getClass())) {
                Long v = asLong(row, f);
                if (v == null) {
                    continue;
                }
                String base = base(f);
                if ("member".equals(base)) {
                    memberIds.add(v);
                } else if ("goods".equals(base)) {
                    goodsIds.add(v);
                } else if ("coupon".equals(base)) {
                    couponIds.add(v);
                } else if (USER_BASES.contains(base)) {
                    userIds.add(v);
                }
            }
        }

        Map<Long, String> memberNames = memberNames(memberIds);
        Map<Long, String> goodsNames = goodsNames(goodsIds);
        Map<Long, String> couponNames = couponNames(couponIds);
        Map<Long, String> userNames = userNames(userIds);

        for (Object row : rows) {
            for (Field f : idFields(row.getClass())) {
                Long v = asLong(row, f);
                if (v == null) {
                    continue;
                }
                String base = base(f);
                String name = switch (base) {
                    case "member" -> memberNames.get(v);
                    case "goods" -> goodsNames.get(v);
                    case "coupon" -> couponNames.get(v);
                    default -> USER_BASES.contains(base) ? userNames.get(v) : null;
                };
                if (name == null) {
                    continue;
                }
                Field target = nameField(row.getClass(), base + "Name");
                if (target == null) {
                    continue;
                }
                try {
                    target.set(row, name);
                } catch (Exception ignored) {
                    // 回填失败不影响主流程
                }
            }
        }
    }

    private String base(Field f) {
        return f.getName().substring(0, f.getName().length() - 2);
    }

    private Map<Long, String> memberNames(Set<Long> ids) {
        Map<Long, String> m = new HashMap<>();
        if (ids.isEmpty()) {
            return m;
        }
        for (Member x : memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .select(Member::getId, Member::getNickName).in(Member::getId, ids))) {
            m.put(x.getId(), x.getNickName());
        }
        return m;
    }

    private Map<Long, String> goodsNames(Set<Long> ids) {
        Map<Long, String> m = new HashMap<>();
        if (ids.isEmpty()) {
            return m;
        }
        for (BabyGoods x : goodsMapper.selectList(new LambdaQueryWrapper<BabyGoods>()
                .select(BabyGoods::getId, BabyGoods::getGoodsName).in(BabyGoods::getId, ids))) {
            m.put(x.getId(), x.getGoodsName());
        }
        return m;
    }

    private Map<Long, String> couponNames(Set<Long> ids) {
        Map<Long, String> m = new HashMap<>();
        if (ids.isEmpty()) {
            return m;
        }
        for (Coupon x : couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .select(Coupon::getId, Coupon::getCouponName).in(Coupon::getId, ids))) {
            m.put(x.getId(), x.getCouponName());
        }
        return m;
    }

    private Map<Long, String> userNames(Set<Long> ids) {
        Map<Long, String> m = new HashMap<>();
        if (ids.isEmpty()) {
            return m;
        }
        for (SysUser u : userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId, SysUser::getRealName, SysUser::getUsername).in(SysUser::getId, ids))) {
            m.put(u.getId(), u.getRealName() == null ? u.getUsername() : u.getRealName());
        }
        return m;
    }

    /** 只保留 Long 类型、以 Id 结尾、且存在同名瞬态 Name 字段的属性 */
    private List<Field> idFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> {
            List<Field> out = new ArrayList<>();
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() != Long.class || !f.getName().endsWith("Id") || f.getName().length() <= 2) {
                    continue;
                }
                if (nameField(c, base(f) + "Name") != null) {
                    f.setAccessible(true);
                    out.add(f);
                }
            }
            return out;
        });
    }

    /** 目标字段必须是未映射到数据库的瞬态 String 字段 */
    private Field nameField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            if (f.getType() != String.class) {
                return null;
            }
            com.baomidou.mybatisplus.annotation.TableField tf =
                    f.getAnnotation(com.baomidou.mybatisplus.annotation.TableField.class);
            if (tf == null || tf.exist()) {
                return null;
            }
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Long asLong(Object row, Field f) {
        try {
            Object v = f.get(row);
            return v == null ? null : ((Number) v).longValue();
        } catch (Exception e) {
            return null;
        }
    }
}
