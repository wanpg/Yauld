# Yauld
Android HotFix Plug-in

####  1. 补丁包文件结构
```
update.zip
	-- patch.dex
	-- resource
		-- assets
		-- res
	-- resources.arsc.patch
	-- assets.change
	-- res.change
```

#### 2. Dex差分
```
1. 转换 dex -> smali
2. 比对 old.smali 与 new.smali 保留变换的smali，移除相同的smali
3. 打包 差分后smali(s) -> patch.dex
```

#### 3. Resource差分
```
1. 差分assets文件夹，将变化和新增的资源放入 assets 并生成 assets 移除列表 assets.change
2. 差分res文件夹，将变化和新增的资源放入 res 并生成 res 移除列表 res.change
3. 二进制diff resources.arsc 生成 resources.arsc.patch
```

#### 4. 补丁包加载流程
```
1. 检查update.zip是否存在如存在继续，不存在退出更新
2. 解压update.zip得到上文中的补丁包文件结构
3. 加载dex文件夹下的dex文件到YauldDexLoader
4. 从YauldDexLoader中取出AppInfo#VERSION 比对内存中的Appinfo.VERSION
5. 新版本版本号大于老版本，继续，否则退出更新
6. 将YauldDexLoader设置为PathClassLoader的Parent，利用ClassLoader加载的双亲委托模式将改变的class加载入内存
7. 如果发现resources.arsc.patch存在则进行二进制BSPATCH，以apk内的resources.arsc为基准生成resources.arsc(new)，并移动到yauld/resource目录，如果不存在则结束更新
8. 拷贝apk内部的res及assets到 yauld/resource目录
9. 根据assets.change和res.change移除无用资源
10. 补丁包中assets和res移动到yauld/resource中替换相应资源
11. 将当前生成的yauld/resource文件夹打包压缩为yauld/update/resource.zip
12. 根据AppInfo.APPLICATION_NAME字段创建RealApplication
13. 调用RealApplication.attachBaseContext
14. 在YauldDexApplication的onCreate的首行，调用YauldDex.monkeyPatchApplication，替换所有需要替换application为RealApplication，替换资源管理器指向地址为yauld/update/resource.zip
```
#### 5. 手机空间目录结构

```
.../files
	-- yauld
		-- update.zip  # 更新下载下来的zip包
		-- update_temp # 更新解压的临时目录
        	-- patch.dex
            -- resource
                -- assets
                -- res
            -- resources.arsc.patch
            -- assets.change
            -- res.change
		-- update # 更新所有数据处理结束后的目录
			-- patch.dex
			-- resources.zip #由apk资源的临时目录压缩得来
		-- resource # apk资源的临时目录，在压缩为 update/resources.zip 后会被删除
			-- assets
			-- res
			-- resources.arsc #会根据update_temp/resources.arsc.patch 做一次二进制合并
```

#### 6. 注意事项

* `patch.dex`，apk差分工具只能生成一个dex的patch包，这就限制了热更新改动的代码不要超越65536个方法数的限制（不是不能做，如果改动非常大，建议直接apk升级）