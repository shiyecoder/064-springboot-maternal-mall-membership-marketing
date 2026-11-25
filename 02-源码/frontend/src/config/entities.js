/**
 * 实体配置表：驱动 CrudView 的列表列、筛选条件、表单字段与行操作。
 * 字段名与后端实体一致（驼峰），与 sql/init.sql 的列一一对应。
 */

// 列类型：text / tag / datetime / date / money / image / progress
export const ENTITIES = {
  member: {
    api: 'member',
    name: '会员档案',
    keyword: '会员编号 / 手机号 / 昵称',
    addText: '新增会员',
    columns: [
      { label: '会员编号', prop: 'memberNo', width: 132 },
      { label: '昵称', prop: 'nickName', width: 90 },
      { label: '手机号', prop: 'mobile', width: 116 },
      { label: '等级', prop: 'levelCode', type: 'tag', width: 88 },
      { label: '注册渠道', prop: 'registerChannel', width: 92 },
      { label: '累计消费', prop: 'totalAmount', type: 'money', width: 100 },
      { label: '消费次数', prop: 'orderCount', width: 80 },
      { label: '积分', prop: 'pointBalance', width: 72 },
      { label: '最近购买', prop: 'lastBuyTime', type: 'datetime', width: 140 },
      { label: '流失风险', prop: 'riskLevel', type: 'tag', width: 92 },
      { label: '状态', prop: 'status', type: 'tag', width: 76 }
    ],
    filters: [
      { prop: 'levelCode', label: '等级', dict: 'member_level', col: 'level_code' },
      { prop: 'riskLevel', label: '风险', dict: 'risk_level', col: 'risk_level' },
      { prop: 'status', label: '状态', dict: 'member_status', col: 'status' }
    ],
    form: [
      { prop: 'mobile', label: '手机号', required: true, tip: '11 位，作为登录账号且全局唯一' },
      { prop: 'nickName', label: '昵称', required: true },
      { prop: 'levelCode', label: '会员等级', type: 'dict', dict: 'member_level' },
      { prop: 'registerChannel', label: '注册渠道', type: 'dict', dict: 'register_channel' },
      { prop: 'status', label: '状态', type: 'dict', dict: 'member_status' },
      { prop: 'totalAmount', label: '累计消费', type: 'number' },
      { prop: 'orderCount', label: '消费次数', type: 'number' },
      { prop: 'pointBalance', label: '积分余额', type: 'number' },
      { prop: 'riskLevel', label: '流失风险', type: 'dict', dict: 'risk_level' }
    ],
    actions: [
      { key: 'recalc', label: '重算等级', api: 'recalcMember', confirm: '按累计消费与次数重算会员等级？结果会写入留痕。' }
    ]
  },

  baby: {
    api: 'baby-profile',
    name: '宝宝档案',
    keyword: '档案编号 / 宝宝昵称',
    addText: '建立宝宝档案',
    columns: [
      { label: '档案编号', prop: 'babyNo', width: 116 },
      { label: '会员', prop: 'memberName', width: 90 },
      { label: '宝宝昵称', prop: 'babyName', width: 96 },
      { label: '性别', prop: 'gender', width: 68 },
      { label: '出生日期', prop: 'birthday', type: 'date', width: 108 },
      { label: '月龄', prop: 'monthAge', width: 68 },
      { label: '身高', prop: 'heightCm', width: 72 },
      { label: '体重', prop: 'weightKg', width: 72 },
      { label: '过敏史', prop: 'allergyDesc', minWidth: 140 },
      { label: '奶粉段位', prop: 'milkStage', width: 88 },
      { label: '纸尿裤', prop: 'diaperSize', width: 88 }
    ],
    filters: [
      { prop: 'gender', label: '性别', dict: 'gender', col: 'gender' },
      { prop: 'milkStage', label: '段位', dict: 'milk_stage', col: 'milk_stage' },
      { prop: 'status', label: '状态', options: ['1', '0'], col: 'status' }
    ],
    form: [
      { prop: 'memberId', label: '所属会员ID', type: 'number', required: true, tip: '会员档案里的主键' },
      { prop: 'babyName', label: '宝宝昵称', required: true },
      { prop: 'gender', label: '性别', type: 'dict', dict: 'gender' },
      { prop: 'birthday', label: '出生日期', type: 'date', required: true, tip: '月龄由后端按出生日期自动计算' },
      { prop: 'heightCm', label: '身高 cm', type: 'number' },
      { prop: 'weightKg', label: '体重 kg', type: 'number' },
      { prop: 'milkStage', label: '奶粉段位', type: 'dict', dict: 'milk_stage' },
      { prop: 'diaperSize', label: '纸尿裤尺码', type: 'dict', dict: 'diaper_size' },
      { prop: 'allergyDesc', label: '过敏史', type: 'textarea' }
    ]
  },

  goods: {
    api: 'baby-goods',
    name: '商品管理',
    keyword: '商品编号 / 名称 / 品牌',
    addText: '新增商品',
    columns: [
      { label: '商品编号', prop: 'goodsNo', width: 128 },
      { label: '主图', prop: 'coverImage', type: 'image', width: 76 },
      { label: '商品名称', prop: 'goodsName', minWidth: 220 },
      { label: '品牌', prop: 'brand', width: 88 },
      { label: '品类', prop: 'goodsType', type: 'tag', width: 88 },
      { label: '适用月龄', prop: 'ageRange', width: 100 },
      { label: '规格', prop: 'specDesc', width: 110 },
      { label: '售价', prop: 'salePrice', type: 'money', width: 88 },
      { label: '会员价', prop: 'memberPrice', type: 'money', width: 92 },
      { label: '库存', prop: 'stockQty', type: 'progress', minProp: 'minQty', width: 110 },
      { label: '状态', prop: 'status', type: 'tag', width: 88 }
    ],
    filters: [
      { prop: 'goodsType', label: '品类', dict: 'goods_type', col: 'goods_type' },
      { prop: 'status', label: '状态', dict: 'goods_status', col: 'status' }
    ],
    form: [
      { prop: 'goodsName', label: '商品名称', required: true },
      { prop: 'brand', label: '品牌', required: true },
      { prop: 'goodsType', label: '品类', type: 'dict', dict: 'goods_type', required: true },
      { prop: 'ageMonthStart', label: '适用月龄起', type: 'number', required: true },
      { prop: 'ageMonthEnd', label: '适用月龄止', type: 'number', required: true },
      { prop: 'specDesc', label: '规格', required: true },
      { prop: 'salePrice', label: '售价', type: 'number', required: true },
      { prop: 'memberPrice', label: '会员价', type: 'number', tip: '不得高于售价，留空按售价结算' },
      { prop: 'stockQty', label: '可售库存', type: 'number' },
      { prop: 'coverImage', label: '主图路径', tip: '上传后由后端返回相对路径，禁止填写外链' },
      { prop: 'status', label: '状态', type: 'dict', dict: 'goods_status' }
    ],
    actions: [
      { key: 'restock', label: '补货上架', api: 'restockGoods', show: (r) => r.status === '已售罄' || r.stockQty === 0,
        confirm: '补货 50 件并恢复在售？' }
    ]
  },

  coupon: {
    api: 'coupon',
    name: '优惠券配置',
    keyword: '券编号 / 券名称',
    addText: '新建券活动',
    columns: [
      { label: '券编号', prop: 'couponNo', width: 128 },
      { label: '券名称', prop: 'couponName', minWidth: 220 },
      { label: '类型', prop: 'couponType', type: 'tag', width: 80 },
      { label: '门槛', prop: 'minAmount', type: 'money', width: 88 },
      { label: '优惠', prop: 'discountAmount', width: 92 },
      { label: '适用范围', prop: 'limitType', width: 100 },
      { label: '发放方式', prop: 'grantType', width: 100 },
      { label: '有效期起', prop: 'validStart', type: 'date', width: 108 },
      { label: '有效期止', prop: 'validEnd', type: 'date', width: 108 },
      { label: '总量', prop: 'totalQty', width: 72 },
      { label: '已领', prop: 'receivedQty', width: 72 },
      { label: '状态', prop: 'status', type: 'tag', width: 88 }
    ],
    filters: [
      { prop: 'couponType', label: '类型', dict: 'coupon_type', col: 'coupon_type' },
      { prop: 'status', label: '状态', dict: 'coupon_status', col: 'status' },
      { prop: 'grantType', label: '发放方式', dict: 'grant_type', col: 'grant_type' }
    ],
    form: [
      { prop: 'couponName', label: '券名称', required: true },
      { prop: 'couponType', label: '券类型', type: 'dict', dict: 'coupon_type', required: true },
      { prop: 'minAmount', label: '使用门槛', type: 'number', required: true },
      { prop: 'discountAmount', label: '优惠金额 / 折扣', type: 'number', required: true },
      { prop: 'limitType', label: '适用范围', type: 'dict', dict: 'limit_type' },
      { prop: 'limitValue', label: '指定品类', tip: '适用范围为指定品类时填写，如 奶粉' },
      { prop: 'grantType', label: '发放方式', type: 'dict', dict: 'grant_type', required: true },
      { prop: 'validStart', label: '有效期开始', type: 'datetime', required: true },
      { prop: 'validEnd', label: '有效期结束', type: 'datetime', required: true },
      { prop: 'totalQty', label: '发放总量', type: 'number', required: true },
      { prop: 'status', label: '状态', type: 'dict', dict: 'coupon_status' }
    ]
  },

  order: {
    api: 'mall-order',
    name: '订单管理',
    keyword: '订单编号 / 收货人',
    addText: '代客下单',
    columns: [
      { label: '订单编号', prop: 'orderNo', width: 136 },
      { label: '会员', prop: 'memberName', width: 90 },
      { label: '件数', prop: 'itemCount', width: 64 },
      { label: '商品金额', prop: 'goodsAmount', type: 'money', width: 96 },
      { label: '优惠', prop: 'discountAmount', type: 'money', width: 88 },
      { label: '运费', prop: 'freight', type: 'money', width: 76 },
      { label: '实付', prop: 'payAmount', type: 'money', width: 96 },
      { label: '积分', prop: 'gainPoint', width: 68 },
      { label: '支付', prop: 'payType', width: 80 },
      { label: '状态', prop: 'status', type: 'tag', width: 88 },
      { label: '下单时间', prop: 'createTime', type: 'datetime', width: 146 }
    ],
    filters: [
      { prop: 'status', label: '状态', dict: 'order_status', col: 'status' },
      { prop: 'payType', label: '支付方式', dict: 'pay_type', col: 'pay_type' }
    ],
    actions: [
      { key: 'ship', label: '发货', api: 'shipOrder', show: (r) => r.status === '待发货', confirm: '确认已拣货并发出？' },
      { key: 'complete', label: '确认收货', api: 'completeOrder', show: (r) => r.status === '已发货',
        confirm: '确认收货后将发放积分并重算会员等级。' },
      { key: 'cancel', label: '取消订单', danger: true, api: 'cancelOrder', show: (r) => ['待支付', '待发货'].includes(r.status),
        dialog: [{ prop: 'reason', label: '取消原因', type: 'textarea', required: true, tip: '不少于 5 个字，将回补库存与优惠券' }] },
      { key: 'refund', label: '退款', danger: true, api: 'refundOrder', show: (r) => r.status === '退货中',
        dialog: [{ prop: 'reason', label: '退款原因', type: 'textarea', required: true, tip: '不少于 5 个字，将扣回积分' }] }
    ]
  },

  review: {
    api: 'goods-review',
    name: '评价审核',
    keyword: '评价内容',
    columns: [
      { label: '商品', prop: 'goodsName', minWidth: 200 },
      { label: '会员', prop: 'memberName', width: 90 },
      { label: '评分', prop: 'score', width: 68 },
      { label: '评价内容', prop: 'content', minWidth: 280 },
      { label: '晒图', prop: 'images', type: 'image', width: 76 },
      { label: '评价时间', prop: 'createTime', type: 'datetime', width: 146 },
      { label: '状态', prop: 'status', type: 'tag', width: 88 }
    ],
    filters: [{ prop: 'status', label: '状态', dict: 'review_status', col: 'status' }],
    actions: [
      { key: 'pass', label: '通过', api: 'auditPass', show: (r) => r.status === '待审核', confirm: '通过后将在商城展示该评价。' },
      { key: 'reject', label: '驳回', danger: true, api: 'auditReject', show: (r) => r.status === '待审核',
        dialog: [{ prop: 'reply', label: '驳回说明', type: 'textarea', required: true, tip: '不少于 5 个字' }] }
    ]
  },

  user: {
    api: 'user',
    name: '用户管理',
    keyword: '账号 / 姓名 / 手机号',
    addText: '新增用户',
    columns: [
      { label: '账号', prop: 'username', width: 110 },
      { label: '姓名', prop: 'realName', width: 90 },
      { label: '头像', prop: 'avatar', type: 'avatar', width: 64 },
      { label: '手机号', prop: 'phone', width: 118 },
      { label: '所属门店', prop: 'deptName', width: 120 },
      { label: '状态', prop: 'status', type: 'tag', width: 76 },
      { label: '创建时间', prop: 'createTime', type: 'datetime', width: 146 }
    ],
    filters: [{ prop: 'status', label: '状态', options: ['正常', '停用'], col: 'status' }],
    form: [
      { prop: 'username', label: '登录账号', required: true },
      { prop: 'password', label: '初始密码', tip: '新增时必填，后端 BCrypt 加密存储' },
      { prop: 'realName', label: '姓名', required: true },
      { prop: 'phone', label: '手机号' },
      { prop: 'deptName', label: '所属门店' },
      { prop: 'status', label: '状态', options: ['正常', '停用'] }
    ]
  },

  operlog: {
    api: 'oper-log',
    name: '操作日志',
    keyword: '模块 / 单号 / 内容',
    readonly: true,
    columns: [
      { label: '模块', prop: 'module', width: 130 },
      { label: '类型', prop: 'operType', type: 'tag', width: 88 },
      { label: '业务单号', prop: 'bizNo', width: 150 },
      { label: '内容', prop: 'content', minWidth: 300 },
      { label: '操作人', prop: 'operName', width: 90 },
      { label: '时间', prop: 'operTime', type: 'datetime', width: 146 },
      { label: 'IP', prop: 'ip', width: 110 }
    ],
    filters: [
      { prop: 'operType', label: '类型', options: ['新增', '修改', '删除', '流转', '发券', '导出'], col: 'oper_type' }
    ]
  }
}
