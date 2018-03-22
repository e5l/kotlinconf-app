package org.jetbrains.kotlinconf.util

import libs.*
import platform.Foundation.*
import platform.darwin.*

fun OMGHTTPURLRQ.Companion.POST(url: String, rawText: String): NSMutableURLRequest {
    return OMGHTTPURLRQ.POST(url, _1 = null, error = null)!!.apply {
        HTTPBody = rawText.toNSString().dataUsingEncoding(NSUTF8StringEncoding)
    }
}

fun OMGHTTPURLRQ.Companion.DELETE(url: String, JSON: NSObject): NSMutableURLRequest {
    return OMGHTTPURLRQ.POST(url, JSON = JSON, error = null)!!.apply {
        this.HTTPMethod = "DELETE"
    }
}
