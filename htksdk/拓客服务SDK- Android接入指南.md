## 拓客服务SDK-Android接入指南

#### 1、环境配置

-将htksdk在主项目build.gradle引入；
    -implementation project(path: ':htksdk')
    -点击sync同步
    -sdk最小支持Android 4.4

#### 2、服务调用

```
-启用服务
```
            val intent = Intent(this, HtkWebActivity::class.java)
            val htkParams = HtkParams()
            htkParams.title = "测试" //标题
            htkParams.url = "https://tkfront.lakala.com/tk-account/index.html#/withdraw" //请求h5地址
            htkParams.appType = "qtk" 
            htkParams.channelId = "qtk" //填写渠道名称，该属性会影响h5主题
            htkParams.temporaryToken = "3273bc00-75ca-41c7-ae03-df281c2add6d" //从接口获取到的token
            //htkParams.needToolBar 表示是否需要导航条 默认false不需要
            //htkParams.statusColor = R.color.black_3 状态栏和导航条的背景色 默认浅蓝色
            //htkParams.backColor = R.color.white 导航条的返回按钮和文字颜色 默认白色
            val bundle = Bundle()
            bundle.putParcelable("params",htkParams)
            intent.putExtras(bundle)
            startActivity(intent)
```
#### 3、附录

1、baseUrl

|  环境   | 地址  |
|  ----  | ----  |
| 测试  | https://tk.wsmsd.cn/sit/tk-account/index.html#/ + ${service}(具体服务名称见service，例如withdraw提现) |
| 生产  | https://tkfront.lakala.com/tk-account/index.html#/ + ${service}(具体服务名称见service，例如withdraw提现) |

2、service

|  服务   | 备注  |
|  ----  | ----  |
| balance | 用户额度信息 |
| withdraw | 提现 |
| flow/can-withdraw | 可提现明细 |
| flow/flow | 收支明细 |
| flow/debit | 待调账明细 |
| flow/month | 月结明细 |

