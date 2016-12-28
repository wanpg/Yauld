package com.wanpg.yauld

/**
 * Created by wangjinpeng on 2016/12/13.
 */
class ConfigParams {
    /**
     * 主dex的保留列表
     * 只适用于 update_enable 为true的情况
     */
    String main_dex_list
    /**
     * 是否按照yauld的build进行
     * 如果是true  application会被替换
     * 如果是false 按照安卓原有的打包方式进行build
     */
    boolean build_enable

    /**
     * 当前的代码和资源版本，区分与android 中的versioncode
     * 这个只代表dex和res的版本
     */
    String version
}
