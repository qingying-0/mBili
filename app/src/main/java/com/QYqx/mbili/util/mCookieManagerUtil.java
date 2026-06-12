package com.QYqx.mbili.util;

public class mCookieManagerUtil {
//    /**
//     * 将 WebView Cookie 持久化到 PersistentCookieJar
//     * @param domain 目标域名（与 WebView 加载的域名一致）
//     */
//    fun persistWebViewCookieToPersistentCookieJar(domain: String) {
//        // 2. 解析 String 为 HttpUrl（非空判断，避免空指针）
//        // 2. 调用扩展函数转换为 HttpUrl（非空判断，避免解析失败）
//        val httpUrl = domain.toHttpUrlOrNull() ?: return // 解析失败则直接返回，或做异常处理
//                // 1. 获取 WebView Cookie 字符串
//                val webViewCookieStr = getWebViewCookie(domain)
//        if (webViewCookieStr.isBlank()) {
//            return
//        }
//
//        // 2. 解析为 OkHttp Cookie 列表
//        val okHttpCookieList = parseWebViewCookieToOkHttp(webViewCookieStr, ".bilibili.com")
//        if (okHttpCookieList.isEmpty()) {
//            return
//        }
//
//        // 3. 注入 PersistentCookieJar 完成持久化
//        // 第一个参数：请求的 URL（可直接传域名，核心是与 Cookie 的 domain 匹配）
//        // 第二个参数：解析后的 Cookie 列表
//        MbiliApplication.GlobalCookieJar.instance.saveFromResponse(httpUrl, okHttpCookieList)
//
//        // 可选：验证是否注入成功（调试用）
//        val cachedCookies = MbiliApplication.GlobalCookieJar.instance.loadForRequest(httpUrl)
//        println("注入 Cookie 数量：${okHttpCookieList.size}，缓存 Cookie 数量：${cachedCookies.size}")
//    }
//    /**
//     * 从 WebView 中获取指定域名的 Cookie 字符串
//     * @param domain 目标域名（如 "https://api.example.com"，无需带路径）
//     * @return 拼接后的 Cookie 字符串（格式：key1=value1; key2=value2; ...）
//     */
//    fun getWebViewCookie(domain: String): String {
//        // 获取 WebView 全局 CookieManager 实例
//        val cookieManager = CookieManager.getInstance()
//        // 设置支持第三方 Cookie（Android 5.0+ 必需，否则可能获取不到 Cookie）
//        cookieManager.setAcceptThirdPartyCookies(WebView(baseContext), true)
//        // 获取指定域名的 Cookie 字符串
//        return cookieManager.getCookie(domain) ?: ""
//    }
//    /**
//     * 将 WebView Cookie 字符串解析为 OkHttp Cookie 列表
//     * @param cookieStr WebView 获取的 Cookie 字符串
//     * @param domain 目标域名（如 "api.example.com"）
//     * @return OkHttp Cookie 列表
//     */
//    fun parseWebViewCookieToOkHttp(cookieStr: String, domain: String): List<Cookie> {
//        val cookieList = mutableListOf<Cookie>()
//        if (cookieStr.isBlank()) {
//            return cookieList
//        }
//
//        val cookieSegments = cookieStr.split(";").map { it.trim() }
//        val uri = URI.create(domain)
//        val host = uri.host ?: domain
//        val isHttps = domain.startsWith("https") // 判断是否为 HTTPS 域名
//
//        Log.d(TAG, "parseWebViewCookieToOkHttp: "+host)
//
//        cookieSegments.forEach { segment ->
//            if (segment.isBlank()) return@forEach
//                    val keyValue = segment.split("=", limit = 2)
//            if (keyValue.size < 2) return@forEach
//
//                    val name = keyValue[0].trim()
//            val value = keyValue[1].trim()
//
//            // 正确构建 Cookie：httpOnly() 和 secure() 无参，通过条件判断是否调用
//            val cookieBuilder = Cookie.Builder()
//                    .name(name)
//                    .value(value)
//                    .domain(host)
//                    .path("/")
//
//            // 条件1：是否设置 HttpOnly（根据业务需求调整，WebView Cookie 一般非 HttpOnly）
//            // 若需要 HttpOnly，直接调用 cookieBuilder.httpOnly()
//            // 此处示例：不设置 HttpOnly（不调用该方法），若需设置则取消注释
//            // cookieBuilder.httpOnly()
//            // 条件2：是否设置 Secure（仅 HTTPS 域名才启用）
//            if (isHttps) {
//                cookieBuilder.secure() // HTTPS 域名：调用 secure()，标记为 Secure Cookie
//            }
//            val cookie = cookieBuilder.build()
//            cookieList.add(cookie)
//        }
//        return cookieList
//    }
}
